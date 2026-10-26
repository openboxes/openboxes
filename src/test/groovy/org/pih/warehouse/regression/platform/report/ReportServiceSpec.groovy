package org.pih.warehouse.regression.platform.report

import spock.lang.Ignore
import org.pih.warehouse.report.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

import org.pih.warehouse.core.DashboardService
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.UserService
import org.pih.warehouse.dashboard.NumberData
import org.pih.warehouse.dashboard.NumberDataService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.IndicatorApiCommand
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService

/**
 * The report service mostly builds SQL for the data service and reshapes the rows it returns; here the data service
 * is a mock, so the statements it receives and the shaping of canned rows can be pinned without a database.
 */
class ReportServiceSpec extends Specification implements ServiceUnitTest<ReportService>, DataTest {

    DataService dataService = Mock()
    DashboardService dashboardService = Mock()
    UserService userService = Mock()
    OrderService orderService = Mock()
    ShipmentService shipmentService = Mock()
    NumberDataService numberDataService = Mock()

    void setupSpec() {
        mockDomains(Location, Product, Category, OrderItem, OrderAdjustment, InvoiceType, Order, Organization, UnitOfMeasure, BudgetCode, PaymentTerm, Person)
    }

    void setup() {
        service.dataService = dataService
        service.dashboardService = dashboardService
        service.userService = userService
        service.orderService = orderService
        service.shipmentService = shipmentService
        service.numberDataService = numberDataService
        LocalizationService localizationStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationStub, LocalizationService)
        }
    }

    private static Product product(String id, String name, BigDecimal price = null) {
        Product product = new Product(name: name, productCode: id.toUpperCase(), pricePerUnit: price)
        product.id = id
        return product
    }

    private static InventoryLevel level(Integer min, Integer reorder, Integer max) {
        return new InventoryLevel(status: InventoryStatus.SUPPORTED, minQuantity: min, reorderQuantity: reorder, maxQuantity: max)
    }

    void 'calculateQuantityOnHandByProductGroup sorts every product into exactly one stock band'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)
        Map quantityMap = [
                (product('a', 'Not stocked'))   : [status: 'NOT_STOCKED', onHandQuantity: 0, inventoryLevel: null],
                (product('b', 'Stocked out'))   : [status: 'STOCK_OUT', onHandQuantity: 0, inventoryLevel: level(5, 10, 20)],
                (product('c', 'Low'))           : [status: 'LOW_STOCK', onHandQuantity: 3, inventoryLevel: level(5, 10, 20)],
                (product('d', 'Reorder'))       : [status: 'REORDER', onHandQuantity: 8, inventoryLevel: level(5, 10, 20)],
                (product('e', 'In stock', 2.5)) : [status: 'IN_STOCK', onHandQuantity: 4, inventoryLevel: null],
                (product('f', 'Ideal'))         : [status: 'IDEAL_STOCK', onHandQuantity: 15, inventoryLevel: level(5, 10, 20)],
                (product('g', 'Over'))          : [status: 'OVERSTOCK', onHandQuantity: 30, inventoryLevel: level(5, 10, 20)],
        ]

        when:
        Map result = service.calculateQuantityOnHandByProductGroup(depot.id)

        then:
        1 * dashboardService.getInventoryStatusAndLevel(_) >> quantityMap
        1 * userService.hasRoleFinance() >> true

        and: 'one product group per product, each in its own band'
        result.productSummary.totalCount == 7
        result.productGroupSummary.totalCountProductGroupsExpected == 7
        result.productGroupSummary.totalCountProductGroupsActual == 7
        ['NOT_STOCKED', 'STOCK_OUT', 'LOW_STOCK', 'REORDER', 'IN_STOCK', 'IDEAL_STOCK', 'OVERSTOCK'].every {
            result.productGroupSummary[it].numProductGroups == 1
        }
        result.productGroupSummary.INVALID.numProductGroups == 0
        result.productGroupDetails.LOW_STOCK.keySet() == ['Low'] as Set
        result.productGroupDetails.OVERSTOCK.keySet() == ['Over'] as Set

        and: 'inventory level counts and finance-only values'
        result.inventoryLevelSummary.hasInventoryLevel == 5
        result.inventoryLevelSummary.hasNoInventoryLevel == 2
        result.inventoryLevelSummary.singleInventoryLevel == 5
        result.productGroupDetails.ALL['In stock'].totalValue == 10.0
        result.productGroupDetails.ALL['In stock'].totalValueFormatted == '10.00'
        result.productGroupDetails.ALL['Low'].status == 'LOW_STOCK'
    }

    void 'calculateQuantityOnHandByProductGroup hides prices from users without the finance role'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)

        when:
        Map result = service.calculateQuantityOnHandByProductGroup(depot.id)

        then:
        1 * dashboardService.getInventoryStatusAndLevel(_) >> [(product('e', 'Priced', 2.5)): [status: 'IN_STOCK', onHandQuantity: 4, inventoryLevel: null]]
        1 * userService.hasRoleFinance() >> false
        result.productGroupDetails.ALL['Priced'].unitPrice == 0
        result.productGroupDetails.ALL['Priced'].totalValue == 0
    }

    void 'the dimension and fact tables are rebuilt with plain statements'() {
        given:
        List<List<String>> batches = []
        dataService.executeStatements(_ as List) >> { args -> batches << (args[0] as List<String>) }

        when:
        service.truncateFacts()
        service.truncateDimensions()
        service.buildTransactionTypeDimension()
        service.buildLotDimension()
        service.buildProductDimension()
        service.buildLocationDimension()
        service.buildTransactionFact()
        service.buildConsumptionFact()
        service.createStockoutFact()
        service.refreshProductDemandData()

        then:
        batches.size() == 10
        batches[0].contains('delete from transaction_fact')
        batches[1].contains('delete from product_dimension')
        batches[2][0].contains('INSERT into transaction_type_dimension')
        batches[3][0].contains('INSERT INTO lot_dimension')
        batches[4][0].contains('INSERT INTO product_dimension')
        batches[5][0].contains('INSERT INTO location_dimension')
        batches[6][1].contains('insert into transaction_fact')
        batches[7][1].contains("WHERE transaction_type.transaction_code = 'DEBIT'")
        batches[8][0].contains('CREATE TABLE IF NOT EXISTS stockout_fact')
        batches[9].size() == 7
        batches[9].last().contains('ALTER TABLE product_demand_details ADD INDEX')
    }

    void 'stockout facts for a day are deleted and repopulated for that date only'() {
        given:
        Date date = Date.parse('yyyy-MM-dd HH:mm', '2026-03-04 15:30')
        List<String> statements = []
        dataService.executeStatement(_ as String) >> { String statement -> statements << statement }

        when:
        service.deleteStockoutFact(date)
        service.populateStockoutFact(date)

        then:
        statements.size() == 2
        statements[0].contains("WHERE date_dimension.date = '2026-03-04'")
        statements[1].contains("where date_dimension.date = '2026-03-04'")
        statements[1].contains('on duplicate key update')
    }

    void 'getStockoutData passes the location, product and day window as parameters'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.id = 'loc'
        Product item = product('p', 'Item')

        when:
        def result = service.getStockoutData(depot, item, 30)

        then:
        1 * dataService.executeQuery({ it.contains('FROM stockout_fact') }, [locationId: 'loc', productId: 'p', days: 30]) >> [[stockoutDays: 4]]
        result == [[stockoutDays: 4]]
    }

    void 'getOnOrderSummary adds the quantities on order and on hand per product'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.id = 'loc'

        when:
        List rows = service.getOnOrderSummary(depot)

        then:
        1 * dataService.executeQuery({ it.contains('from on_order_summary oos') }, { it.locationId == 'loc' && it.synonymTypeCode == 'DISPLAY_NAME' }) >> [
                [productCode: 'P1', productName: 'Gauze', displayName: 'Gaze', qtyOnHand: 5, qtyOrderedNotShipped: 3, qtyShippedNotReceived: 2,
                 productFamilyName: 'Dressings', productCategoryName: 'Supplies', productCatalogs: 'Essential'],
                [productCode: 'P2', productName: 'Tape', displayName: null, qtyOnHand: null, qtyOrderedNotShipped: null, qtyShippedNotReceived: null],
        ]
        rows.size() == 2
        rows[0].displayNameWithLocaleCode == 'Gauze (EN: Gaze)'
        rows[0].totalOnOrder == 5
        rows[0].totalOnHandAndOnOrder == 10
        rows[0].productFamily == 'Dressings'
        rows[1].displayNameWithLocaleCode == 'Tape'
        rows[1].totalOnHand == 0
        rows[1].totalOnOrder == 0
        rows[1].qtyOrderedNotShipped == ''
        rows[1].category == ''
    }

    void 'getQuantityOnOrder sums ordered-not-shipped and shipped-not-received quantities per product'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)
        Product gauze = new Product(name: 'Gauze')
        gauze.save(validate: false, flush: true)
        OrderItem ordered = Stub(OrderItem) {
            getProduct() >> gauze
            getQuantityRemaining() >> 2
            getQuantityPerUom() >> 10
        }
        ShipmentItem shipped = Stub(ShipmentItem) {
            getProduct() >> gauze
            getQuantityRemaining() >> 7
        }

        when:
        Map result = service.getQuantityOnOrder(depot.id, [gauze.id])

        then:
        1 * orderService.getPendingInboundOrderItems(depot, [gauze]) >> [ordered]
        1 * shipmentService.getPendingInboundShipmentItems(depot, [gauze]) >> [shipped]
        result == [(gauze.id): [qtyOrderedNotShipped: 20, qtyShippedNotReceived: 7]]
    }

    void 'getOnOrderData joins the pending quantities onto the on-hand rows'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)
        Product gauze = new Product(name: 'Gauze')
        gauze.save(validate: false, flush: true)
        orderService.getPendingInboundOrderItems(*_) >> []
        shipmentService.getPendingInboundShipmentItems(*_) >> [Stub(ShipmentItem) { getProduct() >> gauze; getQuantityRemaining() >> 4 }]

        when:
        List rows = service.getOnOrderData(depot.id, [gauze.id, 'tape'])

        then:
        1 * dataService.executeQuery({ it.contains("oos.product_id in ('${gauze.id}','tape')") }, [locationId: depot.id]) >> [
                [productId: gauze.id, qtyOnHand: 9],
                [productId: 'tape', qtyOnHand: null],
        ]
        rows == [
                [productId: gauze.id, totalOnOrder: 4, totalOnHand: 9],
                [productId: 'tape', totalOnOrder: 0, totalOnHand: 0],
        ]
    }

    void 'getForecastReport averages demand per month over the period and adds on-order data'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)
        Category parent = new Category(name: 'Parent')
        parent.save(validate: false, flush: true)
        Date start = Date.parse('yyyy-MM-dd', '2026-01-01')
        Map params = [originId: depot.id, startDate: start, endDate: start + 60, category: parent.id,
                      locations: ['ward-1', 'ward-2'] as String[], tags: 'tag-1', catalogs: ['cat-1'] as String[]]
        String query = null
        orderService.getPendingInboundOrderItems(*_) >> []
        shipmentService.getPendingInboundShipmentItems(*_) >> []

        when:
        List rows = service.getForecastReport(params)

        then:
        1 * dataService.executeQuery({ it.contains('FROM product_demand_details pdd') }, params) >> { String sql, Map ignored ->
            query = sql
            return [
                    [product_id: 'p1', quantity_demand: 30],
                    [product_id: 'p1', quantity_demand: 90],
                    [product_id: 'p2', quantity_demand: 6],
            ]
        }
        1 * dataService.executeQuery({ it.contains('from on_order_summary oos') }, _) >> [[productId: 'p1', qtyOnHand: 12]]
        query.contains("pdd.destination_id in ('ward-1','ward-2')")
        query.contains("product.category_id in ('${parent.id}')")
        query.contains("product_tag.tag_id in ('tag-1')")
        query.contains("product_catalog_item.product_catalog_id in ('cat-1')")
        rows.size() == 2
        rows.find { it.productId == 'p1' }.averageMonthlyDemand == 60
        rows.find { it.productId == 'p1' }.totalOnHand == 12
        rows.find { it.productId == 'p2' }.averageMonthlyDemand == 3
        rows.find { it.productId == 'p2' }.totalOnHand == ''
    }

    void 'getForecastReport returns nothing when no demand was recorded'() {
        when:
        List rows = service.getForecastReport([originId: 'loc', startDate: new Date() - 30, endDate: new Date()])

        then:
        1 * dataService.executeQuery(_ as String, _ as Map) >> []
        rows == []
    }

    void 'getAmountOutstandingOnOrders returns one blank row when nothing is outstanding'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)

        when:
        List rows = service.getAmountOutstandingOnOrders(depot.id)

        then:
        2 * dataService.executeQuery({ it.contains('AND o.destination_id = :currentLocationId') }, { it.currentLocationId == depot.id }) >> []
        rows.size() == 1
        rows[0]['PO Number'] == ''
        rows[0].keySet()*.toString().contains('Cost per UOM (USD)')
    }

    void 'getAmountOutstandingOnOrders skips rows whose item or adjustment no longer exists'() {
        when:
        List rows = service.getAmountOutstandingOnOrders('missing-location')

        then:
        1 * dataService.executeQuery({ it.contains('order_item_invoice_summary') }, _) >> [[id: 'gone', quantity_ordered: 5]]
        1 * dataService.executeQuery({ it.contains('order_adjustment_payment_status') }, _) >> [[id: 'gone-too']]
        rows.size() == 1
        rows[0].Supplier == ''
    }

    void 'getProductsInventoried reports the number of products counted in the range'() {
        given:
        Location depot = new Location(name: 'Depot')
        Date start = new Date() - 7
        Date end = new Date()
        IndicatorApiCommand command = new IndicatorApiCommand(facility: depot, startDate: start, endDate: end)

        when:
        Map result = service.getProductsInventoried(command)

        then:
        1 * numberDataService.getItemsInventoriedInRange(depot, start, end) >> new NumberData(12)
        result == [name: 'productsInventoried', value: 12, type: 'SINGLE']
    }

    void 'getProductsInventoried reports zero when there is no data'() {
        when:
        Map result = service.getProductsInventoried(new IndicatorApiCommand())

        then:
        1 * numberDataService.getItemsInventoriedInRange(*_) >> null
        result.value == 0
    }

    private static TransactionEntry entry(TransactionCode code, int quantity, String typeName = code.name()) {
        TransactionType type = new TransactionType(name: typeName, transactionCode: code)
        Transaction transaction = new Transaction(transactionType: type)
        return new TransactionEntry(transaction: transaction, quantity: quantity)
    }

    void 'credits are positive credit entries; debits are debit entries and negative credits'() {
        given:
        TransactionEntry credit = entry(TransactionCode.CREDIT, 10)
        TransactionEntry negativeCredit = entry(TransactionCode.CREDIT, -3)
        TransactionEntry debit = entry(TransactionCode.DEBIT, 4)
        List<TransactionEntry> entries = [credit, negativeCredit, debit]

        expect:
        service.getCreditTransactionEntries(entries) == [credit]
        service.getDebitTransactionEntries(entries) == [negativeCredit, debit]
        service.isTransactionEntryCredit(credit)
        !service.isTransactionEntryDebit(credit)
        service.isTransactionEntryDebit(negativeCredit)

        and: 'walking a balance back over the entries removes credits and restores debits'
        service.calculateBalance(entries, 100) == 100 - 10 + 7
        service.calculateBalance([], 100) == 100
    }

    void 'the detailed transaction data totals the quantities per transaction type and product'() {
        given:
        Product gauze = product('gauze', 'Gauze')
        Map data = [(gauze): [entry(TransactionCode.CREDIT, 10, 'Receipt'), entry(TransactionCode.CREDIT, 5, 'Receipt'),
                              entry(TransactionCode.DEBIT, 4, 'Expired')]]

        expect:
        service.getDetailedTransactionReportData(data) == [(gauze): [Receipt: 15, Expired: 4]]
    }

    void 'a transaction report row has the summary columns in order'() {
        when:
        Map row = service.getTransactionReportRow(productCode: 'P1', name: 'Gauze', displayName: 'Gaze', categoryName: 'Supplies',
                pricePerUnit: 2, openingBalance: 5, credits: 10, debits: 3, adjustments: 7, closingBalance: 12)

        then:
        row.keySet() as List == ['Code', 'Name', 'Display Name', 'Category', 'Unit Cost', 'Opening', 'Credits', 'Debits',
                                 'Adjustments', 'Closing']
        row.Closing == 12
    }

    void 'a detailed transaction report row nets the adjustment types and adds one column per other type'() {
        given:
        TransactionType adjustmentCredit = new TransactionType(name: 'Adjustment credit', transactionCode: TransactionCode.CREDIT)
        adjustmentCredit.id = '3'
        TransactionType adjustmentDebit = new TransactionType(name: 'Adjustment debit', transactionCode: TransactionCode.DEBIT)
        adjustmentDebit.id = '10'
        TransactionType receipt = new TransactionType(name: 'Receipt', transactionCode: TransactionCode.CREDIT)
        receipt.id = 'receipt'
        TransactionEntry backdatedCredit = entry(TransactionCode.CREDIT, 6)
        TransactionEntry backdatedDebit = entry(TransactionCode.DEBIT, 2)

        when:
        Map row = service.getTransactionReportDetailedRow(productCode: 'P1', name: 'Gauze', productFamilyName: '', displayName: '',
                categoryName: 'Supplies', formulary: 'Essential', tag: 'Fast', pricePerUnit: 1, openingBalance: 0, credits: 9,
                debits: 2, closingBalance: 7,
                detailedReportData: ['Adjustment credit': 5, 'Adjustment debit': 2, Receipt: 4],
                availableTransactionTypes: [adjustmentCredit, adjustmentDebit, receipt],
                backdatedTransactionEntries: [backdatedCredit, backdatedDebit])

        then:
        row['Adjustment (Credit and Debit)'] == 3
        row.Receipt == 4
        !row.containsKey('Adjustment credit')
        row.Closing == 7
        row['Backdated inventory correction'] == 4
        row.Formulary == 'Essential'
    }

    void 'outstanding order items and adjustments become rows sorted by order number'() {
        given:
        Organization supplier = new Organization(code: 'SUP', name: 'Supplier Inc').save(validate: false, flush: true)
        Location origin = new Location(name: 'Supplier', organization: supplier).save(validate: false, flush: true)
        Location destination = new Location(name: 'Depot').save(validate: false, flush: true)
        PaymentTerm terms = new PaymentTerm(name: 'Net 30').save(validate: false, flush: true)
        BudgetCode budget = new BudgetCode(code: 'BUD-1').save(validate: false, flush: true)
        Order order = new Order(orderNumber: 'PO-2', origin: origin, destination: destination, paymentTerm: terms)
                .save(validate: false, flush: true)
        Order otherOrder = new Order(orderNumber: 'PO-1', origin: origin, destination: destination).save(validate: false, flush: true)
        Product gauze = new Product(name: 'Gauze', productCode: 'G1').save(validate: false, flush: true)
        OrderItem item = new OrderItem(order: order, product: gauze, quantity: 10, quantityPerUom: 10, unitPrice: 2.5,
                quantityUom: new UnitOfMeasure(code: 'BX', name: 'Box').save(validate: false, flush: true), budgetCode: budget,
                estimatedReadyDate: Date.parse('yyyy-MM-dd', '2026-05-01')).save(validate: false, flush: true)
        OrderAdjustment adjustment = new OrderAdjustment(order: otherOrder, amount: 15, budgetCode: budget).save(validate: false, flush: true)
        LocalizationService localizationStub = Stub(LocalizationService) { getCurrentLocale() >> Locale.ENGLISH }
        defineBeans { localizationService(InstanceFactoryBean, localizationStub, LocalizationService) }

        when:
        List rows = service.getAmountOutstandingOnOrders(destination.id)

        then:
        1 * dataService.executeQuery({ it.contains('order_item_invoice_summary') }, _) >> [
                [id: item.id, quantity_ordered: 10, quantity_shipped: 4, quantity_invoiced: 1]]
        1 * dataService.executeQuery({ it.contains('order_adjustment_payment_status') }, _) >> [[id: adjustment.id]]
        rows*.'PO Number' == ['PO-1', 'PO-2']

        and: 'the adjustment row'
        rows[0].Type == 'Adjustment'
        rows[0].Supplier == 'SUP - Supplier Inc'
        rows[0]['Total Value not invoiced'] == '15.00'
        rows[0]['Budget Code'] == 'BUD-1'

        and: 'the item row: 6 ordered and not shipped, 3 shipped and not invoiced, 9 not invoiced'
        Map itemRow = rows[1]
        itemRow.Type == 'Item'
        itemRow.Code == 'G1'
        itemRow.UOM == 'BX/10'
        itemRow['Qty Ordered not shipped (UOM)'] == '6'
        itemRow['Qty Ordered not shipped (Each)'] == '60'
        itemRow['Value ordered not shipped'] == '15.00'
        itemRow['Qty Shipped not Invoiced (UOM)'] == '3'
        itemRow['Total Qty not Invoiced (UOM)'] == '9'
        itemRow['Total Value not invoiced'] == '22.50'
        itemRow['Payment Terms'] == 'Net 30'
        itemRow['Estimated Ready Date'] == '05/01/2026'
    }

}
