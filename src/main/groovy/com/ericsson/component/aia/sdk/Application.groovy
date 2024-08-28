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

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.feign.EnableFeignClients

/**
 * The main class run by spring boot application. It take advantage of spring boot existing features such as
 * autoconfigure and flexible configurations.
 */
@CompileStatic
@SpringBootApplication
@EnableFeignClients
public class Application {

    static {
        populateSystemPropertiesBeforeSpringBootAppStart()
    }

    public static void populateSystemPropertiesBeforeSpringBootAppStart() {
        System.properties['info.app.startup.time'] = new Date(
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()
        ).format('yyyy-MM-dd HH:mm:ss z')

        if (!isProfileAlreadySet()) {
            String classPath = System.properties['java.class.path']
            System.properties['spring.profiles.active'] =
                    classPath.indexOf('test-classes') > 0 ? 'gateway,paas,local,test' :
                            classPath.indexOf('classes') > 0 ? 'gateway,paas,local' : 'gateway,paas,prod'
        }
    }

    public static boolean isProfileAlreadySet() {
        if (System.getenv('SPRING_PROFILES_ACTIVE') || System.properties['spring.profiles.active']) {
            true
        }else{
            String fullCommand=System.properties['sun.java.command']
            fullCommand.indexOf('--spring.profiles.active') > 0
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args)
    }


}