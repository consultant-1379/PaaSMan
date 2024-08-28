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
package com.ericsson.component.aia.sdk.store

/**
 * Underline storage interface for site and paas management functions
 */
public interface Store {
    /**
     * Return all sub folders of a given path as maven groupId. This could be used:
     *  * to discover all versions for a given artifact
     *  * to discover all artifacts in a group
     *
     * @param groupId maven style groupId
     * @return list of sub folders
     */
    List<String> listFolders(String groupId)

    /**
     * Resolve maven artifact as local ile. If the file is a jar file, will extract the jar file and return
     * the extracted folder.
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @param classifier
     * @param extension
     * @return a local file for none jar artifact and a folder if extension is jar
     */
    File resolveArtifact(String groupId,String artifactId, String version, String classifier, String extension)

    /**
     * Save given pba as maven repo layout locally for testing purpose. Official application should released
     * in maven nexus repo to be shared for all deployments
     * @param groupId
     * @param artifactId
     * @param version
     * @param pbaFile
     */
    void createApplication(String groupId,String artifactId, String version, byte[] pbaFile)

    /**
     * Delete the application from local file system for testing purpose
     * @param groupId
     * @param artifactId
     * @param version
     */
    void deleteApplication(String groupId,String artifactId, String version)
}