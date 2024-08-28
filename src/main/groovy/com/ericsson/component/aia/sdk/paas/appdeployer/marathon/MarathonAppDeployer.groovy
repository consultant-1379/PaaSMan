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

import com.ericsson.component.aia.sdk.paas.config.ConfigKeys
import com.ericsson.component.aia.sdk.paas.appdeployer.AppDeployer
import com.ericsson.component.aia.sdk.store.Store
import feign.FeignException
import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.json.JsonParserFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Log
@Profile('paas')
class MarathonAppDeployer implements AppDeployer {


    @Value('${paas.host}')
    String appHostname

    @Autowired
    MarathonRestApi marathonRestApi

    @Autowired
    Store store

    def list() {
        marathonRestApi.list()
    }

    def deploy(def pbaInfo) {
        def dependencyResolved=false
        def result = [:]
        pbaInfo.image?.each { def image ->
            def deployedApp=marathonRestApi.deploy(buildMarathonRequest(pbaInfo, image))
            deployedApp=waitUntilRunning(deployedApp)
            result[image.image_name]= deployedApp
        }
        pbaInfo.pba.deployments?.each {def key,  def deployment ->
            try{
                marathonRestApi.show(deployment.id)
                println "INFO app $deployment.id already exist"
            }catch (FeignException ex){
                deployment.env?.each{def envKey,def envValue->
                    if(envValue.contains('$')){
                        if(!dependencyResolved){
                            resolveDependencies(pbaInfo)
                            dependencyResolved=true
                        }
                        pbaInfo.appHostname=appHostname
                        def stringTemplate = new SimpleTemplateEngine().createTemplate(envValue)
                        deployment.env[envKey]=stringTemplate.make(pbaInfo).toString()
                    }
                }
                if(deployment.args){
                    def args=deployment.args.join(',')
                    if(args.contains('$')){
                        if(!dependencyResolved){
                            resolveDependencies(pbaInfo)
                            dependencyResolved=true
                        }
                        pbaInfo.appHostname=appHostname
                        def stringTemplate = new SimpleTemplateEngine().createTemplate(args)
                        args=stringTemplate.make(pbaInfo).toString()
                        deployment.args=args.split(',')
                    }
                }
                marathonRestApi.deploy(deployment)
            }
            def deployedApp=waitUntilRunning(deployment)
            result[key]= deployedApp
            deployment['app']=deployedApp
        }
        [apps:result]
    }

    def undeploy(def pbaInfo) {
        def result = [:]
        pbaInfo.image?.each { def image ->
            def appId = "/${image.image_name}-${image.image_version}"
            try {
                result[image.image_name]= marathonRestApi.undeploy(appId)
                Thread.sleep(2000) //TODO: to wait properly to make sure app is gone
            } catch (Exception ex) {
                log.info("undeploy app failed with result: $ex.message")
            }
        }
        pbaInfo.pba.deployments?.reverseEach {def key, def deployment ->
            try {
                result[key]=marathonRestApi.undeploy(deployment.id)
                Thread.sleep(2000) //TODO: to wait properly to make sure app is gone
            } catch (Exception ex) {
                log.info("undeploy app failed with result: $ex.message")
            }
        }
        [apps:result]
    }

    def resolveDependencies(def pbaInfo){
        pbaInfo.pba.dependencies?.each{def key,def dependency->
            def pbaFile=store.resolveArtifact(ConfigKeys.GROUP_ID_APPLICATIONS,dependency.name,dependency.version,'pba','json')
            if(pbaFile){
                def dependencyPbaInfo=JsonParserFactory.jsonParser.parseMap(pbaFile.text)
                dependency['apps']=deploy(dependencyPbaInfo).apps
                return
            }
            throw new RuntimeException("unable to resolve dependency $dependency.name:$dependency.version")
        }
    }

    def waitUntilRunning(def deployedApp){
        int timeout=60
        while(timeout){
            deployedApp=marathonRestApi.show(deployedApp.id).app
            if(deployedApp.deployments && !deployedApp.lastTaskFailure){
                timeout--
                Thread.sleep(1000)
            }else{
                deployedApp.endpoints=[]
                deployedApp.tasks?.each{def task->
                    task.ports?.each{def port->
                        deployedApp.endpoints << "$task.host:$port".toString()
                        deployedApp.host=task.host
                    }
                }
                break
            }
        }
        deployedApp
    }


    @CompileStatic(TypeCheckingMode.SKIP)
    private Map buildMarathonRequest(def pbaInfo, def image) {
        def portMappings = []
        pbaInfo.pba.service?.each { def service ->
            portMappings << [
                    containerPort: service.container_port,
                    hostPort     : service.service_port,
                    protocol     : 'tcp',
                    servicePort  : service.service_port
            ]
        }
        def requestPayload = [
                id       : "/${image.image_name}-${image.image_version}".toString(),
                instances: 1,
                cpus     : 1,
                mem      : 512,
                container: [
                        type  : 'DOCKER',
                        docker: [
                                image         : "${image.image_id}".toString(),
                                network       : 'BRIDGE',
                                forcePullImage: false,
                                portMappings  : portMappings,
                                privileged    : true
                        ]
                ]
        ]
        requestPayload
    }
}
