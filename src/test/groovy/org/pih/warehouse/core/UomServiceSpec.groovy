package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier

@Unroll
class UomServiceSpec extends Specification implements ServiceUnitTest<UomService>, DataTest {

    UnitOfMeasure packageUom

    void setupSpec() {
        mockDomains(UnitOfMeasure, UnitOfMeasureClass, Product, ProductSupplier, ProductPackage)
    }

    void setup() {
        UnitOfMeasureClass currencyClass = new UnitOfMeasureClass(name: 'Currency', code: 'CURRENCY', type: UnitOfMeasureType.CURRENCY)
                .save(failOnError: true, flush: true)
        UnitOfMeasureClass packageClass = new UnitOfMeasureClass(name: 'Package', code: 'PACKAGE', type: UnitOfMeasureType.PACKAGE)
                .save(failOnError: true, flush: true)
        new UnitOfMeasure(name: 'US Dollar', code: 'USD', uomClass: currencyClass).save(failOnError: true, flush: true)
        new UnitOfMeasure(name: 'Euro', code: 'EUR', uomClass: currencyClass).save(failOnError: true, flush: true)
        packageUom = new UnitOfMeasure(name: 'Box', code: 'BX', uomClass: packageClass).save(failOnError: true, flush: true)
    }

    void 'getCurrencies should return only unit of measures of currency type'() {
        expect:
        service.getCurrencies()*.code as Set == ['USD', 'EUR'] as Set
    }

    void 'getUoms should return unit of measures of type #type'() {
        expect:
        service.getUoms(type)*.code as Set == expectedCodes as Set

        where:
        type                       || expectedCodes
        UnitOfMeasureType.CURRENCY || ['USD', 'EUR']
        UnitOfMeasureType.PACKAGE  || ['BX']
        UnitOfMeasureType.MASS     || []
        null                       || []
    }

    void 'getProductPackage should return #expectedPackageName for product #productCode, uom #uomCode and quantity #quantity'() {
        given:
        Product productWithPackages = new Product(productCode: 'P1').save(validate: false, flush: true)
        new Product(productCode: 'P2').save(validate: false, flush: true)
        new ProductPackage(name: 'Box of 10', product: productWithPackages, uom: packageUom, quantity: 10)
                .save(failOnError: true, flush: true)
        new ProductPackage(name: 'Box of 20', product: productWithPackages, uom: packageUom, quantity: 20)
                .save(failOnError: true, flush: true)

        when:
        ProductPackage productPackage = service.getProductPackage(Product.findByProductCode(productCode), UnitOfMeasure.findByCode(uomCode), quantity)

        then:
        productPackage?.name == expectedPackageName

        where:
        productCode | uomCode | quantity || expectedPackageName
        'P1'        | 'BX'    | 10       || 'Box of 10'
        'P1'        | 'BX'    | 20       || 'Box of 20'
        'P1'        | 'BX'    | 5        || null
        'P1'        | 'USD'   | 10       || null
        'P2'        | 'BX'    | 10       || null
    }

    void 'getProductPackage should not return package assigned to product supplier'() {
        given:
        Product product = new Product(productCode: 'P1').save(validate: false, flush: true)
        ProductSupplier productSupplier = new ProductSupplier(name: 'Supplier', product: product)
                .save(failOnError: true, flush: true)
        new ProductPackage(name: 'Supplier box of 10', product: product, uom: packageUom, quantity: 10, productSupplier: productSupplier)
                .save(failOnError: true, flush: true)

        expect:
        service.getProductPackage(product, packageUom, 10) == null
    }
}
