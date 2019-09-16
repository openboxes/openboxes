package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ProcessRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/process"
    static at = { title == "Process requisition"}
    static content = {
        firstLi(wait:true) { $(".ui-accordion-li-fix").first() }
        firstAccordionRow(wait: true) { $(firstLi.find(".accordion-header")) }
        firstRowContent(wait: true) { $(firstLi.find(".accordion-content"))}
        firstRowContentPicked(wait: true) { $(firstRowContent.find(".number")) }
        secondLi(wait:true) { $(".ui-accordion-li-fix").last() }
        secondAccordionRow(wait: true) { $(secondLi.find(".accordion-header")) }
        secondRowContent(wait: true) { $(secondLi.find(".accordion-content")) }
        secondRowContentPicked(wait: true) { $(secondRowContent.find(".number")) }
        processRequisitionButton(to: ShowRequisitionPage) { $("#save-requisition") }
    }
}
