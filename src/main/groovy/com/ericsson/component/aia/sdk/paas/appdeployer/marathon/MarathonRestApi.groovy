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

import groovy.transform.CompileStatic
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@CompileStatic
//@FeignClient(url = '${paas.marathon.url:http://atrcxb2560-1.athtem.eei.ericsson.se:18080}')
//@FeignClient(url = '${paas.marathon.url:http://192.168.99.100:18080}')
@FeignClient(url = '${paas.marathon.url:null}')
interface MarathonRestApi{

    @RequestMapping(method = RequestMethod.GET, value = "/v2/apps")
    def list()

    @RequestMapping(method = RequestMethod.POST, value = "/v2/apps", consumes = 'application/json')
    def deploy(def pba)

    @RequestMapping(method = RequestMethod.GET, value = "/v2/apps{appId}")
    def show(@PathVariable('appId') String appId)

    @RequestMapping(method = RequestMethod.DELETE, value = "/v2/apps{appId}")
    def undeploy(@PathVariable('appId') String appId)
}
