databaseChangeLog = {

    changeSet(author: "drodzewicz", id: "270620241200-0", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "auto_allocated", tableName: "requisition_item")
            }
        }

        addColumn(tableName: "requisition_item") {
            column(name: "auto_allocated", type: "BIT", defaultValueBoolean: "true")
        }
    }

}
