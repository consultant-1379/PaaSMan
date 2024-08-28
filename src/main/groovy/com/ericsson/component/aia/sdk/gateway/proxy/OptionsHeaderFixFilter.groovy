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

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

/**
 * Fix response headers for developer and proxy cross domain requirement
 */
@CompileStatic
@Component
@Profile('gateway')
public class OptionsHeaderFixFilter implements Filter {

    @Value('${gateway.server.header:GATEWAY}')
    String serverHeader

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        ((HttpServletResponse) res).addHeader('Server', serverHeader);
        if (isRequestNeedFix(httpServletRequest)) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) res;
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader(HttpHeaders.ORIGIN));
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD));
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS));
            chain.doFilter(new HeaderFixRequest(httpServletRequest), res);
        } else {
            chain.doFilter(req, res);
        }
    }

    private boolean isRequestNeedFix(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getMethod().equals("OPTIONS")
                || (httpServletRequest.getHeader(HttpHeaders.ORIGIN) && httpServletRequest.requestURI.indexOf('font')>0)
        )
    }

    public void destroy() {
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

    static class HeaderFixRequest extends HttpServletRequestWrapper {
        public HeaderFixRequest(HttpServletRequest request) {
            super(request);
        }

        public String getHeader(String name) {
            String header = super.getHeader(name);
            if (name.equals(HttpHeaders.ORIGIN) || name.equals(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)) {
                header = null;
            }
            return header;
        }
    }
}

