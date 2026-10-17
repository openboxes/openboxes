// Companion spec: extra features for upstream's ProductSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.product

import spock.lang.PendingFeature

import org.pih.warehouse.product.*
import grails.testing.gorm.DomainUnitTest
import java.time.Instant
import org.apache.commons.lang.NotImplementedException
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Document
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode

@Unroll
class ProductMoreSpec extends Specification implements DomainUnitTest<Product> {

    void setupSpec() {
        mockDomains(Product, ProductAssociation, Inventory, InventoryLevel, InventoryItem, UnitOfMeasure, ProductPackage,
                ProductAvailability, TransactionEntry, ShipmentItem)

        ApplicationTagLib gStub = Stub(ApplicationTagLib)
        Product.metaClass.getApplicationTagLib = { -> gStub }
    }

    void cleanupSpec() {
        GroovySystem.metaClassRegistry.removeMetaClass(Product)
    }

    void 'getImages should return only the documents with an image content type'() {
        given:
        Document image = new Document(name: 'image', contentType: 'image/png')
        Document pdf = new Document(name: 'pdf', contentType: 'application/pdf')
        domain.documents = [image, pdf] as Set

        expect:
        assert domain.images as List == [image]
    }

    void 'getThumbnail should return the oldest image of the product'() {
        given:
        Document newer = new Document(name: 'newer', contentType: 'image/png')
        newer.dateCreated = new Date(2000L)
        Document older = new Document(name: 'older', contentType: 'image/jpeg')
        older.dateCreated = new Date(1000L)
        Document pdf = new Document(name: 'pdf', contentType: 'application/pdf')
        pdf.dateCreated = new Date(0L)
        domain.documents = [newer, pdf, older] as Set

        expect:
        assert domain.thumbnail.is(older)
    }

    void 'getThumbnail should return null when the product has no documents'() {
        expect:
        assert domain.thumbnail == null
    }

    @PendingFeature(reason = 'OBAF getThumbnail calls first() on an empty image list')
    void 'getThumbnail should return null when none of the documents is an image'() {
        given:
        domain.documents = [new Document(name: 'pdf', contentType: 'application/pdf')] as Set

        expect:
        assert domain.thumbnail == null
    }

    @PendingFeature(reason = 'OBAF getImages NPEs on a document without a content type')
    void 'getImages should skip documents without a content type'() {
        given:
        Document image = new Document(name: 'image', contentType: 'image/png')
        domain.documents = [image, new Document(name: 'unknown')] as Set

        expect:
        assert domain.images as List == [image]
    }

    void 'getGenericProduct should return the product group that sorts first by name'() {
        given:
        ProductGroup groupB = new ProductGroup(name: 'B group')
        ProductGroup groupA = new ProductGroup(name: 'A group')
        domain.productGroups = [groupB, groupA] as Set

        expect:
        assert domain.genericProduct.is(groupA)
    }

    void 'getGenericProduct should return null when the product has no product groups'() {
        expect:
        assert domain.genericProduct == null
    }

    void 'getProductAttribute should return the product level attribute and ignore supplier specific ones'() {
        given:
        Attribute color = new Attribute(name: 'color')
        color.id = 'color'
        Attribute size = new Attribute(name: 'size')
        size.id = 'size'
        ProductAttribute supplierColor = new ProductAttribute(attribute: color, value: 'blue', productSupplier: new ProductSupplier())
        ProductAttribute productColor = new ProductAttribute(attribute: color, value: 'red')
        domain.attributes = [supplierColor, productColor]

        expect:
        assert domain.getProductAttribute(color).is(productColor)
        assert domain.getProductAttribute(size) == null
        assert domain.getProductAttribute(null) == null
    }

    void 'getInventoryItem should find inventory items by lot number and treat the default lot as no lot'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        InventoryItem lotItem = new InventoryItem(product: product, lotNumber: 'LOT1').save(validate: false)
        InventoryItem noLotItem = new InventoryItem(product: product, lotNumber: null).save(validate: false)

        expect:
        assert product.getInventoryItem('LOT1') == lotItem
        assert product.getInventoryItem('UNKNOWN') == null
        assert product.getInventoryItem(null) == noLotItem
        assert product.getInventoryItem('default') == noLotItem
        assert product.defaultInventoryItem == noLotItem
    }

    void 'getInventoryItem should fall back to the item with an empty lot number when there is no item without lot'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        InventoryItem emptyLotItem = new InventoryItem(product: product, lotNumber: '').save(validate: false)

        expect:
        assert product.getInventoryItem(null) == emptyLotItem
    }

    void 'getAbcClassification should prefer the classification of the inventory level at the location'() {
        given:
        Inventory inventory = new Inventory().save(validate: false)
        Location location = new Location(inventory: inventory).save(validate: false)
        Product product = new Product(abcClass: 'C').save(validate: false)
        new InventoryLevel(product: product, inventory: inventory, abcClass: levelAbcClass).save(validate: false)

        expect:
        assert product.getAbcClassification(location.id) == expectedAbcClass

        where:
        levelAbcClass || expectedAbcClass
        'A'           || 'A'
        null          || 'C'
    }

    void 'getAbcClassification and getStatus should not look up inventory levels for an unsaved product'() {
        given:
        domain.abcClass = 'B'

        expect:
        assert domain.getAbcClassification('any-location') == 'B'
        assert domain.getStatus('any-location', 10) == null
    }

    void 'getQuantityOnHand should not be implemented'() {
        when:
        domain.getQuantityOnHand(1)

        then:
        thrown(NotImplementedException)
    }

    void 'getProductPackage should return the package of the product with the given unit of measure code'() {
        given:
        UnitOfMeasure box = new UnitOfMeasure(code: 'BX', name: 'Box').save(validate: false)
        new UnitOfMeasure(code: 'CS', name: 'Case').save(validate: false)
        Product product = new Product(name: 'p').save(validate: false)
        ProductPackage boxPackage = new ProductPackage(product: product, uom: box, quantity: 10).save(validate: false)

        expect:
        assert product.getProductPackage('BX') == boxPackage
        assert product.getProductPackage('CS') == null
    }

    void 'associatedProducts should return the products of all associations while alternativeProducts only returns substitutes'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        Product substitute = new Product(name: 'substitute').save(validate: false)
        Product equivalent = new Product(name: 'equivalent').save(validate: false)
        new ProductAssociation(code: ProductAssociationTypeCode.SUBSTITUTE, product: product, associatedProduct: substitute).save(validate: false)
        new ProductAssociation(code: ProductAssociationTypeCode.EQUIVALENT, product: product, associatedProduct: equivalent).save(validate: false)

        expect:
        assert product.associatedProducts() == [substitute, equivalent] as Set
        assert product.alternativeProducts() == [substitute] as Set
        assert product.substitutions*.associatedProduct == [substitute]
    }

    void 'isValidSubstitution should only accept products associated as a substitute'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        Product substitute = new Product(name: 'substitute').save(validate: false)
        Product equivalent = new Product(name: 'equivalent').save(validate: false)
        new ProductAssociation(code: ProductAssociationTypeCode.SUBSTITUTE, product: product, associatedProduct: substitute).save(validate: false)
        new ProductAssociation(code: ProductAssociationTypeCode.EQUIVALENT, product: product, associatedProduct: equivalent).save(validate: false)

        expect:
        assert product.isValidSubstitution(substitute)
        assert !product.isValidSubstitution(equivalent)
    }

    void 'hasAssociatedTransactionEntriesOrShipmentItems should return false for a product without any activity'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        new InventoryItem(product: product, lotNumber: 'LOT1').save(validate: false)

        expect:
        assert product.hasAssociatedTransactionEntriesOrShipmentItems() == false
    }

    void 'hasAssociatedTransactionEntriesOrShipmentItems should return true when an inventory item has a transaction entry'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'LOT1').save(validate: false)
        new TransactionEntry(inventoryItem: inventoryItem, quantity: 1).save(validate: false)

        expect:
        assert product.hasAssociatedTransactionEntriesOrShipmentItems() == true
    }

    void 'hasAssociatedTransactionEntriesOrShipmentItems should return true when the product was shipped'() {
        given:
        Product product = new Product(name: 'p').save(validate: false)
        new ShipmentItem(product: product, quantity: 1).save(validate: false)

        expect:
        assert product.hasAssociatedTransactionEntriesOrShipmentItems() == true
    }

    void 'tagsToString should return the tags sorted and comma separated'() {
        given:
        domain.tags = [new Tag(tag: 'vaccine'), new Tag(tag: 'antibiotic')] as Set

        expect:
        assert domain.tagsToString() == 'antibiotic,vaccine'
        assert domain.hasTag('vaccine')
        assert !domain.hasTag('syringe')
    }

    void 'tagsToString should return an empty string when the product has no tags'() {
        expect:
        assert domain.tagsToString() == ''
    }

    void 'hasOneOfTags should return true only if the product has at least one of the given tags'() {
        given:
        Tag vaccine = new Tag(tag: 'vaccine')
        Tag antibiotic = new Tag(tag: 'antibiotic')
        Tag syringe = new Tag(tag: 'syringe')
        domain.tags = [vaccine, antibiotic] as Set

        expect:
        assert domain.hasOneOfTags([syringe, antibiotic])
        assert !domain.hasOneOfTags([syringe])
        assert !domain.hasOneOfTags([])
    }

    void 'hasOneOfCatalogs and productCatalogsToString should use the catalogs of the catalog items'() {
        given:
        ProductCatalog essentials = new ProductCatalog(name: 'Essentials')
        ProductCatalog cardiology = new ProductCatalog(name: 'Cardiology')
        ProductCatalog surgery = new ProductCatalog(name: 'Surgery')
        domain.productCatalogItems = [
                new ProductCatalogItem(productCatalog: essentials),
                new ProductCatalogItem(productCatalog: cardiology),
        ] as Set

        expect:
        assert domain.productCatalogsToString() == 'Cardiology,Essentials'
        assert domain.hasOneOfCatalogs([surgery, essentials])
        assert !domain.hasOneOfCatalogs([surgery])
    }

    void 'productCatalogsToString should return an empty string when the product is in no catalog'() {
        expect:
        assert domain.productCatalogsToString() == ''
    }

    void 'validateRequiredFields should succeed when the product type has no required fields'() {
        given:
        domain.productType = productType

        expect:
        assert domain.validateRequiredFields()
        assert !domain.errors.hasErrors()

        where:
        productType << [null, new ProductType(name: 'type')]
    }

    void 'validateRequiredFields should reject each missing required field that is not ignored'() {
        given:
        ProductType productType = new ProductType(name: 'type')
        productType.requiredFields = [ProductField.BRAND_NAME, ProductField.GL_ACCOUNT, ProductField.VENDOR_NAME] as Set
        domain.productType = productType
        domain.vendorName = 'vendor'

        expect:
        assert !domain.validateRequiredFields([ProductField.GL_ACCOUNT])
        assert domain.errors.getFieldError('brandName').code == 'default.null.message'
        assert domain.errors.getFieldError('glAccount') == null
        assert domain.errors.getFieldError('vendorName') == null
    }

    void 'validateRequiredFields should succeed when all required fields are set'() {
        given:
        ProductType productType = new ProductType(name: 'type')
        productType.requiredFields = [ProductField.BRAND_NAME, ProductField.GL_ACCOUNT] as Set
        domain.productType = productType
        domain.brandName = 'brand'
        domain.glAccount = new GlAccount(code: 'GL')

        expect:
        assert domain.validateRequiredFields()
    }

    void 'validateRequiredFieldsInLocation should require the GL account only where accounting is required'() {
        given:
        ProductType productType = new ProductType(name: 'type')
        productType.requiredFields = [ProductField.GL_ACCOUNT] as Set
        domain.productType = productType
        Location location = Stub(Location) {
            isAccountingRequired() >> accountingRequired
        }

        expect:
        assert domain.validateRequiredFieldsInLocation(location) == expectedResult

        where:
        accountingRequired || expectedResult
        true               || false
        false              || true
    }

    void 'compareTo should sort products by name and then by id'() {
        given:
        Product b = new Product(name: 'B')
        b.id = '1'
        Product a2 = new Product(name: 'A')
        a2.id = '2'
        Product a1 = new Product(name: 'A')
        a1.id = '1'

        expect:
        assert [b, a2, a1].sort() == [a1, a2, b]
    }

    void 'equals and hashCode should be based on the id'() {
        given:
        Product product = new Product(name: 'one')
        product.id = '1'
        Product sameId = new Product(name: 'other name')
        sameId.id = '1'
        Product otherId = new Product(name: 'one')
        otherId.id = '2'

        expect:
        assert product.equals(sameId)
        assert product.hashCode() == sameId.hashCode()
        assert !product.equals(otherId)
        assert !product.equals('1')
        assert product.toString() == 'one'
    }

    void 'getHandlingIcons should return one icon per handling flag'() {
        given:
        domain.coldChain = true
        domain.controlledSubstance = false
        domain.hazardousMaterial = true
        domain.reconditioned = true

        expect:
        assert domain.handlingIcons*.icon == ['fa-snowflake', 'fa-exclamation-triangle', 'fa-prescription-bottle']
        assert domain.handlingIcons*.color == ['#3bafda', '#ffa500', '#a9a9a9']
    }

    void 'getHandlingIcons should return no icons for a product without handling flags'() {
        expect:
        assert domain.handlingIcons == []
    }

    void 'getUoms should return the distinct unit of measure and quantity of the packages'() {
        given:
        UnitOfMeasure box = new UnitOfMeasure(code: 'BX')
        UnitOfMeasure caseUom = new UnitOfMeasure(code: 'CS')
        domain.packages = [
                new ProductPackage(name: 'box 1', uom: box, quantity: 10),
                new ProductPackage(name: 'box 2', uom: box, quantity: 10),
                new ProductPackage(name: 'case', uom: caseUom, quantity: 100),
        ] as Set

        expect:
        assert domain.uoms.size() == 2
        assert domain.uoms as Set == [[uom: 'BX', quantity: 10], [uom: 'CS', quantity: 100]] as Set
    }

    void 'toFullJson should reference associations by id and keep null ids for missing associations'() {
        given:
        Category category = new Category(name: 'category')
        category.id = 'category-id'
        domain.id = 'product-id'
        domain.name = 'product'
        domain.category = category
        domain.abcClass = 'A'
        domain.packageSize = 5
        domain.lotAndExpiryControl = true

        when:
        Map json = domain.toFullJson()

        then:
        assert json.id == 'product-id'
        assert json.name == 'product'
        assert json.category == [id: 'category-id']
        assert json.productType == [id: null]
        assert json.glAccount == [id: null]
        assert json.createdBy == [id: null]
        assert json.abcClass == 'A'
        assert json.packageSize == 5
        assert json.lotControl == true
        assert json.lotAndExpiryControl == true
    }
}
