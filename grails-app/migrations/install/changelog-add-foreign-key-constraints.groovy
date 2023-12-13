package install

databaseChangeLog = {

    changeSet(author: "openboxes (generated)", id: "1700664714834-463") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_dimension", constraintName: "FK1143A95C8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-464") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK143BF46AA462C195", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-465") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK143BF46AFF37FDB5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-466") {
        addForeignKeyConstraint(baseColumnNames: "verified_by_id", baseTableName: "requisition", constraintName: "FK1799509C20E33E1C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-467") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition", constraintName: "FK1799509C217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-468") {
        addForeignKeyConstraint(baseColumnNames: "requisition_template_id", baseTableName: "requisition", constraintName: "FK1799509C2BDD17B3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-469") {
        addForeignKeyConstraint(baseColumnNames: "received_by_id", baseTableName: "requisition", constraintName: "FK1799509C36C69275", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-470") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition", constraintName: "FK1799509C426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-471") {
        addForeignKeyConstraint(baseColumnNames: "delivered_by_id", baseTableName: "requisition", constraintName: "FK1799509C4CF042D8", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-472") {
        addForeignKeyConstraint(baseColumnNames: "issued_by_id", baseTableName: "requisition", constraintName: "FK1799509CD196DBBF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-473") {
        addForeignKeyConstraint(baseColumnNames: "checked_by_id", baseTableName: "requisition", constraintName: "FK1799509CD2CB8BBB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-474") {
        addForeignKeyConstraint(baseColumnNames: "reviewed_by_id", baseTableName: "requisition", constraintName: "FK1799509CDFA74E0B", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-475") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-476") {
        addForeignKeyConstraint(baseColumnNames: "product_catalog_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9FB5E604E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_catalog", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-477") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "tag", constraintName: "FK1BF9A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-478") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "tag", constraintName: "FK1BF9A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-479") {
        addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "party_role", constraintName: "FK1C92FE2F3E67CF9F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-480") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72D72882836", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-481") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DA27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-482") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-483") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA354381", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction_type_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-484") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DD1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-485") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "synonym", constraintName: "FK299E50ABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-486") {
        addForeignKeyConstraint(baseColumnNames: "origin_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D6418D76D84", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-487") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "order_item", constraintName: "FK2D110D6429542386", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-488") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "order_item", constraintName: "FK2D110D6429B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-489") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order_item", constraintName: "FK2D110D6444979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-490") {
        addForeignKeyConstraint(baseColumnNames: "parent_order_item_id", baseTableName: "order_item", constraintName: "FK2D110D6451A9416E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-491") {
        addForeignKeyConstraint(baseColumnNames: "quantity_uom_id", baseTableName: "order_item", constraintName: "FK2D110D645ED93B03", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-492") {
        addForeignKeyConstraint(baseColumnNames: "destination_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D64605326C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-493") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "order_item", constraintName: "FK2D110D64911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-494") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "order_item", constraintName: "FK2D110D64AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-495") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_item", constraintName: "FK2D110D64D08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-496") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "order_item", constraintName: "FK2D110D64DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-497") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "order_item", constraintName: "FK2D110D64EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-498") {
        addForeignKeyConstraint(baseColumnNames: "order_comments_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EB8839C0F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-499") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-500") {
        addForeignKeyConstraint(baseColumnNames: "from_unit_of_measure_id", baseTableName: "unit_of_measure_conversion", constraintName: "FK2E4511844A3E746", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-501") {
        addForeignKeyConstraint(baseColumnNames: "to_unit_of_measure_id", baseTableName: "unit_of_measure_conversion", constraintName: "FK2E4511849B9434D5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-502") {
        addForeignKeyConstraint(baseColumnNames: "parent_category_id", baseTableName: "category", constraintName: "FK302BCFE619A2EF8", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-503") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_id", baseTableName: "shipment_reference_number", constraintName: "FK312F6C292388BC5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-504") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "product_group_product", constraintName: "FK313A4BDF14F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-505") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_group_product", constraintName: "FK313A4BDFDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-506") {
        addForeignKeyConstraint(baseColumnNames: "document_type_id", baseTableName: "document", constraintName: "FK335CD11B6631D8CC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-507") {
        addForeignKeyConstraint(baseColumnNames: "warehouse_id", baseTableName: "user", constraintName: "FK36EBCB1F28CE07", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-508") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "user", constraintName: "FK36EBCB41E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-509") {
        addForeignKeyConstraint(baseColumnNames: "sender_id", baseTableName: "comment", constraintName: "FK38A5EE5FAF1302EB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-510") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "comment", constraintName: "FK38A5EE5FF885F087", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-511") {
        addForeignKeyConstraint(baseColumnNames: "attribute_id", baseTableName: "attribute_entity_type_codes", constraintName: "FK38EE09DA47B0D087", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "attribute", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-512") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-513") {
        addForeignKeyConstraint(baseColumnNames: "manufacturer_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C2A475A37", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-514") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-515") {
        addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CF42F7E5C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-516") {
        addForeignKeyConstraint(baseColumnNames: "shipper_service_id", baseTableName: "shipment_method", constraintName: "FK40203B26296B2CA3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipper_service", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-517") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipment_method", constraintName: "FK40203B263896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-518") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "receipt", constraintName: "FK408272383B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-519") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt", constraintName: "FK4082723844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-520") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "requisition", constraintName: "FK414EF28F1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-521") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition", constraintName: "FK414EF28F44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-522") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "requisition", constraintName: "FK414EF28F94567276", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-523") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "requisition", constraintName: "FK414EF28FDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-524") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition", constraintName: "FK414EF28FDD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-525") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFE3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-526") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-527") {
        addForeignKeyConstraint(baseColumnNames: "order_adjustment_type_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFEE1A39520", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_adjustment_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-528") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "shipment_workflow_reference_number_type", constraintName: "FK4BB27241154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-529") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_item", constraintName: "FK4DA982C35DE21C87", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-530") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-531") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-532") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-533") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-534") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_group", constraintName: "FK51F3772FEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-535") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "requisition_item", constraintName: "FK5358E4D614F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-536") {
        addForeignKeyConstraint(baseColumnNames: "substitution_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D61594028E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-537") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-538") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "requisition_item", constraintName: "FK5358E4D629B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-539") {
        addForeignKeyConstraint(baseColumnNames: "modification_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6405AC22D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-540") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-541") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition_item", constraintName: "FK5358E4D644979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-542") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6DD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-543") {
        addForeignKeyConstraint(baseColumnNames: "parent_requisition_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6F84BDE18", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-544") {
        addForeignKeyConstraint(baseColumnNames: "fulfilled_by_id", baseTableName: "fulfillment", constraintName: "FK5A2551DEAC392B33", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-545") {
        addForeignKeyConstraint(baseColumnNames: "event_type_id", baseTableName: "event", constraintName: "FK5C6729A3D970DB4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-546") {
        addForeignKeyConstraint(baseColumnNames: "event_location_id", baseTableName: "event", constraintName: "FK5C6729A4415A5B0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-547") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-548") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-549") {
        addForeignKeyConstraint(baseColumnNames: "base_uom_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A6B9DFD", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-550") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_package", constraintName: "FK615A48F6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-551") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_package", constraintName: "FK615A48F629542386", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-552") {
        addForeignKeyConstraint(baseColumnNames: "uom_id", baseTableName: "product_package", constraintName: "FK615A48F63906C4CF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-553") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_package", constraintName: "FK615A48F6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-554") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_package", constraintName: "FK615A48F6DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-555") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "order", constraintName: "FK651874E1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-556") {
        addForeignKeyConstraint(baseColumnNames: "approved_by_id", baseTableName: "order", constraintName: "FK651874E240896CB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-557") {
        addForeignKeyConstraint(baseColumnNames: "destination_party_id", baseTableName: "order", constraintName: "FK651874E35D76CB0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-558") {
        addForeignKeyConstraint(baseColumnNames: "completed_by_id", baseTableName: "order", constraintName: "FK651874E41B7275F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-559") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order", constraintName: "FK651874E44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-560") {
        addForeignKeyConstraint(baseColumnNames: "payment_method_type_id", baseTableName: "order", constraintName: "FK651874E6A8010C1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "payment_method_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-561") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "order", constraintName: "FK651874E6D91063C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-562") {
        addForeignKeyConstraint(baseColumnNames: "order_type_id", baseTableName: "order", constraintName: "FK651874E8AF312E3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-563") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "order", constraintName: "FK651874E8E7F7DCF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-564") {
        addForeignKeyConstraint(baseColumnNames: "payment_term_id", baseTableName: "order", constraintName: "FK651874E9E52B00C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "payment_term", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-565") {
        addForeignKeyConstraint(baseColumnNames: "ordered_by_id", baseTableName: "order", constraintName: "FK651874EAF6D8801", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-566") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "order", constraintName: "FK651874EDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-567") {
        addForeignKeyConstraint(baseColumnNames: "party_type_id", baseTableName: "party", constraintName: "FK6581AE69DFE4C4C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-568") {
        addForeignKeyConstraint(baseColumnNames: "default_location_id", baseTableName: "party", constraintName: "FK6581AE6D1DFC6D7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-569") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_invoice", constraintName: "FK6A1A433C3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-570") {
        addForeignKeyConstraint(baseColumnNames: "invoice_item_id", baseTableName: "order_invoice", constraintName: "FK6A1A433CB95ED8E0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-571") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_document", constraintName: "FK6C5BE20C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-572") {
        addForeignKeyConstraint(baseColumnNames: "shipment_events_id", baseTableName: "shipment_event", constraintName: "FK6D032BB53B350242", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-573") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "shipment_event", constraintName: "FK6D032BB5786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-574") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location", constraintName: "FK714F9FB528F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-575") {
        addForeignKeyConstraint(baseColumnNames: "location_group_id", baseTableName: "location", constraintName: "FK714F9FB53BB36E94", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_group", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-576") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "location", constraintName: "FK714F9FB541E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-577") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "location", constraintName: "FK714F9FB5606C7D95", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-578") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location", constraintName: "FK714F9FB561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-579") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "location", constraintName: "FK714F9FB572A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-580") {
        addForeignKeyConstraint(baseColumnNames: "parent_location_id", baseTableName: "location", constraintName: "FK714F9FB57AF9A3C0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-581") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-582") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-583") {
        addForeignKeyConstraint(baseColumnNames: "uom_class_id", baseTableName: "unit_of_measure", constraintName: "FK7348B49197D8303E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-584") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B54769DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-585") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B5478ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-586") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-587") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-588") {
        addForeignKeyConstraint(baseColumnNames: "order_events_id", baseTableName: "order_event", constraintName: "FK74D92A693D2E628A", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-589") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "order_event", constraintName: "FK74D92A69786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-590") {
        addForeignKeyConstraint(baseColumnNames: "destination_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F4CC49445", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-591") {
        addForeignKeyConstraint(baseColumnNames: "source_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F57563498", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-592") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location_group", constraintName: "FK7A19D7561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-593") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location_type_supported_activities", constraintName: "FK7AFF67F928F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-594") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-595") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-596") {
        addForeignKeyConstraint(baseColumnNames: "confirmed_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE3265A8A9", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-597") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-598") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-599") {
        addForeignKeyConstraint(baseColumnNames: "incoming_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5F12AFED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-600") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE72A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-601") {
        addForeignKeyConstraint(baseColumnNames: "source_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE828481AF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-602") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB3FB7111", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-603") {
        addForeignKeyConstraint(baseColumnNames: "outgoing_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB80B3233", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-604") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "transaction", constraintName: "FK7FA0D2DED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-605") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-606") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_id", baseTableName: "transaction_type_dimension", constraintName: "FK7FA87A22B3FB7111", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-607") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_shipment", constraintName: "FK9475736B3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-608") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "order_shipment", constraintName: "FK9475736BB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-609") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_attribute", constraintName: "FK94A534C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-610") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_attribute", constraintName: "FK94A534C29542386", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-611") {
        addForeignKeyConstraint(baseColumnNames: "attribute_id", baseTableName: "product_attribute", constraintName: "FK94A534C47B0D087", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "attribute", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-612") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_attribute", constraintName: "FK94A534CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-613") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "synonym", constraintName: "FK98293BFB217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-614") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "synonym", constraintName: "FK98293BFB426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-615") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-616") {
        addForeignKeyConstraint(baseColumnNames: "shipment_workflow_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36EC587CFB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_workflow", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-617") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_category", constraintName: "FKA0303E4EEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-618") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "product_tag", constraintName: "FKA71CAC4A9740C85F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "tag", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-619") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_tag", constraintName: "FKA71CAC4ADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-620") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A49072882836", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-621") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490A27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-622") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490CA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-623") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490D1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-624") {
        addForeignKeyConstraint(baseColumnNames: "transaction_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD12EF4C7F4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-625") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD169DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-626") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-627") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-628") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-629") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-630") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-631") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-632") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "receipt_item", constraintName: "FKAE3064BADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-633") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-634") {
        addForeignKeyConstraint(baseColumnNames: "component_product_id", baseTableName: "product_component", constraintName: "FKB511C5AD20E351EA", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-635") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_component", constraintName: "FKB511C5AD24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-636") {
        addForeignKeyConstraint(baseColumnNames: "assembly_product_id", baseTableName: "product_component", constraintName: "FKB511C5ADFB4C199C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-637") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCD8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-638") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCDAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-639") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCDDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-640") {
        addForeignKeyConstraint(baseColumnNames: "internal_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E16CDADD53", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-641") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "inventory_level", constraintName: "FKC254A2E172A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-642") {
        addForeignKeyConstraint(baseColumnNames: "preferred_bin_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1CFDCB4DF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-643") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-644") {
        addForeignKeyConstraint(baseColumnNames: "replenishment_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1F07D879A", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-645") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "shipment_comment", constraintName: "FKC398CCBAC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-646") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_dimension", constraintName: "FKC73E1616DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-647") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_class_id", baseTableName: "attribute", constraintName: "FKC7AA9C4013CE80", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-648") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "consumption", constraintName: "FKCD71F39B8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-649") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "consumption", constraintName: "FKCD71F39BAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-650") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "consumption", constraintName: "FKCD71F39BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-651") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "product_document", constraintName: "FKD08A526BC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-652") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_document", constraintName: "FKD08A526BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-653") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "lot_dimension", constraintName: "FKD2EAD9F8AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-654") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-655") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-656") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "picklist", constraintName: "FKD3F8383F5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-657") {
        addForeignKeyConstraint(baseColumnNames: "picker_id", baseTableName: "picklist", constraintName: "FKD3F8383FA3E976BC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-658") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "product_availability", constraintName: "FKD3FC6EAB69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-659") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "product_availability", constraintName: "FKD3FC6EAB8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-660") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "product_availability", constraintName: "FKD3FC6EABAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-661") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_availability", constraintName: "FKD3FC6EABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-662") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment_workflow", constraintName: "FKD584C4C4FF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-663") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "reference_number", constraintName: "FKD790DEBD154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-664") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2981CD3412D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-665") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2983B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-666") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-667") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29849AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-668") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29869DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-669") {
        addForeignKeyConstraint(baseColumnNames: "container_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2987400E88E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-670") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-671") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-672") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "shipment_workflow_container_type", constraintName: "FKDEF5AD1317A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-673") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipper_service", constraintName: "FKDF7559D73896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-674") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-675") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_item_shipment_items_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB42751E1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "fulfillment_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-676") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "shipment", constraintName: "FKE139719A1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-677") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "shipment", constraintName: "FKE139719A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-678") {
        addForeignKeyConstraint(baseColumnNames: "carrier_id", baseTableName: "shipment", constraintName: "FKE139719A294C1012", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-679") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "shipment", constraintName: "FKE139719A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-680") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment", constraintName: "FKE139719A44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-681") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment", constraintName: "FKE139719A49AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-682") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "shipment", constraintName: "FKE139719A5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-683") {
        addForeignKeyConstraint(baseColumnNames: "shipment_method_id", baseTableName: "shipment", constraintName: "FKE139719AA28CC5FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_method", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-684") {
        addForeignKeyConstraint(baseColumnNames: "current_event_id", baseTableName: "shipment", constraintName: "FKE139719AD95ACF25", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-685") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "shipment", constraintName: "FKE139719ADBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-686") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment", constraintName: "FKE139719AFF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-687") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "order_document", constraintName: "FKE698D2ECC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-688") {
        addForeignKeyConstraint(baseColumnNames: "order_documents_id", baseTableName: "order_document", constraintName: "FKE698D2ECFE10118D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-689") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "picklist_item", constraintName: "FKE7584B1369DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-690") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "container", constraintName: "FKE7814C8117A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-691") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "container", constraintName: "FKE7814C813B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-692") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "container", constraintName: "FKE7814C8144979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-693") {
        addForeignKeyConstraint(baseColumnNames: "parent_container_id", baseTableName: "container", constraintName: "FKE7814C814B6A2E03", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-694") {
        addForeignKeyConstraint(baseColumnNames: "associated_product_id", baseTableName: "product_association", constraintName: "FKED441931C8653BC0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-695") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_association", constraintName: "FKED441931DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-696") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product", constraintName: "FKED8DCCEF217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-697") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product", constraintName: "FKED8DCCEF426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-698") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product", constraintName: "FKED8DCCEFABD88AC6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-699") {
        addForeignKeyConstraint(baseColumnNames: "default_uom_id", baseTableName: "product", constraintName: "FKED8DCCEFEEB2908D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-700") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product", constraintName: "FKED8DCCEFEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-701") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD447EBE106", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-702") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD494567276", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-703") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD4AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-704") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_supported_activities", constraintName: "FKF58372688ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-706") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item", constraintName: "FKFE019416DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-707") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "budget_code", constraintName: "fk_budget_code_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-708") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "budget_code", constraintName: "fk_budget_code_organization", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-709") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "budget_code", constraintName: "fk_budget_code_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-710") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "category", constraintName: "fk_category_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-711") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "event", constraintName: "fk_event_comment", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-712") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "event", constraintName: "fk_event_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-713") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "gl_account", constraintName: "fk_gl_account_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-714") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_type_id", baseTableName: "gl_account", constraintName: "fk_gl_account_gl_account_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-715") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "gl_account_type", constraintName: "fk_gl_account_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-716") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "gl_account_type", constraintName: "fk_gl_account_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-717") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "gl_account", constraintName: "fk_gl_account_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-718") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice", constraintName: "fk_invoice_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-719") {
        addForeignKeyConstraint(baseColumnNames: "currency_uom_id", baseTableName: "invoice", constraintName: "fk_invoice_currency_uom", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-720") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "invoice_document", constraintName: "fk_invoice_document_document", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-721") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_document", constraintName: "fk_invoice_document_invoice", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-722") {
        addForeignKeyConstraint(baseColumnNames: "invoice_type_id", baseTableName: "invoice", constraintName: "fk_invoice_invoice_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-723") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-724") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-725") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-726") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_invoice", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-727") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_product", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-728") {
        addForeignKeyConstraint(baseColumnNames: "quantity_uom_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_quantity_uom", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-729") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-730") {
        addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "invoice", constraintName: "fk_invoice_party", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-731") {
        addForeignKeyConstraint(baseColumnNames: "party_from_id", baseTableName: "invoice", constraintName: "fk_invoice_party_from", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-732") {
        addForeignKeyConstraint(baseColumnNames: "invoice_reference_numbers_id", baseTableName: "invoice_reference_number", constraintName: "fk_invoice_reference_number_invoice_id", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-733") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_id", baseTableName: "invoice_reference_number", constraintName: "fk_invoice_reference_number_reference_number_id", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-734") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice_type", constraintName: "fk_invoice_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-735") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "invoice_type", constraintName: "fk_invoice_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-736") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "invoice", constraintName: "fk_invoice_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-737") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "order_adjustment", constraintName: "fk_order_adjustment_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-738") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_adjustment", constraintName: "fk_order_adjustment_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-739") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_adjustment_type", constraintName: "fk_order_adjustment_type_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-740") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "order_item", constraintName: "fk_order_item_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-741") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_item", constraintName: "fk_order_item_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-742") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "order_type", constraintName: "fk_order_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-743") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "order_type", constraintName: "fk_order_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-744") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "picklist_item", constraintName: "fk_picklist_item_order_item", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-745") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "picklist", constraintName: "fk_picklist_order", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-746") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "preference_type", constraintName: "fk_preference_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-747") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "preference_type", constraintName: "fk_preference_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-748") {
        addForeignKeyConstraint(baseColumnNames: "mutual_association_id", baseTableName: "product_association", constraintName: "fk_product_association_mutual_association", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_association", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-749") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "product", constraintName: "fk_product_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-750") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-751") {
        addForeignKeyConstraint(baseColumnNames: "obsolete_product_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_obsolete_product", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-752") {
        addForeignKeyConstraint(baseColumnNames: "primary_product_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_primary_product", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-753") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-754") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_price", constraintName: "fk_product_price_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-755") {
        addForeignKeyConstraint(baseColumnNames: "currency_id", baseTableName: "product_price", constraintName: "fk_product_price_unit_of_measure", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-756") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_price", constraintName: "fk_product_price_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-757") {
        addForeignKeyConstraint(baseColumnNames: "product_family_id", baseTableName: "product", constraintName: "fk_product_product_family", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-758") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-759") {
        addForeignKeyConstraint(baseColumnNames: "destination_party_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_destination_party", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-760") {
        addForeignKeyConstraint(baseColumnNames: "preference_type_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_preference_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "preference_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-761") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_product_supplier", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-762") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-763") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product_type_displayed_fields", constraintName: "fk_product_type_displayed_fields_product_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-764") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product_type_required_fields", constraintName: "fk_product_type_required_fields_product_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-765") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product_type_supported_activities", constraintName: "fk_product_type_supported_activities_product_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-766") {
        addForeignKeyConstraint(baseColumnNames: "approved_by_id", baseTableName: "requisition", constraintName: "fk_requisition_approved_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-767") {
        addForeignKeyConstraint(baseColumnNames: "person_id", baseTableName: "requisition_approvers", constraintName: "fk_requisition_approvers_person", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-768") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_approvers", constraintName: "fk_requisition_approvers_requisition", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-769") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "requisition_comment", constraintName: "fk_requisition_comment_comment", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-770") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_comment", constraintName: "fk_requisition_comment_requisition", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-771") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "requisition_event", constraintName: "fk_requisition_event_event", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-772") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_event", constraintName: "fk_requisition_event_requisition", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-773") {
        addForeignKeyConstraint(baseColumnNames: "rejected_by_id", baseTableName: "requisition", constraintName: "fk_requisition_rejected_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-774") {
        addForeignKeyConstraint(baseColumnNames: "shipment_reference_numbers_id", baseTableName: "shipment_reference_number", constraintName: "fk_shipment_reference_number_shipment", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-775") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "location_role", constraintName: "location_role_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-776") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "location_role", constraintName: "location_role_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-777") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_role", constraintName: "location_role_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-778") {
        addForeignKeyConstraint(baseColumnNames: "order_adjustment_id", baseTableName: "order_adjustment_invoice", constraintName: "order_adjustment_invoice_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_adjustment", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-779") {
        addForeignKeyConstraint(baseColumnNames: "invoice_item_id", baseTableName: "order_adjustment_invoice", constraintName: "order_adjustment_invoice_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-780") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-781") {
        addForeignKeyConstraint(baseColumnNames: "picklist_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "picklist", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-782") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-783") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-784") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-785") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "shipment_invoice", constraintName: "shipment_invoice_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-786") {
        addForeignKeyConstraint(baseColumnNames: "invoice_item_id", baseTableName: "shipment_invoice", constraintName: "shipment_invoice_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_item", validate: "true")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-787") {
        addForeignKeyConstraint(baseColumnNames: "zone_id", baseTableName: "location", constraintName: "zone_location_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }
}
