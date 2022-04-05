/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.product

import grails.util.Holders
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.apache.commons.lang.NotImplementedException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventorySnapshotEvent
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.shipping.ShipmentItem

@Schema(description = '''\
A product is an instance of a generic. For instance, the product might be
Ibuprofen, but the product [sic.] is Advil 200mg.

We *only* track products and lots in the warehouse. Generics help us report
on product availability across a generic product like Ibuprofen no matter
what size or shape it is.

We will just support "1 unit" for now. Variations of products will eventually
be stored as product variants (e.g. a 200 count bottle of 20 mg tablets vs a
50 count bottle of 20 mg tablets will both be stored as 20 mg tablets).'''
)
class Product implements Comparable, Serializable {

    @Hidden
    def beforeInsert = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                createdBy = currentUser
                updatedBy = currentUser
            }
        }
    }

    @Hidden
    def beforeUpdate = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                updatedBy = currentUser
            }
        }
    }

    @Hidden
    def publishPersistenceEvent = {
        Holders.grailsApplication.mainContext.publishEvent(new InventorySnapshotEvent(this))
    }

    @Hidden
    def afterInsert = publishPersistenceEvent
    @Hidden
    def afterUpdate = publishPersistenceEvent
    @Hidden
    def afterDelete = publishPersistenceEvent

    @Schema(
        accessMode = Schema.AccessMode.READ_ONLY,
        description = "database identifier, may be uuid or numeric string",
        format = "uuid",
        required = true
    )
    String id

    @Schema(
        description = "name of the product",
        maxLength = 255,
        required = false  // FIXME should be true, but breaks swagger client
    )
    String name

    @Schema(
        description = "a more detailed description of the product",
        maxLength = 255,
        nullable = true
    )
    String description

    @Schema(
        description = "internal product code identifier (or SKU)",
        externalDocs = @ExternalDocumentation(url = "http://en.wikipedia.org/wiki/Stock_keeping_unit"),
        nullable = true
    )
    String productCode

    @Hidden
    @Schema(description = "type of product (good, service, fixed asset)")
    ProductType productType

    @Hidden
    @Schema(description = "price per unit (global for the entire system)", nullable = true)
    BigDecimal pricePerUnit

    // FIXME what's the difference between cost and price here?
    @Hidden
    @Schema(description = "cost per unit", nullable = true)
    BigDecimal costPerUnit

    @Hidden
    @Schema(
        description = "whether the product is a controlled substance",
        externalDocs = @ExternalDocumentation(url = "http://en.wikipedia.org/wiki/Controlled_Substances_Act"),
        nullable = true
    )
    Boolean controlledSubstance = Boolean.FALSE

    @Hidden
    @Schema(
        description = "whether the product is a hazardous material",
        externalDocs = @ExternalDocumentation(url = "http://www.fmcsa.dot.gov/facts-research/research-technology/visorcards/yellowcard.pdf"),
        nullable = true
    )
    Boolean hazardousMaterial = Boolean.FALSE

    @Hidden
    @Schema(description = '''\
            Indicates whether the product is active. Setting a product as
            inactive is similar to removing from the database (it cannot be
            used if it is not active). However, we may not want to delete the
            product, because it may be referenced in existing transactions,
            shipments, requisitions, inventory items, etc.''')
    Boolean active = Boolean.TRUE

    @Hidden
    @Schema(
        description = "whether the product requires temperature-controlled supply chain",
        externalDocs = @ExternalDocumentation(url = "http://en.wikipedia.org/wiki/Cold_chain"),
        nullable = true
    )
    Boolean coldChain = Boolean.FALSE

    // Allows tracking of inventory by serial number (includes products such as computers and electronics).
    // A serialized product has a qty = 1 per serial number
    @Hidden
    Boolean serialized = Boolean.FALSE

    // Allows tracking of inventory by lot or batch number (primary medicines and medical suppies)
    // http://docs.oracle.com/cd/A60725_05/html/comnls/us/inv/lotcntrl.htm
    @Hidden
    Boolean lotControl = Boolean.TRUE

    // Used to indicate that the product is an essential med (as defined by the WHO, MSPP, or PIH).
    // WHO Model Lists of Essential Medicines - http://www.who.int/medicines/publications/essentialmedicines/en/
    @Hidden
    Boolean essential = Boolean.TRUE

    // Used to indicate that the product is to be reconditioned
    @Hidden
    Boolean reconditioned = Boolean.FALSE

    @Hidden
    @Schema(description = "primary category", required = true)
    Category category

    @Hidden
    @Schema(description = "default ABC classification", nullable = true)
    String abcClass

    // For better or worse, unit of measure and dosage form are used somewhat interchangeably
    // (e.g. each, tablet, pill, bottle, box)
    // http://help.sap.com/saphelp_45b/helpdata/en/c6/f83bb94afa11d182b90000e829fbfe/content.htm
    @Hidden
    @Schema(
        description = '''\
            for better or worse, unit of measure and dosage form are used
            somewhat interchangeably (e.g. each, tablet, pill, bottle, box)
            ''',
        externalDocs = @ExternalDocumentation(url = "http://help.sap.com/saphelp_45b/helpdata/en/c6/f83bb94afa11d182b90000e829fbfe/content.htm"),
        nullable = true
    )
    String unitOfMeasure

    // The default unit of measure used for this product
    // Not used at the moment, but this should actually be separated into multiple field
    // stockkeeping UoM, purchasing UoM, shipping UoM, dispensing UoM, requisition UoM,
    // issuing UoM, and reporting UoM.
    @Hidden
    UnitOfMeasure defaultUom
    // UnitOfMeasure shippingUom
    // UnitOfMeasure UoM
    // UnitOfMeasure issuingUom

    @Hidden
    @Schema(
        description = "universal product code identifier (UPC)",
        externalDocs = @ExternalDocumentation(url = "http://en.wikipedia.org/wiki/Universal_Product_Code"),
        nullable = true
    )
    String upc

    @Hidden
    @Schema(
        description = "national drug code identifier (NDC)",
        externalDocs = @ExternalDocumentation(url = "http://en.wikipedia.org/wiki/National_Drug_Code"),
        nullable = true
    )
    String ndc

    @Hidden
    @Schema(description = "manufacturer name", maxLength = 255, nullable = true)
    String manufacturer

    @Hidden
    @Schema(description = "manufacturer's product name or catalog code", maxLength = 255, nullable = true)
    String manufacturerCode

    @Hidden
    @Schema(description = "what the manufacturer calls the product", maxLength = 255, nullable = true)
    String manufacturerName

    @Hidden
    @Schema(description = "manufacturer's brand name for the product", maxLength = 255, nullable = true)
    String brandName

    @Hidden
    @Schema(description = "manufacturer's model number (OK to contain letters)'", maxLength = 255, nullable = true)
    String modelNumber

    @Hidden
    @Schema(description = "vendor name", maxLength = 255, nullable = true)
    String vendor

    @Hidden
    @Schema(description = "vendor's product name or catalog code", maxLength = 255, nullable = true)
    String vendorCode
    @Hidden
    @Schema(description = "what the vendor calls the product", maxLength = 255, nullable = true)
    String vendorName

    // Almost all products will have a packageSize = 1
    // The product package association *should* be used to represent packages.
    // However, there are cases (packdowns) in which we need to create the
    // each-level product as well as a second product to handle the package
    // size because the system does not currently support quantities at
    // multiple levels, so we'll need to convert from the EA product to the
    // product with a packageSize > 1.
    @Hidden
    Integer packageSize = 1

    // Figure out how we want to handle multiple names
    /*
    String genericName				// same as the non-proprietary name
    String proprietaryName			// a brand name or trademark under which a proprietary product is marketed
    String nonProprietaryName		// a short name coined for a drug or
                                    // chemical not subject to proprietary (trademark)
                                    // rights and recommended or recognized by an official body
    String pharmacyEquivalentName	// PEN a shortened name for a drug or combination of drugs;
                                    // when used for a combination of drugs, the term usually
                                    // consists of the prefix co- plus an abbreviation for each
                                    // drug in the combination.
    */

    //String route
    //String dosageForm

    // Associations

    // Custom product attributes
    @Hidden
    List attributes = new ArrayList()

    // Secondary categories (currently not used)
    @Hidden
    List categories = new ArrayList()

    // List of product components - bill of materials
    @Hidden
    List productComponents

    @Hidden
    GlAccount glAccount

    // Auditing
    @Hidden
    Date dateCreated
    @Hidden
    Date lastUpdated
    @Hidden
    User createdBy
    @Hidden
    User updatedBy

    @Hidden
    String productColor

    static transients = ["rootCategory",
                         "categoriesList",
                         "images",
                         "genericProduct",
                         "thumbnail",
                         "binLocation",
                         "substitutions",
                         "color",
                         "applicationTagLib",
                         "handlingIcons"
    ]

    static hasMany = [
            categories         : Category,
            attributes         : ProductAttribute,
            tags               : Tag,
            documents          : Document,
            productGroups      : ProductGroup,
            packages           : ProductPackage,
            synonyms           : Synonym,
            inventoryLevels    : InventoryLevel,
            inventoryItems     : InventoryItem,
            productComponents  : ProductComponent,
            productSuppliers   : ProductSupplier,
            productCatalogItems: ProductCatalogItem
    ]

    static mapping = {
        id generator: 'uuid'
        cache true
        tags joinTable: [name: 'product_tag', column: 'tag_id', key: 'product_id']
        categories joinTable: [name: 'product_category', column: 'category_id', key: 'product_id']
        attributes joinTable: [name: 'product_attribute', column: 'attribute_id', key: 'product_id']
        documents joinTable: [name: 'product_document', column: 'document_id', key: 'product_id']
        productGroups joinTable: [name: 'product_group_product', column: 'product_group_id', key: 'product_id']
        synonyms cascade: 'all-delete-orphan', sort: 'name'
        productSuppliers cascade: 'all-delete-orphan'//, sort: 'dateCreated'
        productComponents cascade: "all-delete-orphan"
        productColor(formula: '(select max(pc.color) from product_catalog_item pci left outer join product_catalog pc on pci.product_catalog_id = pc.id where pci.product_id = id group by pci.product_id)')
    }

    static mappedBy = [productComponents: "assemblyProduct"]

    static constraints = {
        name(nullable: false, blank: false, maxSize: 255)
        description(nullable: true)
        productCode(nullable: true, maxSize: 255, unique: true)
        unitOfMeasure(nullable: true, maxSize: 255)
        category(nullable: false)
        productType(nullable: true)
        active(nullable: true)
        coldChain(nullable: true)
        reconditioned(nullable: true)
        controlledSubstance(nullable: true)
        hazardousMaterial(nullable: true)
        serialized(nullable: true)
        lotControl(nullable: true)
        essential(nullable: true)

        defaultUom(nullable: true)
        upc(nullable: true, maxSize: 255)
        ndc(nullable: true, maxSize: 255)

        abcClass(nullable: true)
        packageSize(nullable: true)
        brandName(nullable: true, maxSize: 255)
        vendor(nullable: true, maxSize: 255)
        vendorCode(nullable: true, maxSize: 255)
        vendorName(nullable: true, maxSize: 255)
        modelNumber(nullable: true, maxSize: 255)
        manufacturer(nullable: true, maxSize: 255)
        manufacturerCode(nullable: true, maxSize: 255)
        manufacturerName(nullable: true, maxSize: 255)
        //route(nullable:true)
        //dosageForm(nullable:true)
        pricePerUnit(nullable: true)
        costPerUnit(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
        glAccount(nullable: true)
        productColor(nullable: true)
    }

    /**
     * Get the list of categories associated with this product.
     *
     * @return
     */
    @Hidden
    def getCategoriesList() {
        return LazyList.decorate(categories,
                FactoryUtils.instantiateFactory(Category.class))
    }

    /**
     * Get the root category.
     *
     * @return
     */
    @Hidden
    Category getRootCategory() {
        Category rootCategory = new Category()
        rootCategory.categories = this.categories
        return rootCategory
    }

    /**
     * Get all images associated with this product.
     *
     * @return
     */
    @Hidden
    Collection getImages() {
        return documents?.findAll { it.contentType.startsWith("image") }
    }

    /**
     * Get the thumbnail (of the first image) associated with this product.
     *
     * @return
     */
    @Hidden
    Document getThumbnail() {
        return this?.images ? this.images?.sort()?.first() : null
    }

    /**
     * Get product package for the given UoM code.
     *
     * @param uomCode
     * @return
     */
    ProductPackage getProductPackage(uomCode) {
        def unitOfMeasure = UnitOfMeasure.findByCode(uomCode)
        return ProductPackage.findByProductAndUom(this, unitOfMeasure)
    }

    /**
     * Get the first generic product (product group) associated with this product.
     * @return
     */
    @Hidden
    ProductGroup getGenericProduct() {
        return productGroups ? productGroups?.sort()?.first() : null
    }

    @Hidden
    List<ProductAssociation> getSubstitutions() {
        return ProductAssociation.findAllByProductAndCode(this, ProductAssociationTypeCode.SUBSTITUTE)
    }

    Boolean isValidSubstitution(Product product) {
        return ProductAssociation.createCriteria().get {
            eq("product", this)
            eq("code", ProductAssociationTypeCode.SUBSTITUTE)
            eq("associatedProduct", product)
        }
    }

    @Hidden
    List<ProductCatalog> getProductCatalogs() {
        return this.productCatalogItems?.productCatalog?.unique()
    }

    /**
     * Get products related to this product through all product groups.
     * @return
     */
    Set<Product> alternativeProducts() {
        return substitutions*.associatedProduct
    }

    /**
     * Get the product attribute associated with the given attribute.
     *
     * @param attribute
     * @return
     */
    ProductAttribute getProductAttribute(Attribute attribute) {
        if (!attribute) {
            return null
        }

        return attributes.find { ProductAttribute productAttribute -> productAttribute.attribute?.id == attribute?.id }
    }

    /**
     * Get the inventory level by location id.
     *
     * @param locationId
     * @return
     */
    InventoryLevel getInventoryLevel(String locationId) {
        if (id) {
            def location = Location.get(locationId)
            return InventoryLevel.findByProductAndInventory(this, location.inventory)
        }
    }

    /**
     * Get ABC classification for this product at the given location.
     * @param locationId
     * @return
     */
    String getAbcClassification(String locationId) {
        def inventoryLevel = getInventoryLevel(locationId)
        return inventoryLevel?.abcClass ?: abcClass
    }

    /**
     * Get the product status given the location and current quantity.
     *
     * @param locationId
     * @param currentQuantity
     * @return
     */
    def getStatus(String locationId, Integer currentQuantity) {
        def inventoryLevel = getInventoryLevel(locationId)
        return inventoryLevel?.statusMessage(currentQuantity)
    }

    /**
     * Currently not implement since it would require coupling InventoryService to Product.
     *
     * @param locationId
     */
    def getQuantityOnHand(Integer locationId) {
        throw new NotImplementedException()
    }

    /**
     * Currently not implement since it would require coupling InventoryService to Product.
     *
     * @param locationId
     */
    def getQuantityAvailableToPromise(Integer locationId) {
        throw new NotImplementedException()
    }


    Date latestInventoryDate(String locationId) {
        return latestTransactionDate(locationId, [TransactionCode.PRODUCT_INVENTORY])
    }

    Date earliestReceivingDate(String locationId) {
        return earliestTransactionDate(locationId, [TransactionCode.CREDIT])
    }

    /**
     * Get the latest inventory date for this product at the given location.
     *
     * @param locationId
     * @return
     */
    Date latestTransactionDate(String locationId, List<TransactionCode> transactionCodes) {
        def inventory = Location.get(locationId).inventory
        def date = TransactionEntry.executeQuery("""
          select 
            max(t.transactionDate) 
          from TransactionEntry as te 
          left join te.inventoryItem as ii 
          left join te.transaction as t 
          where ii.product= :product 
          and t.inventory = :inventory 
          and t.transactionType.transactionCode in (:transactionCodes)
          """, [product: this, inventory: inventory, transactionCodes: transactionCodes]).first()
        return date
    }

/**
 * Get the first receiving date for this inventory item in given bin location at the given location.
 *
 * @param locationId
 * @return
 */
    Date earliestTransactionDate(String locationId, List<TransactionCode> transactionCodes) {
        Inventory inventory = Location.get(locationId).inventory
        def date
        date = TransactionEntry.executeQuery("""
                select 
                  min(t.transactionDate)
                from TransactionEntry as te
                left join te.transaction as t
                left join te.inventoryItem as ii
                where ii.product = :product
                and t.inventory = :inventory
                and t.transactionType.transactionCode in (:transactionCodes)
                """, [inventory: inventory, product: this, transactionCodes: transactionCodes]).first()
        return date
    }

    /**
     * Get bin location for this product in a given location.
     *
     * @param locationId
     * @return
     */
    String getBinLocation(String locationId) {
        def inventoryLevel = getInventoryLevel(locationId)
        return inventoryLevel?.binLocation
    }

    /**
     * @return tags as a comma separated string
     */
    String tagsToString() {
        return (tags) ? tags.sort { it.tag }.collect { it.tag }.join(",") : ""
    }

    /**
     * Inidicates whether this product has the given tag.
     *
     * @param tag
     * @return
     */
    Boolean hasTag(tag) {
        return tagsToString()?.contains(tag)
    }

    Boolean hasOneOfTags(List<Tag> tagsInput) {
        return tagsInput.find { tag ->
            tags?.contains(tag)
        }
    }

    Boolean hasOneOfCatalogs(List<ProductCatalog> catalogs) {
        return catalogs.find { catalog ->
            productCatalogs?.contains(catalog)
        }
    }

    /**
     * Converts product catalog association to string.
     *
     * @return
     */
    String productCatalogsToString() {
        return productCatalogs ? productCatalogs.sort { it?.name }.collect {
            it.name
        }.join(",") : ""
    }

    /**
     *
     * @return
     */
    String toString() { return "${name}" }

    /**
     * Sort by name
     */
    int compareTo(obj) {

        def sortOrder =
                name <=> obj?.name ?:
                        id <=> obj?.id
        return sortOrder
    }

    /**
     * Some utility methods
     */

    /**
     * Returns true if there are any transaction entries or shipment items in the system associated with this product, false otherwise
     */
    Boolean hasAssociatedTransactionEntriesOrShipmentItems() {
        def items = InventoryItem.findAllByProduct(this)
        if (items && items.find { TransactionEntry.findByInventoryItem(it) }) {
            return true
        }
        if (ShipmentItem.findByProduct(this)) {
            return true
        }
        return false
    }

    @Schema(nullable = true, readOnly = true)
    def getColor() {
        def results = ProductCatalogItem.executeQuery("select pci.productCatalog.color " +
                " from ProductCatalogItem pci where pci.product = :product " +
                " AND pci.productCatalog.color is not null",
                [product: this, max: 1])
        return results ? results[0] : null
    }

    @Hidden
    def getApplicationTagLib() {
        return Holders.grailsApplication.mainContext.getBean( ApplicationTagLib )
    }

    @Hidden
    def getHandlingIcons() {
        def g = getApplicationTagLib()
        def handlingIcons = []
        if (this.coldChain) handlingIcons.add([icon: "fa-snowflake", color: "#3bafda", label: "${g.message(code: 'product.coldChain.label')}"])
        if (this.controlledSubstance) handlingIcons.add([icon: "fa-exclamation-circle", color: "#db1919", label: "${g.message(code: 'product.controlledSubstance.label')}"])
        if (this.hazardousMaterial) handlingIcons.add([icon: "fa-exclamation-triangle", color: "#ffa500", label: "${g.message(code: 'product.hazardousMaterial.label')}"])
        if (this.reconditioned) handlingIcons.add([icon: "fa-prescription-bottle", color: "#a9a9a9", label: "${g.message(code: 'product.reconditioned.label')}"])
        return handlingIcons
    }

    Map toJson() {
        [
                id           : id,
                productCode  : productCode,
                name         : name,
                description  : description,
                category     : category?.toJson(),
                unitOfMeasure: unitOfMeasure,
                pricePerUnit : pricePerUnit,
                dateCreated  : dateCreated,
                lastUpdated  : lastUpdated,
                color        : productColor,
                handlingIcons: handlingIcons
        ]
    }
}
