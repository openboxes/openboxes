package install

databaseChangeLog = {

    changeSet(author: "openboxes (generated)", id: "1692069478960-3", objectQuotingStrategy: "LEGACY") { 
        insert(tableName: "container_type") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Container")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Pallet")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Suitcase")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Box")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Trunk")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "6")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Item")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "7")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Other")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "8")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-02-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2011-02-25T00:00")
            column(name: "name", value: "Crate")
            column(name: "sort_order", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-5", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "document_type") { 
            column(name: "id", value: "1") 
            column(name: "version", valueNumeric: "0") 
            column(name: "date_created", value: "2010-08-25T00:00") 
            column(name: "description") 
            column(name: "last_updated", value: "2010-08-25T00:00") 
            column(name: "name", value: "Facture donn") 
            column(name: "sort_order", valueNumeric: "0") 
            column(name: "document_code")
        }

        insert(tableName: "document_type") { 
            column(name: "id", value: "2") 
            column(name: "version", valueNumeric: "0") 
            column(name: "date_created", value: "2010-08-25T00:00") 
            column(name: "description") 
            column(name: "last_updated", value: "2010-08-25T00:00") 
            column(name: "name", value: "Bill of Lading") 
            column(name: "sort_order", valueNumeric: "0") 
            column(name: "document_code") 
        }

        insert(tableName: "document_type") { 
            column(name: "id", value: "3") 
            column(name: "version", valueNumeric: "0") 
            column(name: "date_created", value: "2010-08-25T00:00") 
            column(name: "description") 
            column(name: "last_updated", value: "2010-08-25T00:00") 
            column(name: "name", value: "Packing List") 
            column(name: "sort_order", valueNumeric: "0") 
            column(name: "document_code") 
        }

        insert(tableName: "document_type") { 
            column(name: "id", value: "4") 
            column(name: "version", valueNumeric: "0") 
            column(name: "date_created", value: "2010-08-25T00:00") 
            column(name: "description") 
            column(name: "last_updated", value: "2010-08-25T00:00") 
            column(name: "name", value: "Certificate of Donation") 
            column(name: "sort_order", valueNumeric: "0") 
            column(name: "document_code") 
        }

        insert(tableName: "document_type") { 
            column(name: "id", value: "5") 
            column(name: "version", valueNumeric: "0") 
            column(name: "date_created", value: "2010-08-25T00:00") 
            column(name: "description") 
            column(name: "last_updated", value: "2010-08-25T00:00") 
            column(name: "name", value: "Commercial Invoice") 
            column(name: "sort_order", valueNumeric: "0") 
            column(name: "document_code") 
        }

        insert(tableName: "document_type") { 
            column(name: "id", value: "6") 
            column(name: "version", valueNumeric: "0") 
            column(name: "date_created", value: "2010-08-25T00:00") 
            column(name: "description") 
            column(name: "last_updated", value: "2010-08-25T00:00") 
            column(name: "name", value: "Material Safey Data Sheet") 
            column(name: "sort_order", valueNumeric: "0") 
            column(name: "document_code") 
        
        }

        insert(tableName: "document_type") {
            column(name: "id", value: "66762f6c61e34cfd9297ecb0fcee2df2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-08-14T15:06:59")
            column(name: "description")
            column(name: "last_updated", value: "2023-08-14T15:06:59")
            column(name: "name", value: "Invoice Template")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "document_code", value: "INVOICE_TEMPLATE")
        }

        insert(tableName: "document_type") {
            column(name: "id", value: "7")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Certificate of Analysis")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "document_code")
        }

        insert(tableName: "document_type") {
            column(name: "id", value: "8")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Manifest")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "document_code")
        }

        insert(tableName: "document_type") {
            column(name: "id", value: "9")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Other")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "document_code")
        }

        insert(tableName: "document_type") {
            column(name: "id", value: "ff80818166ac5f350166ac84c2150001")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-10-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2018-10-25T00:00")
            column(name: "name", value: "Shipping Template")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "document_code", value: "SHIPPING_TEMPLATE")
        }

        insert(tableName: "document_type") {
            column(name: "id", value: "REQUISITION_TEMPLATE")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-08-14T15:09:29")
            column(name: "description")
            column(name: "last_updated", value: "2023-08-14T15:09:29")
            column(name: "name", value: "Requisition Template")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "document_code", value: "REQUISITION_TEMPLATE")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-7", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "event_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-03T00:00")
            column(name: "description", value: "Shipment has been shipped")
            column(name: "event_code", value: "SHIPPED")
            column(name: "last_updated", value: "2011-03-03T00:00")
            column(name: "name", value: "Shipped")
            column(name: "sort_order", valueNumeric: "2")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-03T00:00")
            column(name: "description", value: "Shipment has been received")
            column(name: "event_code", value: "RECEIVED")
            column(name: "last_updated", value: "2011-03-03T00:00")
            column(name: "name", value: "Received")
            column(name: "sort_order", valueNumeric: "3")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-09-10T00:00")
            column(name: "description", value: "Shipment has been partially received")
            column(name: "event_code", value: "PARTIALLY_RECEIVED")
            column(name: "last_updated", value: "2018-09-10T00:00")
            column(name: "name", value: "Partially Received")
            column(name: "sort_order", valueNumeric: "3")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "abb5b3d2-da3e-4a4c-9e3c-de139147f822")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-09-21T00:00")
            column(name: "description", value: "Request has been sent for approval")
            column(name: "event_code", value: "PENDING_APPROVAL")
            column(name: "last_updated", value: "2023-09-21T00:00")
            column(name: "name", value: "Waiting for approval")
            column(name: "sort_order", valueNumeric: "1")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "1738ff9b-b9fb-4369-81d6-08f61f058137")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-09-21T00:00")
            column(name: "description", value: "Request has been approved")
            column(name: "event_code", value: "APPROVED")
            column(name: "last_updated", value: "2023-09-21T00:00")
            column(name: "name", value: "Approved")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "30e81101-e20e-47cc-aef0-9004609dd38e")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-09-21T00:00")
            column(name: "description", value: "Request has been submitted")
            column(name: "event_code", value: "SUBMITTED")
            column(name: "last_updated", value: "2023-09-21T00:00")
            column(name: "name", value: "Submitted")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "32fa3298-439c-4f12-a3bc-9a29ddf11a21")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-09-21T00:00")
            column(name: "description", value: "Request has been rejected")
            column(name: "event_code", value: "REJECTED")
            column(name: "last_updated", value: "2023-09-21T00:00")
            column(name: "name", value: "Rejected")
            column(name: "sort_order", valueNumeric: "0")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692069478960-9", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "invoice_type") {
            column(name: "id", value: "1")
            column(name: "name", value: "Purchase Invoice")
            column(name: "description", value: "")
            column(name: "code", value: "INVOICE")
            column(name: "date_created", value: "2023-08-14T15:00:15")
            column(name: "last_updated", value: "2023-08-14T15:00:15")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "invoice_type") {
            column(name: "id", value: "2")
            column(name: "name", value: "Sales Invoice")
            column(name: "description", value: "")
            column(name: "code", value: "INVOICE")
            column(name: "date_created", value: "2023-08-14T15:00:15")
            column(name: "last_updated", value: "2023-08-14T15:00:15")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "invoice_type") {
            column(name: "id", value: "3")
            column(name: "name", value: "Return Invoice")
            column(name: "description", value: "")
            column(name: "code", value: "INVOICE")
            column(name: "date_created", value: "2023-08-14T15:00:15")
            column(name: "last_updated", value: "2023-08-14T15:00:15")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "invoice_type") {
            column(name: "id", value: "4")
            column(name: "name", value: "Credit Note")
            column(name: "description", value: "")
            column(name: "code", value: "CREDIT_NOTE")
            column(name: "date_created", value: "2023-08-14T15:00:15")
            column(name: "last_updated", value: "2023-08-14T15:00:15")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "invoice_type") {
            column(name: "id", value: "5")
            column(name: "name", value: "Prepayment Invoice")
            column(name: "description")
            column(name: "code", value: "PREPAYMENT_INVOICE")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-11", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "location_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-12-06T00:00")
            column(name: "description", value: "Depot")
            column(name: "last_updated", value: "2010-12-06T00:00")
            column(name: "name", value: "Depot")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "DEPOT")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-12-06T00:00")
            column(name: "description", value: "Dispensary")
            column(name: "last_updated", value: "2011-11-14T00:00")
            column(name: "name", value: "Dispensary")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "DISPENSARY")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-12-06T00:00")
            column(name: "description", value: "Supplier")
            column(name: "last_updated", value: "2010-12-06T00:00")
            column(name: "name", value: "Supplier")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "SUPPLIER")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2012-12-06T00:00")
            column(name: "description", value: "Ward")
            column(name: "last_updated", value: "2012-12-06T00:00")
            column(name: "name", value: "Ward")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "WARD")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "6")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2012-12-06T00:00")
            column(name: "description", value: "Pharmacy")
            column(name: "last_updated", value: "2012-12-06T00:00")
            column(name: "name", value: "Pharmacy")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "DISPENSARY")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "cab2b48e649c71940164a13750f40001")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-07-15T00:00")
            column(name: "description", value: "Default receiving location type")
            column(name: "last_updated", value: "2018-07-15T00:00")
            column(name: "name", value: "Cross-docking")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "INTERNAL")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "cab2b4f35ba2d867015ba2e17e390001")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2017-08-07T00:00")
            column(name: "description", value: "Default bin location type")
            column(name: "last_updated", value: "2017-08-07T00:00")
            column(name: "name", value: "Bin Location")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "BIN_LOCATION")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "ff8081816482352b01648249e8cc0001")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-07-09T00:00")
            column(name: "description", value: "Default receiving location type")
            column(name: "last_updated", value: "2018-07-09T00:00")
            column(name: "name", value: "Receiving")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "INTERNAL")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "HOLD_LOCATION")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-08-14T15:05:15")
            column(name: "description", value: "Default hold location type")
            column(name: "last_updated", value: "2023-08-14T15:05:15")
            column(name: "name", value: "Hold")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "INTERNAL")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "ZONE")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2021-05-28T00:00")
            column(name: "description", value: "Default zone location type")
            column(name: "last_updated", value: "2021-05-28T00:00")
            column(name: "name", value: "Zone")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "location_type_code", value: "ZONE")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-12", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "MANAGE_INVENTORY")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "PLACE_ORDER")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "PLACE_REQUEST")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "FULFILL_REQUEST")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "SEND_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "RECEIVE_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "EXTERNAL")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "ADJUST_INVENTORY")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "SUBMIT_REQUEST")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "ENABLE_FULFILLER_APPROVAL_NOTIFICATIONS")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "PICK_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "2")
            column(name: "supported_activities_string", value: "PUTAWAY_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "3")
            column(name: "supported_activities_string", value: "DYNAMIC_CREATION")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "3")
            column(name: "supported_activities_string", value: "SEND_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "3")
            column(name: "supported_activities_string", value: "RECEIVE_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "4")
            column(name: "supported_activities_string", value: "FULFILL_ORDER")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "4")
            column(name: "supported_activities_string", value: "SEND_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "4")
            column(name: "supported_activities_string", value: "EXTERNAL")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "5")
            column(name: "supported_activities_string", value: "DYNAMIC_CREATION")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "6")
            column(name: "supported_activities_string", value: "DYNAMIC_CREATION")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "ff8081816482352b01648249e8cc0001")
            column(name: "supported_activities_string", value: "RECEIVE_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "ff8081816482352b01648249e8cc0001")
            column(name: "supported_activities_string", value: "PUTAWAY_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "ff8081816482352b01648249e8cc0001")
            column(name: "supported_activities_string", value: "PICK_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "cab2b4f35ba2d867015ba2e17e390001")
            column(name: "supported_activities_string", value: "PUTAWAY_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "cab2b4f35ba2d867015ba2e17e390001")
            column(name: "supported_activities_string", value: "PICK_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "cab2b48e649c71940164a13750f40001")
            column(name: "supported_activities_string", value: "RECEIVE_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "cab2b48e649c71940164a13750f40001")
            column(name: "supported_activities_string", value: "PUTAWAY_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "cab2b48e649c71940164a13750f40001")
            column(name: "supported_activities_string", value: "PICK_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "HOLD_LOCATION")
            column(name: "supported_activities_string", value: "HOLD_STOCK")
        }

        insert(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", value: "HOLD_LOCATION")
            column(name: "supported_activities_string", value: "PUTAWAY_STOCK")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-13", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "order_adjustment_type") {
            column(name: "id", value: "DISCOUNT")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Discount")
            column(name: "code", value: "DISCOUNT_ADJUSTMENT")
            column(name: "description")
            column(name: "date_created", value: "2020-02-27T00:00")
            column(name: "last_updated", value: "2020-02-27T00:00")
            column(name: "gl_account_id")
        }

        insert(tableName: "order_adjustment_type") {
            column(name: "id", value: "FEE")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Fee")
            column(name: "code", value: "FEE")
            column(name: "description")
            column(name: "date_created", value: "2020-02-27T00:00")
            column(name: "last_updated", value: "2020-02-27T00:00")
            column(name: "gl_account_id")
        }

        insert(tableName: "order_adjustment_type") {
            column(name: "id", value: "MISCELLANEOUS")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Miscellaneous Charge")
            column(name: "code", value: "MISCELLANEOUS_CHARGE")
            column(name: "description")
            column(name: "date_created", value: "2020-02-27T00:00")
            column(name: "last_updated", value: "2020-02-27T00:00")
            column(name: "gl_account_id")
        }

        insert(tableName: "order_adjustment_type") {
            column(name: "id", value: "SALES_TAX")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Sales Tax")
            column(name: "code", value: "SALES_TAX")
            column(name: "description")
            column(name: "date_created", value: "2020-02-27T00:00")
            column(name: "last_updated", value: "2020-02-27T00:00")
            column(name: "gl_account_id")
        }

        insert(tableName: "order_adjustment_type") {
            column(name: "id", value: "SHIPPING_CHARGE")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Shipping Charge")
            column(name: "code", value: "SHIPPING_CHARGE")
            column(name: "description")
            column(name: "date_created", value: "2020-02-27T00:00")
            column(name: "last_updated", value: "2020-02-27T00:00")
            column(name: "gl_account_id")
        }

        insert(tableName: "order_adjustment_type") {
            column(name: "id", value: "SURCHARGE")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Surcharge")
            column(name: "code", value: "SURCHARGE_ADJUSTMENT")
            column(name: "description")
            column(name: "date_created", value: "2020-02-27T00:00")
            column(name: "last_updated", value: "2020-02-27T00:00")
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-14", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "order_type") {
            column(name: "id", value: "PRODUCTION_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Production Order")
            column(name: "description", value: "")
            column(name: "code", value: "PRODUCTION_ORDER")
            column(name: "order_type_code", value: "PRODUCTION_ORDER")
            column(name: "date_created", value: "2023-08-14T15:04:38")
            column(name: "last_updated", value: "2023-08-14T15:04:38")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "order_type") {
            column(name: "id", value: "PURCHASE_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Purchase Order")
            column(name: "description", value: "")
            column(name: "code", value: "PURCHASE_ORDER")
            column(name: "order_type_code", value: "PURCHASE_ORDER")
            column(name: "date_created", value: "2023-08-14T15:04:38")
            column(name: "last_updated", value: "2023-08-14T15:04:38")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "order_type") {
            column(name: "id", value: "PUTAWAY_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Putaway Order")
            column(name: "description", value: "")
            column(name: "code", value: "PUTAWAY_ORDER")
            column(name: "order_type_code", value: "TRANSFER_ORDER")
            column(name: "date_created", value: "2023-08-14T15:04:38")
            column(name: "last_updated", value: "2023-08-14T15:04:38")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "order_type") {
            column(name: "id", value: "RETURN_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Return Order")
            column(name: "description", value: "")
            column(name: "code", value: "RETURN_ORDER")
            column(name: "order_type_code", value: "TRANSFER_ORDER")
            column(name: "date_created", value: "2023-08-14T15:06:45")
            column(name: "last_updated", value: "2023-08-14T15:06:45")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "order_type") {
            column(name: "id", value: "SALES_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Sales Order")
            column(name: "description", value: "")
            column(name: "code", value: "SALES_ORDER")
            column(name: "order_type_code", value: "SALES_ORDER")
            column(name: "date_created", value: "2023-08-14T15:04:38")
            column(name: "last_updated", value: "2023-08-14T15:04:38")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "order_type") {
            column(name: "id", value: "TRANSFER_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Transfer Order")
            column(name: "description", value: "")
            column(name: "code", value: "TRANSFER_ORDER")
            column(name: "order_type_code", value: "TRANSFER_ORDER")
            column(name: "date_created", value: "2023-08-14T15:04:38")
            column(name: "last_updated", value: "2023-08-14T15:04:38")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "order_type") {
            column(name: "id", value: "WORK_ORDER")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Work Order")
            column(name: "description", value: "")
            column(name: "code", value: "WORK_ORDER")
            column(name: "order_type_code", value: "WORK_ORDER")
            column(name: "date_created", value: "2023-08-14T15:04:38")
            column(name: "last_updated", value: "2023-08-14T15:04:38")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-17", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "party_type") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "ORG")
            column(name: "date_created", value: "2018-03-06T00:00")
            column(name: "description", value: "Organization")
            column(name: "last_updated", value: "2018-03-06T00:00")
            column(name: "name", value: "Organization")
            column(name: "party_type_code", value: "ORGANIZATION")
        }

        insert(tableName: "party_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "PERSON")
            column(name: "date_created", value: "2018-03-06T00:00")
            column(name: "description", value: "Person")
            column(name: "last_updated", value: "2018-03-06T00:00")
            column(name: "name", value: "Person")
            column(name: "party_type_code", value: "PERSON")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-18", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "payment_term") {
            column(name: "id", value: "COD")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Cash On Delivery")
            column(name: "code", value: "COD")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent")
            column(name: "days_to_payment")
        }

        insert(tableName: "payment_term") {
            column(name: "id", value: "NET30")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Net 30")
            column(name: "code", value: "NET30")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent")
            column(name: "days_to_payment", valueNumeric: "30")
        }

        insert(tableName: "payment_term") {
            column(name: "id", value: "PIA")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Payment In Advance")
            column(name: "code", value: "PIA")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent")
            column(name: "days_to_payment")
        }

        insert(tableName: "payment_term") {
            column(name: "id", value: "PP100")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Prepayment 100")
            column(name: "code", value: "PP100")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent", valueNumeric: "100.00")
            column(name: "days_to_payment")
        }

        insert(tableName: "payment_term") {
            column(name: "id", value: "PP25")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Prepayment 25/Net 30")
            column(name: "code", value: "PP25")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent", valueNumeric: "25.00")
            column(name: "days_to_payment", valueNumeric: "30")
        }

        insert(tableName: "payment_term") {
            column(name: "id", value: "PP50")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Prepayment 50/Net 30")
            column(name: "code", value: "PP50")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent", valueNumeric: "50.00")
            column(name: "days_to_payment", valueNumeric: "30")
        }

        insert(tableName: "payment_term") {
            column(name: "id", value: "US")
            column(name: "version", valueNumeric: "0")
            column(name: "name", value: "Upon shipment")
            column(name: "code", value: "US")
            column(name: "description")
            column(name: "date_created", value: "2023-08-14T15:02:59")
            column(name: "last_updated", value: "2023-08-14T15:02:59")
            column(name: "prepayment_percent")
            column(name: "days_to_payment", valueNumeric: "30")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-20", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "preference_type") {
            column(name: "id", value: "APPROVED")
            column(name: "name", value: "Approved")
            column(name: "validation_code", value: "DEFAULT")
            column(name: "date_created", value: "2023-08-14T14:59:20")
            column(name: "last_updated", value: "2023-08-14T14:59:20")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "preference_type") {
            column(name: "id", value: "CONTRACT")
            column(name: "name", value: "Contract")
            column(name: "validation_code", value: "PREFERRED")
            column(name: "date_created", value: "2023-08-14T14:59:20")
            column(name: "last_updated", value: "2023-08-14T14:59:20")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "preference_type") {
            column(name: "id", value: "DO NOT ORDER")
            column(name: "name", value: "Do Not Order")
            column(name: "validation_code", value: "BLOCK")
            column(name: "date_created", value: "2023-08-14T14:59:20")
            column(name: "last_updated", value: "2023-08-14T14:59:20")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "preference_type") {
            column(name: "id", value: "NOT QUALIFIED")
            column(name: "name", value: "Not Qualified")
            column(name: "validation_code", value: "HIDE")
            column(name: "date_created", value: "2023-08-14T14:59:20")
            column(name: "last_updated", value: "2023-08-14T14:59:20")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "preference_type") {
            column(name: "id", value: "PREFERRED")
            column(name: "name", value: "Preferred")
            column(name: "validation_code", value: "DEFAULT")
            column(name: "date_created", value: "2023-08-14T14:59:20")
            column(name: "last_updated", value: "2023-08-14T14:59:20")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }

        insert(tableName: "preference_type") {
            column(name: "id", value: "QUALIFIED")
            column(name: "name", value: "Qualified")
            column(name: "validation_code", value: "DEFAULT")
            column(name: "date_created", value: "2023-08-14T14:59:20")
            column(name: "last_updated", value: "2023-08-14T14:59:20")
            column(name: "created_by_id")
            column(name: "updated_by_id")
            column(name: "version", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-21", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "product_type") {
            column(name: "id", value: "DEFAULT")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2023-08-14T15:08:23")
            column(name: "last_updated", value: "2023-08-14T15:08:23")
            column(name: "name", value: "Default")
            column(name: "product_type_code", value: "GOOD")
            column(name: "product_identifier_format", value: "")
            column(name: "code", value: "")
            column(name: "sequence_number", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-22", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "product_type_supported_activities") {
            column(name: "product_type_id", value: "DEFAULT")
            column(name: "product_activity_code", value: "SEARCHABLE_NO_STOCK")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-23", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "reference_number_type") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description", value: "Purchase Order Number")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Purchase Order Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "10")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-09-10T00:00")
            column(name: "description", value: "Tracking Number")
            column(name: "last_updated", value: "2018-09-10T00:00")
            column(name: "name", value: "Tracking Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description", value: "Customer name")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Customer name")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description", value: "Internal Identifier")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Internal Identifier")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description", value: "Bill of Lading Number")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Bill of Lading Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-01T00:00")
            column(name: "description", value: "Air Waybill Number")
            column(name: "last_updated", value: "2011-03-01T00:00")
            column(name: "name", value: "Air Waybill Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "6")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-01T00:00")
            column(name: "description", value: "Container Number")
            column(name: "last_updated", value: "2011-03-01T00:00")
            column(name: "name", value: "Container Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "7")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-01T00:00")
            column(name: "description", value: "Seal Number")
            column(name: "last_updated", value: "2011-03-01T00:00")
            column(name: "name", value: "Seal Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "8")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-01T00:00")
            column(name: "description", value: "Flight Number")
            column(name: "last_updated", value: "2011-03-01T00:00")
            column(name: "name", value: "Flight Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "9")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2012-01-26T00:00")
            column(name: "description", value: "License Plate Number")
            column(name: "last_updated", value: "2012-01-26T00:00")
            column(name: "name", value: "License Plate Number")
            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "reference_number_type") {
            column(name: "id", value: "VENDOR_INVOICE_NUMBER")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2021-03-16T00:00")
            column(name: "description", value: "Invoice reference number generated by vendor")
            column(name: "last_updated", value: "2021-03-16T00:00")
            column(name: "name", value: "Vendor Invoice Number")
            column(name: "sort_order", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-24", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "role") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_ADMIN")
            column(name: "name", value: "Admin")
        }

        insert(tableName: "role") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_MANAGER")
            column(name: "name", value: "Manager")
        }

        insert(tableName: "role") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_BROWSER")
            column(name: "name", value: "Browser")
        }

        insert(tableName: "role") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_ASSISTANT")
            column(name: "name", value: "Assistant")
        }

        insert(tableName: "role") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_SUPERUSER")
            column(name: "name", value: "Superuser")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff80818162d9f9cc0162d9fbbeb40001")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about users.")
            column(name: "role_type", value: "ROLE_USER_NOTIFICATION")
            column(name: "name", value: "User Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff80818162d9f9cc0162d9fbf09e0002")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about products.")
            column(name: "role_type", value: "ROLE_PRODUCT_NOTIFICATION")
            column(name: "name", value: "Product Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff80818162d9f9cc0162d9fc1c220003")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about shipments.")
            column(name: "role_type", value: "ROLE_SHIPMENT_NOTIFICATION")
            column(name: "name", value: "All Shipment Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff80818162d9f9cc0162d9fc48990004")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about errors.")
            column(name: "role_type", value: "ROLE_ERROR_NOTIFICATION")
            column(name: "name", value: "Error Notification")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff80818162d9f9cc0162d9fc74fa0005")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about feedback.")
            column(name: "role_type", value: "ROLE_FEEDBACK_NOTIFICATION")
            column(name: "name", value: "Feedback Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff80818162debf330162dec0548e0001")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about orders.")
            column(name: "role_type", value: "ROLE_ORDER_NOTIFICATION")
            column(name: "name", value: "Order Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff808181681c757c01681c89c4960001")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Allow user to view and modify financial information")
            column(name: "role_type", value: "ROLE_FINANCE")
            column(name: "name", value: "Finance")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff808181681c757c01681c89c4960002")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Allow user to view and modify invoice information")
            column(name: "role_type", value: "ROLE_INVOICE")
            column(name: "name", value: "Invoice")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff8081816cdefc9d016cdf09a9ee0001")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about orders.")
            column(name: "role_type", value: "ROLE_ITEM_ALL_NOTIFICATION")
            column(name: "name", value: "All Stock Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff8081816cfeeaf0016cff174ca70001")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about expiring/expired items.")
            column(name: "role_type", value: "ROLE_ITEM_EXPIRY_NOTIFICATION")
            column(name: "name", value: "Expiry Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff8081816cfeeaf0016cff18057e0002")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about overstocked items.")
            column(name: "role_type", value: "ROLE_ITEM_OVERSTOCK_NOTIFICATION")
            column(name: "name", value: "Overstock Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff8081816cfeeaf0016cff1834080003")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about reorder items.")
            column(name: "role_type", value: "ROLE_ITEM_REORDER_NOTIFICATION")
            column(name: "name", value: "Reorder Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff8081816cfeeaf0016cff18617d0004")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about low stock items.")
            column(name: "role_type", value: "ROLE_ITEM_LOW_STOCK_NOTIFICATION")
            column(name: "name", value: "Low Stock Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ff8081816cfeeaf0016cff1b6e780005")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who should receive notifications about stockouts.")
            column(name: "role_type", value: "ROLE_ITEM_OUT_OF_STOCK_NOTIFICATION")
            column(name: "name", value: "Out of Stock Notifications")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ROLE_PURCHASE_APPROVER")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_PURCHASE_APPROVER")
            column(name: "name", value: "Purchase approver")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ROLE_REQUISITION_APPROVER")
            column(name: "version", valueNumeric: "0")
            column(name: "description")
            column(name: "role_type", value: "ROLE_REQUISITION_APPROVER")
            column(name: "name", value: "Request approver")
        }

        insert(tableName: "role") {
            column(name: "id", value: "ROLE_PRODUCT_MANAGER")
            column(name: "version", valueNumeric: "0")
            column(name: "description", value: "Role that represents users who have the permission to manage products")
            column(name: "role_type", value: "ROLE_PRODUCT_MANAGER")
            column(name: "name", value: "Product Manager")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-25", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "shipment_type") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Air")
            column(name: "sort_order", valueNumeric: "1")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Sea")
            column(name: "sort_order", valueNumeric: "2")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-08-22T00:00")
            column(name: "description", value: "")
            column(name: "last_updated", value: "2011-08-22T00:00")
            column(name: "name", value: "Land")
            column(name: "sort_order", valueNumeric: "3")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Suitcase")
            column(name: "sort_order", valueNumeric: "4")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2018-08-28T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2018-08-28T00:00")
            column(name: "name", value: "Default")
            column(name: "sort_order", valueNumeric: "5")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-26", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "shipment_workflow") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-02-28T00:00")
            column(name: "last_updated", value: "2011-02-28T00:00")
            column(name: "name", value: "Air shipment workflow")
            column(name: "shipment_type_id", value: "1")
            column(name: "excluded_fields", value: "carrier,recipient")
            column(name: "document_template")
        }

        insert(tableName: "shipment_workflow") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-02-28T00:00")
            column(name: "last_updated", value: "2011-02-28T00:00")
            column(name: "name", value: "Sea shipment workflow")
            column(name: "shipment_type_id", value: "2")
            column(name: "excluded_fields", value: "carrier,recipient")
            column(name: "document_template")
        }

        insert(tableName: "shipment_workflow") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-02-28T00:00")
            column(name: "last_updated", value: "2011-02-28T00:00")
            column(name: "name", value: "Suitcase shipment workflow")
            column(name: "shipment_type_id", value: "4")
            column(name: "excluded_fields", value: "shipmentMethod.shipper")
            column(name: "document_template", value: "suitcaseLetter")
        }

        insert(tableName: "shipment_workflow") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2012-01-26T00:00")
            column(name: "last_updated", value: "2012-01-26T00:00")
            column(name: "name", value: "Land shipment workflow")
            column(name: "shipment_type_id", value: "3")
            column(name: "excluded_fields")
            column(name: "document_template")
        }

        insert(tableName: "shipment_workflow") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-08-28T00:00")
            column(name: "last_updated", value: "2018-08-28T00:00")
            column(name: "name", value: "Stock movement shipment workflow")
            column(name: "shipment_type_id", value: "5")
            column(name: "excluded_fields")
            column(name: "document_template")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-27", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", value: "1")
            column(name: "container_type_id", value: "2")
            column(name: "container_types_idx", valueNumeric: "0")
        }

        insert(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", value: "1")
            column(name: "container_type_id", value: "8")
            column(name: "container_types_idx", valueNumeric: "1")
        }

        insert(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", value: "2")
            column(name: "container_type_id", value: "2")
            column(name: "container_types_idx", valueNumeric: "0")
        }

        insert(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", value: "2")
            column(name: "container_type_id", value: "8")
            column(name: "container_types_idx", valueNumeric: "1")
        }

        insert(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", value: "3")
            column(name: "container_type_id", value: "3")
            column(name: "container_types_idx", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-28", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "1")
            column(name: "reference_number_type_id", value: "5")
            column(name: "reference_number_types_idx", valueNumeric: "0")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "1")
            column(name: "reference_number_type_id", value: "6")
            column(name: "reference_number_types_idx", valueNumeric: "1")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "1")
            column(name: "reference_number_type_id", value: "7")
            column(name: "reference_number_types_idx", valueNumeric: "2")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "2")
            column(name: "reference_number_type_id", value: "4")
            column(name: "reference_number_types_idx", valueNumeric: "0")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "2")
            column(name: "reference_number_type_id", value: "6")
            column(name: "reference_number_types_idx", valueNumeric: "1")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "2")
            column(name: "reference_number_type_id", value: "7")
            column(name: "reference_number_types_idx", valueNumeric: "2")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "3")
            column(name: "reference_number_type_id", value: "8")
            column(name: "reference_number_types_idx", valueNumeric: "0")
        }

        insert(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", value: "4")
            column(name: "reference_number_type_id", value: "9")
            column(name: "reference_number_types_idx", valueNumeric: "0")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-31", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "transaction_type") {
            column(name: "id", value: "10")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-04-05T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2011-04-05T00:00")
            column(name: "name", value: "Adjustment - Debit")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "DEBIT")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "11")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-04-06T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2011-04-06T00:00")
            column(name: "name", value: "Product Inventory")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "PRODUCT_INVENTORY")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-11-08T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-11-08T00:00")
            column(name: "name", value: "Consumption")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "DEBIT")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-11-08T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-11-08T00:00")
            column(name: "name", value: "Adjustment")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "CREDIT")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-11-08T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-11-08T00:00")
            column(name: "name", value: "Expired")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "DEBIT")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-11-08T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-11-08T00:00")
            column(name: "name", value: "Damaged")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "DEBIT")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "7")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-12-06T00:00")
            column(name: "description", value: "Inventory")
            column(name: "last_updated", value: "2010-12-06T00:00")
            column(name: "name", value: "Inventory")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "INVENTORY")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "8")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-17T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2011-03-17T00:00")
            column(name: "name", value: "Transfer In")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "CREDIT")
        }

        insert(tableName: "transaction_type") {
            column(name: "id", value: "9")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2011-03-17T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2011-03-17T00:00")
            column(name: "name", value: "Transfer Out")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "transaction_code", value: "DEBIT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-32", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "BTL")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "BTL")
            column(name: "description")
            column(name: "name", value: "Bottle")
            column(name: "uom_class_id", value: "QUANTITY")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "BX")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "BX")
            column(name: "description")
            column(name: "name", value: "Box")
            column(name: "uom_class_id", value: "QUANTITY")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "CS")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "CS")
            column(name: "description")
            column(name: "name", value: "Case")
            column(name: "uom_class_id", value: "QUANTITY")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "EA")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "EA")
            column(name: "description")
            column(name: "name", value: "Each")
            column(name: "uom_class_id", value: "QUANTITY")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "EUR")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "EUR")
            column(name: "description", value: "EUR (€)")
            column(name: "name", value: "Euro")
            column(name: "uom_class_id", value: "CURRENCY")
            column(name: "date_created", value: "2018-09-10T00:00")
            column(name: "last_updated", value: "2018-09-10T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "PK")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "PK")
            column(name: "description")
            column(name: "name", value: "Pack")
            column(name: "uom_class_id", value: "QUANTITY")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "RL")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "RL")
            column(name: "description")
            column(name: "name", value: "Roll")
            column(name: "uom_class_id", value: "QUANTITY")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }

        insert(tableName: "unit_of_measure") {
            column(name: "id", value: "USD")
            column(name: "version", valueNumeric: "0")
            column(name: "code", value: "USD")
            column(name: "description", value: "USD (US\$)")
            column(name: "name", value: "US Dollar")
            column(name: "uom_class_id", value: "CURRENCY")
            column(name: "date_created", value: "2018-09-10T00:00")
            column(name: "last_updated", value: "2018-09-10T00:00")
            column(name: "created_by_id", value: "1")
            column(name: "updated_by_id", value: "1")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-33", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "unit_of_measure_class") { 
            column(name: "id", value: "CURRENCY")
            column(name: "active", valueBoolean: "true")
            column(name: "name", value: "Currency")
            column(name: "code", value: "CURRENCY")
            column(name: "description", value: "")
            column(name: "base_uom_id", value: "USD")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2018-09-10T00:00")
            column(name: "last_updated", value: "2018-09-10T00:00")
            column(name: "type", value: "CURRENCY")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }

        insert(tableName: "unit_of_measure_class") { 
            column(name: "id", value: "QUANTITY")
            column(name: "active", valueBoolean: "true")
            column(name: "name", value: "Quantity")
            column(name: "code", value: "QUANTITY")
            column(name: "description", value: "")
            column(name: "base_uom_id", value: "EA")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2020-04-25T00:00")
            column(name: "last_updated", value: "2020-04-25T00:00")
            column(name: "type", value: "QUANTITY")
            column(name: "created_by_id")
            column(name: "updated_by_id")
        }
    }
}
