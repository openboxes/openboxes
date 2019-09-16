package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class CreateRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/create"
    static at = { title == "Create Requisition"}
    static content = {
        selectRequestingDepot { $("select[name='origin.id']") }
        autocompleteRequestedBy { $("input[name='requestedBy']") }
        firstSuggestion(wait: true){$("ul.ui-autocomplete li.ui-menu-item a").first()}
        createRequisitionButton { $("#save-requisition")}
        cancelRequisitionButton(to: ListRequisitionPage) { ${"input[name='cancelRequisition']"}}

    }

    def save() {
        $("#save-requisition").click()
        waitFor{ title == "Edit Requisition"}

    }
}
