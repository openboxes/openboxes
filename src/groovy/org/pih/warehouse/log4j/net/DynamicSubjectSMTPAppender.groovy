package org.pih.warehouse.log4j.net

import org.apache.log4j.Layout
import org.apache.log4j.PatternLayout
import org.apache.log4j.helpers.LogLog
import org.apache.log4j.net.SMTPAppender
import org.apache.log4j.spi.LoggingEvent

import javax.mail.Multipart

// import java.util.Date;

import javax.mail.Transport
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility

/**
 *
 */
class DynamicSubjectSMTPAppender extends SMTPAppender {
    @Override
    protected void sendBuffer() {

        // Note: this code already owns the monitor for this
        // appender. This frees us from needing to synchronize on 'cb'.
        try {
            MimeBodyPart part = new MimeBodyPart()

            int length = cb.length()

            StringBuffer sbuf = new StringBuffer()
            String t = layout.getHeader()
            if (t != null) sbuf.append(t)
            int len = cb.length()
            for (int i = 0; i < len; i++) {
                LoggingEvent event = cb.get()

                // setting the subject
                if (i == 0) {
                    Layout subjectLayout = new PatternLayout(getSubject())
                    String subject = MimeUtility.encodeText(subjectLayout.format(event), "UTF-8", null)
                    // Remove newlines from subject
                    if (subject != null) subject = subject.replace("\n", "")
                    msg.setSubject(subject)
                }

                sbuf.append(layout.format(event))
                if (layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep()
                    if (s != null) {
                        for (int j = 0; j < s.length; j++) {
                            sbuf.append(s[j])
                            sbuf.append(Layout.LINE_SEP)
                        }
                    }
                }
            }
            t = layout.getFooter()
            if (t != null) sbuf.append(t)
            part.setContent(sbuf.toString(), layout.getContentType())

            Multipart mp = new MimeMultipart()
            mp.addBodyPart(part)
            msg.setContent(mp)

            msg.setSentDate(new Date())
            Transport.send(msg)
        } catch (Exception e) {
            LogLog.error("Error occured while sending e-mail notification.", e)
        }
    }
}
