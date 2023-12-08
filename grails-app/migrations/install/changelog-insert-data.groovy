package install

databaseChangeLog = {

    changeSet(author: "openboxes (generated)", id: "1692069478960-1", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "address") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "address", value: "888 Commonwealth Avenue")
            column(name: "address2", value: "Third Floor")
            column(name: "city", value: "Boston")
            column(name: "country", value: "United States")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "postal_code", value: "02215")
            column(name: "state_or_province", value: "MA")
            column(name: "description")
        }

        insert(tableName: "address") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "address", value: "1000 State Street")
            column(name: "address2", value: "Building A")
            column(name: "city", value: "Miami")
            column(name: "country", value: "United States")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "postal_code", value: "33126")
            column(name: "state_or_province", value: "FL")
            column(name: "description")
        }

        insert(tableName: "address") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "address", value: "12345 Main Street")
            column(name: "address2", value: "Suite 401")
            column(name: "city", value: "Tabarre")
            column(name: "country", value: "Haiti")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "postal_code")
            column(name: "state_or_province")
            column(name: "description")
        }

        insert(tableName: "address") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "0")
            column(name: "address", value: "2482 Massachusetts Ave")
            column(name: "address2", value: "")
            column(name: "city", value: "Boston")
            column(name: "country", value: "United Status")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "postal_code", value: "02215")
            column(name: "state_or_province", value: "MA")
            column(name: "description")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-6", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "donor") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Donor Organization ABC")
        }

        insert(tableName: "donor") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Donor Organization XYZ")
        }

        insert(tableName: "donor") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Donor Organization 123")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-8", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "inventory") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "last_inventory_date")
            column(name: "date_created", value: "2011-02-17T00:00")
            column(name: "last_updated", value: "2011-02-17T00:00")
        }

        insert(tableName: "inventory") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "last_inventory_date")
            column(name: "date_created", value: "2011-02-17T00:00")
            column(name: "last_updated", value: "2011-02-17T00:00")
        }

        insert(tableName: "inventory") {
            column(name: "id", value: "ff8081813a512a91013a512f140a0001")
            column(name: "version", valueNumeric: "0")
            column(name: "last_inventory_date")
            column(name: "date_created", value: "2012-10-11T14:56:10")
            column(name: "last_updated", value: "2012-10-11T14:56:10")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-10", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "location") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "address_id", value: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Main Warehouse")
            column(name: "inventory_id", value: "1")
            column(name: "manager_id", value: "1")
            column(name: "active", valueBoolean: "true")
            column(name: "location_type_id", value: "2")
            column(name: "bg_color", value: "FFFFFF")
            column(name: "fg_color", value: "000000")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "organization_id", value: "1")
        }

        insert(tableName: "location") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Main Supplier")
            column(name: "active", valueBoolean: "true")
            column(name: "location_type_id", value: "4")
            column(name: "bg_color", value: "FFFFFF")
            column(name: "fg_color", value: "000000")
            column(name: "sort_order", valueNumeric: "0")
            column(name: "organization_id", value: "2")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-15", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "party") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2020-12-26T00:00")
            column(name: "last_updated", value: "2020-12-26T00:00")
            column(name: "party_type_id", value: "1")
            column(name: "class", value: "org.pih.warehouse.core.Organization")
            column(name: "code", value: "MO")
            column(name: "description")
            column(name: "name", value: "Main Organization")
            column(name: "default_location_id")
            column(name: "active", valueBoolean: "true")
        }

        insert(tableName: "party") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "date_created", value: "2020-12-26T00:00")
            column(name: "last_updated", value: "2020-12-26T00:00")
            column(name: "party_type_id", value: "1")
            column(name: "class", value: "org.pih.warehouse.core.Organization")
            column(name: "code", value: "SO")
            column(name: "description")
            column(name: "name", value: "Supplier Organization")
            column(name: "default_location_id")
            column(name: "active", valueBoolean: "true")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-16", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "party_role") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "0")
            column(name: "end_date")
            column(name: "party_id", value: "1")
            column(name: "role_type", value: "ROLE_ORGANIZATION")
            column(name: "start_date")
        }

        insert(tableName: "party_role") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "0")
            column(name: "end_date")
            column(name: "party_id", value: "2")
            column(name: "role_type", value: "ROLE_SUPPLIER")
            column(name: "start_date")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-19", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "person") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "email", value: "admin@admin.com")
            column(name: "first_name", value: "Miss")
            column(name: "last_name", value: "Administrator")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "phone_number")
            column(name: "active", valueBoolean: "true")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-29", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "shipper") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "FedEx")
            column(name: "parameter_name")
            column(name: "tracking_format", value: "999999999999")
            column(name: "tracking_url", value: "http://www.fedex.com/Tracking?ascend_header=1&amp;clienttype=dotcom&amp;cntry_code=us&amp;language=english&amp;tracknumbers=%s")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "UPS")
            column(name: "parameter_name")
            column(name: "tracking_format", value: "1Z9999W99999999999")
            column(name: "tracking_url", value: "http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&amp;tracknums_displayed=1&amp;TypeOfInquiryNumber=T&amp;loc=en_US&amp;InquiryNumber1=%s&amp;track.x=0&amp;track.y=0")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "DHL")
            column(name: "parameter_name", value: "q")
            column(name: "tracking_format")
            column(name: "tracking_url", value: "http://www.google.com/search?hl=en&amp;site=&amp;q=")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "USPS")
            column(name: "parameter_name", value: "q")
            column(name: "tracking_format")
            column(name: "tracking_url", value: "http://www.google.com/search?hl=en&amp;site=&amp;q=")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "1")
            column(name: "date_created", value: "2010-08-25T00:00")
            column(name: "description")
            column(name: "last_updated", value: "2010-08-25T00:00")
            column(name: "name", value: "Courier")
            column(name: "parameter_name", value: "q")
            column(name: "tracking_format")
            column(name: "tracking_url", value: "http://www.google.com/search?hl=en&amp;site=&amp;q=")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-30", objectQuotingStrategy: "LEGACY") {

        insert(tableName: "shipper_service") {
            column(name: "id", value: "1")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "Same Day Delivery")
            column(name: "name", value: "Same Day Air")
            column(name: "shipper_id", value: "1")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "10")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "3-5 Business Days")
            column(name: "name", value: "Ground")
            column(name: "shipper_id", value: "4")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "11")
            column(name: "version", valueNumeric: "1")
            column(name: "description")
            column(name: "name", value: "International Flight")
            column(name: "shipper_id", value: "5")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "2")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "Next Day Delivery")
            column(name: "name", value: "Express Freight")
            column(name: "shipper_id", value: "1")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "3")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "3-5 Business Days")
            column(name: "name", value: "Ground")
            column(name: "shipper_id", value: "1")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "4")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "Same Day Delivery")
            column(name: "name", value: "Same Day Air")
            column(name: "shipper_id", value: "2")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "5")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "Next Day Delivery")
            column(name: "name", value: "Express Freight")
            column(name: "shipper_id", value: "2")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "6")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "3-5 Business Days")
            column(name: "name", value: "Ground")
            column(name: "shipper_id", value: "2")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "7")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "Same Day Delivery")
            column(name: "name", value: "Same Day Air")
            column(name: "shipper_id", value: "3")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "8")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "Next Day Delivery")
            column(name: "name", value: "Express Freight")
            column(name: "shipper_id", value: "3")
        }

        insert(tableName: "shipper_service") {
            column(name: "id", value: "9")
            column(name: "version", valueNumeric: "1")
            column(name: "description", value: "3-5 Business Days")
            column(name: "name", value: "Ground")
            column(name: "shipper_id", value: "3")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-34", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "user") {
            column(name: "id", value: "1")
            column(name: "last_login_date")
            column(name: "manager_id")
            column(name: "password", value: "password")
            column(name: "username", value: "admin")
            column(name: "warehouse_id", value: "1")
            column(name: "photo")
            column(name: "locale")
            column(name: "remember_last_location")
            column(name: "timezone")
            column(name: "dashboard_config")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1692069478960-35", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "user_role") {
            column(name: "user_id", value: "1")
            column(name: "role_id", value: "5")
        }

        insert(tableName: "user_role") {
            column(name: "user_id", value: "1")
            column(name: "role_id", value: "ff80818162d9f9cc0162d9fc1c220003")
        }

        insert(tableName: "user_role") {
            column(name: "user_id", value: "1")
            column(name: "role_id", value: "ff80818162d9f9cc0162d9fbf09e0002")
        }

        insert(tableName: "user_role") {
            column(name: "user_id", value: "1")
            column(name: "role_id", value: "ff808181681c757c01681c89c4960001")
        }
    }
}
