/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import grails.util.Environment

import javax.sql.DataSource

import liquibase.Liquibase
import liquibase.database.DatabaseFactory

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.order.Order
import org.pih.warehouse.picklist.*
import org.pih.warehouse.product.*
import org.pih.warehouse.requisition.*
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem



class BootStrap {

	def shipmentService
	def productService
	
	DataSource dataSource;
	
	def init = { servletContext ->
		
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

		insertTestFixture()
		
		assignProductIdentifiers()
		assignShipmentIdentifiers()
		assignOrderIdentifiers()
		assignRequisitionIdentifiers()
		assignTransactionIdentifiers()
	}		
		
				
	def destroy = {
		
	}


    static String TestFixure = "Test-Fixture"


    def insertTestFixture(){
        if(Environment.current != Environment.DEVELOPMENT && Environment.current != Environment.TEST && Environment.current.name != "loadtest")
            return

        log.info("Setup test fixture...")

        createSupplierLocation()
        def medicines = Category.findByName("Medicines")
        def suppliers = Category.findByName("Supplies")

        assert medicines != null
        assert suppliers != null
        def  testData = [
         ['expiration':new Date().plus(3), 'productGroup':'PainKiller','quantity':10000, 'product':  new Product(category: medicines, name: "Advil 200mg", manufacturer:"ABC", productCode:"00001",manufacturerCode:"9001" )]
        ,['expiration':new Date().plus(20),'productGroup':'PainKiller', 'quantity':10000, 'product': new Product(category: medicines, name: "Tylenol 325mg", manufacturer:"MedicalGait", productCode:"00002",manufacturerCode:"9002" )]
        ,['expiration':new Date().plus(120),'productGroup':'PainKiller', 'quantity':10000, 'product': new Product(category: medicines, name: "Aspirin 20mg", manufacturer:"ABC", productCode:"00003",manufacturerCode:"9001" ) ]
        ,['expiration':new Date().plus(200),'productGroup':'PainKiller', 'quantity':10000, 'product': new Product(category: medicines, name: "General Pain Reliever", manufacturer:"MedicalGait", productCode:"00004",manufacturerCode:"9002" )]
        ,['expiration':new Date().minus(1),'productGroup':'Iron', 'quantity':10000, 'product': new Product(category: medicines, name: "Similac Advance low iron 400g", manufacturer:"ABC", productCode:"00005",manufacturerCode:"9001" )]
        ,['expiration':new Date().minus(30),'productGroup':'Iron', 'quantity':10000, 'product' : new Product(category: medicines, name: "Similac Advance + iron 365g", manufacturer:"MedicalGait", productCode:"00006",manufacturerCode:"9002" ) ]
        ,['expiration':new Date().plus(1000), 'productGroup':'Laptop','quantity':10000, 'product':new Product(category: suppliers, name: "MacBook Pro 8G", manufacturer:"Apple", productCode:"00007",manufacturerCode:"9003" ) ]
        ,['expiration':null, 'productGroup':'Paper','quantity':10000, 'product': new Product(category: suppliers, name: "Print Paper A4", manufacturer:"DSC", productCode:"00008",manufacturerCode:"9004" )]
                ]


       if(Environment.current == Environment.TEST)
           deleteTestFixture(testData)

        createTestFixtureIfNotExist(testData)
        if(Environment.current.name == "loadtest")
          createLoadtestData()

        log.info("Created test fixture.")
    }

    def createLoadtestData(){
      def medicines = Category.findByName("Medicines")
      def numberOfProducts = Integer.parseInt(System.getenv()["load"] ?: "5000")
      if(Product.count() >= numberOfProducts) return
      def nameSeeds = ["foo", "jing", "moon", "mountain", "evening", "smooth", "train", "paper", "sharp","see","ocean","apple"]
      def data = []
      for(i in 0..numberOfProducts){
        def nameSeed = nameSeeds[i%nameSeeds.size()]
        data.add( ['expiration':new Date().plus(500),'quantity':(i+1)*10, 'product':  new Product(category: medicines, name: nameSeed+i, manufacturer:"ABC", productCode:"00001",manufacturerCode:"9001" )]
)
      }
       createTestFixtureIfNotExist(data)
       log.info("load test data created.")
    }

    def createSupplierLocation(){
      def name = "Test Supplier"
      if(Location.findByName(name)) return
      def locationType = LocationType.get("4")
      def supplierLocation = new Location(name: name, locationType: locationType)
      supplierLocation.save(flush:true, failOnError: true)
    }

    def deleteTestFixture(List<Map<String, Object>> testData) {
        deleteRequisitions()
        testData.each {deleteProductAndInventoryItems(it)}
        Transaction.findByComment(TestFixure).each { it.delete()}
    }

    def createTestFixtureIfNotExist(List<Map<String, Object>> testData) {
        testData.each{ addProductAndInventoryItemIfNotExist(it)}
    }

    private def deleteRequisitions(){
      Picklist.list().each{ it.delete(failOnError: true, flush:true)}
      Requisition.list().each{ it.delete(failOnError: true, flush:true)}
    }

    private def deleteProductAndInventoryItems(Map<String, Object> inventoryItemInfo) {
        def product = Product.findByName(inventoryItemInfo.product.name)
        if(!product) return
        def shipmentItems = ShipmentItem.findAllByProduct(product)

        for(shipmentItem in shipmentItems){
            shipmentItem.delete(failOnError:true, flush:true)
        }

        def inventoryItems = InventoryItem.findAllByProduct(product)
        for (item in inventoryItems) {
            for (entry in TransactionEntry.findAllByInventoryItem(item)) {
                entry.delete(failOnError:true, flush:true)
            }

            item.delete(failOnError:true,flush:true)
        }
        product.delete(failOnError:true, flush:true)

    }

     private def addProductAndInventoryItemIfNotExist(Map<String, Object> inventoryItemInfo) {
        def productGroup = null
        if(inventoryItemInfo.productGroup){
          productGroup = ProductGroup.findByDescription(inventoryItemInfo.productGroup)
          if(!productGroup){
              productGroup = new ProductGroup(description: inventoryItemInfo.productGroup)
              productGroup.category = inventoryItemInfo.product.category
              productGroup.save(failOnError:true,flush:true)
          }
        }
        Product product = Product.findByName(inventoryItemInfo.product.name)

        if(!product){
          log.info("creating product ${inventoryItemInfo.product.name}")
           product = inventoryItemInfo.product
           product.save(failOnError:true,flush:true)
           if(inventoryItemInfo.productGroup){
             productGroup.addToProducts(product)
             productGroup.save(failOnError:true,flush:true)
             product.addToProductGroups(productGroup)
             product.save(failOnError:true,flush:true)
           }
           addInventoryItem(product, inventoryItemInfo.expiration, inventoryItemInfo.quantity)
        }


    }
	 
	void assignShipmentIdentifiers() { 		
		def shipments = Shipment.findAll("from Shipment as s where shipmentNumber is null or shipmentNumber = ''")
		shipments.each { shipment ->
			println "Assigning identifier to " + shipment.id + " " + shipment.name
			shipment.shipmentNumber = productService.generateIdentifier("NNNLLL")
			if (!shipment.save(flush:true,validate:false)) { 
				println shipment.errors
			}
		}
	}
	
	void assignRequisitionIdentifiers() {	
		def requisitions = Requisition.findAll("from Requisition as r where requestNumber is null or requestNumber = ''")
		requisitions.each { requisition ->
			println "Assigning identifier to " + requisition.id + " " + requisition.name
			requisition.requestNumber = productService.generateIdentifier("NNNLLL")
			if (!requisition.save(flush:true,validate:false)) { 
				println requisition.errors
			}
		}
	}
	
	void assignOrderIdentifiers() {
		def orders = Order.findAll("from Order as o where orderNumber is null or orderNumber = ''")
		orders.each { order ->
			println "Assigning identifier to " + order.id + " " + order.name
			order.orderNumber = productService.generateIdentifier("NNNLLL")
			if (!order.save(flush:true,validate:false)) { 
				println order.errors
			}
		}
	}
	void assignTransactionIdentifiers() {
		def transactions = Transaction.findAll("from Transaction as t where transactionNumber is null or transactionNumber = ''")
		transactions.each { transaction ->
			println "Assigning identifier to " + transaction.id 
			transaction.transactionNumber = productService.generateIdentifier("AAA-AAA-AAA")
			if (!transaction.save(flush:true,validate:false)) { 
				println transaction.errors
			}
		}
	}

	void assignProductIdentifiers() {
		try { 
			def products = Product.findAll("from Product as p where productCode is null or productCode = ''")			
			products.each { product -> 
				println "Assigning identifier to " + product.id + " " + product.name
				product.productCode = productService.generateIdentifier("LLNN")
				if (!product.save(flush:true,validate:false)) { 
					println product.errors
				}
			}
		} catch(Exception e) { 
			log.error("Unable to assign product identifier ", e)
		}
	}


    private def addInventoryItem(product, expirationDate, quantity){

        InventoryItem item = new InventoryItem()
        item.product = product
        item.lotNumber = "lot57"
        item.expirationDate = expirationDate
        item.save(failOnError:true, flush:true)

        Location boston =  Location.findByName("Boston Headquarters");
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
        transaction.save(failOnError:true, flush:true)
    }
		
	
}
