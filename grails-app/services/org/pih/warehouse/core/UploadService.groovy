/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.codehaus.groovy.grails.commons.GrailsApplication

class UploadService {

    GrailsApplication grailsApplication
    FileService fileService

    boolean transactional = false

    File createLocalFile(String filename) {
        log.info "Create local file ${filename}"
        def uploadsDirectory = findOrCreateUploadsDirectory()
        return new File(uploadsDirectory, filename)
    }

    File findOrCreateUploadsDirectory() {
        String directoryPath = grailsApplication.config.openboxes.uploads.location
        log.info("Find or create uploads directory ${directoryPath}")
        if (!directoryPath) {
            throw new IllegalStateException("Directory path for uploads directory must be configured in openboxes-config.properties [openboxes.uploads.location]")
        }
        // Replace tilde with user home
        directoryPath = directoryPath.replaceFirst("^~", System.getProperty("user.home"))
        return fileService.createDirectory(directoryPath)
    }

}
