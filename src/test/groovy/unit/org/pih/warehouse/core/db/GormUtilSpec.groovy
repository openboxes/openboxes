package unit.org.pih.warehouse.core.db

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Person
import org.pih.warehouse.core.db.GormUtil

@Unroll
class GormUtilSpec extends Specification implements DomainUnitTest<Person> {

    void 'GormUtil.sanitizeExecuteQueryArgs should return #expectedSqlArgs when given #givenSqlArgs'() {
        given: "a SQL query containing a single parameter 'x'"
        String sql = "SELECT z FROM Table t WHERE t.x = :x"

        expect:
        GormUtil.sanitizeExecuteQueryArgs(sql, givenSqlArgs) == expectedSqlArgs

        where:
        givenSqlArgs   || expectedSqlArgs
        null             || [:]
        [:]              || [:]
        [x: "x"]         || [x: "x"]
        [y: "y"]         || [:]
        [x: "x", y: "y"] || [x: "x"]
    }
}
