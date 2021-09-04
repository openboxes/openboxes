package org.pih.warehouse.jobs

import net.schmizz.sshj.connection.ConnectionException
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.pih.warehouse.integration.ftp.SecureFtpClient
import org.quartz.DisallowConcurrentExecution

import java.nio.charset.StandardCharsets

@DisallowConcurrentExecution
class MessageRetrievalJob {

    def grailsApplication

    // cron job needs to be triggered
    static triggers = {
        cron name: 'messageRetrievalCronTrigger',
                cronExpression: CH.config.openboxes.jobs.messageRetrievalJob.cronExpression?:"0/5 * * * * ?"
    }

    def execute(context) {
        log.info "Executing message retrieval job "
        Boolean enabled = CH.config.openboxes.jobs.messageRetrievalJob.enabled?:true
        if (!enabled) {
            return
        }

        log.info "Starting message retrieval job at ${new Date()}"
        def startTime = System.currentTimeMillis()

        // Configuration
        String directory = grailsApplication.config.openboxes.integration.ftp.directory

        // Retrieve files by SFTP
        Map sftpConfig = grailsApplication.config.openboxes.integration.ftp.flatten()
        //SecureFtpClient sftpClient = new SecureFtpClient(sftpConfig)
        try {
//            // Get filenames
//            def filenames = sftpClient.listFiles(directory)
//            log.info "Processing ${filenames.size()} files"
//
//            // Process each file individually
//            filenames.each { String filename ->
//                String source = "${directory}/${filename}"
//                log.info "retrieving file ${source} ..."
//                def inputStream = sftpClient.retrieveFileAsInputStream(source)
//                log.info "source: ${source}"
//
//                String messageContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
//                // TODO Pass messageContent to be message pipeline to be validated and processed
//            }

        } catch(ConnectionException e) {
            log.error "Unable to retrieve message due to exception: " + e.message, e
        }
        finally {
            //sftpClient.disconnect()
        }
        log.info "Finished message retrieval job in " + (System.currentTimeMillis() - startTime) + " ms"
    }


}
