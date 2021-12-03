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

import net.schmizz.sshj.sftp.SFTPClient
import org.apache.commons.io.IOUtils
import org.pih.warehouse.integration.ftp.SecureFtpCommand

class FileTransferService {

    def grailsApplication

    SecureFtpCommand getSecureFtpCommand() {
        Map sftpConfig = grailsApplication.config.openboxes.integration.ftp.flatten()
        return new SecureFtpCommand(sftpConfig)
    }

    def listMessages(String directory, boolean includeContent = false) {
        SecureFtpCommand command
        try {
            command = getSecureFtpCommand()
            def files = command.listFiles(directory)
            def messages = files.collect { Map fileInfo ->
                if (includeContent) {
                    log.info "Retrieve content for ${fileInfo.path}"
                    fileInfo.content = command.retrieveFileAsString(fileInfo.path)
                }
                return fileInfo
            }
            return messages
        } catch (Exception e) {

        }
        finally {
            IOUtils.closeQuietly(command)
        }
    }

    def listMessages(String directory, List<String> subdirectories, boolean includeContent = false) {

        SecureFtpCommand command
        try {
            command = getSecureFtpCommand()
            def files = command.listFilesInSubdirectories(directory, subdirectories)
            log.info "Returned ${files?.size()?:0} files"

            def messages = files.collect { Map fileInfo ->
                if (includeContent) {
                    log.info "Retrieve content for ${fileInfo.path}"
                    fileInfo.content = command.retrieveFileAsString(fileInfo.path)
                }
                return fileInfo
            }

            messages = messages.findAll { !it.isDirectory }.sort { it.mtime }

            return messages
        } catch (Exception e) {
            log.error("Error occurred while listing messages: " + e.message, e)
        }
        finally {
            IOUtils.closeQuietly(command)
        }

    }

    def retrieveMessage(String source) {
        SecureFtpCommand command
        try  {
            command = getSecureFtpCommand()
            command.retrieveFileAsString(source)
        } finally {
            IOUtils.closeQuietly(command)
        }
    }

    def storeMessage(File file, String destination) {
        SecureFtpCommand command
        try {
            command = getSecureFtpCommand()
            command.storeFile(file, destination)
        } finally {
            IOUtils.closeQuietly(command)
        }
    }

    def storeMessage(String filename, String contents, String destination) {
        SecureFtpCommand command
        try {
            command = getSecureFtpCommand()
            command.storeFile(filename, contents, destination)
        } finally {
            IOUtils.closeQuietly(command)
        }
    }

    def deleteMessage(String target) {
        SecureFtpCommand command
        try {
            command = getSecureFtpCommand()
            command.deleteFile(target)
        } finally {
            IOUtils.closeQuietly(command)
        }
    }

    def moveMessage(String oldPath, String newPath) {
        SecureFtpCommand command
        try {
            SFTPClient sftpClient
            try {
                command = getSecureFtpCommand()
                sftpClient = command.createNewSftpClient()
                log.info "Move remote file ${oldPath} to destination ${newPath}"
                sftpClient.rename(oldPath, newPath)
            } finally {
                IOUtils.closeQuietly(sftpClient)
            }
        } finally {
            IOUtils.closeQuietly(command)
        }
    }
}
