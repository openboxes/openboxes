package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class EditRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/edit"
    static at = { title == "create requisition"}
    static content = {
        selectRequestingDepot { $("select[name='origin.id']") }
        autocompleteRequestedBy { $("input[name='requestedBy']") }
        submitRequisitionButton(to: EditRequisitionPage) { $("#save-requisition")}
        firstRequisitionItemProduct { $("input[name='ko_unique_2']")}
        firstRequisitionItemQuantity { $("input[name='ko_unique_3']")}
        secondRequisitionItemProduct(wait: true) { $("input[name='ko_unique_8']")}
        secondRequisitionItemQuantity(wait: true) { $("input[name='ko_unique_9']")}
        firstProductSuggestion(wait: true){$("#searchProduct0 li.ui-menu-item a").first()}
        secondProductSuggestion(wait: true){$("#searchProduct1 li.ui-menu-item a").first() }
        addRowButton { $("input[name='addRequisitionItemRow']") }
    }
}
