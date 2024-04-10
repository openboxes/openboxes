databaseChangeLog = {

    changeSet(author: "drodzewicz", id: "100420241200-0", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                sqlCheck("""
                    SELECT count(*)
                    FROM product_supplier
                    WHERE manufacturer_id = '';
                    """,
                    expectedResult: "0")
            }
        }

        sql("""
            UPDATE product_supplier
            SET manufacturer_id = NULL
            WHERE manufacturer_id = '';
            """,
            splitStatements: "true",
            stripComments: "false")
    }

    changeSet(author: "drodzewicz", id: "100420241200-1", objectQuotingStrategy: "LEGACY") {
        preConditions(onError: "HALT", onFail: "MARK_RAN", onSqlOutput: "IGNORE") {
            not {
                sqlCheck("""
                    SELECT count(*)
                    FROM product_supplier
                    WHERE supplier_id = '';
                    """,
                    expectedResult: "0")
            }
        }

        sql("""
            UPDATE product_supplier
            SET supplier_id = NULL
            WHERE supplier_id = '';
            """,
            splitStatements: "true",
            stripComments: "false")
    }
}
