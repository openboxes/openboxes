databaseChangeLog = {

    changeSet(author: "drodzewicz", id: "210620241200-0", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "pick_type", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(name: "pick_type", type: "CHAR(38)")
        }
    }

}
