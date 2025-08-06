package unit.org.pih.warehouse.organization

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.apache.commons.lang.WordUtils
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

    @Ignore("Ignoring because we shouldn't test libraries, but it's useful to know the behaviour.")
    void 'WordUtils.abbreviate behaviour: #string becomes #abbreviatedString'() {
        expect:
        WordUtils.abbreviate(string, minSize, maxSize, null) == abbreviatedString

        where:
        minSize | maxSize | string                || abbreviatedString
        2       | 3       | "Really Big Bad Guys" || "Rea"
        2       | 3       | "Big Bad Guys"        || "Big"
        2       | 3       | "Big Bad"             || "Big"
        2       | 3       | "Bigger"              || "Big"
        2       | 3       | "Big"                 || "Big"
        2       | 3       | "Bi"                  || "Bi"
        // Note that if the input string is shorter than minSize, it is returned as is.
        2       | 3       | "B"                   || "B"
    }

    void 'generate should succeed when no other organizations exist with a matching code for reason: #reason'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.organization.minSize', Integer) >> minSize
        configServiceStub.getProperty('openboxes.identifier.organization.maxSize', Integer) >> maxSize

        expect:
        assert service.generate(name) == expectedCode

        where:
        minSize | maxSize | name                     || expectedCode | reason
        2       | 3       | "Really Big Bad Guys"    || "RBB"        | "Too long, too many words becomes shorted acronym"
        2       | 3       | "Big Bad Guys"           || "BBG"        | "Too long, exact num words becomes acronym"
        2       | 3       | "Big Bad"                || "BB"         | "Too long, fewer words becomes shorter acronym"
        2       | 3       | "Bigger"                 || "BIG"        | "Too long word shortened"
        2       | 3       | "Big"                    || "BIG"        | "Exact length word used as is"
        2       | 3       | "Bi"                     || "BI"         | "Short enough word used as is"
        // Note that we can end up with codes that are shorter than the minimum! Should this be B0 instead?
        2       | 3       | "B"                      || "B"          | "Too short word used as is"
        2       | 3       | ""                       || ""           | "Empty string used as is"
        // OBPIH-7441: Verifying PIH prod settings. Note that having minSize == 1 means we always take the acronym
        1       | 20      | "Reallybiggerbadguys"    || "R"          | "Too long single-word becomes acronym (one letter)"
        1       | 20      | "Really Bigger Bad Guys" || "RBBG"       | "Too long multi-word becomes acronym"
        1       | 20      | "Big Bad Guys"           || "BBG"        | "multi-word becomes acronym"
        1       | 20      | "Big"                    || "B"          | "Single-word becomes acronym (one letter)"
        1       | 20      | "B"                      || "B"          | "One letter word used as is"
    }

    void 'generate should succeed for sequential identifiers when we exceed the sequential number minSize'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.organization.minSize', Integer) >> minSize
        configServiceStub.getProperty('openboxes.identifier.organization.maxSize', Integer) >> maxSize

        and: 'an org already exists with the code'
        new Organization(code: service.generate(name)).save(validate: false)

        expect:
        assert service.generate(name) == expectedCode

        where:
        minSize | maxSize | name                     || expectedCode | reason
        2       | 3       | "Really Big Bad Guys"    || "RB0"        | "Too long, too many words becomes shorted acronym"
        2       | 3       | "Big Bad Guys"           || "BB0"        | "Too long, exact num words becomes acronym"
        2       | 3       | "Big Bad"                || "BB0"        | "Too long, fewer words becomes shorter acronym"
        2       | 3       | "Bigger"                 || "BI0"        | "Too long word shortened"
        2       | 3       | "Big"                    || "BI0"        | "Exact length word used as is"
        2       | 3       | "Bi"                     || "BI0"        | "Short enough word used as is"
        // Note that we can end up with codes that are shorter than the minimum! Should this be B0 instead?
        2       | 3       | "B"                      || "B0"         | "Too short word used as is"
        // OBPIH-7441: Verifying PIH prod settings. Note that having minSize == 1 means we always take the acronym
        1       | 20      | "Reallybiggerbadguys"    || "R0"         | "Too long single-word becomes acronym (one letter)"
        1       | 20      | "Really Bigger Bad Guys" || "RBBG0"      | "Too long multi-word becomes acronym"
        1       | 20      | "Big Bad Guys"           || "BBG0"       | "multi-word becomes acronym"
        1       | 20      | "Big"                    || "B0"         | "Single-word becomes acronym (one letter)"
        1       | 20      | "B"                      || "B0"         | "One letter word used as is"
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
