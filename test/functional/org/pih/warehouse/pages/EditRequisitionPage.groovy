package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class EditRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/edit"
    static at = { title == "create requisition"}
    static content = {
        selectRequestingDepot { $("select[name='origin.id']") }
        autocompleteRequestedBy { $("input[name='requestedBy']") }
        editRequisitionButton(to: EditRequisitionPage) { $("#save-requisition")}
        cancelRequisition(to: ListRequisitionPage) { ${"input[name='cancelRequisition']"}}
    }
}
