package org.pih.warehouse.smoke.spec

import javax.sql.DataSource

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.smoke.spec.base.SmokeSpec

/**
 * Tests the charset-alignment changesets that guard the event log foreign keys
 * (0.9.x create-table-event-log / shipment-event-log / order-event-log).
 *
 * New tables inherit the database default charset, so on databases whose
 * referenced tables carry a different charset (converted over time on
 * long-lived installs) the FK adds fail with errno 150. The alignment
 * changesets convert each FK child column to whatever its referenced column
 * actually uses, and no-op everywhere else. The mechanism cases below build
 * scratch tables with deliberately mismatched collations, since the test
 * database itself is charset-uniform (see testcontainers/my.cnf).
 */
class MigrationCharsetAlignmentSpec extends SmokeSpec {

    private static final List<String> EVENT_LOG_FK_NAMES = [
            'fk_event_log_event',
            'fk_event_log_location',
            'fk_event_log_created_by',
            'fk_event_log_updated_by',
            'fk_shipment_event_log_shipment',
            'fk_shipment_event_log_event_log',
            'fk_order_event_log_order',
            'fk_order_event_log_event_log',
    ]

    @Autowired
    DataSource dataSource

    // A single cached connection so the SET @... session variables used by the
    // alignment SQL survive across statements, exactly as they do when
    // Liquibase runs a multi-statement <sql> changeset on one connection.
    Sql sql

    void setup() {
        sql = new Sql(dataSource.connection)
        dropScratchTables()
    }

    void cleanup() {
        try {
            dropScratchTables()
        } finally {
            sql?.close()
        }
    }

    private void dropScratchTables() {
        sql.execute('DROP TABLE IF EXISTS mcas_child')
        sql.execute('DROP TABLE IF EXISTS mcas_parent')
    }

    /**
     * Runs the same guarded statement sequence the alignment changesets use,
     * targeting a scratch child/parent column pair.
     */
    private void runAlignment(String childTable, String childColumn, String parentTable, String expectedShape, String nullability) {
        sql.execute("SET @ref_charset = (SELECT c.CHARACTER_SET_NAME FROM information_schema.COLUMNS c JOIN information_schema.TABLES t ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = '${parentTable}' AND c.COLUMN_NAME = 'id' AND t.TABLE_TYPE = 'BASE TABLE')".toString())
        sql.execute("SET @ref_collation = (SELECT c.COLLATION_NAME FROM information_schema.COLUMNS c JOIN information_schema.TABLES t ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = '${parentTable}' AND c.COLUMN_NAME = 'id' AND t.TABLE_TYPE = 'BASE TABLE')".toString())
        sql.execute("SET @cur_collation = (SELECT c.COLLATION_NAME FROM information_schema.COLUMNS c JOIN information_schema.TABLES t ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = '${childTable}' AND c.COLUMN_NAME = '${childColumn}' AND t.TABLE_TYPE = 'BASE TABLE')".toString())
        sql.execute("SET @cur_shape = (SELECT CONCAT(c.DATA_TYPE, ':', c.CHARACTER_MAXIMUM_LENGTH, ':', c.IS_NULLABLE) FROM information_schema.COLUMNS c JOIN information_schema.TABLES t ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = '${childTable}' AND c.COLUMN_NAME = '${childColumn}' AND t.TABLE_TYPE = 'BASE TABLE')".toString())
        sql.execute("SET @align_ddl = IF(@ref_charset IS NULL OR @ref_collation IS NULL OR @cur_collation IS NULL OR @cur_shape IS NULL OR @cur_collation = @ref_collation OR @cur_shape <> '${expectedShape}', 'SELECT 1', CONCAT('ALTER TABLE `${childTable}` MODIFY `${childColumn}` CHAR(38) CHARACTER SET ', @ref_charset, ' COLLATE ', @ref_collation, ' ${nullability}'))".toString())
        sql.execute('PREPARE align_stmt FROM @align_ddl')
        sql.execute('EXECUTE align_stmt')
        sql.execute('DEALLOCATE PREPARE align_stmt')
    }

    private String collationOf(String table, String column) {
        sql.firstRow("SELECT COLLATION_NAME AS c FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :t AND COLUMN_NAME = :c", [t: table, c: column]).c
    }

    void "event log migrations applied cleanly with all foreign keys and column definitions intact"() {
        when:
        List<String> fkNames = sql.rows("""
            SELECT CONSTRAINT_NAME AS name FROM information_schema.REFERENTIAL_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME IN ('${EVENT_LOG_FK_NAMES.join("','")}')
        """.toString())*.name

        List<Map> columns = sql.rows("""
            SELECT TABLE_NAME AS tbl, COLUMN_NAME AS col, DATA_TYPE AS dataType,
                   CHARACTER_MAXIMUM_LENGTH AS maxLength, IS_NULLABLE AS nullable
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME IN ('event_log', 'shipment_event_log', 'order_event_log')
              AND COLUMN_NAME IN ('event_id', 'location_id', 'created_by_id', 'updated_by_id',
                                  'shipment_id', 'order_id', 'event_log_id')
        """.toString())

        then:
        // The alignment changesets ran during boot migrations (no-op path on
        // this charset-uniform database) and every FK still got created.
        assert fkNames.toSet() == EVENT_LOG_FK_NAMES.toSet()

        // Column definitions were not altered by the alignment changesets.
        assert columns.every { it.dataType == 'char' && it.maxLength == 38 }
        assert columns.findAll { it.tbl == 'event_log' && it.col in ['event_id', 'location_id'] }.every { it.nullable == 'YES' }
        assert columns.findAll { !(it.tbl == 'event_log' && it.col in ['event_id', 'location_id']) }.every { it.nullable == 'NO' }
    }

    void "alignment converts a mismatched child column to the referenced column's charset so the FK add succeeds"() {
        given:
        // Parent deliberately differs from the database default charset.
        sql.execute('CREATE TABLE mcas_parent (id CHAR(38) NOT NULL PRIMARY KEY) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
        sql.execute('CREATE TABLE mcas_child (id CHAR(38) NOT NULL PRIMARY KEY, parent_id CHAR(38) NULL)')
        assert collationOf('mcas_child', 'parent_id') != collationOf('mcas_parent', 'id')

        when:
        runAlignment('mcas_child', 'parent_id', 'mcas_parent', 'char:38:YES', 'NULL')

        then:
        assert collationOf('mcas_child', 'parent_id') == collationOf('mcas_parent', 'id')

        when:
        sql.execute('ALTER TABLE mcas_child ADD CONSTRAINT fk_mcas_parent FOREIGN KEY (parent_id) REFERENCES mcas_parent (id)')

        then:
        noExceptionThrown()
    }

    void "alignment leaves the child column untouched when collations already match"() {
        given:
        sql.execute('CREATE TABLE mcas_parent (id CHAR(38) NOT NULL PRIMARY KEY)')
        sql.execute('CREATE TABLE mcas_child (id CHAR(38) NOT NULL PRIMARY KEY, parent_id CHAR(38) NULL)')
        String before = collationOf('mcas_child', 'parent_id')
        assert before == collationOf('mcas_parent', 'id')

        when:
        runAlignment('mcas_child', 'parent_id', 'mcas_parent', 'char:38:YES', 'NULL')

        then:
        assert collationOf('mcas_child', 'parent_id') == before
    }

    void "alignment no-ops when the child column does not have the expected definition"() {
        given:
        sql.execute('CREATE TABLE mcas_parent (id CHAR(38) NOT NULL PRIMARY KEY) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
        sql.execute('CREATE TABLE mcas_child (id CHAR(38) NOT NULL PRIMARY KEY, parent_id VARCHAR(38) NULL)')

        when:
        runAlignment('mcas_child', 'parent_id', 'mcas_parent', 'char:38:YES', 'NULL')

        then:
        // Drifted definition: left exactly as found, never rewritten.
        Map row = sql.firstRow("SELECT DATA_TYPE AS dataType, CHARACTER_MAXIMUM_LENGTH AS maxLength FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mcas_child' AND COLUMN_NAME = 'parent_id'")
        assert row.dataType == 'varchar' && row.maxLength == 38
    }
}
