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

import org.pih.warehouse.integration.ftp.SecureFtpClient


class FileTransferService {

    def grailsApplication

    SecureFtpClient getSecureFtpClient() {
        Map sftpConfig = grailsApplication.config.openboxes.integration.ftp.flatten()
        return new SecureFtpClient(sftpConfig)
    }

    def listMessages(String directory, boolean includeContent = false) {
        try {
            def files = secureFtpClient.listFiles(directory)
            def messages = files.collect { Map fileInfo ->
                if (includeContent) {
                    fileInfo.content = secureFtpClient.retrieveFileAsString(fileInfo.path)
                }
                return fileInfo
            }
            return messages
        } catch (Exception e) {

        }
        finally {
            secureFtpClient.disconnect()
        }
    }

    def listMessages(String directory, List<String> subdirectories, boolean includeContent = false) {
        try {
            def files = secureFtpClient.listFilesInSubdirectories(directory, subdirectories)
            log.info "files ${files.size()}"
            def messages = files.collect { Map fileInfo ->
                if (includeContent) {
                    fileInfo.content = secureFtpClient.retrieveFileAsString(fileInfo.path)
                }
                return fileInfo
            }
            return messages
            return files
        } catch (Exception e) {
            log.error("Error doing something", e)
        }
        finally {
            secureFtpClient.disconnect()
        }

    }

    def retrieveMessage(String source) {
        try {
            secureFtpClient.retrieveFileAsString(source)
        } finally {
            secureFtpClient.disconnect()
        }
    }

    def storeMessage(File file, String destination) {
        try {
            secureFtpClient.storeFile(file, destination)
        } finally {
            secureFtpClient.disconnect()
        }
    }

    def storeMessage(String filename, String contents, String destination) {
        try {
            secureFtpClient.storeFile(filename, contents, destination)
        } finally {
            secureFtpClient.disconnect()
        }
    }

    def deleteMessage(String target) {
        try {
            secureFtpClient.deleteFile(target)
        } finally {
            secureFtpClient.disconnect()
        }


    }

}
