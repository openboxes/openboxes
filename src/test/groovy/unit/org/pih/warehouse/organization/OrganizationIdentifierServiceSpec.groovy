package unit.org.pih.warehouse.organization

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.OrganizationIdentifierService

@Unroll
class OrganizationIdentifierServiceSpec extends Specification implements ServiceUnitTest<OrganizationIdentifierService>, DataTest {

    @Shared
    ConfigService configServiceStub

    void setupSpec() {
        mockDomains(Organization)
    }

    void setup() {
        configServiceStub = Stub(ConfigService)
        service.configService = configServiceStub
    }

    void 'generate should succeed when no other organizations exist with a matching code for reason: #reason'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.organization.minSize', Integer) >> minSize
        configServiceStub.getProperty('openboxes.identifier.organization.maxSize', Integer) >> maxSize

        expect:
        assert service.generate(name) == expectedCode

        where:
        minSize | maxSize | name                     || expectedCode            | reason
        // Verifying the default settings that we provide
        2       | 3       | "Really Big Bad Guys"    || "RBB"                   | "Too long, too many words becomes shortened acronym"
        2       | 3       | "Big Bad Guys"           || "BBG"                   | "Too long, acronym fits"
        2       | 3       | "Big Bad"                || "BB"                    | "Too long, fewer words becomes shorter acronym"
        2       | 3       | "Bigger"                 || "BIG"                   | "Too long word shortened"
        2       | 3       | "Big"                    || "BIG"                   | "Exact length word used as is"
        2       | 3       | "Bi"                     || "BI"                    | "Short enough word used as is"
        // Note that we can end up with codes that are shorter than the minimum! Should this be B0 instead?
        2       | 3       | "B"                      || "B"                     | "Too short word used as is"

        // Miscellaneous cases
        2       | 3       | ""                       || null                    | "Empty string is null"
        2       | 3       | null                     || null                    | "Null name is null"
        3       | 5       | "Big Bad"                || "BIGBA"                 | "Too few words merged and trimmed"
        3       | 5       | "Bigger, Inc."           || "BIGGE"                 | "Everything after comma ignored"
        3       | 5       | ", Inc."                 || null                    | "Starts with comma is null"
        3       | 5       | "Bi*&)+^g"               || "BIG"                   | "Special characters ignored"
        3       | 5       | "Big *"                  || "BIG"                   | "Special characters after space ignored"
        3       | 5       | "***"                    || null                    | "Special characters only is null"
        3       | 5       | "1hi12hi"                || "1HI12"                 | "Numbers allowed"
        3       | 5       | " HI "                   || "HI"                    | "Whitespace on ends is ignored"

        // OBPIH-7441: Verifying old PIH prod settings.
        1       | 20      | "ASuperReallyLongWordX"  || "ASUPERREALLYLONGWORD"  | "Too long single-word trimmed"
        1       | 20      | "Really Bigger Bad Guys" || "RBBG"                  | "Too long multi-word becomes acronym"
        1       | 20      | "Big"                    || "BIG"                   | "Single-word used as is"
        1       | 20      | "B"                      || "B"                     | "One letter word used as is"
    }

    void 'generate should succeed when other organizations exist with same code for reason: #reason'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.organization.minSize', Integer) >> minSize
        configServiceStub.getProperty('openboxes.identifier.organization.maxSize', Integer) >> maxSize

        and: 'an org already exists with the code'
        new Organization(code: service.generate(name)).save(validate: false)

        expect:
        assert service.generate(name) == expectedCode

        where:
        minSize | maxSize | name                     || expectedCode           | reason
        // Verifying the default settings that we provide
        2       | 3       | "Really Big Bad Guys"    || "RB0"                  | "Too long, too many words becomes shortened acronym"
        2       | 3       | "Big Bad Guys"           || "BB0"                  | "Too long, acronym fits"
        2       | 3       | "Big Bad"                || "BB0"                  | "Too long, fewer words becomes shorter acronym"
        2       | 3       | "Bigger"                 || "BI0"                  | "Too long word shortened"
        2       | 3       | "Big"                    || "BI0"                  | "Exact length word shortened"
        2       | 3       | "Bi"                     || "BI0"                  | "Short enough word used as is"
        2       | 3       | "B"                      || "B0"                   | "Too short word used as is"

        // OBPIH-7441: Verifying old PIH prod settings.
        1       | 20      | "ASuperReallyLongWordX"  || "ASUPERREALLYLONGWOR0" | "Too long single-word trimmed"
        1       | 20      | "Really Bigger Bad Guys" || "RBBG0"                | "Too long multi-word becomes acronym"
        1       | 20      | "Big Bad Guys"           || "BBG0"                 | "Multi-word becomes acronym"
        1       | 20      | "Bigger"                 || "BIGGER0"              | "Single-word used as is"
        1       | 20      | "Big"                    || "BIG0"                 | "Shorter single-word used as is"
        1       | 20      | "B"                      || "B0"                   | "Too short word word used as is"
    }

    @Ignore("See TODO in OrganizationIdentifierService. We need to handle this case better")
    void 'generate should handle the case when we already have 10 organizations with the same code'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.organization.minSize', Integer) >> 2
        configServiceStub.getProperty('openboxes.identifier.organization.maxSize', Integer) >> 3

        and: 'and ten organizations already exists with the code'
        String code = "BB"
        new Organization(code: code).save(validate: false)
        for (int i = 0; i < 10; i++) {
            new Organization(code: code + i).save(validate: false)
        }

        expect:
        assert service.generate("Big Bad") == "BB10"  // Right now this returns "B:" which is bad!
    }
}
