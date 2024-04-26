databaseChangeLog = {

    changeSet(author: "anadolny", id: "260420241200-0", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            and {
                not {
                    columnExists(columnName: "quantity_picked", tableName: "picklist_item")
                }

                columnExists(columnName: "quantity", tableName: "picklist_item")
            }
        }

        renameColumn(
                newColumnName: "quantity_picked",
                oldColumnName: "quantity",
                tableName: "picklist_item",
                columnDataType: "INT",
        )
    }

    changeSet(author: "anadolny", id: "260420241200-1", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "quantity", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(defaultValueNumeric: "0", name: "quantity", type: "INT")
        }

        update(tableName: "picklist_item") {
            column(name: "quantity", valueComputed: "quantity_picked")
        }
    }

    changeSet(author: "anadolny", id: "260420241200-2", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "date_picked", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(name: "date_picked", type: "DATETIME")
        }
    }

    changeSet(author: "anadolny", id: "260420241200-3", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                columnExists(columnName: "picker_id", tableName: "picklist_item")
            }
        }

        addColumn(tableName: "picklist_item") {
            column(name: "picker_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "anadolny", id: "260420241200-4", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                foreignKeyConstraintExists(foreignKeyName: "fk_picker_person")
            }
        }

        addForeignKeyConstraint(
                baseColumnNames: "picker_id",
                baseTableName: "picklist_item",
                constraintName: "fk_picker_person",
                deferrable: "false",
                initiallyDeferred: "false",
                referencedColumnNames: "id",
                referencedTableName: "person"
        )
    }
}
