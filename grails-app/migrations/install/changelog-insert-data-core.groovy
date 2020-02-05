package install

databaseChangeLog = {

    changeSet(author: "jmiranda (generated)", id: "1580407083683-1", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "address") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "address", value: "888 Commonwealth Avenue")

            column(name: "address2", value: "Third Floor")

            column(name: "city", value: "Boston")

            column(name: "country", value: "United States")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

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

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

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

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

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

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "postal_code", value: "02215")

            column(name: "state_or_province", value: "MA")

            column(name: "description")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-2", objectQuotingStrategy: "LEGACY") {

        // Remove this once we recreate the database from scratch
        validCheckSum "8:36f8b27eff37938c5bedefbf7b9a5bf2"

        insert(tableName: "category") {
            column(name: "id", value: "ROOT")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "ROOT")

            column(name: "parent_category_id")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Medicines")

            column(name: "parent_category_id", value: "ROOT")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "10")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Surgical Equipment")

            column(name: "parent_category_id", value: "3")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "11")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "IT Equipment")

            column(name: "parent_category_id", value: "3")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "12")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Furniture and Equipment")

            column(name: "parent_category_id", value: "3")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "13")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Food")

            column(name: "parent_category_id", value: "4")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "14")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "ARVS")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "15")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Anesteshia")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "16")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Cancer")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "17")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Chronic Care")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "18")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Pain")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "19")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "TB")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Supplies")

            column(name: "parent_category_id", value: "ROOT")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "20")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Other")

            column(name: "parent_category_id", value: "1")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "21")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Lab")

            column(name: "parent_category_id", value: "6")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "22")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Surgical")

            column(name: "parent_category_id", value: "6")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "23")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "X-Ray")

            column(name: "parent_category_id", value: "6")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "24")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Dental")

            column(name: "parent_category_id", value: "6")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "25")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Other")

            column(name: "parent_category_id", value: "6")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Equipment")

            column(name: "parent_category_id", value: "ROOT")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Perishables")

            column(name: "parent_category_id", value: "ROOT")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "5")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Other")

            column(name: "parent_category_id", value: "ROOT")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "6")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Medical Supplies")

            column(name: "parent_category_id", value: "2")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "7")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Hospital and Clinic Supplies")

            column(name: "parent_category_id", value: "2")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "8")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Office Supplies")

            column(name: "parent_category_id", value: "2")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }

        insert(tableName: "category") {
            column(name: "id", value: "9")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Medical Equipment")

            column(name: "parent_category_id", value: "3")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "is_root")
        }


    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-3", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "container_type") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Container|fr:Conteneur")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Pallet|fr:Palette")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Suitcase|fr:Valise/Malette")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Box|fr:Boite")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "5")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Trunk|fr:Coffre")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "6")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Item|fr:Element")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "7")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Other|fr:Autre")

            column(name: "sort_order", valueNumeric: "0")
        }

        insert(tableName: "container_type") {
            column(name: "id", value: "8")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2011-02-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2011-02-25T00:00:00")

            column(name: "name", value: "Crate|fr:Caisse")

            column(name: "sort_order", valueNumeric: "0")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-4", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "donor") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Donor Organization ABC")
        }

        insert(tableName: "donor") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Donor Organization XYZ")
        }

        insert(tableName: "donor") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Donor Organization 123")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-5", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "event_type") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2011-03-03T00:00:00")

            column(name: "description", value: "Shipment has been shipped")

            column(name: "event_code", value: "SHIPPED")

            column(name: "last_updated", valueDate: "2011-03-03T00:00:00")

            column(name: "name", value: "Shipped|fr:Exp")

            column(name: "sort_order", valueNumeric: "2")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2011-03-03T00:00:00")

            column(name: "description", value: "Shipment has been received")

            column(name: "event_code", value: "RECEIVED")

            column(name: "last_updated", valueDate: "2011-03-03T00:00:00")

            column(name: "name", value: "Received|fr:Re")

            column(name: "sort_order", valueNumeric: "3")
        }

        insert(tableName: "event_type") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2018-09-10T00:00:00")

            column(name: "description", value: "Shipment has been partially received")

            column(name: "event_code", value: "PARTIALLY_RECEIVED")

            column(name: "last_updated", valueDate: "2018-09-10T00:00:00")

            column(name: "name", value: "Partially Received")

            column(name: "sort_order", valueNumeric: "3")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-6", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "inventory") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "last_inventory_date")

            column(name: "date_created", valueDate: "2011-02-17T00:00:00")

            column(name: "last_updated", valueDate: "2011-02-17T00:00:00")
        }

        insert(tableName: "inventory") {
            column(name: "id", value: "ff8081813a512a91013a512f140a0001")

            column(name: "version", valueNumeric: "0")

            column(name: "last_inventory_date")

            column(name: "date_created", valueDate: "2012-10-11T14:56:10")

            column(name: "last_updated", valueDate: "2012-10-11T14:56:10")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-7", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "location") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "address_id", value: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "logo_url", value: "http://a3.twimg.com/profile_images/134665083/BOS_Red_Sox_normal.PNG")

            column(name: "name", value: "Boston Headquarters")

            column(name: "inventory_id", value: "1")

            column(name: "manager_id", value: "2")

            column(name: "logo")

            column(name: "managed_locally", valueBoolean: "true")

            column(name: "active", valueBoolean: "true")

            column(name: "location_type_id", value: "2")

            column(name: "parent_location_id")

            column(name: "local", valueBoolean: "true")

            column(name: "bg_color")

            column(name: "fg_color")

            column(name: "location_group_id")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_number")

            column(name: "description")

            column(name: "organization_id")
        }

        insert(tableName: "location") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "address_id", value: "2")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "logo_url", value: "http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg")

            column(name: "name", value: "Miami Warehouse")

            column(name: "inventory_id")

            column(name: "manager_id", value: "2")

            column(name: "logo")

            column(name: "managed_locally", valueBoolean: "true")

            column(name: "active", valueBoolean: "true")

            column(name: "location_type_id", value: "2")

            column(name: "parent_location_id")

            column(name: "local", valueBoolean: "true")

            column(name: "bg_color")

            column(name: "fg_color")

            column(name: "location_group_id")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_number")

            column(name: "description")

            column(name: "organization_id")
        }

        insert(tableName: "location") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "address_id", value: "3")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "logo_url", value: "http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg")

            column(name: "name", value: "Tabarre Depot")

            column(name: "inventory_id")

            column(name: "manager_id", value: "2")

            column(name: "logo")

            column(name: "managed_locally", valueBoolean: "true")

            column(name: "active", valueBoolean: "true")

            column(name: "location_type_id", value: "2")

            column(name: "parent_location_id")

            column(name: "local", valueBoolean: "true")

            column(name: "bg_color")

            column(name: "fg_color")

            column(name: "location_group_id")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_number")

            column(name: "description")

            column(name: "organization_id")
        }

        insert(tableName: "location") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "1")

            column(name: "address_id", value: "4")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "last_updated", valueDate: "2012-10-11T14:56:10")

            column(name: "logo_url", value: "http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg")

            column(name: "name", value: "ZZZ Supply Company")

            column(name: "inventory_id", value: "ff8081813a512a91013a512f140a0001")

            column(name: "manager_id", value: "2")

            column(name: "logo")

            column(name: "managed_locally", valueBoolean: "true")

            column(name: "active", valueBoolean: "true")

            column(name: "location_type_id", value: "2")

            column(name: "parent_location_id")

            column(name: "local", valueBoolean: "true")

            column(name: "bg_color")

            column(name: "fg_color")

            column(name: "location_group_id")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_number")

            column(name: "description")

            column(name: "organization_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-8", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "location_type") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-12-06T00:00:00")

            column(name: "description", value: "Depot")

            column(name: "last_updated", valueDate: "2010-12-06T00:00:00")

            column(name: "name", value: "Depot|fr:D")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "DEPOT")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-12-06T00:00:00")

            column(name: "description", value: "Dispensary")

            column(name: "last_updated", valueDate: "2011-11-14T00:00:00")

            column(name: "name", value: "Dispensary|fr:Dispensaire")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "DISPENSARY")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2010-12-06T00:00:00")

            column(name: "description", value: "Supplier")

            column(name: "last_updated", valueDate: "2010-12-06T00:00:00")

            column(name: "name", value: "Supplier|fr:Fournisseurs")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "SUPPLIER")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "5")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2012-12-06T00:00:00")

            column(name: "description", value: "Ward")

            column(name: "last_updated", valueDate: "2012-12-06T00:00:00")

            column(name: "name", value: "Ward|fr:Ward")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "WARD")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "6")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2012-12-06T00:00:00")

            column(name: "description", value: "Pharmacy")

            column(name: "last_updated", valueDate: "2012-12-06T00:00:00")

            column(name: "name", value: "Pharmacy|fr:Pharmacy")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "DISPENSARY")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "cab2b48e649c71940164a13750f40001")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2018-07-15T00:00:00")

            column(name: "description", value: "Default receiving location type")

            column(name: "last_updated", valueDate: "2018-07-15T00:00:00")

            column(name: "name", value: "Cross-docking")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "INTERNAL")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "cab2b4f35ba2d867015ba2e17e390001")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2017-08-07T00:00:00")

            column(name: "description", value: "Default bin location type")

            column(name: "last_updated", valueDate: "2017-08-07T00:00:00")

            column(name: "name", value: "Bin Location")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "BIN_LOCATION")
        }

        insert(tableName: "location_type") {
            column(name: "id", value: "ff8081816482352b01648249e8cc0001")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2018-07-09T00:00:00")

            column(name: "description", value: "Default receiving location type")

            column(name: "last_updated", valueDate: "2018-07-09T00:00:00")

            column(name: "name", value: "Receiving")

            column(name: "sort_order", valueNumeric: "0")

            column(name: "location_type_code", value: "INTERNAL")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-9", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "party_type") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "0")

            column(name: "code", value: "ORG")

            column(name: "date_created", valueDate: "2018-03-06T00:00:00")

            column(name: "description", value: "Organization")

            column(name: "last_updated", valueDate: "2018-03-06T00:00:00")

            column(name: "name", value: "Organization")

            column(name: "party_type_code", value: "ORGANIZATION")
        }

        insert(tableName: "party_type") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "0")

            column(name: "code", value: "PERSON")

            column(name: "date_created", valueDate: "2018-03-06T00:00:00")

            column(name: "description", value: "Person")

            column(name: "last_updated", valueDate: "2018-03-06T00:00:00")

            column(name: "name", value: "Person")

            column(name: "party_type_code", value: "PERSON")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-10", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "person") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "email", value: "admin@pih.org")

            column(name: "first_name", value: "Miss")

            column(name: "last_name", value: "Administrator")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "phone_number")
        }

        insert(tableName: "person") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "5")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "email", value: "manager@pih.org")

            column(name: "first_name", value: "Mister")

            column(name: "last_name", value: "Manager")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "phone_number")
        }

        insert(tableName: "person") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "2")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "email", value: "jmiranda@pih.org")

            column(name: "first_name", value: "Justin")

            column(name: "last_name", value: "Miranda")

            column(name: "last_updated", valueDate: "2019-08-05T14:29:35")

            column(name: "phone_number")
        }

        insert(tableName: "person") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "email", value: "inactive@pih.org")

            column(name: "first_name", value: "In")

            column(name: "last_name", value: "Active")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "phone_number")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-11", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "shipment_type") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Air|fr:Air")

            column(name: "sort_order", valueNumeric: "1")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Sea|fr:Mer")

            column(name: "sort_order", valueNumeric: "2")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "0")

            column(name: "date_created", valueDate: "2011-08-22T00:00:00")

            column(name: "description", value: "")

            column(name: "last_updated", valueDate: "2011-08-22T00:00:00")

            column(name: "name", value: "Land|fr:Terrains")

            column(name: "sort_order", valueNumeric: "3")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Suitcase|fr:Valise/Malette")

            column(name: "sort_order", valueNumeric: "4")
        }

        insert(tableName: "shipment_type") {
            column(name: "id", value: "5")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2018-08-28T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2018-08-28T00:00:00")

            column(name: "name", value: "Default")

            column(name: "sort_order", valueNumeric: "5")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-12", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "shipper") {
            column(name: "id", value: "1")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "FedEx")

            column(name: "parameter_name")

            column(name: "tracking_format", value: "999999999999")

            column(name: "tracking_url", value: "http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "2")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "UPS")

            column(name: "parameter_name")

            column(name: "tracking_format", value: "1Z9999W99999999999")

            column(name: "tracking_url", value: "http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=%s&track.x=0&track.y=0")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "3")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "DHL")

            column(name: "parameter_name", value: "q")

            column(name: "tracking_format")

            column(name: "tracking_url", value: "http://www.google.com/search?hl=en&site=&q=")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "4")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "USPS")

            column(name: "parameter_name", value: "q")

            column(name: "tracking_format")

            column(name: "tracking_url", value: "http://www.google.com/search?hl=en&site=&q=")
        }

        insert(tableName: "shipper") {
            column(name: "id", value: "5")

            column(name: "version", valueNumeric: "1")

            column(name: "date_created", valueDate: "2010-08-25T00:00:00")

            column(name: "description")

            column(name: "last_updated", valueDate: "2010-08-25T00:00:00")

            column(name: "name", value: "Courier")

            column(name: "parameter_name", value: "q")

            column(name: "tracking_format")

            column(name: "tracking_url", value: "http://www.google.com/search?hl=en&site=&q=")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580407083683-13", objectQuotingStrategy: "LEGACY") {
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

    changeSet(author: "jmiranda (generated)", id: "1580407083683-14", objectQuotingStrategy: "LEGACY") {
        insert(tableName: "user") {
            column(name: "id", value: "1")

            column(name: "active", valueBoolean: "true")

            column(name: "last_login_date")

            column(name: "manager_id")

            column(name: "password", value: "password")

            column(name: "username", value: "admin")

            column(name: "warehouse_id", value: "1")

            column(name: "photo")

            column(name: "locale")

            column(name: "remember_last_location")

            column(name: "timezone")
        }

        insert(tableName: "user") {
            column(name: "id", value: "2")

            column(name: "active", valueBoolean: "true")

            column(name: "last_login_date")

            column(name: "manager_id", value: "1")

            column(name: "password", value: "password")

            column(name: "username", value: "manager")

            column(name: "warehouse_id", value: "1")

            column(name: "photo")

            column(name: "locale")

            column(name: "remember_last_location")

            column(name: "timezone")
        }

        insert(tableName: "user") {
            column(name: "id", value: "3")

            column(name: "active", valueBoolean: "true")

            column(name: "last_login_date", valueDate: "2019-08-05T14:29:35")

            column(name: "manager_id", value: "2")

            column(name: "password", value: "password")

            column(name: "username", value: "jmiranda")

            column(name: "warehouse_id", value: "1")

            column(name: "photo")

            column(name: "locale")

            column(name: "remember_last_location")

            column(name: "timezone")
        }

        insert(tableName: "user") {
            column(name: "id", value: "4")

            column(name: "active", valueBoolean: "false")

            column(name: "last_login_date")

            column(name: "manager_id", value: "2")

            column(name: "password", value: "password")

            column(name: "username", value: "inactive")

            column(name: "warehouse_id", value: "1")

            column(name: "photo")

            column(name: "locale")

            column(name: "remember_last_location")

            column(name: "timezone")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-enable-foreign-key-checks", runAlways: true) {
        sql("SET FOREIGN_KEY_CHECKS=0;")
    }


}
