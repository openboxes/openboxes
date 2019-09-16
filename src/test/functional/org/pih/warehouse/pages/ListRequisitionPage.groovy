package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ListRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/list"
    static at = { title == "Requisition"}
    static content = {
    }
}