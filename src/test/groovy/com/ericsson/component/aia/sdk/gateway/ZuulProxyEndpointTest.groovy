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
package com.ericsson.component.aia.sdk.gateway

import com.ericsson.component.aia.sdk.AbstractTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class ZuulProxyEndpointTest extends AbstractTest {

    void 'proxy endpoints should exist'(def path, def expectedText) {
        when:
        def entity = restGet(path, String.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.contains(expectedText)

        where:
        path                                 | expectedText
        '/paas/v1/serviceability/info'       | 'startup'
    }

    void 'proxy endpoints should not contain duplicated headers'() {
        when:
        def entity = restGet('/paas/v1/serviceability/info', Map.class, [Origin: 'http://localhost:18888'])

        then:
        entity.statusCode == HttpStatus.OK
        entity.headers.keySet().each { key ->
            assert entity.headers[key].size() == 1
        }
    }

    void 'proxy endpoints should support cross domain headers'() {
        when:
        def entity = restCall('/paas/v1/serviceability/info', String.class, HttpMethod.OPTIONS,
                [
                        Origin                          : 'http://foo.bar',
                        'Access-Control-Request-Method' : 'POST',
                        'Access-Control-Request-Headers': 'accept, content-type']
        )

        then:
        entity.statusCode == HttpStatus.OK
        entity.headers['Access-Control-Allow-Origin'].first() == 'http://foo.bar'
        entity.headers['Access-Control-Request-Method'].first() == 'POST'
        entity.headers['Access-Control-Allow-Headers'].first() == 'accept, content-type'
    }
}