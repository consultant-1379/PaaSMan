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
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.InputStreamResource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.HandlerMapping

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * To access legacy static resource come with the paas manager
 */
@CompileStatic
@RestController
@Profile('gateway')
public class StaticResourceController {

    @Value('${gateway.static.resource.path}')
    String webStaticResourcePath

    /**
     * Provide access to download zip file for links on new CDS Portal page
     * @param request
     * @param response
     * @return
     */
    @RequestMapping('/download/**')
    def forceDownloadTheFile(HttpServletRequest request, HttpServletResponse response) {
        String requestPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
        def filePath = requestPath.substring(requestPath.indexOf('/', 2))
        def resource = loadStaticResource(filePath)
        if (resource) {
            def fileName = requestPath.substring(requestPath.lastIndexOf('/') + 1);
            response.setHeader('Content-Description', 'File Transfer')
            response.setHeader('Content-Disposition', "attachment; filename=$fileName")
            resource
        } else {
            response.sendError(404)
        }
    }

    private def loadStaticResource(String path) {
        def localFile = new File(webStaticResourcePath, path)
        if (localFile.exists()) {
            return new FileSystemResource(localFile)
        } else {
            def resourceStream = getClass().getResourceAsStream("/static$path")
            if (resourceStream) {
                return new InputStreamResource(resourceStream)
            } else {
                return null
            }
        }
    }
}