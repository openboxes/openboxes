package org.pih.warehouse.outbound

class ImportPackingListErrors {

    Map<String, List<String>> fulfillmentDetails

    Map<String, List<String>> sendingOptions

    Map<String, Map<String, Map<String, List<String>>>> packingList = new HashMap<>()
}
