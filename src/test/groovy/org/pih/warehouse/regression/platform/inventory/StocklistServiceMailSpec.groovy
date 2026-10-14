package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.core.Attachment
import org.pih.warehouse.requisition.Requisition

/**
 * What a stock list mail carries: the spreadsheet and the PDF only when asked for, named after the stock list,
 * with the right content types.
 */
@Unroll
class StocklistServiceMailSpec extends Specification implements ServiceUnitTest<StocklistService>, DataTest {

    List<Attachment> sent
    Collection sentTo

    void setup() {
        Stocklist stocklist = new Stocklist(requisition: new Requisition(name: 'Ward A'), name: 'Ward A')
        service.metaClass.getStocklist = { String id -> id == 'sl-1' ? stocklist : null }
        service.documentService = [generateStocklistCsv: { OutputStream os, Stocklist s -> os.write([1, 2, 3] as byte[]) }]
        service.pdfRenderingService = [render: { Map args -> new ByteArrayOutputStream().with { write(9); it } }]
        service.mailService = [sendHtmlMailWithAttachment: { Collection to, String subject, String body, List<Attachment> attachments ->
            sentTo = to
            sent = attachments
            true
        }]
    }

    @PendingFeature(reason = 'OBAF labels the emailed .xls attachment with the Word document MIME type')
    void 'includeXls #includeXls and includePdf #includePdf attach #names'() {
        when:
        service.sendMail('sl-1', 'Subject', 'Body', ['a@example.org'], includePdf, includeXls)

        then:
        sentTo == ['a@example.org']
        sent*.name == names
        sent*.mimeType == types

        where:
        includeXls | includePdf || names                                        | types
        false      | false      || []                                           | []
        true       | false      || ['Stocklist - Ward A.xls']                   | ['application/vnd.ms-excel']
        false      | true       || ['Stocklist - Ward A.pdf']                   | ['application/pdf']
        true       | true       || ['Stocklist - Ward A.xls', 'Stocklist - Ward A.pdf'] | ['application/vnd.ms-excel', 'application/pdf']
    }

    void 'the spreadsheet attachment carries the generated workbook bytes'() {
        when:
        service.sendMail('sl-1', 'Subject', 'Body', ['a@example.org'], false, true)

        then:
        sent[0].bytes == [1, 2, 3] as byte[]
    }
}
