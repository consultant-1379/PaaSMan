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

import com.ericsson.component.aia.sdk.AbstractTest
import org.springframework.http.HttpStatus

class SiteControllerTest extends AbstractTest {

    void 'render schema-registry-client latest site'() {
        when:
        def entity = restGet('/site/com.ericsson.aia.model/schema-registry-client/1.0.3/', String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.contains('Schema Registry')
    }

    void 'access none exist site page should get 404'() {
        when:
        def entity = restGet('/site/com.ericsson.aia.model/schema-registry-client/1.0.3/not-exist', String.class)

        then:
        entity.statusCode == HttpStatus.NOT_FOUND
    }

    void 'download a site file should have special header'() {
        when:
        def entity = restGet('/site/com.ericsson.aia.model/schema-registry-client/1.0.3/index.html?download=true', String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.headers['Content-Description'].first()=='File Transfer'
    }

    void 'site should be accessible by shortcut'() {
        when:
        def entity = restGet('/paas-ui/', String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.contains('container/main.js')
    }

}
