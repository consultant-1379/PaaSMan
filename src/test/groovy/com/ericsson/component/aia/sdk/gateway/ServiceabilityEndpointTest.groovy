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
import org.springframework.http.HttpStatus

class ServiceabilityEndpointTest extends AbstractTest {

    void 'serviceability endpoints should exist'(def path, def expectedElement) {
        when:
        def entity = restGet(path, Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        Eval.x(entity.body, "x.$expectedElement")

        where:
        path                        | expectedElement
        '/v1/serviceability/info'   | 'app.startup.time'
        '/v1/serviceability/health' | 'status'
    }

    void 'health endpoint should be in UP status'() {
        when:
        def entity = restGet('/v1/serviceability/health', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.status == 'UP'
        entity.body.diskSpace.status == 'UP'
    }

    void 'health endpoint should not contain sensitive headers'() {
        when:
        def entity = restGet('/v1/serviceability/health', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.headers.find { it.key == 'X-Application-Context' } == null
        entity.headers.find { it.key == 'Server' }.value == ['GATEWAY']
    }

    void 'health endpoint via proxy should not contain sensitive headers'() {
        when:
        def entity = restGet('/paas/v1/serviceability/health', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.headers.find { it.key == 'X-Application-Context' } == null
        entity.headers.find { it.key == 'Server' }.value == ['GATEWAY']
    }
}