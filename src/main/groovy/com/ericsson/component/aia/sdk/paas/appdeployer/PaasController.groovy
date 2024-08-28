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
package com.ericsson.component.aia.sdk.paas.appdeployer

import com.ericsson.component.aia.sdk.paas.config.ConfigKeys
import com.ericsson.component.aia.sdk.store.Store
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.JsonParserFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@CompileStatic
@RestController
@Profile('paas')
public class PaasController {

    @Autowired
    AppDeployer appDeployer

    @Autowired
    Store store

    @RequestMapping('/v1/applications')
    def listRunningApps() {
        appDeployer.list()
    }

    @RequestMapping(value = '/v1/applications/{appName}/{appVersion:.+}')
    def listRunningApp(@PathVariable String appName, @PathVariable String appVersion) {
        def foundApp=[:]
        String appId="/$appName"
        ((Map)listRunningApps()).apps.each{Map app->
            if(app.id==appId){
                foundApp=app
            }
        }
        foundApp
    }

    @RequestMapping(value = '/v1/applications', method = RequestMethod.POST)
    def deployWithBody(@Valid @RequestBody def pbaInfo) {
        appDeployer.deploy(pbaInfo)
    }

    @RequestMapping(value = '/v1/applications/{appName}/{appVersion:.+}', method = RequestMethod.POST)
    def deployWithPath(@PathVariable String appName, @PathVariable String appVersion) {
        def pbaFile = store.resolveArtifact(ConfigKeys.GROUP_ID_APPLICATIONS, appName, appVersion, 'pba', 'json')
        def pbaInfo=JsonParserFactory.jsonParser.parseMap(pbaFile.text)
        deployWithBody(pbaInfo)
    }

    @RequestMapping(value = '/v1/applications', method = RequestMethod.DELETE)
    def undeployWithBody(@Valid @RequestBody def pbaInfo) {
        appDeployer.undeploy(pbaInfo)
    }

    @RequestMapping(value = '/v1/applications/{appName}/{appVersion:.+}', method = RequestMethod.DELETE)
    def undeployWithPath(@PathVariable String appName,@PathVariable String appVersion) {
        def pbaFile = store.resolveArtifact(ConfigKeys.GROUP_ID_APPLICATIONS, appName, appVersion, 'pba', 'json')
        def pbaInfo=JsonParserFactory.jsonParser.parseMap(pbaFile.text)
        undeployWithBody(pbaInfo)
    }
}