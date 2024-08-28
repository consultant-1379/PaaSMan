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
package com.ericsson.component.aia.sdk.store.maven

class VersionUtils {

    static List<String> sortVersions(List<String> versions) {
        def sorted = versions.sort(false) { a, b ->
            compareVersion(a, b)
        }
        sorted
    }

    private static int compareVersion(String a, String b) {
        List verA = a.replaceAll('-', '.').replaceAll('[^\\d\\.]', '.').tokenize('.')
        List verB = b.replaceAll('-', '.').replaceAll('[^\\d\\.]', '').tokenize('.')
        def commonIndices = Math.min(verA.size(), verB.size())
        for (int i = 0; i < commonIndices; ++i) {
            def numA = verA[i].toLong()
            def numB = verB[i].toLong()
            if (numA != numB) {
                return numA <=> numB
            }
        }
        if (verA.size() != verB.size()) {
            verA.size() <=> verB.size()
        } else {
            a <=> b
        }
    }

}
