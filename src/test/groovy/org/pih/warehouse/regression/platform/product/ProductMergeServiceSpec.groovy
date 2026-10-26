package org.pih.warehouse.regression.platform.product

import org.pih.warehouse.product.*
import grails.testing.gorm.DataTest
import org.springframework.validation.ObjectError
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryCountService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.InventoryTransactionSummaryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceService
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.shipping.ShipmentItem

/**
 * Unit tests for merging an obsolete product into a primary one. The relation queries that need a real database
 * (the left outer join criteria and the picklist bulk update) are stubbed here and run for real in
 * ProductMergeServiceIntegrationSpec.
 */
@Unroll
class ProductMergeServiceSpec extends Specification implements DataTest {

    ProductMergeService service
    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    RequisitionService requisitionService
    InvoiceService invoiceService
    InventoryCountService inventoryCountService
    InventoryTransactionSummaryService inventoryTransactionSummaryService

    Category category
    ProductType productType
    Product primary
    Product obsolete

    void setupSpec() {
        mockDomains(Product, Category, ProductType, ProductSupplier, ProductComponent, ProductMergeLogger, UnitOfMeasure,
                InventoryItem, ProductAvailability, PicklistItem, Transaction, TransactionEntry, TransactionType,
                Inventory, Location)

        // Product validation runs javax validation through the "defaultValidator" bean, which a running app gets
        // from Boot's autoconfiguration, and the ProductValidator component, whose deactivation rules (stock,
        // shipments, stock lists) are not what these tests are about.
        defineBeans {
            defaultValidator(LocalValidatorFactoryBean)
        }
        applicationContext.beanFactory.registerSingleton("productValidator", new PermissiveProductValidator())
    }

    void setup() {
        service = Spy(ProductMergeService)
        inventoryService = Mock(InventoryService)
        productAvailabilityService = Mock(ProductAvailabilityService)
        requisitionService = Stub(RequisitionService)
        invoiceService = Stub(InvoiceService)
        inventoryCountService = Mock(InventoryCountService)
        inventoryTransactionSummaryService = Mock(InventoryTransactionSummaryService)
        service.inventoryService = inventoryService
        service.productAvailabilityService = productAvailabilityService
        service.requisitionService = requisitionService
        service.invoiceService = invoiceService
        service.inventoryCountService = inventoryCountService
        service.inventoryTransactionSummaryService = inventoryTransactionSummaryService

        category = new Category(name: "Medicines").save(validate: false)
        productType = new ProductType(name: "Good", code: "GOOD").save(validate: false)
        primary = createProduct("PRIM1", "Primary")
        obsolete = createProduct("OBSO1", "Obsolete")
    }

    void cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(PicklistItem)
    }

    private Product createProduct(String code, String name) {
        Product product = new Product(productCode: code, name: name, category: category, productType: productType, active: true)
        product.save(flush: true, failOnError: true)
        return product
    }

    private static InventoryItem lot(Product product, String lotNumber, Date expirationDate = null) {
        return new InventoryItem(product: product, lotNumber: lotNumber, expirationDate: expirationDate)
    }

    private static Transaction transactionOfType(String id, String typeId) {
        TransactionType type = new TransactionType(name: "type ${typeId}")
        type.id = typeId
        Transaction transaction = new Transaction(transactionType: type)
        transaction.id = id
        return transaction
    }

    /** A plain set of transaction ids (an 'as Set' cast in an interaction would read as a type constraint). */
    private static Set<String> idSet(String... values) {
        return new HashSet<String>(Arrays.asList(values))
    }

    /** A set that matches members by identity, for objects whose equals is not reliable when unsaved. */
    private static Set identities(List objects) {
        Set set = Collections.newSetFromMap(new IdentityHashMap())
        set.addAll(objects)
        return set
    }

    /** No pending work on the obsolete product, and no related rows unless a feature stubs them. */
    private void stubNothingPendingAndNoRelatedRows() {
        requisitionService.getPendingRequisitionItems(_ as Product) >> []
        invoiceService.getPendingInvoiceItems(_ as Product) >> []
        inventoryService.getInventoriesWithTransactionsByProduct(_) >> []
        service.getRelatedObjectsForProduct(_, _) >> []
        service.getRelatedObjectsForProduct(_, _, false) >> []
    }

    // getPrimaryInventoryItem

    void 'getPrimaryInventoryItem should match obsolete lot #obsoleteLot against primary lots #primaryLots'() {
        given:
        List<InventoryItem> primaryItems = primaryLots.collect { lot(primary, it) }

        when:
        InventoryItem match = service.getPrimaryInventoryItem(primaryItems as LinkedHashSet, lot(obsolete, obsoleteLot))

        then:
        expectedIndex < 0 ? match == null : match.is(primaryItems[expectedIndex])

        where:
        obsoleteLot | primaryLots       || expectedIndex
        "A1"        | ["B2", "A1"]      || 1
        " A1 "      | ["A1"]            || 0
        "C3"        | ["A1", "B2"]      || -1
        null        | ["A1", null]      || 1
        ""          | ["A1", "  "]      || 1
        null        | ["A1", ""]        || 1
        "  "        | ["A1"]            || -1
        "A1"        | []                || -1
    }

    void 'getPrimaryInventoryItem should return null when the primary product has no inventory items'() {
        expect:
        service.getPrimaryInventoryItem(null, lot(obsolete, "A1")) == null
    }

    // validateProducts

    void 'validateProducts should reject a missing #which product'() {
        given:
        String primaryId = which == "primary" ? "does-not-exist" : primary.id
        String obsoleteId = which == "obsolete" ? "does-not-exist" : obsolete.id

        when:
        service.validateProducts(primaryId, obsoleteId)

        then:
        IllegalArgumentException e = thrown()
        e.message == "No Product found with ID does-not-exist"

        where:
        which << ["primary", "obsolete"]
    }

    void 'validateProducts should reject merging a product into itself'() {
        when:
        service.validateProducts(primary.id, primary.id)

        then:
        IllegalArgumentException e = thrown()
        e.message == "Cannot merge the product with itself"
    }

    void 'validateProducts should list each pending requisition of the obsolete product once'() {
        given:
        // distinct request dates: unique() compares requisitions by compareTo, which ignores the request number
        Requisition first = new Requisition(requestNumber: "REQ-1", dateRequested: new Date(1000L))
        Requisition second = new Requisition(requestNumber: "REQ-2", dateRequested: new Date(2000L))
        requisitionService.getPendingRequisitionItems(obsolete) >> [
                new RequisitionItem(requisition: first),
                new RequisitionItem(requisition: first),
                new RequisitionItem(requisition: second),
        ]

        when:
        service.validateProducts(primary.id, obsolete.id)

        then:
        IllegalArgumentException e = thrown()
        e.message == "Obsolete product has pending stock movements or requisitions (REQ-1, REQ-2). " +
                "Please finish or cancel these stock movements or requisitions before merging products."
    }

    void 'validateProducts should list each pending invoice of the obsolete product once'() {
        given:
        Invoice invoice = new Invoice(invoiceNumber: "INV-7")
        requisitionService.getPendingRequisitionItems(obsolete) >> []
        invoiceService.getPendingInvoiceItems(obsolete) >> [new InvoiceItem(invoice: invoice), new InvoiceItem(invoice: invoice)]

        when:
        service.validateProducts(primary.id, obsolete.id)

        then:
        IllegalArgumentException e = thrown()
        e.message == "Obsolete product has pending invoices (INV-7). Please post these invoices before merging products."
    }

    void 'validateProducts should accept two distinct products without pending work'() {
        given:
        requisitionService.getPendingRequisitionItems(obsolete) >> []
        invoiceService.getPendingInvoiceItems(obsolete) >> null

        when:
        service.validateProducts(primary.id, obsolete.id)

        then:
        noExceptionThrown()
    }

    // getLocationsWithPendingTransactions

    void 'getLocationsWithPendingTransactions should return each origin of the pending requisitions once'() {
        given:
        Location depot = new Location(name: "Depot")
        Location ward = new Location(name: "Ward")
        requisitionService.getPendingRequisitionItems(primary) >> [
                new RequisitionItem(requisition: new Requisition(origin: depot)),
                new RequisitionItem(requisition: new Requisition(origin: ward)),
                new RequisitionItem(requisition: new Requisition(origin: depot)),
        ]

        expect:
        service.getLocationsWithPendingTransactions(primary)*.name == ["Depot", "Ward"]
    }

    void 'getLocationsWithPendingTransactions should return nothing when there are no pending requisitions'() {
        given:
        requisitionService.getPendingRequisitionItems(primary) >> []

        expect:
        !service.getLocationsWithPendingTransactions(primary)
    }

    // logProductMergeData

    void 'logProductMergeData should record the merge itself when there is no related object'() {
        when:
        service.logProductMergeData(primary, obsolete, null)

        then:
        ProductMergeLogger log = ProductMergeLogger.list().first()
        ProductMergeLogger.count() == 1
        log.primaryProduct == primary
        log.obsoleteProduct == obsolete
        log.relatedObjectId == ""
        log.relatedObjectClassName == ""
        log.dateMerged != null
        log.dateReverted == null
    }

    void 'logProductMergeData should record the id and class of the related object'() {
        given:
        ProductSupplier supplier = new ProductSupplier(name: "Supplier", product: obsolete, code: "SUP-1").save(flush: true, failOnError: true)

        when:
        service.logProductMergeData(primary, obsolete, supplier)

        then:
        ProductMergeLogger log = ProductMergeLogger.list().first()
        log.relatedObjectId == supplier.id
        log.relatedObjectClassName == "class org.pih.warehouse.product.ProductSupplier"
    }

    void 'logProductMergeData should fail when the log entry cannot be saved'() {
        when:
        service.logProductMergeData(null, obsolete, null)

        then:
        Exception e = thrown()
        e.message.startsWith("Cannot save product merge logger due to: ")
        ProductMergeLogger.count() == 0
    }

    // updatePicklistItemsOnProductMerge

    void 'updatePicklistItemsOnProductMerge should repoint picklist items from the obsolete to the primary inventory item'() {
        given:
        List<Map> updates = []
        PicklistItem.metaClass.static.executeUpdate = { String statement, Map params ->
            updates << [statement: statement, params: params]
            return 2
        }
        InventoryItem primaryItem = lot(primary, "A1").save(flush: true, validate: false)
        InventoryItem obsoleteItem = lot(obsolete, "A1").save(flush: true, validate: false)

        when:
        service.updatePicklistItemsOnProductMerge(primaryItem, obsoleteItem)

        then:
        updates == [[
                statement: "update PicklistItem pi set pi.inventoryItem = :primaryInventoryItem " +
                        "where pi.inventoryItem.id = :obsoleteInventoryItemId",
                params   : [primaryInventoryItem: primaryItem, obsoleteInventoryItemId: obsoleteItem.id],
        ]]
    }

    void 'updatePicklistItemsOnProductMerge should do nothing unless both inventory items are saved'() {
        given:
        List updates = []
        PicklistItem.metaClass.static.executeUpdate = { String statement, Map params -> updates << statement; 0 }
        InventoryItem saved = lot(primary, "A1").save(flush: true, validate: false)

        when:
        service.updatePicklistItemsOnProductMerge(saved, lot(obsolete, "A1"))
        service.updatePicklistItemsOnProductMerge(lot(primary, "A1"), saved)
        service.updatePicklistItemsOnProductMerge(null, saved)

        then:
        updates.empty
    }

    // getRelatedObjectsForProduct

    void 'getRelatedObjectsForProduct should match the product directly or through a left joined inventory item'() {
        given:
        MergeCriteriaRecorder criteria = new MergeCriteriaRecorder()

        when:
        service.getRelatedObjectsForProduct(criteria, obsolete)

        then:
        criteria.calls == [
                [name: "or", args: []],
                [name: "eq", args: ["product", obsolete]],
                [name: "inventoryItem", args: [org.hibernate.sql.JoinType.LEFT_OUTER_JOIN.joinTypeValue]],
                [name: "eq", args: ["product", obsolete]],
        ]
    }

    void 'getRelatedObjectsForProduct should match only the product for objects without an inventory item'() {
        given:
        MergeCriteriaRecorder criteria = new MergeCriteriaRecorder()

        when:
        service.getRelatedObjectsForProduct(criteria, obsolete, false)

        then:
        criteria.calls == [
                [name: "or", args: []],
                [name: "eq", args: ["product", obsolete]],
        ]
    }

    // getProductMergeLogs

    void 'getProductMergeLogs should filter by the primary and obsolete product codes'() {
        given:
        Product other = createProduct("OTHER", "Other")
        service.logProductMergeData(primary, obsolete, null)
        service.logProductMergeData(primary, other, null)
        service.logProductMergeData(other, obsolete, null)

        expect:
        service.getProductMergeLogs(params).collect { "${it.primaryProduct.productCode}<${it.obsoleteProduct.productCode}" } as Set == expected as Set

        where:
        params                                                    || expected
        [:]                                                       || ["PRIM1<OBSO1", "PRIM1<OTHER", "OTHER<OBSO1"]
        [primaryProductCode: "PRIM1"]                             || ["PRIM1<OBSO1", "PRIM1<OTHER"]
        [obsoleteProductCode: "OBSO1"]                            || ["PRIM1<OBSO1", "OTHER<OBSO1"]
        [primaryProductCode: "OTHER", obsoleteProductCode: "OBSO1"] || ["OTHER<OBSO1"]
        [primaryProductCode: "NONE"]                              || []
    }

    // mergeProduct

    void 'mergeProduct should stop before changing anything when validation fails'() {
        given:
        requisitionService.getPendingRequisitionItems(obsolete) >> [new RequisitionItem(requisition: new Requisition(requestNumber: "REQ-1"))]

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then:
        thrown(IllegalArgumentException)
        0 * inventoryService._
        ProductMergeLogger.count() == 0
        Product.get(obsolete.id).active
    }

    void 'mergeProduct should move suppliers and components the primary product does not have yet, and deactivate the obsolete product'() {
        given:
        stubNothingPendingAndNoRelatedRows()
        UnitOfMeasure each = new UnitOfMeasure(name: "Each", code: "EA").save(validate: false)
        Product partX = createProduct("PARTX", "Part X")
        Product partY = createProduct("PARTY", "Part Y")

        and: 'a supplier code only the obsolete product has, and one both have'
        ProductSupplier movedSupplier = new ProductSupplier(name: "Moved", product: obsolete, code: "SUP-1").save(failOnError: true)
        ProductSupplier keptSupplier = new ProductSupplier(name: "Kept", product: obsolete, code: "SUP-2").save(failOnError: true)
        new ProductSupplier(name: "Existing", product: primary, code: "SUP-2").save(failOnError: true)

        and: 'a component only the obsolete product has, and one both have'
        ProductComponent movedComponent = new ProductComponent(assemblyProduct: obsolete, componentProduct: partX, quantity: 1, unitOfMeasure: each).save(failOnError: true)
        ProductComponent keptComponent = new ProductComponent(assemblyProduct: obsolete, componentProduct: partY, quantity: 2, unitOfMeasure: each).save(failOnError: true)
        new ProductComponent(assemblyProduct: primary, componentProduct: partY, quantity: 3, unitOfMeasure: each).save(failOnError: true, flush: true)

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then:
        ProductSupplier.findAllByProduct(primary)*.code.sort() == ["SUP-1", "SUP-2"]
        ProductSupplier.get(movedSupplier.id).product == primary
        ProductSupplier.get(keptSupplier.id).product == obsolete

        and:
        ProductComponent.get(movedComponent.id).assemblyProduct == primary
        ProductComponent.get(keptComponent.id).assemblyProduct == obsolete

        and: 'the obsolete product is deactivated, the primary stays active'
        !Product.get(obsolete.id).active
        Product.get(primary.id).active

        and: 'one log entry for the merge and one per moved relation'
        ProductMergeLogger.list().collect { [it.relatedObjectId, it.relatedObjectClassName] } as Set == [
                ["", ""],
                [movedSupplier.id, "class org.pih.warehouse.product.ProductSupplier"],
                [movedComponent.id, "class org.pih.warehouse.product.ProductComponent"],
        ] as Set
        ProductMergeLogger.list().every { it.primaryProduct == primary && it.obsoleteProduct == obsolete }
    }

    void 'mergeProduct should move lots the primary product lacks and fold shared lots into the primary lot'() {
        given:
        stubNothingPendingAndNoRelatedRows()
        InventoryItem primaryLotA = lot(primary, "A").save(failOnError: true)
        InventoryItem obsoleteLotA = lot(obsolete, "A").save(failOnError: true)
        InventoryItem obsoleteLotB = lot(obsolete, "B").save(failOnError: true)
        InventoryItem obsoleteLotC = lot(obsolete, "C").save(failOnError: true)

        and: 'availability rows for the shared lot A and the new lot B, and a picklist row for lot A'
        ProductAvailability availabilityA = new ProductAvailability(inventoryItem: obsoleteLotA, product: obsolete)
        availabilityA.id = "pa-a"
        availabilityA.save(validate: false)
        ProductAvailability availabilityB = new ProductAvailability(inventoryItem: obsoleteLotB, product: obsolete)
        availabilityB.id = "pa-b"
        availabilityB.save(validate: false)
        new PicklistItem(inventoryItem: obsoleteLotA).save(validate: false, flush: true)

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then: 'the shared lot stays on the obsolete product; its availability and picklist rows move to the primary lot'
        1 * productAvailabilityService.updateProductAvailabilityOnMergeProduct(primaryLotA, obsoleteLotA, primary, obsolete)
        1 * service.updatePicklistItemsOnProductMerge(primaryLotA, obsoleteLotA) >> null

        and: 'new lots move to the primary product; only the one with availability rows refreshes them'
        1 * productAvailabilityService.updateProductAvailabilityOnMergeProduct(obsoleteLotB, primary, obsolete)
        0 * productAvailabilityService.updateProductAvailabilityOnMergeProduct(obsoleteLotC, _, _)
        0 * service.updatePicklistItemsOnProductMerge(_, obsoleteLotB)

        and:
        InventoryItem.get(obsoleteLotA.id).product == obsolete
        InventoryItem.findAllByProduct(primary)*.lotNumber.sort() == ["A", "B", "C"]
        obsoleteLotB.disableRefresh
        obsoleteLotC.disableRefresh

        and:
        ProductMergeLogger.list()*.relatedObjectId as Set == ["", obsoleteLotB.id, obsoleteLotC.id] as Set
    }

    void 'mergeProduct should move transaction entries to the primary product and lot, and refresh the views of their transaction types'() {
        given:
        stubNothingPendingAndNoRelatedRows()
        InventoryItem primaryLotA = lot(primary, "A").save(failOnError: true, flush: true)

        and: 'entries in an adjustment (two entries), a baseline, a transfer in, a transfer out and a consumption'
        Transaction adjustment = transactionOfType("t-adj", Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        Transaction baseline = transactionOfType("t-base", Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID)
        Transaction transferIn = transactionOfType("t-in", Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
        Transaction transferOut = transactionOfType("t-out", Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
        Transaction consumption = transactionOfType("t-cons", Constants.CONSUMPTION_TRANSACTION_TYPE_ID)
        InventoryItem obsoleteLotB = lot(obsolete, "B")
        TransactionEntry sharedLotEntry = new TransactionEntry(transaction: adjustment, inventoryItem: lot(obsolete, "A"))
        TransactionEntry newLotEntry = new TransactionEntry(transaction: adjustment, inventoryItem: obsoleteLotB)
        List<TransactionEntry> others = [baseline, transferIn, transferOut, consumption].collect { Transaction t ->
            new TransactionEntry(transaction: t, inventoryItem: lot(obsolete, "Z"))
        }
        List<TransactionEntry> entries = [sharedLotEntry, newLotEntry] + others

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then:
        1 * service.getRelatedObjectsForProduct({ it.targetClass == TransactionEntry }, obsolete) >> entries

        and: 'every entry is logged and moved to the primary product'
        6 * service.logProductMergeData(primary, obsolete, { identities(entries).contains(it) })
        entries.every { it.product == primary }

        and: 'the shared lot entry moves to the primary lot, the others keep theirs'
        sharedLotEntry.inventoryItem.is(primaryLotA)
        newLotEntry.inventoryItem.is(obsoleteLotB)

        and: 'adjustments refresh the adjustment candidates and movement summary, once per transaction'
        1 * inventoryCountService.refreshAdjustmentCandidatesViewAfterProductMerge(idSet("t-adj"), obsolete.id, primary.id)
        1 * inventoryTransactionSummaryService.refreshInventoryMovementSummaryViewAfterProductMerge(idSet("t-adj"), obsolete, primary)

        and: 'baselines refresh the inventory summary and baseline candidates'
        1 * inventoryTransactionSummaryService.refreshProductInventorySummaryViewAfterProductMerge(idSet("t-base"), obsolete, primary)
        1 * inventoryCountService.refreshInventoryBaselineCandidatesViewAfterProductMerge(idSet("t-base"), obsolete.id, primary.id)

        and: 'transfers refresh the movement summary; other types refresh nothing'
        1 * inventoryTransactionSummaryService.refreshInventoryMovementSummaryViewAfterProductMerge(idSet("t-in"), obsolete, primary)
        1 * inventoryTransactionSummaryService.refreshInventoryMovementSummaryViewAfterProductMerge(idSet("t-out"), obsolete, primary)
        0 * inventoryTransactionSummaryService._
        0 * inventoryCountService._
    }

    void 'mergeProduct should move requisition, shipment, order, receipt and invoice items to the primary product and lot'() {
        given:
        stubNothingPendingAndNoRelatedRows()
        Date expiry = new Date(1800000000000L)
        InventoryItem primaryLotA = lot(primary, "A", expiry).save(failOnError: true, flush: true)

        and:
        RequisitionItem requisitionItem = Mock(RequisitionItem) { getInventoryItem() >> lot(obsolete, "A") }
        ShipmentItem shipmentItem = Mock(ShipmentItem) { getInventoryItem() >> lot(obsolete, "A") }
        OrderItem orderItem = Mock(OrderItem) { getInventoryItem() >> lot(obsolete, "A") }
        ReceiptItem receiptItem = Mock(ReceiptItem) { getInventoryItem() >> lot(obsolete, "A") }
        ShipmentItem shipmentItemNewLot = Mock(ShipmentItem) { getInventoryItem() >> lot(obsolete, "B") }
        InvoiceItem invoiceItem = Mock(InvoiceItem)

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then:
        1 * service.getRelatedObjectsForProduct({ it.targetClass == RequisitionItem }, obsolete) >> [requisitionItem]
        1 * service.getRelatedObjectsForProduct({ it.targetClass == ShipmentItem }, obsolete) >> [shipmentItem, shipmentItemNewLot]
        1 * service.getRelatedObjectsForProduct({ it.targetClass == OrderItem }, obsolete) >> [orderItem]
        1 * service.getRelatedObjectsForProduct({ it.targetClass == ReceiptItem }, obsolete) >> [receiptItem]
        1 * service.getRelatedObjectsForProduct({ it.targetClass == InvoiceItem }, obsolete, false) >> [invoiceItem]

        and: 'requisition and order items take the primary product and lot'
        1 * requisitionItem.setProduct(primary)
        1 * requisitionItem.setInventoryItem(primaryLotA)
        1 * requisitionItem.save([flush: true])
        1 * orderItem.setProduct(primary)
        1 * orderItem.setInventoryItem(primaryLotA)
        1 * orderItem.save([flush: true])

        and: 'shipment and receipt items also copy the lot number and expiry of the primary lot'
        1 * shipmentItem.setProduct(primary)
        1 * shipmentItem.setInventoryItem(primaryLotA)
        1 * shipmentItem.setLotNumber("A")
        1 * shipmentItem.setExpirationDate(expiry)
        1 * shipmentItem.save([flush: true])
        1 * receiptItem.setProduct(primary)
        1 * receiptItem.setInventoryItem(primaryLotA)
        1 * receiptItem.setLotNumber("A")
        1 * receiptItem.setExpirationDate(expiry)
        1 * receiptItem.save([flush: true])

        and: 'a shipment item of a lot the primary lacks keeps its lot'
        1 * shipmentItemNewLot.setProduct(primary)
        0 * shipmentItemNewLot.setInventoryItem(_)
        0 * shipmentItemNewLot.setLotNumber(_)
        1 * shipmentItemNewLot.save([flush: true])

        and: 'invoice items only change product'
        1 * invoiceItem.setProduct(primary)
        1 * invoiceItem.save([flush: true])
        0 * invoiceItem.setInventoryItem(_)

        and:
        6 * service.logProductMergeData(primary, obsolete,
                { identities([requisitionItem, shipmentItem, shipmentItemNewLot, orderItem, receiptItem, invoiceItem]).contains(it) })
    }

    void 'mergeProduct should write a baseline of both products current stock in each inventory they share'() {
        given:
        requisitionService.getPendingRequisitionItems(_ as Product) >> []
        invoiceService.getPendingInvoiceItems(_ as Product) >> []
        service.getRelatedObjectsForProduct(_, _) >> []
        service.getRelatedObjectsForProduct(_, _, false) >> []

        and:
        TransactionType baselineType = new TransactionType(name: "Inventory baseline")
        baselineType.id = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID
        baselineType.save(validate: false, flush: true)
        Location depot = new Location(name: "Depot")
        Inventory shared = new Inventory(warehouse: depot)
        Inventory obsoleteOnly = new Inventory(warehouse: new Location(name: "Other"))
        Location bin = new Location(name: "Bin 1")
        InventoryItem obsoleteLot = lot(obsolete, "X")
        InventoryItem primaryLot = lot(primary, "Y")
        List<TransactionEntry> obsoleteEntries = [new TransactionEntry(quantity: 1)]
        List<TransactionEntry> primaryEntries = [new TransactionEntry(quantity: 2)]
        Map obsoleteQuantities = [(obsoleteLot): 5]
        Map primaryQuantities = [(primaryLot): 3]
        Transaction baseline = null

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then:
        1 * inventoryService.getInventoriesWithTransactionsByProduct(obsolete) >> [shared, obsoleteOnly]
        1 * inventoryService.getInventoriesWithTransactionsByProduct(primary) >> [shared]
        1 * inventoryService.getTransactionEntriesByInventoryAndProduct(shared, [obsolete]) >> obsoleteEntries
        1 * inventoryService.getTransactionEntriesByInventoryAndProduct(shared, [primary]) >> primaryEntries
        1 * inventoryService.getQuantityAvailableByProductAndInventoryItemMap(obsolete, depot) >> obsoleteQuantities
        1 * inventoryService.getQuantityAvailableByProductAndInventoryItemMap(primary, depot) >> primaryQuantities
        1 * inventoryService.getAvailableItems({ it.is(obsoleteEntries) }, { it.is(obsoleteQuantities) }) >> [
                new AvailableItem(inventoryItem: obsoleteLot, binLocation: bin, quantityOnHand: 5)]
        1 * inventoryService.getAvailableItems({ it.is(primaryEntries) }, { it.is(primaryQuantities) }) >> [
                new AvailableItem(inventoryItem: primaryLot, binLocation: null, quantityOnHand: 3)]
        1 * inventoryService.generateTransactionNumber(_ as Transaction) >> { Transaction t -> baseline = t; "TX-1" }
        0 * inventoryService.getTransactionEntriesByInventoryAndProduct(obsoleteOnly, _)

        and:
        baseline.inventory.is(shared)
        baseline.transactionType.id == Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID
        baseline.transactionNumber == "TX-1"
        baseline.transactionDate != null
        baseline.disableRefresh
        baseline.comment == "Created while merging product OBSO1 into PRIM1 " +
                "(this happens if both products have transaction entries in the same inventory)"
        baseline.transactionEntries.collect { [it.inventoryItem, it.binLocation, it.quantity] } as Set == [
                [obsoleteLot, bin, 5],
                [primaryLot, null, 3],
        ] as Set
    }

    void 'mergeProduct should fail when the #which product cannot be saved'() {
        given:
        stubNothingPendingAndNoRelatedRows()
        (which == "obsolete" ? obsolete : primary).name = null

        when:
        service.mergeProduct(primary.id, obsolete.id)

        then:
        Exception e = thrown()
        e.message.startsWith("Cannot save ${which} product due to: ")

        where:
        which << ["obsolete", "primary"]
    }
}

/** Records the criteria DSL calls a closure makes, running nested closures against itself. */
class MergeCriteriaRecorder {
    List<Map> calls = []

    def list(Closure closure) {
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        return []
    }

    def methodMissing(String name, Object args) {
        List argList = (args as Object[]).toList()
        calls << [name: name, args: argList.findAll { !(it instanceof Closure) }]
        argList.findAll { it instanceof Closure }.each { Closure nested ->
            nested.delegate = this
            nested.resolveStrategy = Closure.DELEGATE_FIRST
            nested.call()
        }
        return null
    }
}

/** Accepts any active flag, so deactivating the obsolete product needs no stock or shipment lookups. */
class PermissiveProductValidator extends ProductValidator {

    @Override
    List<ObjectError> validateActive(Product product) {
        return Collections.emptyList()
    }
}
