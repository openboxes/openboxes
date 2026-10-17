package org.pih.warehouse.core

import spock.lang.Specification

class LabelTemplateExceptionSpec extends Specification {

    void "the message names the template and keeps uploaded text out of HTML and on one line"() {
        given:
        Document document = new Document(name: 'bin <script>x</script>', documentNumber: "barcodeLabel:\ninternalLocation")

        when:
        String message = new LabelTemplateException(document, new IllegalArgumentException('bad "${<img src=x>}"\r\nnext')).message

        then:
        message.startsWith("Label template 'bin scriptx/script' (barcodeLabel: internalLocation) can't be rendered: ")
        !message.contains("<") && !message.contains(">") && !message.contains('"')
        !message.contains("\n") && !message.contains("\r")
        message.endsWith("Update the template and upload it again.")
    }
}
