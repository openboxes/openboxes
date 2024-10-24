package unit.org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.identification.IdentifierGeneratorContext
import org.pih.warehouse.core.identification.RandomCondition
import org.pih.warehouse.core.identification.RandomIdentifierGenerator
import org.pih.warehouse.data.DataService
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceIdentifierService

/**
 * Test the generate methods of IdentifierService.
 *
 * Note that IdentifierService is an abstract class and so we need to use a concrete implementation in the tests.
 * InvoiceIdentifierService was chosen arbitrarily.
 */
@Unroll
class IdentifierServiceSpec extends Specification implements ServiceUnitTest<InvoiceIdentifierService>, DataTest {

    @Shared
    ConfigService configServiceStub

    void setupSpec() {
        mockDomain(Invoice)
    }

    void setup() {
        configServiceStub = Stub(ConfigService)
        service.configService = configServiceStub
    }

    void 'generate should return the format as provided if no keywords are specified'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "testing"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        expect:
        assert service.generate(new Invoice()) == "testing"
    }

    void 'generate should return #expectedIdentifier given a delimiter #delimiter and a format #format'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> format
        configServiceStub.getProperty('openboxes.identifier.invoice.delimiter', String) >> delimiter
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        expect:
        assert service.generate(new Invoice()) == expectedIdentifier

        where:
        format                                                   | delimiter || expectedIdentifier
        "\${delimiter}"                                          | "-"       || ""
        "x\${delimiter}\${delimiter}y"                           | "-"       || "x-y"
        "\${delimiter}\${delimiter}x"                            | "-"       || "x"
        "x\${delimiter}\${delimiter}"                            | "-"       || "x"
        "x\${delimiter} "                                        | "-"       || "x"
        " \${delimiter}x"                                        | "-"       || "x"
        "x\${delimiter}y"                                        | "."       || "x.y"
        "x\${delimiter}y"                                        | "_"       || "x_y"
        "x\${delimiter}y"                                        | " "       || "x y"
    }

    void 'generate should return #expectedIdentifier given a prefix #prefix and a suffix #suffix'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "hi"
        configServiceStub.getProperty('openboxes.identifier.invoice.delimiter', String) >> delimiter
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        when:
        String identifier = service.generate(new Invoice(), IdentifierGeneratorContext.builder()
                .prefix(prefix)
                .suffix(suffix)
                .build())
        then:
        assert identifier == expectedIdentifier

        where:
        prefix | suffix | delimiter || expectedIdentifier
        "pre"  | "post" | "-"       || "pre-hi-post"
        "pre"  | "post" | "."       || "pre.hi.post"
        null   | "post" | "-"       || "hi-post"
        "pre"  | null   | "-"       || "pre-hi"
        null   | null   | "-"       || "hi"
    }

    void 'generate should return #expectedIdentifier given a format #format with randomness in it'() {
        given:
        service.randomIdentifierGenerator = Stub(RandomIdentifierGenerator) {
            generate(_ as String) >> "ABC123"
        }

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> format
        configServiceStub.getProperty('openboxes.identifier.invoice.random.condition', RandomCondition) >> RandomCondition.ALWAYS
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        expect:
        assert service.generate(new Invoice()) == expectedIdentifier

        where:
        format                  || expectedIdentifier
        "\${random}"            || "ABC123"
        "x-\${random}"          || "x-ABC123"
        "\${random} \${random}" || "ABC123 ABC123"
    }

    void 'generate should add conditional randomness only if there are no duplicates without it'() {
        given: 'a not so random random'
        service.randomIdentifierGenerator = Stub(RandomIdentifierGenerator) {
            generate(_ as String) >> "-ABC123"
        }

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "x\${random}"
        configServiceStub.getProperty('openboxes.identifier.invoice.random.condition', RandomCondition) >> RandomCondition.ON_DUPLICATE
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        when:
        String identifier = service.generate(new Invoice())

        then: 'randomness is not needed'
        assert identifier == "x"

        when: 'we have an invoice that already exists with that id'
        new Invoice(invoiceNumber: "x").save(validate: false)

        then: 'randomness is now needed'
        assert service.generate(new Invoice()) == "x-ABC123"
    }

    void 'generate should return null if we exceed the number of retries'() {
        given: 'a not so random random'
        service.randomIdentifierGenerator = Stub(RandomIdentifierGenerator) {
            generate(_ as String) >> "ABC123"
        }

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "\${random}"
        configServiceStub.getProperty('openboxes.identifier.invoice.random.condition', RandomCondition) >> RandomCondition.ALWAYS
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        and: 'an invoice that already exists with that id'
        new Invoice(invoiceNumber: "ABC123").save(validate: false)

        expect:
        assert service.generate(new Invoice()) == null
    }

    void 'generate should prioritize an overridden format if one is provided'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "hi"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        when:
        String identifier = service.generate(new Invoice(), IdentifierGeneratorContext.builder()
                .formatOverride("bye")
                .build())

        then:
        assert identifier == "bye"
    }

    void 'generate should correctly apply custom keys'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "\${custom.x}"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        when:
        String identifier = service.generate(new Invoice(), IdentifierGeneratorContext.builder()
                .customProperties('x': 'thisIsCustom')
                .build())

        then:
        assert identifier == 'thisIsCustom'
    }

    void 'generate should fail with custom keys that were not provided'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "\${custom.x}"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        when:
        service.generate(new Invoice(), IdentifierGeneratorContext.builder()
                .customProperties('y': 'wrongKey')
                .build())

        then:
        thrown(IllegalArgumentException)
    }

    void 'generate should correctly apply entity properties'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "\${name}"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> ['name': 'name']

        and:
        service.dataService = Stub(DataService) {
            transformObject(_, _ as Map) >> ['name': 'myNameIs']
        }

        when:
        String identifier = service.generate(new Invoice(name: 'myNameIs'))

        then:
        assert identifier == 'myNameIs'
    }

    void 'generate should leave out entity properties that are not actually fields on the entity'() {
        given: 'a property that does not exist on the entity'
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> format
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> ['notName': 'name', 'name': 'name']
        configServiceStub.getProperty('openboxes.identifier.invoice.delimiter', String) >> '-'

        and: 'so the field lookup on the entity fails'
        service.dataService = Stub(DataService) {
            transformObject(_, _ as Map) >> ['notName': '', 'name': 'myNameIs']
        }

        when:
        String identifier = service.generate(new Invoice(name: 'myNameIs'))

        then:
        assert identifier == expectedIdentifier

        where:
        format                                 || expectedIdentifier
        "\${notName}"                          || ''
        "\${notName}\${delimiter}\${notName}"  || ''
        "\${notName}\${delimiter}\${name}"     || 'myNameIs'
    }

    void 'generate should fail on unknown keywords when no entity is provided'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "\${name}"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        when:
        service.generate(new Invoice())

        then:
        thrown(IllegalArgumentException)
    }

    void 'generate should prioritize feature format over default'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "x"
        configServiceStub.getProperty('openboxes.identifier.default.format', String) >> "y"
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        expect:
        assert service.generate(new Invoice()) == "x"
    }

    void 'generate should prioritize feature delimiters over default'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.invoice.format', String) >> "x\${delimiter}y"
        configServiceStub.getProperty('openboxes.identifier.default.delimiter', String) >> "-"
        configServiceStub.getProperty('openboxes.identifier.invoice.delimiter', String) >> "."
        configServiceStub.getProperty('openboxes.identifier.invoice.properties', Map) >> null

        expect:
        assert service.generate(new Invoice()) == "x.y"
    }
}
