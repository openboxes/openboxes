package org.pih.warehouse.requisition

enum RequisitionItemSortByCode {

    DEFAULT("Sort by date added", "requisitionItemsByDateCreated"),
    SORT_INDEX("Sort by sort index", "requisitionItemsByOrderIndex"),
    CATEGORY("Sort by category", "requisitionItemsByCategory")

    // You could potentially add a human-readable name and method name to be called when
    final String friendlyName
    final String methodName

    RequisitionItemSortByCode(String friendlyName, String methodName) {
        this.friendlyName = friendlyName
        this.methodName = methodName
    }


    static list() {
        [DEFAULT, CATEGORY, SORT_INDEX]
    }

}

