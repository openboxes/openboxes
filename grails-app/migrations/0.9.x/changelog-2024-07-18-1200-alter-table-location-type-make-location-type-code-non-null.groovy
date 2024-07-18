databaseChangeLog = {

    changeSet(author: "ewaterman", id: "1721321391197-94", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            and {
                columnExists(columnName: "location_type_code", tableName: "location_type")

                sqlCheck("""SELECT COUNT(*) FROM location_type WHERE location_type_code IS NULL""", expectedResult: "0")
            }
        }

        addNotNullConstraint(columnDataType: "VARCHAR(100)", columnName: "location_type_code", tableName: "location_type")
    }
}
