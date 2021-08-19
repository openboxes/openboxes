package org.pih.warehouse.jobs

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
                cronExpression: CH.config.openboxes.jobs.messageRetrievalJob.cronExpression?:"0 * * * * ?"
    }

    def execute(context) {
        log.info "Executing"
        Boolean enabled = CH.config.openboxes.jobs.messageRetrievalJob.enabled?:true
        if (!enabled) {
            return
        }


        log.info "Starting message retrieval job at ${new Date()}"
        def startTime = System.currentTimeMillis()

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
        log.info "Found ${filenames.size()} files: "
        filenames.each { String filename ->
            String source = "${directory}/${filename}"
            log.info "retrieving file ${source} ..."
            //ftpClient.retrieveFile("${source}", "/tmp")
            def inputStream = ftpClient.retrieveFileAsInputStream(source)
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
            log.info "contents: " + text
        }
        ftpClient.disconnect()

        log.info "Finished message retrieval job in " + (System.currentTimeMillis() - startTime) + " ms"
    }


}
