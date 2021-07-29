/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.data

import org.springframework.context.annotation.Bean
import org.springframework.integration.Message
import org.springframework.integration.MessagingException
import org.springframework.integration.core.MessageHandler
import org.springframework.integration.core.MessageSource
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory
import org.springframework.scheduling.support.PeriodicTrigger


class SampleService {

    //DefaultSftpSessionFactory sftpSessionFactory


    @Bean
    DefaultSftpSessionFactory sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        //factory.setPrivateKey(privateKey);
        factory.setHost("localhost");
        factory.setUser("jmiranda");
        factory.setPassword("password")
        //factory.setPort(22);
        //factory.setAllowUnknownKeys(true);
        return factory;
    }

    @Bean(name=PollerMetadata.DEFAULT_POLLER_METADATA_BEAN_NAME)
    public PollerMetadata defaultPoller() {
        PollerMetadata pollerMetadata = new PollerMetadata()
        pollerMetadata.setTrigger(new PeriodicTrigger(600000))
        return pollerMetadata
    }

    @Bean
    SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
        SftpInboundFileSynchronizer fileSync = new SftpInboundFileSynchronizer(sftpSessionFactory());
        fileSync.setDeleteRemoteFiles(false);
        fileSync.setRemoteDirectory("messages");
        return fileSync;
    }

    @Bean
    public MessageSource<File> sftpMessageSource() {
    SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
        source.setLocalDirectory(new File("/tmp/local_inbound"));
        source.setAutoCreateLocalDirectory(true);
        return source;
    }

    @Bean
    MessageHandler messageHandler() {
        return new MessageHandler() {
            public void handleMessage(Message<?> arg0) throws MessagingException {
                File f = (File) arg0.getPayload();
                log.info "new file uploaded: " + f.name
                //do something usefull
            }

        };
    }


    boolean transactional = true

    def serviceMethod() {

        return sftpSessionFactory().properties



    }
}
