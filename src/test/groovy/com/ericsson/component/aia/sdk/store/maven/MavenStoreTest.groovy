package com.ericsson.component.aia.sdk.store.maven

import com.ericsson.component.aia.sdk.AbstractTest
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus

public class MavenStoreTest extends AbstractTest {

    @Value('${test.store.maven.test.artifact.dir}')
    File artifactDir

    void 'render site should download artifact to local folder'() {
        given:
        FileUtils.deleteDirectory(artifactDir)

        when:
        def entity = restGet('/site/com.ericsson.aia.model/schema-registry-client/1.0.3/', String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.contains('Schema Registry')
        new File(artifactDir,'1.0.3/site/index.html').exists()
    }



    void 'access site should populate maven store'() {
        when:
        def entity = restGet('/paas/v1/maven/cache', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.values().find{!it.isEmpty()}!=null
    }

    void 'maven store cache should not be empty after reset'() {
        when:
        restGet('/paas/v1/maven/cache?reset=true', Map.class)
        def entity = restGet('/paas/v1/maven/cache', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.values().find{!it.isEmpty()}!=null
    }

}