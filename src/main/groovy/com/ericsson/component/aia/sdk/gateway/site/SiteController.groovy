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
package com.ericsson.component.aia.sdk.gateway.site

import com.ericsson.component.aia.sdk.store.Store
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.HandlerMapping

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Provide access to site documentation published in maven. e.g.:
 * For artifact https://arm101-eiffel004.lmera.ericsson.se:8443/nexus/content/repositories/aia-releases/com/ericsson/aia/ui/aia-ui-paas/
 * Which could be accessed as /site/com.ericsson.aia.ui/aia-ui-paas/latest/
 */
@CompileStatic
@RestController
@Profile('gateway')
public class SiteController {

    @Autowired
    Store store

    /**
     * Render site resource based on the request
     * @param request
     * @param response
     */
    @RequestMapping('/site/**')
    def site(HttpServletRequest request, HttpServletResponse response) {
        renderSite(request,response,(String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
    }

    /**
     * Render site resource based on the request path
     * @param request
     * @param response
     * @param requestPath
     */
    def renderSite(HttpServletRequest request, HttpServletResponse response,String requestPath) {
        def pathInfo = requestPath.split('/', 6)
        def resource
        if (pathInfo.length == 6) {
            resource = renderFromJarFile(
                    request,
                    response,
                    pathInfo[2],
                    pathInfo[3],
                    pathInfo[4],
                    'site',
                    pathInfo[5]
            )
        }
        if (resource) {
            resource
        } else {
            response.sendError(404)
        }
    }

    private def renderFromJarFile(HttpServletRequest request, HttpServletResponse response, String groupId, String artifactId, String version, String classifier,  String filePath) {
        boolean forceDownload=('true'.equals(request.getParameter('download')) || filePath.endsWith('.zip'))
        if (filePath.length() == 0 || filePath.endsWith('/')) {
            filePath = "${filePath}index.html"
        }

        def siteBaseFolder=store.resolveArtifact(groupId,artifactId, version, classifier, 'jar')
        if(siteBaseFolder){
            def localFile = new File(siteBaseFolder, filePath)
            if (localFile.exists()) {
                def fileResource = new FileSystemResource(localFile)
                if (forceDownload) {
                    response.setHeader('Content-Description', 'File Transfer')
                    response.setHeader('Content-Disposition', "attachment; filename=$localFile.name")
                }
                fileResource
            }
        }
    }
}