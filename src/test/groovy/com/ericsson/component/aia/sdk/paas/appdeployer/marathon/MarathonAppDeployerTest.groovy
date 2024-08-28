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
package com.ericsson.component.aia.sdk.paas.appdeployer.marathon

import com.ericsson.component.aia.sdk.AbstractTest
import org.springframework.http.HttpStatus


class MarathonAppDeployerTest extends AbstractTest {

    def testAppId = '/test-web-server-latest'
    def testPbaInfo = [
            pba  : [
                    name       : 'test-web-server',
                    service    : [[
                                          name          : 'test-service',
                                          container_port: 80,
                                          host_port     : 0,
                                          service_port  : 0
                                  ]],
                    deployments: [
                            webServer2:
                                    [
                                            id       : '/test-web-server2-latest',
                                            instances: 1,
                                            cpus     : 1,
                                            mem      : 512,
                                            "env": [
                                                    "ZOOKEEPER_CONNECTION_STRING": '${pba.dependencies.zookeeper.apps.zookeeper.endpoints[0]}'
                                            ],
                                            container: [
                                                    type  : 'DOCKER',
                                                    docker: [
                                                            "image"         : "nginx",
                                                            "network"       : "BRIDGE",
                                                            "forcePullImage": false,
                                                            "portMappings"  : [
                                                                    [
                                                                            "containerPort": 80,
                                                                            "hostPort"     : 0,
                                                                            "protocol"     : "tcp",
                                                                            "servicePort"  : 0
                                                                    ]
                                                            ],
                                                            "privileged"    : true
                                                    ]
                                            ]
                                    ]
                    ],
                    "dependencies":[
                        "zookeeper":[
                            "name":"platform-service-zookeeper",
                            "version": "latest"
                        ]
                    ],

            ],
            image: [[
                            image_id     : 'nginx',
                            image_name   : 'test-web-server',
                            image_version: 'latest'
                    ]],
    ]

    def 'make sure old app not exist'() {
        when:
        restDelete('/paas/v1/applications', Map.class, [:], testPbaInfo)
        def entity = restDelete('/paas/v1/applications', Map.class, [:], testPbaInfo)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.apps.size() == 0
    }


    def 'deploy real application to docker'() {
        when:
        def entity = restPost('/paas/v1/applications', Map.class, [:], testPbaInfo)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.apps.find { it.value.id == testAppId && it.value.endpoints }
    }

    void 'list app should return running docker containers'() {
        when:
        def entity = restGet('/paas/v1/applications', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.apps.find { it.id == testAppId }
    }

    void 'get app should return running docker containers'() {
        when:
        def entity = restGet("/paas/v1/applications${testAppId}/0.0.1", Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.id == testAppId
    }


    def 'undeploy real application to docker'() {
        when:
        def entity = restDelete('/paas/v1/applications', Map.class, [:], testPbaInfo)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.apps.size() == 2
    }

    void 'list app should not contain deleted containers'() {
        when:
        def entity = restGet('/paas/v1/applications', Map.class)

        then:
        entity.statusCode == HttpStatus.OK
        entity.body.apps.find { it.id == testAppId } == null
    }
}
