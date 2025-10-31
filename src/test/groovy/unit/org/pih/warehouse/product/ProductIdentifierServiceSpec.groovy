package unit.org.pih.warehouse.product

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.identification.RandomCondition
import org.pih.warehouse.core.identification.RandomIdentifierGenerator
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.product.ProductTypeService

@Unroll
class ProductIdentifierServiceSpec extends Specification implements ServiceUnitTest<ProductIdentifierService>, DataTest {

    @Shared
    ConfigService configServiceStub

    @Shared
    ProductTypeService productTypeServiceStub

    @Shared
    RandomIdentifierGenerator randomIdentifierGeneratorStub

    void setupSpec() {
        mockDomains(Product, ProductType)
    }

    void setup() {
        configServiceStub = Stub(ConfigService)
        service.configService = configServiceStub

        productTypeServiceStub = Stub(ProductTypeService)
        service.productTypeService = productTypeServiceStub

        randomIdentifierGeneratorStub = Stub(RandomIdentifierGenerator)
        service.randomIdentifierGenerator = randomIdentifierGeneratorStub
    }

    void 'generate should succeed for the default product type with a sequential and random override'() {
        given:
        productTypeServiceStub.getAndSetNextSequenceNumber(_ as ProductType) >> 16
        randomIdentifierGeneratorStub.generate("MNNNN-00016") >> "M1234-00016"

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.default.sequenceNumber.minSize', Integer) >> 5
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null
        configServiceStub.getProperty('openboxes.identifier.product.random.condition', RandomCondition) >> RandomCondition.ALWAYS
        configServiceStub.getProperty('openboxes.productType.default.id') >> "1"

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: "MNNNN-00000",
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        expect:
        assert service.generate(product) == "M1234-00016"
    }

    void 'generate should succeed for the default product type with a sequential number that exceeds the minSize'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null
        configServiceStub.getProperty('openboxes.productType.default.id') >> "1"
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.SEQUENCE
        configServiceStub.getProperty('openboxes.identifier.product.sequence.format') >> "\${sequenceNumber}"

        and: 'Min size is two digits'
        configServiceStub.getProperty('openboxes.identifier.default.sequenceNumber.minSize', Integer) >> 2

        and: 'Sequence number is at three digits'
        productTypeServiceStub.getAndSetNextSequenceNumber(_ as ProductType) >> 160

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: null,
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        expect:
        assert service.generate(product) == "160"
    }

    void 'generate should succeed for the default product type with no override'() {
        given:
        productTypeServiceStub.getAndSetNextSequenceNumber(_ as ProductType) >> 16

        and:
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.SEQUENCE
        configServiceStub.getProperty('openboxes.identifier.product.sequence.format') >> "\${custom.productTypeCode}\${delimiter}\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.identifier.product.delimiter', String) >> '-'
        configServiceStub.getProperty('openboxes.identifier.default.sequenceNumber.minSize', Integer) >> 5
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null
        configServiceStub.getProperty('openboxes.productType.default.id') >> "1"

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: null,
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        expect:
        assert service.generate(product) == "code-00016"
    }

    void 'generate should succeed for non-default products with a sequential and random override'() {
        given:
        productTypeServiceStub.getAndSetNextSequenceNumber(_ as ProductType) >> 16
        randomIdentifierGeneratorStub.generate("MNNNN-00016") >> "M1234-00016"

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.default.sequenceNumber.minSize', Integer) >> 5
        configServiceStub.getProperty('openboxes.identifier.product.random.condition', RandomCondition) >> RandomCondition.ALWAYS
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null
        configServiceStub.getProperty('openboxes.productType.default.id') >> "999"

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: "MNNNN-00000",
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        expect:
        assert service.generate(product) == "M1234-00016"
    }

    void 'generate should succeed for non-default products with a sequential number that exceeds the minSize'() {
        given:
        randomIdentifierGeneratorStub.generate("160") >> "160"

        and:
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null
        configServiceStub.getProperty('openboxes.productType.default.id') >> "999"
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.product.random.condition', RandomCondition) >> RandomCondition.ALWAYS

        and: 'Sequence number is at three digits'
        productTypeServiceStub.getAndSetNextSequenceNumber(_ as ProductType) >> 160

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: "00",  // Min size is two digits
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        expect:
        assert service.generate(product) == "160"
    }

    void 'generate should succeed for non-default products with no override'() {
        given:
        randomIdentifierGeneratorStub.generate("LLNN") >> "AB12"

        and:
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.RANDOM
        configServiceStub.getProperty('openboxes.identifier.product.random.format') >> "\${random}"
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 1
        configServiceStub.getProperty('openboxes.identifier.product.random.condition', RandomCondition) >> RandomCondition.ALWAYS
        configServiceStub.getProperty('openboxes.identifier.product.random.template', String) >> "LLNN"
        configServiceStub.getProperty('openboxes.productType.default.id') >> "999"
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: null,
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        expect:
        assert service.generate(product) == "AB12"
    }

    void 'generate should fail for non-default products with no override, a sequential format, and no type code'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.SEQUENCE
        configServiceStub.getProperty('openboxes.identifier.product.sequence.format') >> "\${custom.productTypeCode}\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.productType.default.id') >> "999"

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: null,
                code: null,
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        when:
        service.generate(product)

        then:
        thrown(IllegalArgumentException)
    }

    void 'generate should fail for products with no override if the random format contains sequence number'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.RANDOM
        configServiceStub.getProperty('openboxes.identifier.product.random.format') >> "\${random}-\${sequenceNumber}"

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: null,
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        when:
        service.generate(product)

        then:
        thrown(IllegalArgumentException)
    }

    void 'generate should fail for products with no override if the sequence format contains random'() {
        given:
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.SEQUENCE
        configServiceStub.getProperty('openboxes.identifier.product.sequence.format') >> "\${random}-\${sequenceNumber}"

        and:
        ProductType productType = new ProductType(
                id: "1",
                productIdentifierFormat: null,
                code: "code",
        ).save(validate:false)

        Product product = new Product(
                productType: productType,
        )

        when:
        service.generate(product)

        then:
        thrown(IllegalArgumentException)
    }
}
