/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

import grails.converters.JSON
import grails.util.Environment
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType


import javax.sql.DataSource

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import util.LiquibaseUtil

class BootStrap {

    def identifierService
    def grailsApplication
    DataSource dataSource;

    def init = { servletContext ->

        // Static data
        JSON.registerObjectMarshaller(ContainerType) { ContainerType containerType -> [
                id: containerType.id,
                name: containerType.name,
                description: containerType.description
        ]}

        JSON.registerObjectMarshaller(LocationType) { LocationType locationType -> [
                id: locationType.id,
                name: locationType.name,
                description: locationType.description,
                locationTypeCode: locationType?.locationTypeCode?.name()
        ]}

        JSON.registerObjectMarshaller(ShipmentType) { ShipmentType shipmentType -> [
                id: shipmentType.id,
                name: shipmentType.name,
                description: shipmentType.description
        ]}


        // Master data

        JSON.registerObjectMarshaller(Category) { Category category -> [
                id: category.id,
                name: category.name,
                parentCategory: category?.parentCategory
        ]}

        JSON.registerObjectMarshaller(Container) { Container container -> [
                id: container.id,
                name: container.name,
                containerNumber: container.containerNumber,
                containerType: container.containerType,
                recipient: container.recipient,
                sortOrder: container.sortOrder,
                shipmentItems: container.shipmentItems
        ]}

        JSON.registerObjectMarshaller(LocationGroup) { LocationGroup locationGroup -> [
                id: locationGroup.id,
                name: locationGroup.name
        ]}

        JSON.registerObjectMarshaller(InventoryItem) { InventoryItem inventoryItem -> [
                id: inventoryItem.id,
                product: [
                        id: inventoryItem?.product?.id,
                        name: inventoryItem?.product?.name,
                        productCode: inventoryItem?.product?.productCode
                ],
                lotNumber: inventoryItem.lotNumber,
                expirationDate: inventoryItem.expirationDate
        ]}

        JSON.registerObjectMarshaller(Location) { Location location -> [
                id: location.id,
                name: location.name,
                description: location.description,
                locationNumber: location.locationNumber,
                locationGroup: location.locationGroup,
                parentLocation: location.parentLocation,
                locationType: location.locationType,
                locationTypeCode: location?.locationType?.locationTypeCode?.name()
        ]}

        JSON.registerObjectMarshaller(Person) { Person person -> [
                id: person.id,
                name: person.name,
                firstName: person.firstName,
                lastName: person.lastName,
                email: person.email,
                username: null
        ]}


        JSON.registerObjectMarshaller(Picklist) { Picklist picklist -> [
                id: picklist.id,
                name: picklist.name,
                description: picklist.description,
                picker: picklist.picker,
                datePicked: picklist.datePicked,
                picklistItems: picklist.picklistItems,
                "requisition.id": picklist?.requisition?.id
        ]}

        JSON.registerObjectMarshaller(PicklistItem) { PicklistItem picklistItem -> [
                id: picklistItem.id,
                inventoryItem: picklistItem.inventoryItem,
                status: picklistItem.status,
                "picklist.id": picklistItem.picklist?.id,
                "requisitionItem.id": picklistItem?.requisitionItem?.id,
                reasonCode: picklistItem.reasonCode,
                comment: picklistItem.comment
        ]}


        JSON.registerObjectMarshaller(Product) { Product product -> [
                id: product.id,
                productCode: product.productCode,
                name: product.name,
                description: product.description,
                category: [id: product?.category?.id, name: product?.category?.name]
        ]}

        JSON.registerObjectMarshaller(Receipt) { Receipt receipt -> [
                id: receipt.id,
                expectedDeliveryDate: receipt.expectedDeliveryDate,
                actualDeliveryDate: receipt.actualDeliveryDate,
                recipient: receipt.recipient,
                shipment: receipt.shipment,
                recipientItems: receipt.receiptItems
        ]}

        JSON.registerObjectMarshaller(ReceiptItem) { ReceiptItem receiptItem -> [
                id: receiptItem.id,
                receipt: receiptItem.receipt,
                product: receiptItem.inventoryItem.product,
                inventoryItem: receiptItem.inventoryItem,
                quantityReceived: receiptItem.quantityReceived,
                quantityShipped: receiptItem.quantityShipped,
                binLocation: receiptItem.binLocation,
                recipient: receiptItem.recipient
        ]}

        JSON.registerObjectMarshaller(Requisition) { Requisition requisition -> [
                id: requisition.id,
                name: requisition.name,
                requisitionNumber: requisition.requestNumber,
                description: requisition.description,
                isTemplate: requisition.isTemplate,
                type: requisition?.type?.name(),
                status: requisition?.status?.name(),
                commodityClass: requisition?.commodityClass?.name(),
                dateRequested: requisition.dateRequested,
                dateReviewed: requisition.dateReviewed,
                dateVerified: requisition.dateVerified,
                dateChecked: requisition.dateChecked,
                dateDelivered: requisition.dateDelivered,
                dateIssued: requisition.dateIssued,
                dateReceived: requisition.dateReceived,
                origin: requisition.origin,
                destination: requisition.destination,
                requestedBy: requisition.requestedBy,
                reviewedBy: requisition.reviewedBy,
                verifiedBy: requisition.verifiedBy,
                checkedBy: requisition.checkedBy,
                deliveredBy: requisition.deliveredBy,
                issuedBy: requisition.issuedBy,
                receivedBy: requisition.receivedBy,
                recipient: requisition.recipient,
                requisitionItems: requisition.requisitionItems
        ]}

        JSON.registerObjectMarshaller(RequisitionItem) { RequisitionItem requisitionItem -> [
                id: requisitionItem.id,
                status: requisitionItem.status?.name(),
                "requisition.id": requisitionItem?.requisition.id,
                product: requisitionItem.product,
                inventoryItem: requisitionItem.inventoryItem,
                quantity: requisitionItem.quantity,
                quantityApproved: requisitionItem.quantityApproved,
                quantityCanceled: requisitionItem.quantityCanceled,
                cancelReasonCode: requisitionItem.cancelReasonCode,
                cancelComments: requisitionItem.cancelComments,
                orderIndex: requisitionItem.orderIndex,
                changes: requisitionItem.change?[requisitionItem.change]:[],
                modification: requisitionItem.modificationItem,
                substitution: requisitionItem.substitutionItem,
                picklistItems: requisitionItem.picklistItems,
        ]}



        JSON.registerObjectMarshaller(Shipment) { Shipment shipment ->
            def containerList = []
            def shipmentItemsByContainer = shipment.shipmentItems.groupBy { it.container }
            shipmentItemsByContainer.each { container, shipmentItems ->
                containerList << [id: container?.id, name: container?.name, type: container?.containerType?.name, shipmentItems: shipmentItems]
            }
            return [
                    id: shipment.id,
                    name: shipment.name,
                    status: shipment?.status?.code?.name(),
                    origin: [
                            id: shipment.origin?.id,
                            name: shipment?.origin?.name,
                            type: shipment?.origin?.locationType?.locationTypeCode?.name()
                    ],
                    destination: [
                            id: shipment?.destination?.id,
                            name: shipment?.destination?.name,
                            type: shipment?.destination?.locationType?.locationTypeCode?.name()

                    ],
                    expectedShippingDate: shipment.expectedShippingDate,
                    actualShippingDate: shipment.actualShippingDate,
                    expectedDeliveryDate: shipment.expectedDeliveryDate,
                    actualDeliveryDate: shipment.actualDeliveryDate,
                    shipmentItems: shipment.shipmentItems,
                    containers: containerList
        ]}

        JSON.registerObjectMarshaller(ShipmentItem) { ShipmentItem shipmentItem ->
            def container = shipmentItem?.container ? [
                    id: shipmentItem?.container?.id,
                    name: shipmentItem?.container?.name,
                    type: shipmentItem?.container?.containerType?.name ] : null
            [
                id: shipmentItem.id,
                inventoryItem: shipmentItem?.inventoryItem,
                quantity: shipmentItem.quantity,
                recipient: shipmentItem.recipient,
                shipment: [id: shipmentItem?.shipment?.id, name: shipmentItem?.shipment?.name],
                container: container
        ]}

        JSON.registerObjectMarshaller(StockMovement) { StockMovement stockMovement ->
            return stockMovement.toJson()
        }

        JSON.registerObjectMarshaller(StockMovementItem) { StockMovementItem stockMovementItem ->
            return stockMovementItem.toJson()
        }

        JSON.registerObjectMarshaller(User) { User user -> [
                id: user.id,
                name: user.name,
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                username: user.username
        ]}



        // ================================    Static Data    ============================================
        //
        // Use the 'demo' environment to create a database with 'static' and 'demo' data.  Then
        // run the following:
        //
        // 		$ grails -Dgrails.env=demo run-app
        //
        // In another terminal, run through these commands to generate the appropriate
        // changelog files for a new version of the data model
        //
        // 		$ grails db-diff-schema > grails-app/migrations/x.x.x/changelog-initial-schema.xml
        // 		$ grails db-diff-index > grails-app/migrations/x.x.x/changelog-initial-indexes.xml
        // 		$ grails db-diff-data > grails-app/migrations/x.x.x/changelog-initial-data.xml
        //
        // Migrating existing data to the new data model is still a work in progress, but you can
        // use the previous versions changelogs.
        //
        //if (GrailsUtil.environment == 'test' || GrailsUtil.environment == 'development' ||
        //GrailsUtil.environment == 'client' || GrailsUtil.environment == 'root') {
        log.info("Running liquibase changelog(s) ...")
        Liquibase liquibase = null
        try {

            def connection = dataSource.getConnection()
            if (connection == null) {
                throw new RuntimeException("Connection could not be created.");
            }
            //LiquibaseUtil.class.getClassLoader();
            def classLoader = getClass().classLoader;
            def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()

            //def fileOpener = new ClassLoaderFileOpener()
            def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
            boolean isRunningMigrations = LiquibaseUtil.isRunningMigrations()
            log.info("Liquibase running: " + isRunningMigrations)
            log.info("Setting default schema to " + connection.catalog)
            log.info("Product Version: " + database.databaseProductVersion)
            log.info("Database Version: " + database.databaseMajorVersion + "." + database.databaseMinorVersion)
            def ranChangeSets = database.getRanChangeSetList()
            database.setDefaultSchemaName(connection.catalog)

            //If nothing has been created yet, let's create all new database objects with the install scripts
            if (!ranChangeSets) {
                liquibase = new Liquibase("install/install.xml", fileOpener, database);
                liquibase.update(null)
            }

            // Run through the updates in the master changelog
            liquibase = new Liquibase("changelog.xml", fileOpener, database);
            liquibase.update(null)
        }
        finally {
            if (liquibase && liquibase.database) {
                liquibase.database.close()
            }
        }
        log.info("Finished running liquibase changelog(s)!")

		def enableFixtures = Boolean.parseBoolean(grailsApplication.config.openboxes.fixtures.enabled?:"true");
        log.info("Insert test fixtures?  " + enableFixtures)
		if (enableFixtures) {
            log.info("Inserting test fixtures ...")
            insertTestFixture()
        }

        //log.info("Default TimeZone set to " + TimeZone.getDefault().displayName)
        //TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        //log.info("Default TimeZone set to " + TimeZone.getDefault().displayName)


        // Debug logging used to figure out what log4j properties are ruining it for the rest of us
        getClass().getClassLoader().getResources("log4j.properties").each {
            log.info "log4j.properties => " + it
        }

        getClass().getClassLoader().getResources("log4j.xml").each {
            log.info "log4j.xml => " +  it
        }

        // Create uploads directory if it doesn't already exist
        def folder = new File("uploads")
        if (!folder.exists()) {
            log.info("Creating uploads directory if it doesn't already exist")
            folder.mkdirs()
        }
        else {
            log.info("Uploads directory already exists")
        }
    }


    def destroy = {

    }


    static String TestFixure = "Test-Fixture"


    def insertTestFixture() {
        if (Environment.current != Environment.DEVELOPMENT && Environment.current != Environment.TEST && Environment.current.name != "loadtest")
            return

        log.info("Setup test fixture...")

        createSupplierLocation()
        def medicines = Category.findByName("Medicines")
        def suppliers = Category.findByName("Supplies")

        assert medicines != null
        assert suppliers != null
        def testData = [
                ['expiration': new Date().plus(3), 'productGroup': 'PainKiller', 'quantity': 10000, 'product': new Product(category: medicines, name: "Advil 200mg", manufacturer: "ABC", productCode: "00001", manufacturerCode: "9001")]
                , ['expiration': new Date().plus(20), 'productGroup': 'PainKiller', 'quantity': 10000, 'product': new Product(category: medicines, name: "Tylenol 325mg", manufacturer: "MedicalGait", productCode: "00002", manufacturerCode: "9002")]
                , ['expiration': new Date().plus(120), 'productGroup': 'PainKiller', 'quantity': 10000, 'product': new Product(category: medicines, name: "Aspirin 20mg", manufacturer: "ABC", productCode: "00003", manufacturerCode: "9001")]
                , ['expiration': new Date().plus(200), 'productGroup': 'PainKiller', 'quantity': 10000, 'product': new Product(category: medicines, name: "General Pain Reliever", manufacturer: "MedicalGait", productCode: "00004", manufacturerCode: "9002")]
                , ['expiration': new Date().minus(1), 'productGroup': 'Iron', 'quantity': 10000, 'product': new Product(category: medicines, name: "Similac Advance low iron 400g", manufacturer: "ABC", productCode: "00005", manufacturerCode: "9001")]
                , ['expiration': new Date().minus(30), 'productGroup': 'Iron', 'quantity': 10000, 'product': new Product(category: medicines, name: "Similac Advance + iron 365g", manufacturer: "MedicalGait", productCode: "00006", manufacturerCode: "9002")]
                , ['expiration': new Date().plus(1000), 'productGroup': 'Laptop', 'quantity': 10000, 'product': new Product(category: suppliers, name: "MacBook Pro 8G", manufacturer: "Apple", productCode: "00007", manufacturerCode: "9003")]
                , ['expiration': null, 'productGroup': 'Paper', 'quantity': 10000, 'product': new Product(category: suppliers, name: "Print Paper A4", manufacturer: "DSC", productCode: "00008", manufacturerCode: "9004")]
        ]


        if (Environment.current == Environment.TEST)
            deleteTestFixture(testData)

        createTestFixtureIfNotExist(testData)
        if (Environment.current.name == "loadtest")
            createLoadtestData()

        log.info("Created test fixture.")
    }

    def createLoadtestData() {
        def medicines = Category.findByName("Medicines")
        def numberOfProducts = Integer.parseInt(System.getenv()["load"] ?: "5000")
        if (Product.count() >= numberOfProducts) return
        def nameSeeds = ["foo", "jing", "moon", "mountain", "evening", "smooth", "train", "paper", "sharp", "see", "ocean", "apple"]
        def data = []
        for (i in 0..numberOfProducts) {
            def nameSeed = nameSeeds[i % nameSeeds.size()]
            data.add(['expiration': new Date().plus(500), 'quantity': (i + 1) * 10, 'product': new Product(category: medicines, name: nameSeed + i, manufacturer: "ABC", productCode: "00001", manufacturerCode: "9001")])
        }
        createTestFixtureIfNotExist(data)
        log.info("load test data created.")
    }

    def createSupplierLocation() {
        def name = "Test Supplier"
        if (Location.findByName(name)) return
        def locationType = LocationType.get("4")
        def supplierLocation = new Location(name: name, locationType: locationType)
        supplierLocation.save(flush: true, failOnError: true)
    }

    def deleteTestFixture(List<Map<String, Object>> testData) {
        deleteRequisitions()
        testData.each { deleteProductAndInventoryItems(it) }
        Transaction.findByComment(TestFixure).each { it.delete() }
    }

    def createTestFixtureIfNotExist(List<Map<String, Object>> testData) {
        testData.each { addProductAndInventoryItemIfNotExist(it) }
    }

    private def deleteRequisitions() {
        Picklist.list().each { it.delete(failOnError: true, flush: true) }
        Requisition.list().each { it.delete(failOnError: true, flush: true) }
    }

    private def deleteProductAndInventoryItems(Map<String, Object> inventoryItemInfo) {
        def product = Product.findByName(inventoryItemInfo.product.name)
        if (!product) return
        def shipmentItems = ShipmentItem.findAllByProduct(product)

        for (shipmentItem in shipmentItems) {
            shipmentItem.delete(failOnError: true, flush: true)
        }

        def inventoryItems = InventoryItem.findAllByProduct(product)
        for (item in inventoryItems) {
            for (entry in TransactionEntry.findAllByInventoryItem(item)) {
                entry.delete(failOnError: true, flush: true)
            }

            item.delete(failOnError: true, flush: true)
        }
        product.delete(failOnError: true, flush: true)

    }

    private def addProductAndInventoryItemIfNotExist(Map<String, Object> inventoryItemInfo) {
        def productGroup = null
        if (inventoryItemInfo.productGroup) {
            productGroup = ProductGroup.findByName(inventoryItemInfo.productGroup)
            if (!productGroup) {
                productGroup = new ProductGroup(name: inventoryItemInfo.productGroup)
                productGroup.category = inventoryItemInfo.product.category
                productGroup.save(failOnError: true, flush: true)
            }
        }
        Product product = Product.findByName(inventoryItemInfo.product.name)

        if (!product) {
            log.info("creating product ${inventoryItemInfo.product.name}")
            product = inventoryItemInfo.product
            product.save(failOnError: true, flush: true)
            if (inventoryItemInfo.productGroup) {
                productGroup.addToProducts(product)
                productGroup.save(failOnError: true, flush: true)
                product.addToProductGroups(productGroup)
                product.save(failOnError: true, flush: true)
            }
            addInventoryItem(product, inventoryItemInfo.expiration, inventoryItemInfo.quantity)
        }
    }



    private def addInventoryItem(product, expirationDate, quantity) {

        InventoryItem item = new InventoryItem()
        item.product = product
        item.lotNumber = "lot57"
        item.expirationDate = expirationDate
        item.save(failOnError: true, flush: true)

        Location boston = Location.findByName("Boston Headquarters");
        assert boston != null

        Transaction transaction = new Transaction()
        transaction.createdBy = User.get(2)
        transaction.comment = TestFixure
        transaction.transactionDate = new Date().minus(1)
        transaction.inventory = boston.inventory
        transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

        TransactionEntry transactionEntry = new TransactionEntry()
        transactionEntry.quantity = quantity
        transactionEntry.inventoryItem = item

        transaction.addToTransactionEntries(transactionEntry)
        transaction.save(failOnError: true, flush: true)
    }


}
