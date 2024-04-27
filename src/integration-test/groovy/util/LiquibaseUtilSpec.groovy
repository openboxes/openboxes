package util

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import spock.lang.Specification

@Integration
@Rollback
class LiquibaseUtilSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "should return valid upgrade changelog versions"() {
        when:
        Set<String> changeLogVersions = LiquibaseUtil.upgradeChangeLogVersions
        then:
        changeLogVersions.containsAll(["0.5.x", "0.6.x", "0.7.x", "0.8.x"])
    }
}
