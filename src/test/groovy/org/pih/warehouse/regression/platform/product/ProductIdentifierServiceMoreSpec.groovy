// Companion spec: extra features for upstream's ProductIdentifierServiceSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.product

import spock.lang.PendingFeature

import org.pih.warehouse.product.*
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
class ProductIdentifierServiceMoreSpec extends Specification implements ServiceUnitTest<ProductIdentifierService>, DataTest {

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

    @PendingFeature(reason = 'OBAF returns a null product code when the next sequence value is taken')
    void 'a sequential product code that is taken already skips to the next sequence number'() {
        given: 'code-00016 was entered by hand'
        productTypeServiceStub.getAndSetNextSequenceNumber(_ as ProductType) >>> [16, 17]
        new Product(productCode: 'code-00016').save(validate: false)

        and:
        configServiceStub.getProperty('openboxes.identifier.attempts.max', Integer) >> 3
        configServiceStub.getProperty('openboxes.identifier.product.generatorType', IdentifierGeneratorTypeCode) >> IdentifierGeneratorTypeCode.SEQUENCE
        configServiceStub.getProperty('openboxes.identifier.product.sequence.format') >> "\${custom.productTypeCode}\${delimiter}\${sequenceNumber}"
        configServiceStub.getProperty('openboxes.identifier.product.delimiter', String) >> '-'
        configServiceStub.getProperty('openboxes.identifier.default.sequenceNumber.minSize', Integer) >> 5
        configServiceStub.getProperty('openboxes.identifier.product.properties', Map) >> null
        configServiceStub.getProperty('openboxes.productType.default.id') >> "1"

        and:
        ProductType productType = new ProductType(id: "1", productIdentifierFormat: null, code: "code").save(validate: false)

        expect:
        service.generate(new Product(productType: productType)) == "code-00017"
    }

}
