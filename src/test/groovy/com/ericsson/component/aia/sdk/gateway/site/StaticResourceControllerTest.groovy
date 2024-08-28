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

class StaticResourceControllerTest extends AbstractTest {

    void 'download file should contain content-* special headers'() {
        when:
        def entity = restGet('/download/index.html', String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.headers['Content-Description'].first()=='File Transfer'
        entity.headers['Content-Disposition'].first()=='attachment; filename=index.html'
    }

    void 'download none existing file should result 404'() {
        when:
        def entity = restGet("/download/test-not-exist${System.currentTimeMillis()}.txt", String.class)

        then:
        entity.statusCode == HttpStatus.NOT_FOUND
    }

    void 'download local static file should take priority'() {
        when:
        def entity = restGet('/download/portal/config.js', String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body=='InvalidJs'
    }
}