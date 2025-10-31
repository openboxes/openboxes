databaseChangeLog = {

    changeSet(author: "anadolny", id: "290420241230-0", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "quantity_picked", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(name: "quantity_picked", type: "INT")
        }
    }

    changeSet(author: "anadolny", id: "290420241230-2", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "date_picked", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(name: "date_picked", type: "DATETIME")
        }
    }

    changeSet(author: "anadolny", id: "290420241230-3", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "picker_id", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(name: "picker_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "anadolny", id: "290420241230-4", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                foreignKeyConstraintExists(foreignKeyName: "fk_picklist_item_picker_id_person")
            }
        }

        addForeignKeyConstraint(
                baseColumnNames: "picker_id",
                baseTableName: "picklist_item",
                constraintName: "fk_picklist_item_picker_id_person",
                deferrable: "false",
                initiallyDeferred: "false",
                referencedColumnNames: "id",
                referencedTableName: "person"
        )
    }
}
