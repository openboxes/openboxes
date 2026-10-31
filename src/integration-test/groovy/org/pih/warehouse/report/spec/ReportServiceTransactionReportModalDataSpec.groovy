package org.pih.warehouse.report.spec

import grails.gorm.transactions.Rollback
import grails.util.GrailsWebMockUtil
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.ReportService

/**
 * Tests ReportService.getTransactionReportModalData, which lists the transactions of a product over a period with a
 * running balance, for the details dialog of the transaction report.
 *
 * Transactions are inserted with SQL instead of GORM so that we control date_created (GORM always sets it to now)
 * and so that saving them doesn't trigger a product availability refresh.
 */
@Rollback
class ReportServiceTransactionReportModalDataSpec extends IntegrationSpec {

    private static final String ADJUSTMENT_CREDIT = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
    private static final String TRANSFER_OUT = Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID
    private static final String PRODUCT_INVENTORY = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
    private static final String INVENTORY_BASELINE = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID

    @Autowired
    ReportService reportService

    @Autowired
    SessionFactory sessionFactory

    Location facility
    Product product
    InventoryItem item1
    InventoryItem item2

    // The report period: transactions after day 5 (exclusive) and before day 15 (exclusive)
    Date startDate = day(5)
    Date endDate = day(15)

    void setup() {
        // The transaction type names are localized with the locale of the current request
        GrailsWebMockUtil.bindMockWebRequest()

        facility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        product = new ProductTestBuilder().randomizedName().category(new CategoryTestBuilder().build(true)).build(true)
        item1 = new InventoryItem(product: product, lotNumber: "L1").save(failOnError: true)
        item2 = new InventoryItem(product: product, lotNumber: "L2").save(failOnError: true)
        sessionFactory.currentSession.flush()
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void 'a #type count sets the balance to the counted quantity'() {
        given: 'a count that recorded 80 when the transactions add up to 90, e.g. because a transaction was deleted'
        insertTransaction(ADJUSTMENT_CREDIT, day(1), [[item1, 100]])
        insertTransaction(TRANSFER_OUT, day(6), [[item1, 10]])
        insertTransaction(transactionTypeId, day(7), [[item1, 80]])
        insertTransaction(ADJUSTMENT_CREDIT, day(8), [[item1, 5]])

        when:
        List<Map> rows = modalData()

        then:
        rows.transactionCode == ["BALANCE_OPENING", "DEBIT", "PRODUCT_INVENTORY", "CREDIT", "BALANCE_CLOSING"]
        rows.balance == [100, 90, 80, 85, 85]

        where:
        type                   | transactionTypeId
        "product inventory"    | PRODUCT_INVENTORY
        "inventory baseline"   | INVENTORY_BASELINE
    }

    void 'the balance of a count with several entries is the sum of its entries'() {
        given:
        insertTransaction(ADJUSTMENT_CREDIT, day(1), [[item1, 100]])
        insertTransaction(INVENTORY_BASELINE, day(7), [[item1, 30], [item2, 20]])
        insertTransaction(TRANSFER_OUT, day(8), [[item2, 5]])

        when:
        List<Map> rows = modalData()

        then: 'the entries of the count can be in either order'
        rows.balance[0] == 100
        rows.balance[1] in [20, 30]
        rows.balance[2..4] == [50, 45, 45]
    }

    void 'transactions with the same transaction date are in the order they were created'() {
        given: 'a debit entered after a count with the same transaction date, inserted first'
        insertTransaction(ADJUSTMENT_CREDIT, day(1), [[item1, 100]])
        insertTransaction(TRANSFER_OUT, day(7), [[item1, 10]], day(7, 20))
        insertTransaction(INVENTORY_BASELINE, day(7), [[item1, 50]], day(7, 10))

        when:
        List<Map> rows = modalData()

        then: 'the count comes first, so the debit is taken from the counted quantity, like quantity on hand'
        rows.transactionCode == ["BALANCE_OPENING", "PRODUCT_INVENTORY", "DEBIT", "BALANCE_CLOSING"]
        rows.balance == [100, 50, 40, 40]
    }

    private List<Map> modalData() {
        // So that the transaction entries inserted with SQL are loaded from the database
        sessionFactory.currentSession.clear()
        return reportService.getTransactionReportModalData(facility, product, startDate, endDate) as List<Map>
    }

    /**
     * MySQL doesn't store milliseconds in a datetime column, so we stay at whole seconds.
     */
    private static Date day(int day, int seconds = 0) {
        Calendar calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(2025, Calendar.JANUARY, day, 0, 0, seconds)
        return calendar.time
    }

    /**
     * Inserts a transaction with the given entries, each given as [inventory item, quantity].
     */
    private void insertTransaction(String transactionTypeId, Date transactionDate, List<List> entries,
                                   Date dateCreated = transactionDate) {
        String transactionId = UUID.randomUUID().toString()
        sessionFactory.currentSession.createSQLQuery("""
                INSERT INTO transaction (id, version, date_created, last_updated, inventory_id, transaction_date,
                                         transaction_type_id)
                VALUES (:id, 0, :dateCreated, :dateCreated, :inventoryId, :transactionDate, :transactionTypeId)
            """)
                .setString("id", transactionId)
                .setTimestamp("dateCreated", dateCreated)
                .setString("inventoryId", facility.inventory.id)
                .setTimestamp("transactionDate", transactionDate)
                .setString("transactionTypeId", transactionTypeId)
                .executeUpdate()

        entries.eachWithIndex { entry, int index ->
            sessionFactory.currentSession.createSQLQuery("""
                    INSERT INTO transaction_entry (id, version, transaction_id, inventory_item_id, quantity,
                                                   transaction_entries_idx)
                    VALUES (:id, 0, :transactionId, :inventoryItemId, :quantity, :index)
                """)
                    .setString("id", UUID.randomUUID().toString())
                    .setString("transactionId", transactionId)
                    .setString("inventoryItemId", (entry[0] as InventoryItem).id)
                    .setInteger("quantity", entry[1] as Integer)
                    .setInteger("index", index)
                    .executeUpdate()
        }
    }
}
