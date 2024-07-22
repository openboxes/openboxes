databaseChangeLog = {

    changeSet(author: "ewaterman", id: "180720241200-0", objectQuotingStrategy: "LEGACY") {

        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            columnExists(columnName: "location_type_code", tableName: "location_type")
        }

        // It is invalid behaviour for a location type to not have a code, so we shouldn't have any in production, but
        // we assign any existing ones an arbitrary code (in this case DEPOT) just in case. We could try to delete the
        // bad records instead, but this is the safer option in case those location types are accidentally being used.
        update(tableName: "location_type") {
            column(name: "location_type_code", value: "DEPOT")
            where("location_type IS NULL")
        }

        addNotNullConstraint(columnDataType: "VARCHAR(100)", columnName: "location_type_code", tableName: "location_type")
    }
}
