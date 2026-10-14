package org.pih.warehouse.regression.platform.reporting

import spock.lang.PendingFeature

import org.pih.warehouse.reporting.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.ConsumptionService
import org.pih.warehouse.report.ReportService
import org.pih.warehouse.shipping.Shipment

class ConsumptionControllerSpec extends Specification implements ControllerUnitTest<ConsumptionController>, DataTest {

    Location warehouse
    Location clinic
    Location ward
    Product aspirin
    Product bandage
    Category medicines
    TransactionType transferOut
    TransactionType transferIn
    TransactionType consumption
    TransactionType expiration
    TransactionType damage

    InventoryService inventoryService = Mock()
    ProductAvailabilityService productAvailabilityService = Mock()
    DataService dataService = Mock()

    void setupSpec() {
        mockDomains(Location, Inventory, Product, Category, Tag, InventoryItem, Transaction, TransactionEntry,
                TransactionType, Shipment, User, ConsumptionFact)
    }

    void setup() {
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
            }, LocalizationService)
        }

        warehouse = new Location(name: "Warehouse").save(validate: false)
        warehouse.inventory = new Inventory(warehouse: warehouse).save(validate: false)
        clinic = new Location(name: "Clinic").save(validate: false)
        ward = new Location(name: "Ward").save(validate: false)
        medicines = new Category(name: "Medicines").save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01", pricePerUnit: 2.0, unitOfMeasure: "EA", category: medicines).save(validate: false)
        bandage = new Product(name: "Bandage", productCode: "BA01", pricePerUnit: 1.0, category: new Category(name: "Supplies").save(validate: false)).save(validate: false)

        transferOut = transactionType(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, "Transfer Out", TransactionCode.DEBIT)
        transferIn = transactionType(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, "Transfer In", TransactionCode.CREDIT)
        consumption = transactionType(Constants.CONSUMPTION_TRANSACTION_TYPE_ID, "Consumption", TransactionCode.DEBIT)
        expiration = transactionType(Constants.EXPIRATION_TRANSACTION_TYPE_ID, "Expired", TransactionCode.DEBIT)
        damage = transactionType(Constants.DAMAGE_TRANSACTION_TYPE_ID, "Damaged", TransactionCode.DEBIT)

        controller.inventoryService = inventoryService
        controller.productAvailabilityService = productAvailabilityService
        controller.dataService = dataService
        controller.userService = Stub(UserService) { hasRoleFinance(_) >> true }
        session.user = new User(username: "manager")
        session.warehouse = warehouse
    }

    private static TransactionType transactionType(String id, String name, TransactionCode code) {
        TransactionType type = new TransactionType(name: name, transactionCode: code)
        type.id = id
        return type
    }

    private Transaction transaction(TransactionType type, Date date, Location source, Location destination, Map<Product, Integer> quantities) {
        Transaction transaction = new Transaction(transactionType: type, transactionDate: date, source: source,
                destination: destination, inventory: warehouse.inventory)
        transaction.transactionEntries = quantities.collect { product, quantity ->
            new TransactionEntry(quantity: quantity, inventoryItem: new InventoryItem(product: product, lotNumber: "L1"))
        }
        return transaction
    }

    private ShowConsumptionCommand command() {
        ShowConsumptionCommand command = new ShowConsumptionCommand()
        command.fromLocations = [warehouse]
        command.fromDate = Date.parse("yyyy-MM-dd", "2024-01-01")
        command.toDate = Date.parse("yyyy-MM-dd", "2024-03-31")
        return command
    }

    private List<Transaction> debits() {
        Date january = Date.parse("yyyy-MM-dd", "2024-01-10")
        Date february = Date.parse("yyyy-MM-dd", "2024-02-10")
        return [
                transaction(transferOut, january, warehouse, clinic, [(aspirin): 10, (bandage): 4]),
                transaction(transferOut, february, warehouse, ward, [(aspirin): 5]),
                transaction(consumption, february, warehouse, null, [(aspirin): 3]),
                transaction(expiration, january, warehouse, null, [(aspirin): 2]),
                transaction(damage, february, warehouse, null, [(bandage): 1]),
        ]
    }

    private Transaction returnCredit() {
        Shipment returnShipment = Stub(Shipment) {
            getIsFromReturnOrder() >> true
        }
        Transaction credit = transaction(transferIn, Date.parse("yyyy-MM-dd", "2024-03-05"), clinic, warehouse, [(aspirin): 4])
        credit.incomingShipment = returnShipment
        return credit
    }

    void "show tallies issued, consumed, returned, expired and damaged quantities per product"() {
        given:
        ShowConsumptionCommand command = command()

        when:
        def model = controller.show(command)
        def aspirinRow = command.rows[aspirin]
        def bandageRow = command.rows[bandage]

        then:
        1 * inventoryService.getDebitsBetweenDates([warehouse], _, command.fromDate, { Date to -> to.format("yyyy-MM-dd") == "2024-04-01" }, _) >> debits()
        1 * inventoryService.getCreditsBetweenDates(_, [warehouse], command.fromDate, _) >> [returnCredit()]
        1 * productAvailabilityService.getCurrentInventory(warehouse) >> [(aspirin): 50]

        model.command.is(command)
        aspirinRow.transferOutQuantity == 15
        aspirinRow.issuedQuantity == 15
        aspirinRow.consumedQuantity == 3
        aspirinRow.returnedQuantity == 4
        aspirinRow.expiredQuantity == 2
        aspirinRow.transferInQuantity == 4
        aspirinRow.totalConsumptionQuantity == 14
        aspirinRow.onHandQuantity == 50
        aspirinRow.pricePerUnit == 2.0
        aspirinRow.transferOutMap[clinic] == 10
        aspirinRow.transferOutMap[ward] == 5
        aspirinRow.transferInMap[clinic] == 4
        aspirinRow.transferOutMonthlyMap["2024-01"] == 10
        aspirinRow.transferOutMonthlyMap["2024-02"] == 8
        aspirinRow.transactions.size() == 5
        aspirinRow.transferBalance == 17

        bandageRow.issuedQuantity == 4
        bandageRow.damagedQuantity == 1
        bandageRow.onHandQuantity == 0

        and: "destinations of the transactions become the selectable locations"
        command.toLocations.containsAll([clinic, ward])
        command.selectedLocations.containsAll([clinic, ward])
        command.selectedDates as Set == ["2024-01", "2024-02", "2024-03"] as Set
        command.transactionTypes as Set == [transferOut, consumption, expiration, damage, transferIn] as Set
    }

    void "show filters rows by the selected products and categories"() {
        given:
        ShowConsumptionCommand command = command()
        command.selectedProducts = [aspirin]
        command.selectedCategories = [medicines]
        command.includeQuantityOnHand = false
        inventoryService.getDebitsBetweenDates(*_) >> debits()
        inventoryService.getCreditsBetweenDates(*_) >> []

        when:
        controller.show(command)

        then:
        command.rows.keySet() as List == [aspirin]
        0 * productAvailabilityService.getCurrentInventory(_)
    }

    void "show hides unit prices from users without the finance role"() {
        given:
        controller.userService = Stub(UserService) { hasRoleFinance(_) >> false }
        ShowConsumptionCommand command = command()
        inventoryService.getDebitsBetweenDates(*_) >> debits()
        inventoryService.getCreditsBetweenDates(*_) >> []
        productAvailabilityService.getCurrentInventory(_) >> [:]

        when:
        controller.show(command)

        then:
        command.rows[aspirin].pricePerUnit == 0
    }

    void "show exports the consumption rows as CSV with monthly and location breakdowns"() {
        given:
        ShowConsumptionCommand command = command()
        command.selectedProperties = ["unitOfMeasure"]
        params.format = "csv"
        inventoryService.getDebitsBetweenDates(*_) >> debits()
        inventoryService.getCreditsBetweenDates(*_) >> [returnCredit()]
        productAvailabilityService.getCurrentInventory(_) >> [(aspirin): 50]
        List<Map> exported = null

        when:
        controller.show(command)

        then:
        1 * dataService.generateCsv(_) >> { args -> exported = args[0]; "csv-body" }
        response.contentType.startsWith("text/csv")
        response.getHeader("Content-disposition").startsWith("attachment; filename=\"Consumption-Warehouse-")
        response.text == "csv-body"

        exported*.get("Product code") == ["AS01", "BA01"]
        Map aspirinRow = exported[0]
        aspirinRow["Product"] == "Aspirin"
        aspirinRow["Category"] == "Medicines"
        aspirinRow["Qty Issued"] == "15"
        aspirinRow["Qty Consumed"] == "3"
        aspirinRow["Qty Returned"] == "4"
        aspirinRow["Total Consumption (Issued+Consumed-Returned)"] == "14"
        aspirinRow["Value Consumed"] == "28"
        aspirinRow["Quantity on hand"] == "50"
        aspirinRow["unitOfMeasure"] == "EA"
        aspirinRow["2024-01"] == 10
        aspirinRow["2024-02"] == 8
        aspirinRow["Clinic"] == 10
        aspirinRow["Ward"] == 5
    }

    void "show refuses a CSV download when the report parameters changed since the page was rendered"() {
        given:
        ShowConsumptionCommand command = command()
        command.parametersHash = "stale-hash"
        command.selectedTags = [new Tag(tag: "cold")]
        params.format = "csv"
        inventoryService.getCreditsBetweenDates(*_) >> []

        when:
        def model = controller.show(command)

        then:
        1 * inventoryService.getDebitsBetweenDates(*_) >> []
        0 * inventoryService.getProductsByTags(_)
        0 * dataService.generateCsv(_)
        flash.message == "Unable to download CSV as parameters have changed. Please try download again."
        command.selectedTags.isEmpty()
        model.command.is(command)
    }

    void "show re-renders the form when the command has errors"() {
        given:
        ShowConsumptionCommand command = new ShowConsumptionCommand()
        command.fromLocations = null
        command.validate()

        when:
        controller.show(command)

        then:
        view == "/consumption/show"
        model.command.is(command)
        0 * inventoryService._
    }

    void "the parameters hash only changes with dates and locations"() {
        given:
        ShowConsumptionCommand command = command()
        String hash = command.generateParametersHash()

        expect:
        hash ==~ /[0-9a-f]{32}/
        new ShowConsumptionCommand(fromLocations: []).generateParametersHash() == null

        when:
        command.parametersHash = hash

        then:
        !command.hasParameterChanged()

        when:
        command.toDate = command.toDate + 1

        then:
        command.hasParameterChanged()
        command.numberOfDays == 91
        command.numberOfWeeks == 13f
    }

    void "row averages divide by the reporting period"() {
        given:
        ShowConsumptionCommand command = command()   // 90 day period
        ShowConsumptionRowCommand row = new ShowConsumptionRowCommand(command: command, product: aspirin,
                totalConsumptionQuantity: 90, onHandQuantity: 60, transferOutQuantity: 45, expiredQuantity: 45)

        expect:
        row.monthlyQuantity == 30f
        row.numberOfMonthsRemaining == 2f
        row.dailyQuantity == 1f
        new ShowConsumptionRowCommand(command: command, totalConsumptionQuantity: 0).numberOfMonthsRemaining == 0f
    }

    @PendingFeature(reason = 'OBAF operator precedence drops the comma after a non-zero transfer-out quantity')
    void "transferOutLocations lists the quantity per location, separated by commas"() {
        given:
        ShowConsumptionRowCommand row = new ShowConsumptionRowCommand(command: command(), product: aspirin)
        row.transferOutMap[clinic] = 10

        expect:
        row.transferOutLocations([clinic, ward]) == "10,0,"
    }

    void "index redirects to the list"() {
        when:
        controller.index()

        then:
        response.redirectedUrl == "/consumption/list"
    }

    void "delete removes consumption records and reports how many"() {
        given:
        controller.consumptionService = Mock(ConsumptionService)

        when:
        controller.delete()

        then:
        1 * controller.consumptionService.deleteConsumptionRecords() >> 12
        flash.message.startsWith("Deleted 12 consumption records in ")
        response.redirectedUrl == "/consumption/list"
    }

    void "refresh rebuilds the consumption fact table"() {
        given:
        controller.reportService = Mock(ReportService)

        when:
        controller.refresh(new ConsumptionCommand())

        then:
        1 * controller.reportService.buildConsumptionFact()
        response.redirectedUrl == "/consumption/list"
    }

    void "pivot and list default to the last six months"() {
        given:
        ConsumptionCommand pivotCommand = new ConsumptionCommand()
        ConsumptionCommand listCommand = new ConsumptionCommand()

        when:
        def pivotModel = controller.pivot(pivotCommand)
        def listModel = controller.list(listCommand)

        then:
        pivotModel.command.endDate != null
        pivotModel.command.startDate < pivotModel.command.endDate - 170
        pivotModel.command.startDate > pivotModel.command.endDate - 190
        listModel.command.startDate != null
    }

    void "list downloads the consumption cross tab as CSV"() {
        given:
        controller.consumptionService = Mock(ConsumptionService)
        ConsumptionCommand command = new ConsumptionCommand(download: true, location: warehouse, category: medicines)

        when:
        controller.list(command)

        then:
        1 * controller.consumptionService.listConsumption(warehouse, medicines, _, _) >> [[row: 1]]
        1 * controller.consumptionService.generateCrossTab([[row: 1]], _, _, null) >> [[AS01: 3]]
        1 * dataService.generateCsv([[AS01: 3]]) >> "a,b"
        response.text == "a,b"
        response.getHeader("Content-disposition").startsWith("attachment; filename=Consumption-Warehouse-")
    }

    void "aggregate renders consumption facts as JSON for the session location"() {
        given:
        controller.consumptionService = Mock(ConsumptionService)
        ConsumptionFact fact = new ConsumptionFact(quantity: 7, unitCost: 1.5, unitPrice: 2.5)
        fact.productKey = new ProductDimension(productCode: "AS01", productName: "Aspirin", categoryName: "Medicines")
        fact.transactionDateKey = new DateDimension(year: 2024, month: 2, dayOfMonth: 14)

        when:
        controller.aggregate(new ConsumptionCommand())

        then:
        1 * controller.consumptionService.listConsumption(warehouse, null, _, _) >> [fact]
        response.json[0].productCode == "AS01"
        response.json[0].productName == "Aspirin"
        response.json[0].categoryName == "Medicines"
        response.json[0].year == 2024
        response.json[0].month == 2
        response.json[0].day == 14
        response.json[0].quantity == 7
    }
}
