databaseChangeLog = {

    changeSet(author: "jmiranda (generated)", id: "1580360689181-325") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_dimension", constraintName: "FK1143A95C8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-326") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK143BF46AA462C195", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-327") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK143BF46AFF37FDB5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-328") {
        addForeignKeyConstraint(baseColumnNames: "verified_by_id", baseTableName: "requisition", constraintName: "FK1799509C20E33E1C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-329") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition", constraintName: "FK1799509C217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-330") {
        addForeignKeyConstraint(baseColumnNames: "requisition_template_id", baseTableName: "requisition", constraintName: "FK1799509C2BDD17B3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-331") {
        addForeignKeyConstraint(baseColumnNames: "received_by_id", baseTableName: "requisition", constraintName: "FK1799509C36C69275", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-332") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition", constraintName: "FK1799509C426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-333") {
        addForeignKeyConstraint(baseColumnNames: "delivered_by_id", baseTableName: "requisition", constraintName: "FK1799509C4CF042D8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-334") {
        addForeignKeyConstraint(baseColumnNames: "issued_by_id", baseTableName: "requisition", constraintName: "FK1799509CD196DBBF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-335") {
        addForeignKeyConstraint(baseColumnNames: "checked_by_id", baseTableName: "requisition", constraintName: "FK1799509CD2CB8BBB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-336") {
        addForeignKeyConstraint(baseColumnNames: "reviewed_by_id", baseTableName: "requisition", constraintName: "FK1799509CDFA74E0B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-337") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-338") {
        addForeignKeyConstraint(baseColumnNames: "product_catalog_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9FB5E604E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_catalog", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-339") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "tag", constraintName: "FK1BF9A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-340") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "tag", constraintName: "FK1BF9A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-341") {
        addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "party_role", constraintName: "FK1C92FE2F3E67CF9F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-342") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72D72882836", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-343") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DA27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-344") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-345") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA354381", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction_type_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-346") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DD1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-347") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "synonym", constraintName: "FK299E50ABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-348") {
        addForeignKeyConstraint(baseColumnNames: "origin_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D6418D76D84", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-349") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order_item", constraintName: "FK2D110D6444979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-350") {
        addForeignKeyConstraint(baseColumnNames: "parent_order_item_id", baseTableName: "order_item", constraintName: "FK2D110D6451A9416E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-351") {
        addForeignKeyConstraint(baseColumnNames: "destination_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D64605326C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-352") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "order_item", constraintName: "FK2D110D64911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-353") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "order_item", constraintName: "FK2D110D64AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-354") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_item", constraintName: "FK2D110D64D08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-355") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "order_item", constraintName: "FK2D110D64DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-356") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "order_item", constraintName: "FK2D110D64EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-357") {
        addForeignKeyConstraint(baseColumnNames: "order_comments_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EB8839C0F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-358") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-359") {
        addForeignKeyConstraint(baseColumnNames: "parent_category_id", baseTableName: "category", constraintName: "FK302BCFE619A2EF8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-360") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_id", baseTableName: "shipment_reference_number", constraintName: "FK312F6C292388BC5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "reference_number", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-361") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "product_group_product", constraintName: "FK313A4BDF14F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-362") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_group_product", constraintName: "FK313A4BDFDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-363") {
        addForeignKeyConstraint(baseColumnNames: "document_type_id", baseTableName: "document", constraintName: "FK335CD11B6631D8CC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-364") {
        addForeignKeyConstraint(baseColumnNames: "warehouse_id", baseTableName: "user", constraintName: "FK36EBCB1F28CE07", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-365") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "user", constraintName: "FK36EBCB41E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-366") {
        addForeignKeyConstraint(baseColumnNames: "sender_id", baseTableName: "comment", constraintName: "FK38A5EE5FAF1302EB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-367") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "comment", constraintName: "FK38A5EE5FF885F087", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-368") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-369") {
        addForeignKeyConstraint(baseColumnNames: "manufacturer_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C2A475A37", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-370") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-371") {
        addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CF42F7E5C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-372") {
        addForeignKeyConstraint(baseColumnNames: "shipper_service_id", baseTableName: "shipment_method", constraintName: "FK40203B26296B2CA3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipper_service", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-373") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipment_method", constraintName: "FK40203B263896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-374") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "receipt", constraintName: "FK408272383B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-375") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt", constraintName: "FK4082723844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-376") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "requisition", constraintName: "FK414EF28F1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-377") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition", constraintName: "FK414EF28F44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-378") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "requisition", constraintName: "FK414EF28F94567276", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-379") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "requisition", constraintName: "FK414EF28FDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-380") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition", constraintName: "FK414EF28FDD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-381") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "shipment_workflow_reference_number_type", constraintName: "FK4BB27241154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-382") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_item", constraintName: "FK4DA982C35DE21C87", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-383") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-384") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-385") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-386") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-387") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_group", constraintName: "FK51F3772FEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-388") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "requisition_item", constraintName: "FK5358E4D614F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-389") {
        addForeignKeyConstraint(baseColumnNames: "substitution_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D61594028E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-390") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-391") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "requisition_item", constraintName: "FK5358E4D629B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-392") {
        addForeignKeyConstraint(baseColumnNames: "modification_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6405AC22D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-393") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-394") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition_item", constraintName: "FK5358E4D644979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-395") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6DD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-396") {
        addForeignKeyConstraint(baseColumnNames: "parent_requisition_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6F84BDE18", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-397") {
        addForeignKeyConstraint(baseColumnNames: "fulfilled_by_id", baseTableName: "fulfillment", constraintName: "FK5A2551DEAC392B33", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-398") {
        addForeignKeyConstraint(baseColumnNames: "event_type_id", baseTableName: "event", constraintName: "FK5C6729A3D970DB4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-399") {
        addForeignKeyConstraint(baseColumnNames: "event_location_id", baseTableName: "event", constraintName: "FK5C6729A4415A5B0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-400") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-401") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-402") {
        addForeignKeyConstraint(baseColumnNames: "base_uom_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A6B9DFD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-403") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_package", constraintName: "FK615A48F6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-404") {
        addForeignKeyConstraint(baseColumnNames: "uom_id", baseTableName: "product_package", constraintName: "FK615A48F63906C4CF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-405") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_package", constraintName: "FK615A48F6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-406") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_package", constraintName: "FK615A48F6DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-407") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "order", constraintName: "FK651874E1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-408") {
        addForeignKeyConstraint(baseColumnNames: "completed_by_id", baseTableName: "order", constraintName: "FK651874E41B7275F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-409") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order", constraintName: "FK651874E44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-410") {
        addForeignKeyConstraint(baseColumnNames: "ordered_by_id", baseTableName: "order", constraintName: "FK651874EAF6D8801", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-411") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "order", constraintName: "FK651874EDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-412") {
        addForeignKeyConstraint(baseColumnNames: "party_type_id", baseTableName: "party", constraintName: "FK6581AE69DFE4C4C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-413") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_document", constraintName: "FK6C5BE20C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-414") {
        addForeignKeyConstraint(baseColumnNames: "shipment_events_id", baseTableName: "shipment_event", constraintName: "FK6D032BB53B350242", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-415") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "shipment_event", constraintName: "FK6D032BB5786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-416") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location", constraintName: "FK714F9FB528F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-417") {
        addForeignKeyConstraint(baseColumnNames: "location_group_id", baseTableName: "location", constraintName: "FK714F9FB53BB36E94", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-418") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "location", constraintName: "FK714F9FB541E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-419") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "location", constraintName: "FK714F9FB5606C7D95", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-420") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location", constraintName: "FK714F9FB561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-421") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "location", constraintName: "FK714F9FB572A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-422") {
        addForeignKeyConstraint(baseColumnNames: "parent_location_id", baseTableName: "location", constraintName: "FK714F9FB57AF9A3C0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-423") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-424") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-425") {
        addForeignKeyConstraint(baseColumnNames: "uom_class_id", baseTableName: "unit_of_measure", constraintName: "FK7348B49197D8303E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-426") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B54769DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-427") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B5478ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-428") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-429") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-430") {
        addForeignKeyConstraint(baseColumnNames: "order_events_id", baseTableName: "order_event", constraintName: "FK74D92A693D2E628A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-431") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "order_event", constraintName: "FK74D92A69786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-432") {
        addForeignKeyConstraint(baseColumnNames: "destination_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F4CC49445", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-433") {
        addForeignKeyConstraint(baseColumnNames: "source_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F57563498", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-434") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location_group", constraintName: "FK7A19D7561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-435") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location_type_supported_activities", constraintName: "FK7AFF67F928F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-436") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-437") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-438") {
        addForeignKeyConstraint(baseColumnNames: "confirmed_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE3265A8A9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-439") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-440") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-441") {
        addForeignKeyConstraint(baseColumnNames: "incoming_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5F12AFED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-442") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE72A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-443") {
        addForeignKeyConstraint(baseColumnNames: "source_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE828481AF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-444") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB3FB7111", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-445") {
        addForeignKeyConstraint(baseColumnNames: "outgoing_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB80B3233", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-446") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "transaction", constraintName: "FK7FA0D2DED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-447") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-448") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_shipment", constraintName: "FK9475736B3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-449") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "order_shipment", constraintName: "FK9475736BB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-450") {
        addForeignKeyConstraint(baseColumnNames: "attribute_id", baseTableName: "product_attribute", constraintName: "FK94A534C47B0D087", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "attribute", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-451") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_attribute", constraintName: "FK94A534CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-452") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "synonym", constraintName: "FK98293BFB217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-453") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "synonym", constraintName: "FK98293BFB426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-454") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-455") {
        addForeignKeyConstraint(baseColumnNames: "shipment_workflow_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36EC587CFB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_workflow", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-456") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_category", constraintName: "FKA0303E4EEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-457") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "product_tag", constraintName: "FKA71CAC4A9740C85F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "tag", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-458") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_tag", constraintName: "FKA71CAC4ADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-459") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A49072882836", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-460") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490A27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-461") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490CA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-462") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490D1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-463") {
        addForeignKeyConstraint(baseColumnNames: "transaction_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD12EF4C7F4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-464") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD169DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-465") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-466") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-467") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-468") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-469") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-470") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-471") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "receipt_item", constraintName: "FKAE3064BADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-472") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-473") {
        addForeignKeyConstraint(baseColumnNames: "component_product_id", baseTableName: "product_component", constraintName: "FKB511C5AD20E351EA", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-474") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_component", constraintName: "FKB511C5AD24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-475") {
        addForeignKeyConstraint(baseColumnNames: "assembly_product_id", baseTableName: "product_component", constraintName: "FKB511C5ADFB4C199C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-476") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCD8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-477") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCDAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-478") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCDDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-479") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "inventory_level", constraintName: "FKC254A2E172A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-480") {
        addForeignKeyConstraint(baseColumnNames: "preferred_bin_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1CFDCB4DF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-481") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-482") {
        addForeignKeyConstraint(baseColumnNames: "replenishment_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1F07D879A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-483") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "shipment_comment", constraintName: "FKC398CCBAC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-484") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_dimension", constraintName: "FKC73E1616DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-485") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "consumption", constraintName: "FKCD71F39B8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-486") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "consumption", constraintName: "FKCD71F39BAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-487") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "consumption", constraintName: "FKCD71F39BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-488") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "product_document", constraintName: "FKD08A526BC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-489") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_document", constraintName: "FKD08A526BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-490") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-491") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-492") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "picklist", constraintName: "FKD3F8383F5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-493") {
        addForeignKeyConstraint(baseColumnNames: "picker_id", baseTableName: "picklist", constraintName: "FKD3F8383FA3E976BC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-494") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment_workflow", constraintName: "FKD584C4C4FF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-495") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "reference_number", constraintName: "FKD790DEBD154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-496") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2981CD3412D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-497") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2983B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-498") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-499") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29849AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-500") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29869DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-501") {
        addForeignKeyConstraint(baseColumnNames: "container_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2987400E88E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-502") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-503") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-504") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "shipment_workflow_container_type", constraintName: "FKDEF5AD1317A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-505") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipper_service", constraintName: "FKDF7559D73896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-506") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-507") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_item_shipment_items_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB42751E1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "fulfillment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-508") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "shipment", constraintName: "FKE139719A1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-509") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "shipment", constraintName: "FKE139719A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-510") {
        addForeignKeyConstraint(baseColumnNames: "carrier_id", baseTableName: "shipment", constraintName: "FKE139719A294C1012", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-511") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "shipment", constraintName: "FKE139719A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-512") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment", constraintName: "FKE139719A44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-513") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment", constraintName: "FKE139719A49AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-514") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "shipment", constraintName: "FKE139719A5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-515") {
        addForeignKeyConstraint(baseColumnNames: "shipment_method_id", baseTableName: "shipment", constraintName: "FKE139719AA28CC5FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_method", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-516") {
        addForeignKeyConstraint(baseColumnNames: "current_event_id", baseTableName: "shipment", constraintName: "FKE139719AD95ACF25", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-517") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "shipment", constraintName: "FKE139719ADBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-518") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment", constraintName: "FKE139719AFF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-519") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "order_document", constraintName: "FKE698D2ECC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-520") {
        addForeignKeyConstraint(baseColumnNames: "order_documents_id", baseTableName: "order_document", constraintName: "FKE698D2ECFE10118D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-521") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "picklist_item", constraintName: "FKE7584B1369DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-522") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "container", constraintName: "FKE7814C8117A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-523") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "container", constraintName: "FKE7814C813B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-524") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "container", constraintName: "FKE7814C8144979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-525") {
        addForeignKeyConstraint(baseColumnNames: "parent_container_id", baseTableName: "container", constraintName: "FKE7814C814B6A2E03", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-526") {
        addForeignKeyConstraint(baseColumnNames: "associated_product_id", baseTableName: "product_association", constraintName: "FKED441931C8653BC0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-527") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_association", constraintName: "FKED441931DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-528") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product", constraintName: "FKED8DCCEF217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-529") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product", constraintName: "FKED8DCCEF426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-530") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product", constraintName: "FKED8DCCEFABD88AC6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-531") {
        addForeignKeyConstraint(baseColumnNames: "default_uom_id", baseTableName: "product", constraintName: "FKED8DCCEFEEB2908D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-532") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product", constraintName: "FKED8DCCEFEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-533") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD447EBE106", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-534") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD494567276", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-535") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD4AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-536") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_supported_activities", constraintName: "FKF58372688ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-537") {
        addForeignKeyConstraint(baseColumnNames: "click_stream_id", baseTableName: "click_stream_request", constraintName: "FKFD8E50671A43AB29", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "click_stream", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-538") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item", constraintName: "FKFE019416DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-539") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "location_role", constraintName: "location_role_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-540") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "location_role", constraintName: "location_role_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-541") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_role", constraintName: "location_role_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-542") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-543") {
        addForeignKeyConstraint(baseColumnNames: "picklist_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "picklist", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-544") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-545") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-546") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }
}
