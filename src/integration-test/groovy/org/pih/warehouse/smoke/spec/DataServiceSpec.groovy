package org.pih.warehouse.smoke.spec

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.springframework.transaction.annotation.Isolation

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

import org.pih.warehouse.data.DataService
import org.pih.warehouse.smoke.spec.base.SmokeSpec

/**
 * Tests DataService.executeStatements() against a real database.
 *
 * DataService is pointed at a single connection for each test so we can inspect the session state
 * (foreign_key_checks, transaction isolation) of the exact connection it used. With the regular pool
 * we'd have no way of knowing which connection a statement ran on. See #6189.
 */
class DataServiceSpec extends SmokeSpec {

    @Autowired
    DataService dataService

    @Autowired
    DataSource dataSource

    DataSource originalDataSource
    Connection connection
    Sql sql

    void setup() {
        originalDataSource = dataService.dataSource
        connection = dataSource.connection
        dataService.dataSource = new SingleConnectionDataSource(connection, true)
        sql = new Sql(connection)

        sql.execute("DROP TABLE IF EXISTS data_service_spec_child")
        sql.execute("DROP TABLE IF EXISTS data_service_spec_parent")
        sql.execute("CREATE TABLE data_service_spec_parent (id INT PRIMARY KEY) ENGINE=InnoDB")
        sql.execute("""
            CREATE TABLE data_service_spec_child (
                id INT PRIMARY KEY,
                parent_id INT,
                FOREIGN KEY (parent_id) REFERENCES data_service_spec_parent (id)
            ) ENGINE=InnoDB
        """)
        sql.execute("INSERT INTO data_service_spec_parent (id) VALUES (1)")
    }

    void cleanup() {
        sql.execute("SET FOREIGN_KEY_CHECKS = 1")
        sql.execute("DROP TABLE IF EXISTS data_service_spec_child")
        sql.execute("DROP TABLE IF EXISTS data_service_spec_parent")
        dataService.dataSource = originalDataSource
        connection.close()
    }

    private Integer getForeignKeyChecks() {
        return sql.firstRow("SELECT @@foreign_key_checks AS value").value as Integer
    }

    private Integer countParents() {
        return sql.firstRow("SELECT count(*) AS value FROM data_service_spec_parent").value as Integer
    }

    void "executeStatements should fail to truncate a referenced table when foreign key checks are enabled"() {
        when:
        dataService.executeStatements(["TRUNCATE data_service_spec_parent"])

        then:
        thrown(SQLException)
        assert countParents() == 1
    }

    void "executeStatements should truncate a referenced table and re-enable foreign key checks"() {
        when:
        dataService.executeStatements(["TRUNCATE data_service_spec_parent"], Isolation.READ_COMMITTED, true)

        then:
        assert countParents() == 0
        assert foreignKeyChecks == 1
    }

    void "executeStatements should re-enable foreign key checks on the same connection when a statement fails"() {
        when:
        dataService.executeStatements([
                "TRUNCATE data_service_spec_parent",
                "TRUNCATE data_service_spec_table_that_does_not_exist"], Isolation.READ_COMMITTED, true)

        then:
        thrown(SQLException)
        assert foreignKeyChecks == 1
    }

    void "executeStatements should roll back the whole batch when a DML statement fails"() {
        when:
        dataService.executeStatements([
                "INSERT INTO data_service_spec_parent (id) VALUES (2)",
                "INSERT INTO data_service_spec_parent (id) VALUES (1)"])  // duplicate key

        then:
        thrown(SQLException)
        assert countParents() == 1
    }

    void "executeStatements should restore the previous transaction isolation after success and failure"() {
        given:
        connection.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ

        when:
        dataService.executeStatements(["INSERT INTO data_service_spec_parent (id) VALUES (2)"], Isolation.READ_COMMITTED)

        then:
        assert connection.transactionIsolation == Connection.TRANSACTION_REPEATABLE_READ

        when:
        dataService.executeStatements(["INSERT INTO data_service_spec_parent (id) VALUES (1)"], Isolation.READ_COMMITTED)

        then:
        thrown(SQLException)
        assert connection.transactionIsolation == Connection.TRANSACTION_REPEATABLE_READ
    }

    void "executeStatements should leave the transaction isolation alone when it already matches"() {
        given:
        connection.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED

        when:
        dataService.executeStatements(["INSERT INTO data_service_spec_parent (id) VALUES (2)"], Isolation.READ_COMMITTED)

        then:
        assert connection.transactionIsolation == Connection.TRANSACTION_READ_COMMITTED
        assert countParents() == 2
    }
}
