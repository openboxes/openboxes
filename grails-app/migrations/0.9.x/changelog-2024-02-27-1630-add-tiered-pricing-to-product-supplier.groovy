databaseChangeLog = {
    changeSet(author: "openboxes (generated)", id: "1709054923255-0") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "tiered_pricing", tableName: "product_supplier")
            }
        }

        addColumn(tableName: "product_supplier") {
            column(name: "tiered_pricing", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }
}
