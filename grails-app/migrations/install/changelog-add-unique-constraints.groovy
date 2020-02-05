databaseChangeLog = { 

    changeSet(author: "jmiranda (generated)", id: "1580360689181-99") {
        addUniqueConstraint(columnNames: "date, location_id, product_id, inventory_item_id", constraintName: "inventory_item_snapshot_key", tableName: "inventory_item_snapshot")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-100") {
        addUniqueConstraint(columnNames: "date, location_id, product_code, lot_number, bin_location_name", constraintName: "inventory_snapshot_uniq_idx", tableName: "inventory_snapshot")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-101") {
        addUniqueConstraint(columnNames: "code, locale", constraintName: "localization_code_locale_idx", tableName: "localization")
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-102") {
        addUniqueConstraint(columnNames: "product_id, lot_number", constraintName: "product_id", tableName: "inventory_item")
    }
}
