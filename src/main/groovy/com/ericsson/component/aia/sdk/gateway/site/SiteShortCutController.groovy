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
package com.ericsson.component.aia.sdk.gateway.site

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.HandlerMapping

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Provide short url to access site resources
 */
@CompileStatic
@RestController
@Profile('gateway')
public class SiteShortCutController {

    @Autowired
    SiteController siteController

    @Value('${gateway.site.homepage.artifactId:aia-homepage}')
    String homepageArtifactId

    /**
     * The short cut to access /site/com.ericsson.aia.ui/aia-ui-paas/latest/
     * @param request
     * @param response
     */
    @RequestMapping('/paas-ui/**')
    def paasUi(HttpServletRequest request, HttpServletResponse response) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
        def pathInfo = requestPath.split('/', 3)
        siteController.renderSite(request,response,"/site/com.ericsson.aia.ui/aia-ui-paas/latest/${pathInfo[2]}")
    }

    /**
     * The short cut to access /site/com.ericsson.aia.ui/aia-homepage/latest/
     * @param request
     * @param response
     */
    @RequestMapping(value = '/welcome/**')
    def welcome(HttpServletRequest request, HttpServletResponse response) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
        def pathInfo = requestPath.split('/', 3)
        response.sendRedirect("/${pathInfo[2]}")
    }

    @RequestMapping(value = '/', produces = 'text/html')
    def homepage(HttpServletRequest request, HttpServletResponse response) {
        newPortalResource(request, response)
    }

    @RequestMapping('/**')
    def app(HttpServletRequest request, HttpServletResponse response) {
        newPortalResource(request, response)
    }

    def newPortalResource(HttpServletRequest request, HttpServletResponse response) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
        siteController.renderSite(request,response,"/site/com.ericsson.aia.ui/$homepageArtifactId/latest$requestPath")
    }
}