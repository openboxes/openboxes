databaseChangeLog = {


        changeSet(author: "jmiranda (generated)", id: "1580360689181-103") {
        createIndex(indexName: "FK1143A95C8ABEBD5", tableName: "location_dimension") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-104") {
        createIndex(indexName: "FK143BF46AA462C195", tableName: "user_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-105") {
        createIndex(indexName: "FK143BF46AFF37FDB5", tableName: "user_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-106") {
        createIndex(indexName: "FK1799509C20E33E1C", tableName: "requisition") {
            column(name: "verified_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-107") {
        createIndex(indexName: "FK1799509C217F5972", tableName: "requisition") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-108") {
        createIndex(indexName: "FK1799509C2BDD17B3", tableName: "requisition") {
            column(name: "requisition_template_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-109") {
        createIndex(indexName: "FK1799509C36C69275", tableName: "requisition") {
            column(name: "received_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-110") {
        createIndex(indexName: "FK1799509C426DD105", tableName: "requisition") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-111") {
        createIndex(indexName: "FK1799509C4CF042D8", tableName: "requisition") {
            column(name: "delivered_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-112") {
        createIndex(indexName: "FK1799509CD196DBBF", tableName: "requisition") {
            column(name: "issued_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-113") {
        createIndex(indexName: "FK1799509CD2CB8BBB", tableName: "requisition") {
            column(name: "checked_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-114") {
        createIndex(indexName: "FK1799509CDFA74E0B", tableName: "requisition") {
            column(name: "reviewed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-115") {
        createIndex(indexName: "FK187E54C9DED5FAE7", tableName: "product_catalog_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-116") {
        createIndex(indexName: "FK187E54C9FB5E604E", tableName: "product_catalog_item") {
            column(name: "product_catalog_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-117") {
        createIndex(indexName: "FK1BF9A217F5972", tableName: "tag") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-118") {
        createIndex(indexName: "FK1BF9A426DD105", tableName: "tag") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-119") {
        createIndex(indexName: "FK1C92FE2F3E67CF9F", tableName: "party_role") {
            column(name: "party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-120") {
        createIndex(indexName: "FK1E50D72D72882836", tableName: "transaction_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-121") {
        createIndex(indexName: "FK1E50D72DA27827C2", tableName: "transaction_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-122") {
        createIndex(indexName: "FK1E50D72DCA32CFEF", tableName: "transaction_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-123") {
        createIndex(indexName: "FK1E50D72DCA354381", tableName: "transaction_fact") {
            column(name: "transaction_type_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-124") {
        createIndex(indexName: "FK1E50D72DD1F27172", tableName: "transaction_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-125") {
        createIndex(indexName: "FK299E50ABDED5FAE7", tableName: "synonym") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-126") {
        createIndex(indexName: "FK2D110D6418D76D84", tableName: "order_item") {
            column(name: "origin_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-127") {
        createIndex(indexName: "FK2D110D6444979D51", tableName: "order_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-128") {
        createIndex(indexName: "FK2D110D6451A9416E", tableName: "order_item") {
            column(name: "parent_order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-129") {
        createIndex(indexName: "FK2D110D64605326C", tableName: "order_item") {
            column(name: "destination_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-130") {
        createIndex(indexName: "FK2D110D64911E7578", tableName: "order_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-131") {
        createIndex(indexName: "FK2D110D64AA992CED", tableName: "order_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-132") {
        createIndex(indexName: "FK2D110D64D08EDBE6", tableName: "order_item") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-133") {
        createIndex(indexName: "FK2D110D64DED5FAE7", tableName: "order_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-134") {
        createIndex(indexName: "FK2D110D64EF4C770D", tableName: "order_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-135") {
        createIndex(indexName: "FK2DE9EE6EB8839C0F", tableName: "order_comment") {
            column(name: "order_comments_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-136") {
        createIndex(indexName: "FK2DE9EE6EC4A49BBF", tableName: "order_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-137") {
        createIndex(indexName: "FK302BCFE619A2EF8", tableName: "category") {
            column(name: "parent_category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-138") {
        createIndex(indexName: "FK312F6C292388BC5", tableName: "shipment_reference_number") {
            column(name: "reference_number_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-139") {
        createIndex(indexName: "FK313A4BDF14F7BB8E", tableName: "product_group_product") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-140") {
        createIndex(indexName: "FK313A4BDFDED5FAE7", tableName: "product_group_product") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-141") {
        createIndex(indexName: "FK335CD11B6631D8CC", tableName: "document") {
            column(name: "document_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-142") {
        createIndex(indexName: "FK36EBCB1F28CE07", tableName: "user") {
            column(name: "warehouse_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-143") {
        createIndex(indexName: "FK36EBCB41E07A73", tableName: "user") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-144") {
        createIndex(indexName: "FK38A5EE5FAF1302EB", tableName: "comment") {
            column(name: "sender_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-145") {
        createIndex(indexName: "FK38A5EE5FF885F087", tableName: "comment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-146") {
        createIndex(indexName: "FK3A097B1C24DEBC91", tableName: "product_supplier") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-147") {
        createIndex(indexName: "FK3A097B1C2A475A37", tableName: "product_supplier") {
            column(name: "manufacturer_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-148") {
        createIndex(indexName: "FK3A097B1CDED5FAE7", tableName: "product_supplier") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-149") {
        createIndex(indexName: "FK3A097B1CF42F7E5C", tableName: "product_supplier") {
            column(name: "supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-150") {
        createIndex(indexName: "FK40203B26296B2CA3", tableName: "shipment_method") {
            column(name: "shipper_service_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-151") {
        createIndex(indexName: "FK40203B263896C98E", tableName: "shipment_method") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-152") {
        createIndex(indexName: "FK408272383B5F6286", tableName: "receipt") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-153") {
        createIndex(indexName: "FK4082723844979D51", tableName: "receipt") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-154") {
        createIndex(indexName: "FK414EF28F1E2B3CDC", tableName: "requisition") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-155") {
        createIndex(indexName: "FK414EF28F44979D51", tableName: "requisition") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-156") {
        createIndex(indexName: "FK414EF28F94567276", tableName: "requisition") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-157") {
        createIndex(indexName: "FK414EF28FDBDEDAC4", tableName: "requisition") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-158") {
        createIndex(indexName: "FK414EF28FDD302242", tableName: "requisition") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-159") {
        createIndex(indexName: "FK4BB27241154F600", tableName: "shipment_workflow_reference_number_type") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-160") {
        createIndex(indexName: "FK4DA982C35DE21C87", tableName: "requisition_item") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-161") {
        createIndex(indexName: "FK4DA982C3AA992CED", tableName: "requisition_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-162") {
        createIndex(indexName: "FK4DA982C3DED5FAE7", tableName: "requisition_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-163") {
        createIndex(indexName: "FK4DA982C3EF4C770D", tableName: "requisition_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-164") {
        createIndex(indexName: "FK51F3772FEF4C770D", tableName: "product_group") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-165") {
        createIndex(indexName: "FK5358E4D614F7BB8E", tableName: "requisition_item") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-166") {
        createIndex(indexName: "FK5358E4D61594028E", tableName: "requisition_item") {
            column(name: "substitution_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-167") {
        createIndex(indexName: "FK5358E4D6217F5972", tableName: "requisition_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-168") {
        createIndex(indexName: "FK5358E4D629B2552E", tableName: "requisition_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-169") {
        createIndex(indexName: "FK5358E4D6405AC22D", tableName: "requisition_item") {
            column(name: "modification_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-170") {
        createIndex(indexName: "FK5358E4D6426DD105", tableName: "requisition_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-171") {
        createIndex(indexName: "FK5358E4D644979D51", tableName: "requisition_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-172") {
        createIndex(indexName: "FK5358E4D6DD302242", tableName: "requisition_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-173") {
        createIndex(indexName: "FK5358E4D6F84BDE18", tableName: "requisition_item") {
            column(name: "parent_requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-174") {
        createIndex(indexName: "FK5A2551DEAC392B33", tableName: "fulfillment") {
            column(name: "fulfilled_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-175") {
        createIndex(indexName: "FK5C6729A3D970DB4", tableName: "event") {
            column(name: "event_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-176") {
        createIndex(indexName: "FK5C6729A4415A5B0", tableName: "event") {
            column(name: "event_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-177") {
        createIndex(indexName: "FK5D1B504A217F5972", tableName: "unit_of_measure_class") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-178") {
        createIndex(indexName: "FK5D1B504A426DD105", tableName: "unit_of_measure_class") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-179") {
        createIndex(indexName: "FK5D1B504A6B9DFD", tableName: "unit_of_measure_class") {
            column(name: "base_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-180") {
        createIndex(indexName: "FK615A48F6217F5972", tableName: "product_package") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-181") {
        createIndex(indexName: "FK615A48F63906C4CF", tableName: "product_package") {
            column(name: "uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-182") {
        createIndex(indexName: "FK615A48F6426DD105", tableName: "product_package") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-183") {
        createIndex(indexName: "FK615A48F6DED5FAE7", tableName: "product_package") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-184") {
        createIndex(indexName: "FK651874E1E2B3CDC", tableName: "order") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-185") {
        createIndex(indexName: "FK651874E41B7275F", tableName: "order") {
            column(name: "completed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-186") {
        createIndex(indexName: "FK651874E44979D51", tableName: "order") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-187") {
        createIndex(indexName: "FK651874EAF6D8801", tableName: "order") {
            column(name: "ordered_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-188") {
        createIndex(indexName: "FK651874EDBDEDAC4", tableName: "order") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-189") {
        createIndex(indexName: "FK6581AE69DFE4C4C", tableName: "party") {
            column(name: "party_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-190") {
        createIndex(indexName: "FK6C5BE20C800AA15", tableName: "shipment_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-191") {
        createIndex(indexName: "FK6D032BB53B350242", tableName: "shipment_event") {
            column(name: "shipment_events_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-192") {
        createIndex(indexName: "FK6D032BB5786431F", tableName: "shipment_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-193") {
        createIndex(indexName: "FK714F9FB528F75F00", tableName: "location") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-194") {
        createIndex(indexName: "FK714F9FB53BB36E94", tableName: "location") {
            column(name: "location_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-195") {
        createIndex(indexName: "FK714F9FB541E07A73", tableName: "location") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-196") {
        createIndex(indexName: "FK714F9FB5606C7D95", tableName: "location") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-197") {
        createIndex(indexName: "FK714F9FB561ED379F", tableName: "location") {
            column(name: "address_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-198") {
        createIndex(indexName: "FK714F9FB572A2C5B4", tableName: "location") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-199") {
        createIndex(indexName: "FK714F9FB57AF9A3C0", tableName: "location") {
            column(name: "parent_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-200") {
        createIndex(indexName: "FK7348B491217F5972", tableName: "unit_of_measure") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-201") {
        createIndex(indexName: "FK7348B491426DD105", tableName: "unit_of_measure") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-202") {
        createIndex(indexName: "FK7348B49197D8303E", tableName: "unit_of_measure") {
            column(name: "uom_class_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-203") {
        createIndex(indexName: "FK740B54769DB749D", tableName: "inventory_snapshot") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-204") {
        createIndex(indexName: "FK740B5478ABEBD5", tableName: "inventory_snapshot") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-205") {
        createIndex(indexName: "FK740B547AA992CED", tableName: "inventory_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-206") {
        createIndex(indexName: "FK740B547DED5FAE7", tableName: "inventory_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-207") {
        createIndex(indexName: "FK74D92A693D2E628A", tableName: "order_event") {
            column(name: "order_events_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-208") {
        createIndex(indexName: "FK74D92A69786431F", tableName: "order_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-209") {
        createIndex(indexName: "FK7975323F4CC49445", tableName: "local_transfer") {
            column(name: "destination_transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-210") {
        createIndex(indexName: "FK7975323F57563498", tableName: "local_transfer") {
            column(name: "source_transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-211") {
        createIndex(indexName: "FK7A19D7561ED379F", tableName: "location_group") {
            column(name: "address_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-212") {
        createIndex(indexName: "FK7AFF67F928F75F00", tableName: "location_type_supported_activities") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-213") {
        createIndex(indexName: "FK7FA0D2DE1E2B3CDC", tableName: "transaction") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-214") {
        createIndex(indexName: "FK7FA0D2DE217F5972", tableName: "transaction") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-215") {
        createIndex(indexName: "FK7FA0D2DE3265A8A9", tableName: "transaction") {
            column(name: "confirmed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-216") {
        createIndex(indexName: "FK7FA0D2DE426DD105", tableName: "transaction") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-217") {
        createIndex(indexName: "FK7FA0D2DE5DE9E374", tableName: "transaction") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-218") {
        createIndex(indexName: "FK7FA0D2DE5F12AFED", tableName: "transaction") {
            column(name: "incoming_shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-219") {
        createIndex(indexName: "FK7FA0D2DE72A2C5B4", tableName: "transaction") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-220") {
        createIndex(indexName: "FK7FA0D2DE828481AF", tableName: "transaction") {
            column(name: "source_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-221") {
        createIndex(indexName: "FK7FA0D2DEB3FB7111", tableName: "transaction") {
            column(name: "transaction_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-222") {
        createIndex(indexName: "FK7FA0D2DEB80B3233", tableName: "transaction") {
            column(name: "outgoing_shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-223") {
        createIndex(indexName: "FK7FA0D2DED08EDBE6", tableName: "transaction") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-224") {
        createIndex(indexName: "FK7FA0D2DEF7076438", tableName: "transaction") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-225") {
        createIndex(indexName: "FK9475736B3BE9D843", tableName: "order_shipment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-226") {
        createIndex(indexName: "FK9475736BB06EC4FB", tableName: "order_shipment") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-227") {
        createIndex(indexName: "FK94A534C47B0D087", tableName: "product_attribute") {
            column(name: "attribute_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-228") {
        createIndex(indexName: "FK94A534CDED5FAE7", tableName: "product_attribute") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-229") {
        createIndex(indexName: "FK98293BFB217F5972", tableName: "synonym") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-230") {
        createIndex(indexName: "FK98293BFB426DD105", tableName: "synonym") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-231") {
        createIndex(indexName: "FK9A945A36C800AA15", tableName: "shipment_workflow_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-232") {
        createIndex(indexName: "FK9A945A36EC587CFB", tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-233") {
        createIndex(indexName: "FKA0303E4EDED5FAE7", tableName: "product_category") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-234") {
        createIndex(indexName: "FKA0303E4EEF4C770D", tableName: "product_category") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-235") {
        createIndex(indexName: "FKA71CAC4A9740C85F", tableName: "product_tag") {
            column(name: "tag_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-236") {
        createIndex(indexName: "FKA71CAC4ADED5FAE7", tableName: "product_tag") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-237") {
        createIndex(indexName: "FKA8B7A49072882836", tableName: "consumption_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-238") {
        createIndex(indexName: "FKA8B7A490A27827C2", tableName: "consumption_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-239") {
        createIndex(indexName: "FKA8B7A490CA32CFEF", tableName: "consumption_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-240") {
        createIndex(indexName: "FKA8B7A490D1F27172", tableName: "consumption_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-241") {
        createIndex(indexName: "FKABC21FD12EF4C7F4", tableName: "transaction_entry") {
            column(name: "transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-242") {
        createIndex(indexName: "FKABC21FD169DB749D", tableName: "transaction_entry") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-243") {
        createIndex(indexName: "FKABC21FD1AA992CED", tableName: "transaction_entry") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-244") {
        createIndex(indexName: "FKABC21FD1DED5FAE7", tableName: "transaction_entry") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-245") {
        createIndex(indexName: "FKAE3064BA44979D51", tableName: "receipt_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-246") {
        createIndex(indexName: "FKAE3064BA69DB749D", tableName: "receipt_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-247") {
        createIndex(indexName: "FKAE3064BAAA992CED", tableName: "receipt_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-248") {
        createIndex(indexName: "FKAE3064BAB06EC4FB", tableName: "receipt_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-249") {
        createIndex(indexName: "FKAE3064BADED5FAE7", tableName: "receipt_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-250") {
        createIndex(indexName: "FKAE3064BAF7076438", tableName: "receipt_item") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-251") {
        createIndex(indexName: "FKB511C5AD20E351EA", tableName: "product_component") {
            column(name: "component_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-252") {
        createIndex(indexName: "FKB511C5AD24DEBC91", tableName: "product_component") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-253") {
        createIndex(indexName: "FKB511C5ADFB4C199C", tableName: "product_component") {
            column(name: "assembly_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-254") {
        createIndex(indexName: "FKB5A4FE84C4A49BBF", tableName: "order_item_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-255") {
        createIndex(indexName: "FKBD34ABCD8ABEBD5", tableName: "inventory_item_snapshot") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-256") {
        createIndex(indexName: "FKBD34ABCDAA992CED", tableName: "inventory_item_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-257") {
        createIndex(indexName: "FKBD34ABCDDED5FAE7", tableName: "inventory_item_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-258") {
        createIndex(indexName: "FKC254A2E172A2C5B4", tableName: "inventory_level") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-259") {
        createIndex(indexName: "FKC254A2E1CFDCB4DF", tableName: "inventory_level") {
            column(name: "preferred_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-260") {
        createIndex(indexName: "FKC254A2E1DED5FAE7", tableName: "inventory_level") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-261") {
        createIndex(indexName: "FKC254A2E1F07D879A", tableName: "inventory_level") {
            column(name: "replenishment_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-262") {
        createIndex(indexName: "FKC398CCBAC4A49BBF", tableName: "shipment_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-263") {
        createIndex(indexName: "FKC73E1616DED5FAE7", tableName: "product_dimension") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-264") {
        createIndex(indexName: "FKCD71F39B8ABEBD5", tableName: "consumption") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-265") {
        createIndex(indexName: "FKCD71F39BAA992CED", tableName: "consumption") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-266") {
        createIndex(indexName: "FKCD71F39BDED5FAE7", tableName: "consumption") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-267") {
        createIndex(indexName: "FKD08A526BC800AA15", tableName: "product_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-268") {
        createIndex(indexName: "FKD08A526BDED5FAE7", tableName: "product_document") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-269") {
        createIndex(indexName: "FKD3F8383F217F5972", tableName: "picklist") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-270") {
        createIndex(indexName: "FKD3F8383F426DD105", tableName: "picklist") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-271") {
        createIndex(indexName: "FKD3F8383F5DE9E374", tableName: "picklist") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-272") {
        createIndex(indexName: "FKD3F8383FA3E976BC", tableName: "picklist") {
            column(name: "picker_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-273") {
        createIndex(indexName: "FKD584C4C4FF77FF9B", tableName: "shipment_workflow") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-274") {
        createIndex(indexName: "FKD790DEBD154F600", tableName: "reference_number") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-275") {
        createIndex(indexName: "FKDA3BB2981CD3412D", tableName: "shipment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-276") {
        createIndex(indexName: "FKDA3BB2983B5F6286", tableName: "shipment_item") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-277") {
        createIndex(indexName: "FKDA3BB29844979D51", tableName: "shipment_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-278") {
        createIndex(indexName: "FKDA3BB29849AB6B52", tableName: "shipment_item") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-279") {
        createIndex(indexName: "FKDA3BB29869DB749D", tableName: "shipment_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-280") {
        createIndex(indexName: "FKDA3BB2987400E88E", tableName: "shipment_item") {
            column(name: "container_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-281") {
        createIndex(indexName: "FKDA3BB298AA992CED", tableName: "shipment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-282") {
        createIndex(indexName: "FKDA3BB298DED5FAE7", tableName: "shipment_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-283") {
        createIndex(indexName: "FKDEF5AD1317A6E251", tableName: "shipment_workflow_container_type") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-284") {
        createIndex(indexName: "FKDF7559D73896C98E", tableName: "shipper_service") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-285") {
        createIndex(indexName: "FKE071DE6DB06EC4FB", tableName: "fulfillment_item_shipment_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-286") {
        createIndex(indexName: "FKE071DE6DB42751E1", tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-287") {
        createIndex(indexName: "FKE139719A1E2B3CDC", tableName: "shipment") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-288") {
        createIndex(indexName: "FKE139719A217F5972", tableName: "shipment") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-289") {
        createIndex(indexName: "FKE139719A294C1012", tableName: "shipment") {
            column(name: "carrier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-290") {
        createIndex(indexName: "FKE139719A426DD105", tableName: "shipment") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-291") {
        createIndex(indexName: "FKE139719A44979D51", tableName: "shipment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-292") {
        createIndex(indexName: "FKE139719A49AB6B52", tableName: "shipment") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-293") {
        createIndex(indexName: "FKE139719A5DE9E374", tableName: "shipment") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-294") {
        createIndex(indexName: "FKE139719AA28CC5FB", tableName: "shipment") {
            column(name: "shipment_method_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-295") {
        createIndex(indexName: "FKE139719AD95ACF25", tableName: "shipment") {
            column(name: "current_event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-296") {
        createIndex(indexName: "FKE139719ADBDEDAC4", tableName: "shipment") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-297") {
        createIndex(indexName: "FKE139719AFF77FF9B", tableName: "shipment") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-298") {
        createIndex(indexName: "FKE698D2ECC800AA15", tableName: "order_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-299") {
        createIndex(indexName: "FKE698D2ECFE10118D", tableName: "order_document") {
            column(name: "order_documents_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-300") {
        createIndex(indexName: "FKE7584B1369DB749D", tableName: "picklist_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-301") {
        createIndex(indexName: "FKE7814C8117A6E251", tableName: "container") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-302") {
        createIndex(indexName: "FKE7814C813B5F6286", tableName: "container") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-303") {
        createIndex(indexName: "FKE7814C8144979D51", tableName: "container") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-304") {
        createIndex(indexName: "FKE7814C814B6A2E03", tableName: "container") {
            column(name: "parent_container_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-305") {
        createIndex(indexName: "FKED441931C8653BC0", tableName: "product_association") {
            column(name: "associated_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-306") {
        createIndex(indexName: "FKED441931DED5FAE7", tableName: "product_association") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-307") {
        createIndex(indexName: "FKED8DCCEF217F5972", tableName: "product") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-308") {
        createIndex(indexName: "FKED8DCCEF426DD105", tableName: "product") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-309") {
        createIndex(indexName: "FKED8DCCEFABD88AC6", tableName: "product") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-310") {
        createIndex(indexName: "FKED8DCCEFEEB2908D", tableName: "product") {
            column(name: "default_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-311") {
        createIndex(indexName: "FKED8DCCEFEF4C770D", tableName: "product") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-312") {
        createIndex(indexName: "FKEDC55CD447EBE106", tableName: "fulfillment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-313") {
        createIndex(indexName: "FKEDC55CD494567276", tableName: "fulfillment_item") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-314") {
        createIndex(indexName: "FKEDC55CD4AA992CED", tableName: "fulfillment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-315") {
        createIndex(indexName: "FKF58372688ABEBD5", tableName: "location_supported_activities") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-316") {
        createIndex(indexName: "FKFD8E50671A43AB29", tableName: "click_stream_request") {
            column(name: "click_stream_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-317") {
        createIndex(indexName: "location_role_ibfk_1", tableName: "location_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-318") {
        createIndex(indexName: "location_role_ibfk_2", tableName: "location_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-319") {
        createIndex(indexName: "location_role_ibfk_3", tableName: "location_role") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-320") {
        createIndex(indexName: "picklist_item_ibfk_1", tableName: "picklist_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-321") {
        createIndex(indexName: "picklist_item_ibfk_2", tableName: "picklist_item") {
            column(name: "picklist_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-322") {
        createIndex(indexName: "picklist_item_ibfk_3", tableName: "picklist_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-323") {
        createIndex(indexName: "picklist_item_ibfk_4", tableName: "picklist_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-324") {
        createIndex(indexName: "picklist_item_ibfk_5", tableName: "picklist_item") {
            column(name: "inventory_item_id")
        }
    }
}
