/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.integration

import org.apache.commons.io.IOUtils
import org.pih.warehouse.integration.ftp.SecureFtpClient

import java.nio.charset.StandardCharsets

class FileTransferService {

    boolean transactional = true
    def grailsApplication

    def listMessages() {
        // Configuration
        String server = grailsApplication.config.openboxes.integration.ftp.server
        Integer port = grailsApplication.config.openboxes.integration.ftp.port?:22
        String user = grailsApplication.config.openboxes.integration.ftp.user
        String password = grailsApplication.config.openboxes.integration.ftp.password
        String directory = grailsApplication.config.openboxes.integration.ftp.directory

        // Retrieve files
        SecureFtpClient ftpClient = new SecureFtpClient(server, port, user, password)
        ftpClient.connect()
        def filenames = ftpClient.listFiles(directory)
        def messages = filenames.collect { String filename ->
            String source = "${directory}/${filename}"
            log.info "retrieving file ${source} ..."
            def inputStream = ftpClient.retrieveFileAsInputStream(source)
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
            return [filename: filename, content: content]
        }
        ftpClient.disconnect()

        return messages
    }

    def retrieveMessage(String filename) {
        // Configuration
        String server = grailsApplication.config.openboxes.integration.ftp.server
        Integer port = grailsApplication.config.openboxes.integration.ftp.port?:22
        String user = grailsApplication.config.openboxes.integration.ftp.user
        String password = grailsApplication.config.openboxes.integration.ftp.password
        String directory = grailsApplication.config.openboxes.integration.ftp.directory

        // Retrieve files
        SecureFtpClient ftpClient = new SecureFtpClient(server, port, user, password)
        ftpClient.connect()
        String source = "${directory}/${filename}"
        log.info "source: ${source}"
        def inputStream = ftpClient.retrieveFileAsInputStream(source)
        String contents = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
        ftpClient.disconnect()
        return contents
    }

}
