package install

databaseChangeLog = {

    changeSet(author: "openboxes (generated)", id: "1700664714834-132") {
        addUniqueConstraint(columnNames: "date, location_id, product_id, inventory_item_id", constraintName: "inventory_item_snapshot_key", tableName: "inventory_item_snapshot")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-133") {
        addUniqueConstraint(columnNames: "date, location_id, product_code, lot_number, bin_location_name", constraintName: "inventory_snapshot_uniq_idx", tableName: "inventory_snapshot")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-134") {
        addUniqueConstraint(columnNames: "code, locale", constraintName: "localization_code_locale_idx", tableName: "localization")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-135") {
        addUniqueConstraint(columnNames: "location_id, product_code, lot_number, bin_location_name", constraintName: "product_availability_uniq_idx", tableName: "product_availability")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-136") {
        addUniqueConstraint(columnNames: "product_id, lot_number", constraintName: "product_id", tableName: "inventory_item")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-137") {
        addUniqueConstraint(columnNames: "product_id, product_supplier_id, uom_id, quantity", constraintName: "product_package_uniq_idx", tableName: "product_package")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-788") {
        addUniqueConstraint(columnNames: "code", constraintName: "party_type_code_uniq_idx", tableName: "party_type")
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-789") {
        addUniqueConstraint(columnNames: "code", constraintName: "code_idx", tableName: "product_catalog")
    }
}
