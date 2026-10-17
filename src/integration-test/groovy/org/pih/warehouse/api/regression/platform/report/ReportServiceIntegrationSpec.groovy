package org.pih.warehouse.api.regression.platform.report


import spock.lang.Ignore
import org.pih.warehouse.report.*
import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.IndicatorApiCommand
import org.pih.warehouse.reporting.InventoryAuditCommand

/**
 * The transaction report and the other database-backed reports of a depot with a known history of one product:
 * a baseline of 20 ten days ago, an adjustment of +5 five days ago and a consumption of 3 three days ago.
 */
class ReportServiceIntegrationSpec extends ServiceIntegrationSpec {

    ReportService reportService

    Location depot
    Product product
    InventoryItem lot

    void setup() {
        depot = newDepot('Report depot')
        product = newProduct()
        String lotNumber = uniqueName('LOT')
        lot = stock(depot, product, lotNumber, null, 20, null, new Date() - 10)
        stock(depot, product, lotNumber, null, 5, null, new Date() - 5, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        stock(depot, product, lotNumber, null, 3, null, new Date() - 3, Constants.CONSUMPTION_TRANSACTION_TYPE_ID)
    }

    void 'the transaction report has the opening and closing balance and the credits and debits in between'() {
        when:
        List rows = asAdminAt(depot) {
            reportService.getTransactionReport(Location.get(depot.id), null, null, null, [Product.get(product.id)], new Date() - 7,
                    new Date() + 1, false)
        }

        then:
        rows.size() == 1
        rows[0].Code == product.productCode
        rows[0].Opening == 20
        rows[0].Credits == 5
        rows[0].Debits == 3
        rows[0].Adjustments == 2
        rows[0].Closing == 22
    }

    void 'the detailed transaction report adds a column per transaction type and the backdated corrections'() {
        when:
        List rows = asAdminAt(depot) {
            reportService.getTransactionReport(Location.get(depot.id), null, null, null, [Product.get(product.id)], new Date() - 7,
                    new Date() + 1, true)
        }

        then:
        rows.size() == 1
        Map row = rows[0]
        row.Opening == 20
        row.Closing == 22
        row['Adjustment (Credit and Debit)'] == 5
        row.Consumption == 3
        row['Backdated inventory correction'] == 2
        row.Formulary == ''
    }

    void 'the transaction report filters by category, tag and catalog'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            Product saved = Product.get(product.id)
            [
                    category: reportService.getTransactionReport(location, [saved.category], null, null, null, new Date() - 7,
                            new Date() + 1, false)*.Code,
                    tag     : reportService.getTransactionReport(location, null, [new org.pih.warehouse.core.Tag(tag: uniqueName('t')).save(flush: true)],
                            null, null, new Date() - 7, new Date() + 1, false),
                    catalog : reportService.getTransactionReport(location, null, null,
                            [new org.pih.warehouse.product.ProductCatalog(name: uniqueName('c'), code: uniqueName('c')).save(flush: true)],
                            null, new Date() - 7, new Date() + 1, false),
            ]
        }

        then:
        result.category == [product.productCode]
        result.tag == []
        result.catalog == []
    }

    void 'the transaction history of a product walks the balance from opening to closing'() {
        when:
        List entries = asAdminAt(depot) {
            reportService.getTransactionReportModalData(Location.get(depot.id), Product.get(product.id), new Date() - 7, new Date() + 1)
        }

        then:
        entries*.transactionCode == ['BALANCE_OPENING', 'CREDIT', 'DEBIT', 'BALANCE_CLOSING']
        entries*.balance == [20, 25, 22, 22]
        entries[1].quantity == 5
        entries[1].transactionTypeName == 'Adjustment'
    }

    void 'the earliest transaction entry of a product in an inventory is the baseline'() {
        when:
        Map earliest = asAdminAt(depot) {
            TransactionEntry entry = reportService.getEarliestTransactionEntry(Product.get(product.id), Location.get(depot.id).inventory)
            [quantity: entry.quantity, type: entry.transaction.transactionType.id]
        }

        then:
        earliest.quantity == 20
        earliest.type == Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID
    }

    void 'the on-order, outstanding, stockout and forecast queries run for a depot without orders'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    onOrder    : reportService.getOnOrderSummary(location),
                    onOrderData: reportService.getOnOrderData(depot.id, [product.id]),
                    outstanding: reportService.getAmountOutstandingOnOrders(depot.id),
                    stockout   : reportService.getStockoutData(location, Product.get(product.id), 30),
                    forecast   : reportService.getForecastReport([originId: depot.id, startDate: new Date() - 30, endDate: new Date()]),
            ]
        }

        then:
        result.onOrder == []
        result.onOrderData == []
        result.outstanding.size() == 1
        result.outstanding[0]['PO Number'] == ''
        result.stockout[0].stockoutDays == 0
        result.forecast == []
    }

    void 'a date dimension is saved once per day, also when the stockout facts of a day are built'() {
        given:
        Date day = Date.parse('yyyy-MM-dd HH:mm', '1999-03-04 15:30')

        when:
        asAdminAt(depot) { reportService.saveDateDimension(day) }
        asAdminAt(depot) { reportService.buildStockoutFact(Date.parse('yyyy-MM-dd HH:mm', '1999-03-04 09:00')) }
        Map saved = asAdminAt(depot) {
            List<DateDimension> dimensions = DateDimension.findAllByDate(Date.parse('yyyy-MM-dd', '1999-03-04'))
            [count: dimensions.size(), month: dimensions[0].month, year: dimensions[0].year, monthYear: dimensions[0].monthYear]
        }

        then:
        saved.count == 1
        saved.month == 3
        saved.year == 1999
        saved.monthYear == '03-1999'

        cleanup:
        committed { DateDimension.findAllByDate(Date.parse('yyyy-MM-dd', '1999-03-04'))*.delete() }
    }
}
