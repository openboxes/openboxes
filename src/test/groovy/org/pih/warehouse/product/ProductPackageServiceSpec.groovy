package org.pih.warehouse.product

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.UnitOfMeasure

@Unroll
class ProductPackageServiceSpec extends Specification implements ServiceUnitTest<ProductPackageService>, DataTest {

    @Shared
    Date validUntil = new Date()

    @Shared
    Date expiredValidUntil = new Date() - 30

    UnitOfMeasure boxUom

    void setupSpec() {
        mockDomains(ProductSupplier)
    }

    void setup() {
        service.productPackageGormService = Mock(ProductPackageDataService)
        boxUom = new UnitOfMeasure(name: 'Box', code: 'BX')
    }

    void 'save should create default product package when product supplier has no package with given uom and quantity'() {
        given:
        Product product = new Product()
        ProductSupplier productSupplier = new ProductSupplier(product: product)
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: boxUom,
                productPackageQuantity: 10,
                minOrderQuantity: 5,
                tieredPricing: true,
        )

        when:
        ProductSupplier savedProductSupplier = service.save(command)

        then:
        assert savedProductSupplier.minOrderQuantity == 5
        assert savedProductSupplier.tieredPricing
        ProductPackage defaultProductPackage = savedProductSupplier.defaultProductPackage
        assert savedProductSupplier.productPackages == [defaultProductPackage] as Set
        assert defaultProductPackage.name == 'BX/10'
        assert defaultProductPackage.description == 'Box of 10'
        assert defaultProductPackage.product == product
        assert defaultProductPackage.uom == boxUom
        assert defaultProductPackage.quantity == 10
    }

    void 'save should reuse existing product package when product supplier already has package with given uom and quantity'() {
        given:
        ProductSupplier productSupplier = new ProductSupplier()
        ProductPackage existingPackage = new ProductPackage(uom: boxUom, quantity: 10)
        productSupplier.addToProductPackages(existingPackage)
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: boxUom,
                productPackageQuantity: 10,
        )

        when:
        ProductSupplier savedProductSupplier = service.save(command)

        then:
        assert savedProductSupplier.productPackages == [existingPackage] as Set
    }

    void 'save should create new product package when product supplier has package with same uom but different quantity'() {
        given:
        ProductSupplier productSupplier = new ProductSupplier()
        ProductPackage existingPackage = new ProductPackage(uom: boxUom, quantity: 20)
        productSupplier.addToProductPackages(existingPackage)
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: boxUom,
                productPackageQuantity: 10,
        )

        when:
        ProductSupplier savedProductSupplier = service.save(command)

        then:
        ProductPackage defaultProductPackage = savedProductSupplier.defaultProductPackage
        assert defaultProductPackage != existingPackage
        assert defaultProductPackage.quantity == 10
        assert savedProductSupplier.productPackages == [existingPackage, defaultProductPackage] as Set
    }

    void 'save should set package price to #expectedPrice when existing price is #existingPrice and given price is #givenPrice'() {
        given:
        ProductSupplier productSupplier = new ProductSupplier()
        ProductPackage existingPackage = new ProductPackage(
                uom: boxUom,
                quantity: 10,
                productPrice: existingPrice != null ? new ProductPrice(price: existingPrice) : null,
        )
        productSupplier.addToProductPackages(existingPackage)
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: boxUom,
                productPackageQuantity: 10,
                productPackagePrice: givenPrice,
        )

        when:
        service.save(command)

        then:
        assert existingPackage.productPrice?.price == expectedPrice

        where:
        existingPrice | givenPrice || expectedPrice
        null          | null       || null
        null          | 5.0        || 5.0
        3.0           | 5.0        || 5.0
        3.0           | null       || 3.0
    }

    void 'save should set contract price to #expectedPrice and valid until to #givenValidUntil when existing contract price is #existingPrice'() {
        given:
        ProductSupplier productSupplier = new ProductSupplier(
                contractPrice: existingPrice != null ? new ProductPrice(price: existingPrice) : null,
        )
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: boxUom,
                productPackageQuantity: 10,
                contractPricePrice: givenPrice,
                contractPriceValidUntil: givenValidUntil,
        )

        when:
        ProductSupplier savedProductSupplier = service.save(command)

        then:
        assert savedProductSupplier.contractPrice?.price == expectedPrice
        assert savedProductSupplier.contractPrice?.toDate == givenValidUntil

        where:
        existingPrice | givenPrice | givenValidUntil   || expectedPrice
        null          | null       | null              || null
        3.0           | null       | null              || null
        null          | 10.0       | null              || 10.0
        null          | null       | validUntil        || null
        3.0           | 10.0       | validUntil        || 10.0
        null          | 10.0       | expiredValidUntil || 10.0
    }

    void 'delete should delete product package by id using data service'() {
        given:
        ProductPackage productPackage = new ProductPackage()
        productPackage.id = '1'

        when:
        service.delete(productPackage)

        then:
        1 * service.productPackageGormService.delete('1')
    }

    void 'delete should delete every given product package using data service'() {
        given:
        ProductPackage firstPackage = new ProductPackage()
        firstPackage.id = '1'
        ProductPackage secondPackage = new ProductPackage()
        secondPackage.id = '2'
        Set<ProductPackage> productPackages = [firstPackage, secondPackage] as Set

        when:
        service.delete(productPackages)

        then:
        1 * service.productPackageGormService.delete('1')
        1 * service.productPackageGormService.delete('2')
    }
}
