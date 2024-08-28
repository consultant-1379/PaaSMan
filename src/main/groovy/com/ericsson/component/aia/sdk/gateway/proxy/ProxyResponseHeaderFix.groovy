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
package com.ericsson.component.aia.sdk.gateway.proxy

import com.netflix.util.Pair
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import groovy.transform.CompileStatic
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Fix headers for response back from zuul proxy which may result duplicated header to client
 */
@CompileStatic
@Component
@Profile('gateway')
public class ProxyResponseHeaderFix extends ZuulFilter {


    public Object run() {
        def ctx = RequestContext.getCurrentContext()
        def zuulResponseHeaders = ctx.getZuulResponseHeaders()
        fixCrossDomainHeaders(zuulResponseHeaders)
    }

    def void fixCrossDomainHeaders(List<Pair<String, String>> zuulResponseHeaders) {
        zuulResponseHeaders.removeAll(findDuplicated(zuulResponseHeaders))
    }

    List findDuplicated(List<Pair<String, String>> zuulResponseHeaders) {
        def duplicatedHeaders = []
        def existingHeaders=[]
        zuulResponseHeaders.each {header->
            String headerName = header.first()
            if (existingHeaders.contains(headerName)) {
                duplicatedHeaders.add(header)
            }else{
                existingHeaders.add(headerName)
            }
        }
        duplicatedHeaders
    }

    public boolean shouldFilter() {
        RequestContext.getCurrentContext().getRequest().getRequestURL().indexOf('v1')>0
    }

    public String filterType() {
        'post'
    }

    public int filterOrder() {
       0
    }
}
