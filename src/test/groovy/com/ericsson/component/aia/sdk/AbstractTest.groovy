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
package com.ericsson.component.aia.sdk

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.TestRestTemplate
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import spock.lang.Specification

@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
class AbstractTest extends Specification {

    static {
        Application.populateSystemPropertiesBeforeSpringBootAppStart()
    }

    @Value('${local.server.port}')
    int port

    @Autowired
    ApplicationContext context

    String url(String path) {
        "http://localhost:$port$path"
    }

    public <T> ResponseEntity<T> restGet(String path, Class<T> responseType, Object... params) {
        restCall(path, responseType, HttpMethod.GET, params)
    }

    public <T> ResponseEntity<T> restPost(String path, Class<T> responseType, Object... params) {
        restCall(path, responseType, HttpMethod.POST, params)
    }

    public <T> ResponseEntity<T> restDelete(String path, Class<T> responseType, Object... params) {
        restCall(path, responseType, HttpMethod.DELETE, params)
    }

    public <T> ResponseEntity<T> restCall(String path, Class<T> responseType, HttpMethod httpMethod, Object... params) {
        HttpHeaders headers = new HttpHeaders();
        if (params && params.first() instanceof Map) {
            params.first().each {String k, String v ->
                headers.set(k, v)
            }
            params = params.drop(1)
        }

        def entity
        if (params.size()==0 || httpMethod==HttpMethod.GET) {
            entity = new HttpEntity(headers)
        } else {
            entity = new HttpEntity(params.first(), headers)
            params = params.drop(1)
        }

        new TestRestTemplate().exchange(url(path), httpMethod, entity, responseType, params)
    }

}