databaseChangeLog = {

    changeSet(author: "anadolny", id: "080520241110-0", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            columnExists(columnName: "picker_id", tableName: "picklist_item")
        }

        renameColumn(
                newColumnName: "pickedBy_id",
                oldColumnName: "picker_id",
                tableName: "picklist_item",
                columnDataType: "CHAR(38)"
        )
    }

    changeSet(author: "anadolny", id: "080520241110-1", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            foreignKeyConstraintExists(foreignKeyName: "fk_picklist_item_picker_id_person")
        }

        dropForeignKeyConstraint(baseTableName: "picklist_item", constraintName: "fk_picklist_item_picker_id_person")
    }

    changeSet(author: "anadolny", id: "080520241110-2", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                foreignKeyConstraintExists(foreignKeyName: "fk_picklist_item_pickedBy_id_person")
            }
        }

        addForeignKeyConstraint(
                baseColumnNames: "pickedBy_id",
                baseTableName: "picklist_item",
                constraintName: "fk_picklist_item_pickedBy_id_person",
                deferrable: "false",
                initiallyDeferred: "false",
                referencedColumnNames: "id",
                referencedTableName: "person"
        )
    }
}
