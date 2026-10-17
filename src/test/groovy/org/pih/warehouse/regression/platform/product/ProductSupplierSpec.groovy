package org.pih.warehouse.regression.platform.product

import org.pih.warehouse.product.*
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User

@Unroll
class ProductSupplierSpec extends Specification implements DomainUnitTest<ProductSupplier> {

    private static ProductPackage productPackage(String uomCode, Integer quantity, BigDecimal price, Date lastUpdated = null) {
        ProductPackage productPackage = new ProductPackage(
                name: "${uomCode}/${quantity}",
                uom: new UnitOfMeasure(code: uomCode),
                quantity: quantity,
                productPrice: price != null ? new ProductPrice(price: price) : null,
        )
        productPackage.lastUpdated = lastUpdated
        return productPackage
    }

    void 'getDefaultProductPackageDerived should return the most recently updated package'() {
        given:
        ProductPackage older = productPackage('BX', 10, 5.0, new Date(1000L))
        ProductPackage newer = productPackage('CS', 100, 40.0, new Date(2000L))
        domain.productPackages = [newer, older] as Set

        expect:
        assert domain.defaultProductPackageDerived.is(newer)
    }

    void 'getDefaultProductPackageDerived should return null when there are no packages'() {
        expect:
        assert domain.defaultProductPackageDerived == null
    }

    void 'getPackageSize should prefer the default package over the derived one'() {
        given:
        domain.defaultProductPackage = productPackage('BX', 10, null)
        domain.productPackages = [productPackage('CS', 100, null, new Date(1000L))] as Set

        expect:
        assert domain.packageSize == 'BX/10'
    }

    void 'getPackageSize should fall back to the most recently updated package'() {
        given:
        domain.productPackages = [
                productPackage('CS', 100, null, new Date(1000L)),
                productPackage('PK', 12, null, new Date(3000L)),
        ] as Set

        expect:
        assert domain.packageSize == 'PK/12'
    }

    void 'getPackageSize should return null when there is no package'() {
        expect:
        assert domain.packageSize == null
    }

    void 'getPackagePrice should round the price of the package to two decimals'() {
        given:
        domain.defaultProductPackage = productPackage('BX', 10, price)

        expect:
        assert domain.packagePrice == expectedPrice

        where:
        price   || expectedPrice
        12.345  || 12.35
        12.344  || 12.34
        7       || 7.00
        null    || 0.0
    }

    void 'getPackagePrice should use the derived package when there is no default package'() {
        given:
        domain.productPackages = [productPackage('CS', 100, 99.999, new Date(1000L))] as Set

        expect:
        assert domain.packagePrice == 100.00
    }

    void 'getPackagePrice should return zero when there is no package'() {
        expect:
        assert domain.packagePrice == 0.0
    }

    void 'getUnitPrice should divide the package price by the package quantity and round to two decimals'() {
        given:
        domain.defaultProductPackage = productPackage('BX', quantity, price)

        expect:
        assert domain.unitPrice == expectedUnitPrice

        where:
        price  | quantity || expectedUnitPrice
        10.00  | 3        || 3.33
        20.00  | 3        || 6.67
        50.00  | 1        || 50.00
        null   | 10       || 0.0
    }

    void 'getUnitPrice should use the derived package when there is no default package'() {
        given:
        domain.productPackages = [productPackage('CS', 4, 10.00, new Date(1000L))] as Set

        expect:
        assert domain.unitPrice == 2.50
    }

    void 'getUnitPrice should return zero when there is no package'() {
        expect:
        assert domain.unitPrice == 0.0
    }

    void 'getEachPrice should divide the rounded package price by the package quantity'() {
        given:
        domain.defaultProductPackage = productPackage('BX', 4, 10.00)

        expect:
        assert domain.eachPrice == 2.5
    }

    void 'getEachPrice should use the derived package quantity when there is no default package'() {
        given:
        domain.productPackages = [productPackage('CS', 8, 2.00, new Date(1000L))] as Set

        expect:
        assert domain.eachPrice == 0.25
    }

    void 'getEachPrice should return zero when the package has no price'() {
        given:
        domain.defaultProductPackage = productPackage('BX', 4, null)

        expect:
        assert domain.eachPrice == 0.0
    }

    void 'getEachPrice should return zero when there is no package'() {
        expect:
        assert domain.eachPrice == 0.0
    }

    void 'getGlobalProductSupplierPreference should return the preference without a destination party'() {
        given:
        ProductSupplierPreference partyPreference = new ProductSupplierPreference(destinationParty: new Organization(name: 'party'))
        ProductSupplierPreference globalPreference = new ProductSupplierPreference(destinationParty: null)
        domain.productSupplierPreferences = [partyPreference, globalPreference] as Set

        expect:
        assert domain.globalProductSupplierPreference.is(globalPreference)
    }

    void 'getGlobalProductSupplierPreference should return null when there is no global preference'() {
        given:
        domain.productSupplierPreferences = preferences

        expect:
        assert domain.globalProductSupplierPreference == null

        where:
        preferences << [
                null,
                [new ProductSupplierPreference(destinationParty: new Organization(name: 'party'))] as Set,
        ]
    }

    void 'getAttributes should return only the product attributes of this product supplier'() {
        given:
        domain.id = 'own'
        ProductSupplier otherSupplier = new ProductSupplier(name: 'other')
        otherSupplier.id = 'other'
        ProductAttribute ownAttribute = new ProductAttribute(value: 'own', productSupplier: domain)
        ProductAttribute otherAttribute = new ProductAttribute(value: 'other', productSupplier: otherSupplier)
        ProductAttribute productAttribute = new ProductAttribute(value: 'product')
        domain.product = new Product(name: 'product')
        domain.product.attributes = [ownAttribute, otherAttribute, productAttribute]

        expect:
        assert domain.attributes == [ownAttribute]
    }

    void 'compareTo should sort by rating, then by date created, then by id'() {
        given:
        ProductSupplier good = new ProductSupplier(ratingTypeCode: RatingTypeCode.GOOD)
        good.id = 'e'
        good.dateCreated = new Date(5000L)
        ProductSupplier outstanding = new ProductSupplier(ratingTypeCode: RatingTypeCode.OUTSTANDING)
        outstanding.id = 'z'
        outstanding.dateCreated = new Date(9000L)
        ProductSupplier poorOlder = new ProductSupplier(ratingTypeCode: RatingTypeCode.POOR)
        poorOlder.id = 'y'
        poorOlder.dateCreated = new Date(1000L)
        ProductSupplier poorNewerB = new ProductSupplier(ratingTypeCode: RatingTypeCode.POOR)
        poorNewerB.id = 'b'
        poorNewerB.dateCreated = new Date(2000L)
        ProductSupplier poorNewerA = new ProductSupplier(ratingTypeCode: RatingTypeCode.POOR)
        poorNewerA.id = 'a'
        poorNewerA.dateCreated = new Date(2000L)

        expect:
        assert [poorNewerB, poorOlder, good, poorNewerA, outstanding].sort() ==
                [outstanding, good, poorOlder, poorNewerA, poorNewerB]
    }

    void 'toJson should fall back to the derived package and render optional associations as null'() {
        given:
        ProductPackage derived = productPackage('CS', 4, 10.00, new Date(1000L))
        domain.id = 'ps-1'
        domain.name = 'source'
        domain.code = 'PS1'
        domain.product = new Product(name: 'product', productCode: 'P1')
        domain.product.id = 'p-1'
        domain.productPackages = [derived] as Set
        domain.ratingTypeCode = RatingTypeCode.FAIR

        when:
        Map json = domain.toJson()

        then:
        assert json.id == 'ps-1'
        assert json.product == [id: 'p-1', name: 'product', productCode: 'P1']
        assert json.defaultProductPackage.is(derived)
        assert json.packageSize == 'CS/4'
        assert json.packagePrice == 10.00
        assert json.unitPrice == 2.50
        assert json.ratingTypeCode == 'FAIR'
        assert json.supplier == null
        assert json.manufacturer == null
        assert json.contractPrice == null
        assert json.createdBy == null
        assert json.productSupplierPreferences == []
        assert json.active == true
        assert json.tieredPricing == false
    }

    void 'toJson should summarize the supplier, manufacturer, contract price and audit users'() {
        given:
        Date validUntil = new Date(86400000L)
        Organization supplier = new Organization(code: 'SUP', name: 'Supplier')
        supplier.id = 'sup-1'
        Organization manufacturer = new Organization(code: 'MAN', name: 'Maker')
        manufacturer.id = 'man-1'
        ProductPrice contractPrice = new ProductPrice(price: 3.5, toDate: validUntil)
        contractPrice.id = 'price-1'
        User createdBy = new User(firstName: 'Ann', lastName: 'Smith')
        createdBy.id = 'user-1'
        domain.product = new Product(name: 'product')
        domain.supplier = supplier
        domain.manufacturer = manufacturer
        domain.contractPrice = contractPrice
        domain.createdBy = createdBy
        domain.defaultProductPackage = productPackage('BX', 10, 25.00)

        when:
        Map json = domain.toJson()

        then:
        assert json.supplier == [id: 'sup-1', name: 'Supplier', code: 'SUP', displayName: 'SUP - Supplier']
        assert json.manufacturer == [id: 'man-1', name: 'Maker', displayName: 'MAN - Maker']
        assert json.contractPrice == [id: 'price-1', price: 3.5, validUntil: validUntil]
        assert json.createdBy.id == 'user-1'
        assert json.defaultProductPackage.is(domain.defaultProductPackage)
        assert json.packageSize == 'BX/10'
        assert json.unitPrice == 2.50
    }
}
