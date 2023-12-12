package install

databaseChangeLog = {

    changeSet(author: "openboxes (generated)", id: "1700664714834-138") {
        createIndex(indexName: "FK1143A95C8ABEBD5", tableName: "location_dimension") {
            column(name: "location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-139") {
        createIndex(indexName: "FK143BF46AA462C195", tableName: "user_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-140") {
        createIndex(indexName: "FK143BF46AFF37FDB5", tableName: "user_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-141") {
        createIndex(indexName: "FK1799509C20E33E1C", tableName: "requisition") {
            column(name: "verified_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-142") {
        createIndex(indexName: "FK1799509C217F5972", tableName: "requisition") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-143") {
        createIndex(indexName: "FK1799509C2BDD17B3", tableName: "requisition") {
            column(name: "requisition_template_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-144") {
        createIndex(indexName: "FK1799509C36C69275", tableName: "requisition") {
            column(name: "received_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-145") {
        createIndex(indexName: "FK1799509C426DD105", tableName: "requisition") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-146") {
        createIndex(indexName: "FK1799509C4CF042D8", tableName: "requisition") {
            column(name: "delivered_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-147") {
        createIndex(indexName: "FK1799509CD196DBBF", tableName: "requisition") {
            column(name: "issued_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-148") {
        createIndex(indexName: "FK1799509CD2CB8BBB", tableName: "requisition") {
            column(name: "checked_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-149") {
        createIndex(indexName: "FK1799509CDFA74E0B", tableName: "requisition") {
            column(name: "reviewed_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-150") {
        createIndex(indexName: "FK187E54C9DED5FAE7", tableName: "product_catalog_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-151") {
        createIndex(indexName: "FK187E54C9FB5E604E", tableName: "product_catalog_item") {
            column(name: "product_catalog_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-152") {
        createIndex(indexName: "FK1BF9A217F5972", tableName: "tag") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-153") {
        createIndex(indexName: "FK1BF9A426DD105", tableName: "tag") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-154") {
        createIndex(indexName: "FK1C92FE2F3E67CF9F", tableName: "party_role") {
            column(name: "party_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-155") {
        createIndex(indexName: "FK1E50D72D72882836", tableName: "transaction_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-156") {
        createIndex(indexName: "FK1E50D72DA27827C2", tableName: "transaction_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-157") {
        createIndex(indexName: "FK1E50D72DCA32CFEF", tableName: "transaction_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-158") {
        createIndex(indexName: "FK1E50D72DCA354381", tableName: "transaction_fact") {
            column(name: "transaction_type_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-159") {
        createIndex(indexName: "FK1E50D72DD1F27172", tableName: "transaction_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-160") {
        createIndex(indexName: "FK299E50ABDED5FAE7", tableName: "synonym") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-161") {
        createIndex(indexName: "FK2D110D6418D76D84", tableName: "order_item") {
            column(name: "origin_bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-162") {
        createIndex(indexName: "FK2D110D6429542386", tableName: "order_item") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-163") {
        createIndex(indexName: "FK2D110D6429B2552E", tableName: "order_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-164") {
        createIndex(indexName: "FK2D110D6444979D51", tableName: "order_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-165") {
        createIndex(indexName: "FK2D110D6451A9416E", tableName: "order_item") {
            column(name: "parent_order_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-166") {
        createIndex(indexName: "FK2D110D645ED93B03", tableName: "order_item") {
            column(name: "quantity_uom_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-167") {
        createIndex(indexName: "FK2D110D64605326C", tableName: "order_item") {
            column(name: "destination_bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-168") {
        createIndex(indexName: "FK2D110D64911E7578", tableName: "order_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-169") {
        createIndex(indexName: "FK2D110D64AA992CED", tableName: "order_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-170") {
        createIndex(indexName: "FK2D110D64D08EDBE6", tableName: "order_item") {
            column(name: "order_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-171") {
        createIndex(indexName: "FK2D110D64DED5FAE7", tableName: "order_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-172") {
        createIndex(indexName: "FK2D110D64EF4C770D", tableName: "order_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-173") {
        createIndex(indexName: "FK2DE9EE6EB8839C0F", tableName: "order_comment") {
            column(name: "order_comments_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-174") {
        createIndex(indexName: "FK2DE9EE6EC4A49BBF", tableName: "order_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-175") {
        createIndex(indexName: "FK2E4511844A3E746", tableName: "unit_of_measure_conversion") {
            column(name: "from_unit_of_measure_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-176") {
        createIndex(indexName: "FK2E4511849B9434D5", tableName: "unit_of_measure_conversion") {
            column(name: "to_unit_of_measure_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-177") {
        createIndex(indexName: "FK302BCFE619A2EF8", tableName: "category") {
            column(name: "parent_category_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-178") {
        createIndex(indexName: "FK312F6C292388BC5", tableName: "shipment_reference_number") {
            column(name: "reference_number_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-179") {
        createIndex(indexName: "FK313A4BDF14F7BB8E", tableName: "product_group_product") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-180") {
        createIndex(indexName: "FK313A4BDFDED5FAE7", tableName: "product_group_product") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-181") {
        createIndex(indexName: "FK335CD11B6631D8CC", tableName: "document") {
            column(name: "document_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-182") {
        createIndex(indexName: "FK36EBCB1F28CE07", tableName: "user") {
            column(name: "warehouse_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-183") {
        createIndex(indexName: "FK36EBCB41E07A73", tableName: "user") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-184") {
        createIndex(indexName: "FK38A5EE5FAF1302EB", tableName: "comment") {
            column(name: "sender_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-185") {
        createIndex(indexName: "FK38A5EE5FF885F087", tableName: "comment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-186") {
        createIndex(indexName: "FK38EE09DA47B0D087", tableName: "attribute_entity_type_codes") {
            column(name: "attribute_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-187") {
        createIndex(indexName: "FK3A097B1C24DEBC91", tableName: "product_supplier") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-188") {
        createIndex(indexName: "FK3A097B1C2A475A37", tableName: "product_supplier") {
            column(name: "manufacturer_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-189") {
        createIndex(indexName: "FK3A097B1CDED5FAE7", tableName: "product_supplier") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-190") {
        createIndex(indexName: "FK3A097B1CF42F7E5C", tableName: "product_supplier") {
            column(name: "supplier_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-191") {
        createIndex(indexName: "FK40203B26296B2CA3", tableName: "shipment_method") {
            column(name: "shipper_service_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-192") {
        createIndex(indexName: "FK40203B263896C98E", tableName: "shipment_method") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-193") {
        createIndex(indexName: "FK408272383B5F6286", tableName: "receipt") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-194") {
        createIndex(indexName: "FK4082723844979D51", tableName: "receipt") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-195") {
        createIndex(indexName: "FK414EF28F1E2B3CDC", tableName: "requisition") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-196") {
        createIndex(indexName: "FK414EF28F44979D51", tableName: "requisition") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-197") {
        createIndex(indexName: "FK414EF28F94567276", tableName: "requisition") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-198") {
        createIndex(indexName: "FK414EF28FDBDEDAC4", tableName: "requisition") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-199") {
        createIndex(indexName: "FK414EF28FDD302242", tableName: "requisition") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-200") {
        createIndex(indexName: "FK4A1ABEFE3BE9D843", tableName: "order_adjustment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-201") {
        createIndex(indexName: "FK4A1ABEFED08EDBE6", tableName: "order_adjustment") {
            column(name: "order_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-202") {
        createIndex(indexName: "FK4A1ABEFEE1A39520", tableName: "order_adjustment") {
            column(name: "order_adjustment_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-203") {
        createIndex(indexName: "FK4BB27241154F600", tableName: "shipment_workflow_reference_number_type") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-204") {
        createIndex(indexName: "FK4DA982C35DE21C87", tableName: "requisition_item") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-205") {
        createIndex(indexName: "FK4DA982C3AA992CED", tableName: "requisition_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-206") {
        createIndex(indexName: "FK4DA982C3DED5FAE7", tableName: "requisition_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-207") {
        createIndex(indexName: "FK4DA982C3EF4C770D", tableName: "requisition_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-208") {
        createIndex(indexName: "FK51F3772FEF4C770D", tableName: "product_group") {
            column(name: "category_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-209") {
        createIndex(indexName: "FK5358E4D614F7BB8E", tableName: "requisition_item") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-210") {
        createIndex(indexName: "FK5358E4D61594028E", tableName: "requisition_item") {
            column(name: "substitution_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-211") {
        createIndex(indexName: "FK5358E4D6217F5972", tableName: "requisition_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-212") {
        createIndex(indexName: "FK5358E4D629B2552E", tableName: "requisition_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-213") {
        createIndex(indexName: "FK5358E4D6405AC22D", tableName: "requisition_item") {
            column(name: "modification_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-214") {
        createIndex(indexName: "FK5358E4D6426DD105", tableName: "requisition_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-215") {
        createIndex(indexName: "FK5358E4D644979D51", tableName: "requisition_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-216") {
        createIndex(indexName: "FK5358E4D6DD302242", tableName: "requisition_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-217") {
        createIndex(indexName: "FK5358E4D6F84BDE18", tableName: "requisition_item") {
            column(name: "parent_requisition_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-218") {
        createIndex(indexName: "FK5A2551DEAC392B33", tableName: "fulfillment") {
            column(name: "fulfilled_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-219") {
        createIndex(indexName: "FK5C6729A3D970DB4", tableName: "event") {
            column(name: "event_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-220") {
        createIndex(indexName: "FK5C6729A4415A5B0", tableName: "event") {
            column(name: "event_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-221") {
        createIndex(indexName: "FK5D1B504A217F5972", tableName: "unit_of_measure_class") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-222") {
        createIndex(indexName: "FK5D1B504A426DD105", tableName: "unit_of_measure_class") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-223") {
        createIndex(indexName: "FK5D1B504A6B9DFD", tableName: "unit_of_measure_class") {
            column(name: "base_uom_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-224") {
        createIndex(indexName: "FK615A48F6217F5972", tableName: "product_package") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-225") {
        createIndex(indexName: "FK615A48F629542386", tableName: "product_package") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-226") {
        createIndex(indexName: "FK615A48F63906C4CF", tableName: "product_package") {
            column(name: "uom_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-227") {
        createIndex(indexName: "FK615A48F6426DD105", tableName: "product_package") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-228") {
        createIndex(indexName: "FK651874E1E2B3CDC", tableName: "order") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-229") {
        createIndex(indexName: "FK651874E240896CB", tableName: "order") {
            column(name: "approved_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-230") {
        createIndex(indexName: "FK651874E35D76CB0", tableName: "order") {
            column(name: "destination_party_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-231") {
        createIndex(indexName: "FK651874E41B7275F", tableName: "order") {
            column(name: "completed_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-232") {
        createIndex(indexName: "FK651874E44979D51", tableName: "order") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-233") {
        createIndex(indexName: "FK651874E6A8010C1", tableName: "order") {
            column(name: "payment_method_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-234") {
        createIndex(indexName: "FK651874E6D91063C", tableName: "order") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-235") {
        createIndex(indexName: "FK651874E8AF312E3", tableName: "order") {
            column(name: "order_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-236") {
        createIndex(indexName: "FK651874E8E7F7DCF", tableName: "order") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-237") {
        createIndex(indexName: "FK651874E9E52B00C", tableName: "order") {
            column(name: "payment_term_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-238") {
        createIndex(indexName: "FK651874EAF6D8801", tableName: "order") {
            column(name: "ordered_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-239") {
        createIndex(indexName: "FK651874EDBDEDAC4", tableName: "order") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-240") {
        createIndex(indexName: "FK6581AE69DFE4C4C", tableName: "party") {
            column(name: "party_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-241") {
        createIndex(indexName: "FK6581AE6D1DFC6D7", tableName: "party") {
            column(name: "default_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-242") {
        createIndex(indexName: "FK6A1A433C3BE9D843", tableName: "order_invoice") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-243") {
        createIndex(indexName: "FK6A1A433CB95ED8E0", tableName: "order_invoice") {
            column(name: "invoice_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-244") {
        createIndex(indexName: "FK6C5BE20C800AA15", tableName: "shipment_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-245") {
        createIndex(indexName: "FK6D032BB53B350242", tableName: "shipment_event") {
            column(name: "shipment_events_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-246") {
        createIndex(indexName: "FK6D032BB5786431F", tableName: "shipment_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-247") {
        createIndex(indexName: "FK714F9FB528F75F00", tableName: "location") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-248") {
        createIndex(indexName: "FK714F9FB53BB36E94", tableName: "location") {
            column(name: "location_group_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-249") {
        createIndex(indexName: "FK714F9FB541E07A73", tableName: "location") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-250") {
        createIndex(indexName: "FK714F9FB5606C7D95", tableName: "location") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-251") {
        createIndex(indexName: "FK714F9FB561ED379F", tableName: "location") {
            column(name: "address_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-252") {
        createIndex(indexName: "FK714F9FB572A2C5B4", tableName: "location") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-253") {
        createIndex(indexName: "FK714F9FB57AF9A3C0", tableName: "location") {
            column(name: "parent_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-254") {
        createIndex(indexName: "FK7348B491217F5972", tableName: "unit_of_measure") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-255") {
        createIndex(indexName: "FK7348B491426DD105", tableName: "unit_of_measure") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-256") {
        createIndex(indexName: "FK7348B49197D8303E", tableName: "unit_of_measure") {
            column(name: "uom_class_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-257") {
        createIndex(indexName: "FK740B54769DB749D", tableName: "inventory_snapshot") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-258") {
        createIndex(indexName: "FK740B547AA992CED", tableName: "inventory_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-259") {
        createIndex(indexName: "FK740B547DED5FAE7", tableName: "inventory_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-260") {
        createIndex(indexName: "FK74D92A693D2E628A", tableName: "order_event") {
            column(name: "order_events_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-261") {
        createIndex(indexName: "FK74D92A69786431F", tableName: "order_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-262") {
        createIndex(indexName: "FK7975323F4CC49445", tableName: "local_transfer") {
            column(name: "destination_transaction_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-263") {
        createIndex(indexName: "FK7975323F57563498", tableName: "local_transfer") {
            column(name: "source_transaction_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-264") {
        createIndex(indexName: "FK7A19D7561ED379F", tableName: "location_group") {
            column(name: "address_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-265") {
        createIndex(indexName: "FK7AFF67F928F75F00", tableName: "location_type_supported_activities") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-266") {
        createIndex(indexName: "FK7FA0D2DE1E2B3CDC", tableName: "transaction") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-267") {
        createIndex(indexName: "FK7FA0D2DE217F5972", tableName: "transaction") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-268") {
        createIndex(indexName: "FK7FA0D2DE3265A8A9", tableName: "transaction") {
            column(name: "confirmed_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-269") {
        createIndex(indexName: "FK7FA0D2DE426DD105", tableName: "transaction") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-270") {
        createIndex(indexName: "FK7FA0D2DE5DE9E374", tableName: "transaction") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-271") {
        createIndex(indexName: "FK7FA0D2DE5F12AFED", tableName: "transaction") {
            column(name: "incoming_shipment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-272") {
        createIndex(indexName: "FK7FA0D2DE72A2C5B4", tableName: "transaction") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-273") {
        createIndex(indexName: "FK7FA0D2DE828481AF", tableName: "transaction") {
            column(name: "source_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-274") {
        createIndex(indexName: "FK7FA0D2DEB3FB7111", tableName: "transaction") {
            column(name: "transaction_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-275") {
        createIndex(indexName: "FK7FA0D2DEB80B3233", tableName: "transaction") {
            column(name: "outgoing_shipment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-276") {
        createIndex(indexName: "FK7FA0D2DED08EDBE6", tableName: "transaction") {
            column(name: "order_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-277") {
        createIndex(indexName: "FK7FA0D2DEF7076438", tableName: "transaction") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-278") {
        createIndex(indexName: "FK7FA87A22B3FB7111", tableName: "transaction_type_dimension") {
            column(name: "transaction_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-279") {
        createIndex(indexName: "FK9475736B3BE9D843", tableName: "order_shipment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-280") {
        createIndex(indexName: "FK9475736BB06EC4FB", tableName: "order_shipment") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-281") {
        createIndex(indexName: "FK94A534C24DEBC91", tableName: "product_attribute") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-282") {
        createIndex(indexName: "FK94A534C29542386", tableName: "product_attribute") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-283") {
        createIndex(indexName: "FK94A534C47B0D087", tableName: "product_attribute") {
            column(name: "attribute_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-284") {
        createIndex(indexName: "FK94A534CDED5FAE7", tableName: "product_attribute") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-285") {
        createIndex(indexName: "FK98293BFB217F5972", tableName: "synonym") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-286") {
        createIndex(indexName: "FK98293BFB426DD105", tableName: "synonym") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-287") {
        createIndex(indexName: "FK9A945A36C800AA15", tableName: "shipment_workflow_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-288") {
        createIndex(indexName: "FK9A945A36EC587CFB", tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-289") {
        createIndex(indexName: "FKA0303E4EDED5FAE7", tableName: "product_category") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-290") {
        createIndex(indexName: "FKA0303E4EEF4C770D", tableName: "product_category") {
            column(name: "category_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-291") {
        createIndex(indexName: "FKA71CAC4A9740C85F", tableName: "product_tag") {
            column(name: "tag_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-292") {
        createIndex(indexName: "FKA71CAC4ADED5FAE7", tableName: "product_tag") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-293") {
        createIndex(indexName: "FKA8B7A49072882836", tableName: "consumption_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-294") {
        createIndex(indexName: "FKA8B7A490A27827C2", tableName: "consumption_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-295") {
        createIndex(indexName: "FKA8B7A490CA32CFEF", tableName: "consumption_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-296") {
        createIndex(indexName: "FKA8B7A490D1F27172", tableName: "consumption_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-297") {
        createIndex(indexName: "FKABC21FD12EF4C7F4", tableName: "transaction_entry") {
            column(name: "transaction_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-298") {
        createIndex(indexName: "FKABC21FD169DB749D", tableName: "transaction_entry") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-299") {
        createIndex(indexName: "FKABC21FD1AA992CED", tableName: "transaction_entry") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-300") {
        createIndex(indexName: "FKABC21FD1DED5FAE7", tableName: "transaction_entry") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-301") {
        createIndex(indexName: "FKAE3064BA44979D51", tableName: "receipt_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-302") {
        createIndex(indexName: "FKAE3064BA69DB749D", tableName: "receipt_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-303") {
        createIndex(indexName: "FKAE3064BAAA992CED", tableName: "receipt_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-304") {
        createIndex(indexName: "FKAE3064BAB06EC4FB", tableName: "receipt_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-305") {
        createIndex(indexName: "FKAE3064BADED5FAE7", tableName: "receipt_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-306") {
        createIndex(indexName: "FKAE3064BAF7076438", tableName: "receipt_item") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-307") {
        createIndex(indexName: "FKB511C5AD20E351EA", tableName: "product_component") {
            column(name: "component_product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-308") {
        createIndex(indexName: "FKB511C5AD24DEBC91", tableName: "product_component") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-309") {
        createIndex(indexName: "FKB511C5ADFB4C199C", tableName: "product_component") {
            column(name: "assembly_product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-310") {
        createIndex(indexName: "FKB5A4FE84C4A49BBF", tableName: "order_item_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-311") {
        createIndex(indexName: "FKBD34ABCD8ABEBD5", tableName: "inventory_item_snapshot") {
            column(name: "location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-312") {
        createIndex(indexName: "FKBD34ABCDAA992CED", tableName: "inventory_item_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-313") {
        createIndex(indexName: "FKBD34ABCDDED5FAE7", tableName: "inventory_item_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-314") {
        createIndex(indexName: "FKC254A2E16CDADD53", tableName: "inventory_level") {
            column(name: "internal_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-315") {
        createIndex(indexName: "FKC254A2E172A2C5B4", tableName: "inventory_level") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-316") {
        createIndex(indexName: "FKC254A2E1CFDCB4DF", tableName: "inventory_level") {
            column(name: "preferred_bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-317") {
        createIndex(indexName: "FKC254A2E1DED5FAE7", tableName: "inventory_level") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-318") {
        createIndex(indexName: "FKC254A2E1F07D879A", tableName: "inventory_level") {
            column(name: "replenishment_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-319") {
        createIndex(indexName: "FKC398CCBAC4A49BBF", tableName: "shipment_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-320") {
        createIndex(indexName: "FKC73E1616DED5FAE7", tableName: "product_dimension") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-321") {
        createIndex(indexName: "FKC7AA9C4013CE80", tableName: "attribute") {
            column(name: "unit_of_measure_class_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-322") {
        createIndex(indexName: "FKCD71F39B8ABEBD5", tableName: "consumption") {
            column(name: "location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-323") {
        createIndex(indexName: "FKCD71F39BAA992CED", tableName: "consumption") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-324") {
        createIndex(indexName: "FKCD71F39BDED5FAE7", tableName: "consumption") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-325") {
        createIndex(indexName: "FKD08A526BC800AA15", tableName: "product_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-326") {
        createIndex(indexName: "FKD08A526BDED5FAE7", tableName: "product_document") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-327") {
        createIndex(indexName: "FKD2EAD9F8AA992CED", tableName: "lot_dimension") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-328") {
        createIndex(indexName: "FKD3F8383F217F5972", tableName: "picklist") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-329") {
        createIndex(indexName: "FKD3F8383F426DD105", tableName: "picklist") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-330") {
        createIndex(indexName: "FKD3F8383F5DE9E374", tableName: "picklist") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-331") {
        createIndex(indexName: "FKD3F8383FA3E976BC", tableName: "picklist") {
            column(name: "picker_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-332") {
        createIndex(indexName: "FKD3FC6EAB69DB749D", tableName: "product_availability") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-333") {
        createIndex(indexName: "FKD3FC6EABAA992CED", tableName: "product_availability") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-334") {
        createIndex(indexName: "FKD584C4C4FF77FF9B", tableName: "shipment_workflow") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-335") {
        createIndex(indexName: "FKD790DEBD154F600", tableName: "reference_number") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-336") {
        createIndex(indexName: "FKDA3BB2981CD3412D", tableName: "shipment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-337") {
        createIndex(indexName: "FKDA3BB2983B5F6286", tableName: "shipment_item") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-338") {
        createIndex(indexName: "FKDA3BB29844979D51", tableName: "shipment_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-339") {
        createIndex(indexName: "FKDA3BB29849AB6B52", tableName: "shipment_item") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-340") {
        createIndex(indexName: "FKDA3BB29869DB749D", tableName: "shipment_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-341") {
        createIndex(indexName: "FKDA3BB2987400E88E", tableName: "shipment_item") {
            column(name: "container_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-342") {
        createIndex(indexName: "FKDA3BB298AA992CED", tableName: "shipment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-343") {
        createIndex(indexName: "FKDA3BB298DED5FAE7", tableName: "shipment_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-344") {
        createIndex(indexName: "FKDEF5AD1317A6E251", tableName: "shipment_workflow_container_type") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-345") {
        createIndex(indexName: "FKDF7559D73896C98E", tableName: "shipper_service") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-346") {
        createIndex(indexName: "FKE071DE6DB06EC4FB", tableName: "fulfillment_item_shipment_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-347") {
        createIndex(indexName: "FKE071DE6DB42751E1", tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-348") {
        createIndex(indexName: "FKE139719A1E2B3CDC", tableName: "shipment") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-349") {
        createIndex(indexName: "FKE139719A217F5972", tableName: "shipment") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-350") {
        createIndex(indexName: "FKE139719A294C1012", tableName: "shipment") {
            column(name: "carrier_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-351") {
        createIndex(indexName: "FKE139719A426DD105", tableName: "shipment") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-352") {
        createIndex(indexName: "FKE139719A44979D51", tableName: "shipment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-353") {
        createIndex(indexName: "FKE139719A49AB6B52", tableName: "shipment") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-354") {
        createIndex(indexName: "FKE139719A5DE9E374", tableName: "shipment") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-355") {
        createIndex(indexName: "FKE139719AA28CC5FB", tableName: "shipment") {
            column(name: "shipment_method_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-356") {
        createIndex(indexName: "FKE139719AD95ACF25", tableName: "shipment") {
            column(name: "current_event_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-357") {
        createIndex(indexName: "FKE139719ADBDEDAC4", tableName: "shipment") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-358") {
        createIndex(indexName: "FKE139719AFF77FF9B", tableName: "shipment") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-359") {
        createIndex(indexName: "FKE698D2ECC800AA15", tableName: "order_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-360") {
        createIndex(indexName: "FKE698D2ECFE10118D", tableName: "order_document") {
            column(name: "order_documents_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-361") {
        createIndex(indexName: "FKE7584B1369DB749D", tableName: "picklist_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-362") {
        createIndex(indexName: "FKE7814C8117A6E251", tableName: "container") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-363") {
        createIndex(indexName: "FKE7814C813B5F6286", tableName: "container") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-364") {
        createIndex(indexName: "FKE7814C8144979D51", tableName: "container") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-365") {
        createIndex(indexName: "FKE7814C814B6A2E03", tableName: "container") {
            column(name: "parent_container_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-366") {
        createIndex(indexName: "FKED441931C8653BC0", tableName: "product_association") {
            column(name: "associated_product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-367") {
        createIndex(indexName: "FKED441931DED5FAE7", tableName: "product_association") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-368") {
        createIndex(indexName: "FKED8DCCEF217F5972", tableName: "product") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-369") {
        createIndex(indexName: "FKED8DCCEF426DD105", tableName: "product") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-370") {
        createIndex(indexName: "FKED8DCCEFABD88AC6", tableName: "product") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-371") {
        createIndex(indexName: "FKED8DCCEFEEB2908D", tableName: "product") {
            column(name: "default_uom_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-372") {
        createIndex(indexName: "FKED8DCCEFEF4C770D", tableName: "product") {
            column(name: "category_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-373") {
        createIndex(indexName: "FKEDC55CD447EBE106", tableName: "fulfillment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-374") {
        createIndex(indexName: "FKEDC55CD494567276", tableName: "fulfillment_item") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-375") {
        createIndex(indexName: "FKEDC55CD4AA992CED", tableName: "fulfillment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-376") {
        createIndex(indexName: "FKF58372688ABEBD5", tableName: "location_supported_activities") {
            column(name: "location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-378") {
        createIndex(indexName: "fk_budget_code_created_by", tableName: "budget_code") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-379") {
        createIndex(indexName: "fk_budget_code_organization", tableName: "budget_code") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-380") {
        createIndex(indexName: "fk_budget_code_updated_by", tableName: "budget_code") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-381") {
        createIndex(indexName: "fk_category_gl_account", tableName: "category") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-382") {
        createIndex(indexName: "fk_event_comment", tableName: "event") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-383") {
        createIndex(indexName: "fk_event_created_by", tableName: "event") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-384") {
        createIndex(indexName: "fk_gl_account_created_by", tableName: "gl_account") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-385") {
        createIndex(indexName: "fk_gl_account_gl_account_type", tableName: "gl_account") {
            column(name: "gl_account_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-386") {
        createIndex(indexName: "fk_gl_account_type_created_by", tableName: "gl_account_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-387") {
        createIndex(indexName: "fk_gl_account_type_updated_by", tableName: "gl_account_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-388") {
        createIndex(indexName: "fk_gl_account_updated_by", tableName: "gl_account") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-389") {
        createIndex(indexName: "fk_invoice_created_by", tableName: "invoice") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-390") {
        createIndex(indexName: "fk_invoice_currency_uom", tableName: "invoice") {
            column(name: "currency_uom_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-391") {
        createIndex(indexName: "fk_invoice_document_document", tableName: "invoice_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-392") {
        createIndex(indexName: "fk_invoice_document_invoice", tableName: "invoice_document") {
            column(name: "invoice_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-393") {
        createIndex(indexName: "fk_invoice_invoice_type", tableName: "invoice") {
            column(name: "invoice_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-394") {
        createIndex(indexName: "fk_invoice_item_budget_code", tableName: "invoice_item") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-395") {
        createIndex(indexName: "fk_invoice_item_created_by", tableName: "invoice_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-396") {
        createIndex(indexName: "fk_invoice_item_gl_account", tableName: "invoice_item") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-397") {
        createIndex(indexName: "fk_invoice_item_invoice", tableName: "invoice_item") {
            column(name: "invoice_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-398") {
        createIndex(indexName: "fk_invoice_item_product", tableName: "invoice_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-399") {
        createIndex(indexName: "fk_invoice_item_quantity_uom", tableName: "invoice_item") {
            column(name: "quantity_uom_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-400") {
        createIndex(indexName: "fk_invoice_item_updated_by", tableName: "invoice_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-401") {
        createIndex(indexName: "fk_invoice_party", tableName: "invoice") {
            column(name: "party_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-402") {
        createIndex(indexName: "fk_invoice_party_from", tableName: "invoice") {
            column(name: "party_from_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-403") {
        createIndex(indexName: "fk_invoice_reference_number_invoice_id", tableName: "invoice_reference_number") {
            column(name: "invoice_reference_numbers_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-404") {
        createIndex(indexName: "fk_invoice_reference_number_reference_number_id", tableName: "invoice_reference_number") {
            column(name: "reference_number_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-405") {
        createIndex(indexName: "fk_invoice_type_created_by", tableName: "invoice_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-406") {
        createIndex(indexName: "fk_invoice_type_updated_by", tableName: "invoice_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-407") {
        createIndex(indexName: "fk_invoice_updated_by", tableName: "invoice") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-408") {
        createIndex(indexName: "fk_order_adjustment_budget_code", tableName: "order_adjustment") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-409") {
        createIndex(indexName: "fk_order_adjustment_gl_account", tableName: "order_adjustment") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-410") {
        createIndex(indexName: "fk_order_adjustment_type_gl_account", tableName: "order_adjustment_type") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-411") {
        createIndex(indexName: "fk_order_item_budget_code", tableName: "order_item") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-412") {
        createIndex(indexName: "fk_order_item_gl_account", tableName: "order_item") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-413") {
        createIndex(indexName: "fk_order_type_created_by", tableName: "order_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-414") {
        createIndex(indexName: "fk_order_type_updated_by", tableName: "order_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-415") {
        createIndex(indexName: "fk_picklist_item_order_item", tableName: "picklist_item") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-416") {
        createIndex(indexName: "fk_picklist_order", tableName: "picklist") {
            column(name: "order_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-417") {
        createIndex(indexName: "fk_preference_type_created_by", tableName: "preference_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-418") {
        createIndex(indexName: "fk_preference_type_updated_by", tableName: "preference_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-419") {
        createIndex(indexName: "fk_product_association_mutual_association", tableName: "product_association") {
            column(name: "mutual_association_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-420") {
        createIndex(indexName: "fk_product_gl_account", tableName: "product") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-421") {
        createIndex(indexName: "fk_product_merge_logger_created_by", tableName: "product_merge_logger") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-422") {
        createIndex(indexName: "fk_product_merge_logger_obsolete_product", tableName: "product_merge_logger") {
            column(name: "obsolete_product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-423") {
        createIndex(indexName: "fk_product_merge_logger_primary_product", tableName: "product_merge_logger") {
            column(name: "primary_product_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-424") {
        createIndex(indexName: "fk_product_merge_logger_updated_by", tableName: "product_merge_logger") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-425") {
        createIndex(indexName: "fk_product_price_created_by", tableName: "product_price") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-426") {
        createIndex(indexName: "fk_product_price_unit_of_measure", tableName: "product_price") {
            column(name: "currency_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-427") {
        createIndex(indexName: "fk_product_price_updated_by", tableName: "product_price") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-428") {
        createIndex(indexName: "fk_product_product_family", tableName: "product") {
            column(name: "product_family_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-429") {
        createIndex(indexName: "fk_product_supplier_preference_created_by", tableName: "product_supplier_preference") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-430") {
        createIndex(indexName: "fk_product_supplier_preference_destination_party", tableName: "product_supplier_preference") {
            column(name: "destination_party_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-431") {
        createIndex(indexName: "fk_product_supplier_preference_preference_type", tableName: "product_supplier_preference") {
            column(name: "preference_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-432") {
        createIndex(indexName: "fk_product_supplier_preference_product_supplier", tableName: "product_supplier_preference") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-433") {
        createIndex(indexName: "fk_product_supplier_preference_updated_by", tableName: "product_supplier_preference") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-434") {
        createIndex(indexName: "fk_product_type_displayed_fields_product_type", tableName: "product_type_displayed_fields") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-435") {
        createIndex(indexName: "fk_product_type_required_fields_product_type", tableName: "product_type_required_fields") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-436") {
        createIndex(indexName: "fk_product_type_supported_activities_product_type", tableName: "product_type_supported_activities") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-437") {
        createIndex(indexName: "fk_requisition_approved_by", tableName: "requisition") {
            column(name: "approved_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-438") {
        createIndex(indexName: "fk_requisition_approvers_person", tableName: "requisition_approvers") {
            column(name: "person_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-439") {
        createIndex(indexName: "fk_requisition_approvers_requisition", tableName: "requisition_approvers") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-440") {
        createIndex(indexName: "fk_requisition_comment_comment", tableName: "requisition_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-441") {
        createIndex(indexName: "fk_requisition_comment_requisition", tableName: "requisition_comment") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-442") {
        createIndex(indexName: "fk_requisition_event_event", tableName: "requisition_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-443") {
        createIndex(indexName: "fk_requisition_event_requisition", tableName: "requisition_event") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-444") {
        createIndex(indexName: "fk_requisition_rejected_by", tableName: "requisition") {
            column(name: "rejected_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-445") {
        createIndex(indexName: "fk_shipment_reference_number_shipment", tableName: "shipment_reference_number") {
            column(name: "shipment_reference_numbers_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-446") {
        createIndex(indexName: "inventory_snapshot_date_idx", tableName: "inventory_snapshot") {
            column(name: "date")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-447") {
        createIndex(indexName: "inventory_snapshot_last_updated_idx", tableName: "inventory_snapshot") {
            column(name: "location_id")

            column(name: "last_updated")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-448") {
        createIndex(indexName: "location_role_ibfk_1", tableName: "location_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-449") {
        createIndex(indexName: "location_role_ibfk_2", tableName: "location_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-450") {
        createIndex(indexName: "location_role_ibfk_3", tableName: "location_role") {
            column(name: "location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-451") {
        createIndex(indexName: "order_adjustment_invoice_ibfk_1", tableName: "order_adjustment_invoice") {
            column(name: "order_adjustment_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-452") {
        createIndex(indexName: "order_adjustment_invoice_ibfk_2", tableName: "order_adjustment_invoice") {
            column(name: "invoice_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-453") {
        createIndex(indexName: "picklist_item_ibfk_1", tableName: "picklist_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-454") {
        createIndex(indexName: "picklist_item_ibfk_2", tableName: "picklist_item") {
            column(name: "picklist_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-455") {
        createIndex(indexName: "picklist_item_ibfk_3", tableName: "picklist_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-456") {
        createIndex(indexName: "picklist_item_ibfk_4", tableName: "picklist_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-457") {
        createIndex(indexName: "picklist_item_ibfk_5", tableName: "picklist_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-458") {
        createIndex(indexName: "product_availability_product_location_idx", tableName: "product_availability") {
            column(name: "product_id")

            column(name: "location_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-460") {
        createIndex(indexName: "shipment_invoice_ibfk_1", tableName: "shipment_invoice") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-461") {
        createIndex(indexName: "shipment_invoice_ibfk_2", tableName: "shipment_invoice") {
            column(name: "invoice_item_id")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-462") {
        createIndex(indexName: "zone_location_ibfk_3", tableName: "location") {
            column(name: "zone_id")
        }
    }
}
