package org.pih.warehouse.api.regression.obaf.events.support

import org.pih.warehouse.core.MailService

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Stands in for NotificationService.mailService while a spec runs: every MailService entry point
 * funnels into doSendMail, which is recorded here instead of contacting an SMTP server.
 */
class RecordingMailService extends MailService {

    static class Mail {
        String subject
        String body
        List<String> to
        String thread
    }

    final List<Mail> sent = new CopyOnWriteArrayList<>()

    @Override
    Boolean doSendMail(String subject, String body, String from, String fromName, Collection to, Collection cc,
                       Collection attachments, Integer port, Boolean useHtml, Boolean override) {
        sent << new Mail(subject: subject, body: body, to: (to ?: []).collect { it as String },
                thread: Thread.currentThread().name)
        return true
    }
}
