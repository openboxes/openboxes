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

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.apache.commons.lang.NotImplementedException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventorySnapshotEvent
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.shipping.ShipmentItem


/**
 * An product is an instance of a generic.  For instance,
 * the product might be Ibuprofen, but the product is Advil 200mg
 *
 * We only track products and lots in the warehouse.  Generics help us
 * report on product availability across a generic product like Ibuprofen
 * no matter what size or shape it is.
 *
 * We will just support "1 unit" for now.  Variations of products will
 * eventually be stored as product variants (e.g. a 200 count bottle of
 * 20 mg tablets vs a 50 count bottle of 20 mg tablets will both be stored
 * as 20 mg tablets).
 */
class Product implements Comparable, Serializable {


    def beforeInsert = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                createdBy = currentUser
                updatedBy = currentUser
            }
        }
    }
    def beforeUpdate = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                updatedBy = currentUser
            }
        }
    }

    def publishPersistenceEvent = {
        publishEvent(new InventorySnapshotEvent(this))
    }

    def afterInsert = publishPersistenceEvent
    def afterUpdate = publishPersistenceEvent
    def afterDelete = publishPersistenceEvent



    // Base product information
    String id

    // Specific description for the product
    String name

    // Details product description
    String description

    // Internal product code identifier (or SKU)
    // http://en.wikipedia.org/wiki/Stock_keeping_unit
    String productCode

    // Type of product (good, service, fixed asset)
    ProductType productType

    // Price per unit (global for the entire system)
    BigDecimal pricePerUnit

    // Cost per unit
    BigDecimal costPerUnit

    // Controlled Substances
    // http://en.wikipedia.org/wiki/Controlled_Substances_Act
    // http://bfa.sdsu.edu/ehs/deapp1.htm
    Boolean controlledSubstance = Boolean.FALSE
    //ControlledSubstanceClass controlledSubstanceClass
    // SCHEDULE_I
    // SCHEDULE_II
    // SCHEDULE_III
    // SCHEDULE_IV
    // SCHEDULE_V

    // Hazardous Materia
    Boolean hazardousMaterial = Boolean.FALSE
    // http://www.fmcsa.dot.gov/facts-research/research-technology/visorcards/yellowcard.pdf
    // HazardousMaterialClass hazardousMaterialClass
    // CLASS_1_EXPLOSIVES
    // CLASS_2_GASES
    // CLASS_3_FLAMMABLE_LIQUIDS
    // CLASS_4_FLAMMABLE_SOLIDS
    // CLASS_5_OXIDIZERS_AND_ORGANIC_PEROXIDES
    // CLASS_6_TOXIC_MATERIALS_AND_INFECTIOUS_SUBSTANCES
    // CLASS_7_RADIOACTIVE_MATERIALS
    // CLASS_8_CORROSIVE_MATERIALS
    // CLASS_9_MISCELLANEOUS
    // DANGEROUS

    // Indicates whether the product is active.  Setting a product as inactive is the same
    // as removing from the database (it cannot be used if it is active).  However, we do
    // not want to delete the product because it may be referenced in existing transactions
    // shipments, requisitions, inventory items, etc.
    Boolean active = Boolean.TRUE

    // Indicates whether the product requires temperature-controlled supply chain
    // http://en.wikipedia.org/wiki/Cold_chain
    Boolean coldChain = Boolean.FALSE

    // Allows tracking of inventory by serial number (includes products such as computers and electronics).
    // A serialized product has a qty = 1 per serial number
    Boolean serialized = Boolean.FALSE

    // Allows tracking of inventory by lot or batch number (primary medicines and medical suppies)
    // http://docs.oracle.com/cd/A60725_05/html/comnls/us/inv/lotcntrl.htm
    Boolean lotControl = Boolean.TRUE

    // Used to indicate that the product is an essential med (as defined by the WHO, MSPP, or PIH).
    // WHO Model Lists of Essential Medicines - http://www.who.int/medicines/publications/essentialmedicines/en/
    Boolean essential = Boolean.TRUE

    // Used to indicate that the product is to be reconditioned
    Boolean reconditioned = Boolean.FALSE

    // primary category
    Category category

    // Default ABC Classification
    String abcClass

    // For better or worse, unit of measure and dosage form are used somewhat interchangeably
    // (e.g. each, tablet, pill, bottle, box)
    // http://help.sap.com/saphelp_45b/helpdata/en/c6/f83bb94afa11d182b90000e829fbfe/content.htm
    String unitOfMeasure

    // The default unit of measure used for this product
    // Not used at the moment, but this should actually be separated into multiple field
    // stockkeeping UoM, purchasing UoM, shipping UoM, dispensing UoM, requisition UoM,
    // issuing UoM, and reporting UoM.
    UnitOfMeasure defaultUom
    // UnitOfMeasure shippingUom
    // UnitOfMeasure UoM
    // UnitOfMeasure issuingUom

    // Universal product code - http://en.wikipedia.org/wiki/Universal_Product_Code
    String upc

    // National drug code - http://en.wikipedia.org/wiki/National_Drug_Code
    String ndc

    // Manufacturer details
    String manufacturer        // Manufacturer
    String manufacturerCode // Manufacturer's product code (e.g. catalog code)
    String manufacturerName // Manufacturer's product name
    String brandName        // Manufacturer's brand name
    String modelNumber        // Manufacturer's model number

    // Vendor details
    String vendor             // Vendor
    String vendorCode        // Vendor's product code
    String vendorName        // Vendor's product name

    // Almost all products will have a packageSize = 1
    // The product package association *should* be used to represent packages.
    // However, there are cases (packdowns) in which we need to create the
    // each-level product as well as a second product to handle the package
    // size because the system does not currently support quantities at
    // multiple levels, so we'll need to convert from the EA product to the
    // product with a packageSize > 1.
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
    List attributes = new ArrayList()

    // Secondary categories (currently not used)
    List categories = new ArrayList()

    // List of product components - bill of materials
    List productComponents

    // Auditing
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    // "inventoryLevels"
    static transients = ["rootCategory", "categoriesList", "images", "genericProduct", "thumbnail", "binLocation", "substitutions", "color"]

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
    }

    /**
     * Get the list of categories associated with this product.
     *
     * @return
     */
    def getCategoriesList() {
        return LazyList.decorate(categories,
                FactoryUtils.instantiateFactory(Category.class))
    }

    /**
     * Get the root category.
     *
     * @return
     */
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
    Collection getImages() {
        return documents?.findAll { it.contentType.startsWith("image") }
    }

    /**
     * Get the thumbnail (of the first image) associated with this product.
     *
     * @return
     */
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
    ProductGroup getGenericProduct() {
        return productGroups ? productGroups?.sort()?.first() : null
    }


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

    def getColor() {
        return this.productCatalogs?.find { it.color }?.color
    }

    Map toJson() {
        [
                id         : id,
                productCode: productCode,
                name       : name,
                description: description,
                category   : category?.toJson(),
                unitOfMeasure: unitOfMeasure,
                pricePerUnit: pricePerUnit,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
                color: color
        ]
    }
}

