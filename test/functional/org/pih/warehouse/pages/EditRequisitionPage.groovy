package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class EditRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/edit"
    static at = { title == "Edit Requisition"}
    static content = {
        selectRequestingDepot { $("select[name='origin.id']") }
        autocompleteRequestedBy { $("input[name='requestedBy']") }
        submitRequisitionButton(to: EditRequisitionPage) { $("#save-requisition")}

        requisitionItemsRows(wait: true) { $("tr.requisitionItemsRow") }
        firstItemRow(wait: true) { requisitionItemsRows.first() }
        secondItemRow(wait: true) { requisitionItemsRows.last() }
        firstRequisitionItemProduct(wait: true) { firstItemRow.find(".autocomplete")}
        firstRequisitionItemQuantity(wait: true) { firstItemRow.find(".quantity")}
        secondRequisitionItemProduct(wait: true) { secondItemRow.find(".autocomplete")}
        secondRequisitionItemQuantity(wait: true) { secondItemRow.find(".quantity")}

        firstProductSuggestion(wait: true){$("#searchProduct0 li.ui-menu-item a").first()}
        secondProductSuggestion(wait: true){$("#searchProduct1 li.ui-menu-item a").first() }
        addRowButton { $("input[name='addRequisitionItemRow']") }
    }
}
