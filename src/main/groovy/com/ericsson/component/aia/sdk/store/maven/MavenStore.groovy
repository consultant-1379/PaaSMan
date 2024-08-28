/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.store.maven

import com.ericsson.component.aia.sdk.store.Store
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableFutureTask
import com.google.common.util.concurrent.MoreExecutors
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Log
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.annotation.PostConstruct
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Log
@CompileStatic
@RestController
public class MavenStore implements Store {

    public static final String FILE_NAME_DISABLED__MARKER = "DISABLED_MARKER"

    @Autowired
    MavenStoreConfig config

    LoadingCache<String, String> latestVersionCache
    LoadingCache<String, List<String>> foldersCache
    Executor executor

    /**
     * init the high performance fail safe cache
     */
    @PostConstruct
    @CompileStatic(TypeCheckingMode.SKIP)
    void initCache() {
        executor = config.asyncReload ? Executors.newSingleThreadExecutor() : MoreExecutors.sameThreadExecutor()

        latestVersionCache = createCache(this.&resolveLatestVersion)
        foldersCache = createCache(this.&resolveLatestFolders)

        config.preloadPackages?.each{
            foldersCache.get(it)
            if(config.asyncReload && !config.reloadOnLoad){
                foldersCache.refresh(it)
            }
        }
    }

    /**
     * Rest api to show and reset the cache. Could be configured to refresh asynchronously for better performance
     * @param resetCache a flag indicate refresh cache or not, default to false
     * @return current cached entries
     */
    @RequestMapping('/v1/maven/cache')
    def mavenCacheController(@RequestParam(name = 'reset', defaultValue = 'false') boolean resetCache) {
        if (resetCache) {
            foldersCache.asMap().keySet().each {
                foldersCache.refresh(it)
            }
            latestVersionCache.asMap().keySet().each {
                latestVersionCache.refresh(it)
            }
        }
        [
                latestVersionCache: latestVersionCache.asMap(),
                foldersCache      : foldersCache.asMap()
        ]
    }


    List<String> listFolders(String groupId) {
        foldersCache.get(groupId.split('\\.').join('/'))
    }


    void createApplication(String groupId, String artifactId, String version, byte[] pbaFile) {
        def artifactFile = resolveArtifactFile("${groupId.split('\\.').join('/')}/$artifactId", artifactId, version, 'pba', 'json')
        artifactFile.parentFile.mkdirs()
        artifactFile.delete()
        artifactFile << pbaFile
        mavenCacheController(true)
    }

    void deleteApplication(String groupId, String artifactId, String version) {
        def artifactFile = resolveArtifactFile("${groupId.split('\\.').join('/')}/$artifactId", artifactId, version, 'pba', 'json')
        artifactFile.delete()
        mavenCacheController(true)
    }

    File resolveArtifact(String groupId, String artifactId, String version, String classifier, String extension) {
        String artifactPath = "${groupId.split('\\.').join('/')}/$artifactId"
        def artifactFile = resolveArtifactFile(artifactPath, artifactId, version, classifier, extension)
        if (!artifactFile.exists()) {
            if (version == 'stable') {
                version = 'latest'
            }
            if (version == 'latest') {
                version = latestVersionCache.get(artifactPath)
            }
            if (version) {
                artifactFile = new File(config.localDir, "${groupId.split('\\.').join('/')}/$artifactId/$version/$artifactId-$version-${classifier}.$extension")
            } else {
                return
            }

            if (!artifactFile.exists()) {
                def artifactUrl = "$config.repoUrl/$artifactPath/$version/$artifactId-$version-${classifier}.$extension"
                downloadArtifact(artifactFile, artifactUrl, extension, classifier)
            }
        }
        if (artifactFile.exists()) {
            if (extension == 'jar') {
                new File(artifactFile.parentFile, classifier)
            } else {
                artifactFile
            }
        }
    }

    private File resolveArtifactFile(String artifactPath, String artifactId, String version, String classifier, String extension) {
        new File(config.localDir, "$artifactPath/$version/$artifactId-$version-${classifier}.$extension")
    }


    private def downloadArtifact(File artifactFile, String artifactUrl, String extension, String classifier) {
        try {
            artifactFile.parentFile.mkdirs()
            artifactFile.delete()
            artifactFile.withOutputStream { out ->
                log.info "Downloading $artifactUrl"
                out << new URL(artifactUrl).openStream()
            }

            if (extension == 'jar') {
                unpackJarFile(artifactFile, classifier)
            }
        } catch (FileNotFoundException ex) {
            FileUtils.deleteDirectory(artifactFile.parentFile)
        }
    }

    synchronized private void unpackJarFile(File artifactFile, String classifier) {
        def expandedJarFolder = new File(artifactFile.parentFile, classifier)
        new FileInputStream(artifactFile).withStream { is ->
            ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("zip", is)
            ZipArchiveEntry entry = null
            while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
                File outFile = new File(expandedJarFolder, entry.getName())
                if (entry.getName().endsWith('/')) {
                    outFile.mkdirs();
                    continue
                }
                outFile.delete()
                outFile << ais
            }
        }
        FileUtils.deleteQuietly(new File(expandedJarFolder, 'META-INF'))
    }

    private def createCache(Closure populateCacheAction) {
        CacheBuilder.newBuilder()
                .maximumSize(1000)
                .refreshAfterWrite(config.cacheInMinutes, TimeUnit.MINUTES)
                .build(
                new CacheLoader<Object, Object>() {
                    public Object load(Object path) {
                        populateCacheAction(path, config.reloadOnLoad)
                    }

                    public ListenableFuture<Object> reload(Object path, Object prevGraph) {
                        ListenableFutureTask<Object> task = ListenableFutureTask.create(new Callable<Object>() {
                            public Object call() {
                                populateCacheAction(path, true)
                            }
                        });
                        executor.execute(task);
                        task
                    }
                }
        )
    }

    private def resolveLatestVersion(String url, boolean reload) {
        def versions = resolveLatestFolders(url, reload)
        versions ? versions[-1] : ''
    }

    private List resolveLatestFolders(String path, boolean reload) {
        def folders = []
        new File(config.localDir, path).listFiles().each { File folder ->
            if (folder.isDirectory()) {
                addIfFolderIsEnabled(folders, path, folder.name)
            }
        }

        if (!folders || reload) {
            try {
                def indexUrl = "$config.repoUrl/$path/"
                log.info "Downloading $indexUrl"
                def indexHtml = new URL(indexUrl).text
                indexHtml.findAll(/>[^<]*\/</).each {
                    addIfFolderIsEnabled(folders, path, it.substring(1, it.length() - 2))
                }
            } catch (FileNotFoundException notFound) {
                log.warning "File not found to download $path : $notFound.message"
            } catch (Exception ignore) {
                ignore.printStackTrace()
                log.warning "Failed to download $path : $ignore.message"
            }
        }
        VersionUtils.sortVersions(folders)
    }

    private def addIfFolderIsEnabled(List folders, String path, String folderName) {
        def baseFolder = new File(config.localDir, "$path/$folderName")
        if (baseFolder.exists()) {
            if (new File(baseFolder, FILE_NAME_DISABLED__MARKER).exists()) {
                return
            }
        } else {
            baseFolder.mkdirs()
        }
        if (!folders.contains(folderName)) {
            folders << folderName
        }
    }

}