package unit.org.pih.warehouse.order

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.IdentifierTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.identification.RandomCondition
import org.pih.warehouse.core.identification.RandomIdentifierGenerator
import org.pih.warehouse.data.DataService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.PurchaseOrderIdentifierService

@Unroll
class PurchaseOrderIdentifierServiceSpec extends Specification implements ServiceUnitTest<PurchaseOrderIdentifierService>, DataTest {

    @Shared
    ConfigService configServiceStub

    @Shared
    RandomIdentifierGenerator randomIdentifierGeneratorStub

    @Shared
    DataService dataServiceStub

    void setupSpec() {
        mockDomains(Order, Organization)
    }

    void setup() {
        configServiceStub = Stub(ConfigService)
        service.configService = configServiceStub

        randomIdentifierGeneratorStub = Stub(RandomIdentifierGenerator)
        service.randomIdentifierGenerator = randomIdentifierGeneratorStub

        dataServiceStub = Stub(DataService)
        service.dataService = dataServiceStub
    }

    void 'generate should succeed for random identifiers'() {
        given:
        randomIdentifierGeneratorStub.generate("NNNLLL") >> "123ABC"

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.properties', Map) >> null
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.random.template', String) >> "NNNLLL"
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.format', String) >> "\${random}"
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.random.condition', RandomCondition) >> RandomCondition.ALWAYS

        and:
        Order order = new Order(
                id: '1',
                name: 'name',
                description: 'description',
        )

        expect:
        assert service.generate(order) == "123ABC"
    }

    void 'generate should succeed for sequential identifiers'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.format', String) >> "PO-\${destinationPartyCode}-\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.properties', Map) >> ["destinationPartyCode": "destinationParty.code"]
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.sequenceNumber.minSize', Integer) >> 6
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1

        and:
        dataServiceStub.transformObject(_, _ as Map) >> ['destinationPartyCode': 'code']

        and:
        Organization organization = new Organization(
                id: '1',
                code: 'code',
                sequences: [(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString()): '16'],
        ).save(validate: false)

        Order order = new Order(
                id: '1',
                name: 'name',
                description: 'description',
                destinationParty: organization,
        )

        expect:
        assert service.generate(order) == "PO-code-000017"
    }

    void 'generate should succeed for sequential identifiers when we exceed the sequential number minSize'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.format', String) >> "PO-\${destinationPartyCode}-\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.properties', Map) >> ["destinationPartyCode": "destinationParty.code"]
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1

        and:
        dataServiceStub.transformObject(_, _ as Map) >> ['destinationPartyCode': 'code']

        and: 'Min size is two digits'
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.sequenceNumber.minSize', Integer) >> 2

        and: 'Sequence number is at three digits'
        Organization organization = new Organization(
                id: '1',
                code: 'code',
                sequences: [(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString()): '160'],
        ).save(validate: false)

        Order order = new Order(
                id: '1',
                name: 'name',
                description: 'description',
                destinationParty: organization,
        )

        expect:
        assert service.generate(order) == "PO-code-161"
    }

    void 'OBPIH-6821: generate should succeed for sequential identifiers given an empty sequence'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.format', String) >> "PO-\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.properties', Map) >> [:]
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.sequenceNumber.minSize', Integer) >> 6
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1

        and: 'an empty sequence number'
        Organization organization = new Organization(
                id: '1',
                code: 'code',
                sequences: [(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString()): ''],
        ).save(validate: false)

        Order order = new Order(
                id: '1',
                name: 'name',
                description: 'description',
                destinationParty: organization,
        )

        expect:
        assert service.generate(order) == "PO-000001"
    }

    void 'OBPIH-6821: generate should succeed for sequential identifiers when a number is already taken'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.format', String) >> "PO-\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.properties', Map) >> [:]
        configServiceStub.getProperty('openboxes.identifier.purchaseOrder.sequenceNumber.minSize', Integer) >> 6

        and: 'a limited number of retries'
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> maxAttepmts

        and: 'the sequence number we are on is already taken'
        Organization organization = new Organization(
                id: '1',
                code: 'code',
                sequences: [(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString()): '0'],
        ).save(validate: false)

        new Order(
                id: '1',
                name: 'name',
                description: 'description',
                destinationParty: organization,
                orderNumber: "PO-000001"  // This is the number we'll generate in our first attempt
        ).save(validate: false)

        Order order = new Order(
                id: '2',
                name: 'name2',
                description: 'description2',
                destinationParty: organization,
        )

        expect:
        assert service.generate(order) == expectedIdentifier

        where:
        maxAttepmts || expectedIdentifier
        1           || null
        2           || "PO-000002"
    }
}
