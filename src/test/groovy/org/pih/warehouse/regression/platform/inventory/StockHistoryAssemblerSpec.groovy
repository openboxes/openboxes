package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

/**
 * The row computation is exercised directly through computeStockHistoryRows with hand-built transaction maps, so each
 * feature can describe one ledger scenario. The HQL projections (executeQuery) are not supported by the in-memory
 * datastore, so they are replaced per class with canned rows; what is under test is how those rows are turned into
 * the id-keyed lookups the page reads.
 */
class StockHistoryAssemblerSpec extends Specification implements DataTest {

    TransactionType creditType = new TransactionType(name: 'Receipt', transactionCode: TransactionCode.CREDIT)
    TransactionType debitType = new TransactionType(name: 'Issue', transactionCode: TransactionCode.DEBIT)
    TransactionType inventoryType = new TransactionType(name: 'Count', transactionCode: TransactionCode.INVENTORY)
    TransactionType productInventoryType = new TransactionType(name: 'Baseline',
            transactionCode: TransactionCode.PRODUCT_INVENTORY)

    Location binA = new Location(name: 'A-01')
    Location binB = new Location(name: 'B-01')

    int entrySequence = 0

    void setupSpec() {
        mockDomains(Product, Transaction, TransactionEntry, TransactionType, LocalTransfer, InventoryItem)
    }

    void cleanup() {
        [Transaction, Shipment, Requisition, Order].each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
    }

    void 'credits and debits should move the running balance and the totals'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType, '2026-03-15')
        Transaction issue = buildTransaction('t2', debitType, '2026-04-02')
        Map transactionMap = [
                (receipt): [buildEntry(receipt, 10)],
                (issue)  : [buildEntry(issue, 3)],
        ]

        when:
        StockHistoryResult result = computeRows(transactionMap)

        then:
        result.stockHistoryList*.balance == [10, 7]
        result.stockHistoryList*.quantity == [10, 3]
        result.stockHistoryList*.isCredit == [true, false]
        result.stockHistoryList*.isDebit == [false, true]
        result.totalCredit == 10
        result.totalDebit == 3
        result.totalBalance == 7

        and: 'no count was taken, so there is no counted quantity'
        result.totalCount == 0

        and: 'the year and the zero based month are kept for grouping the rows'
        result.stockHistoryList[0].transactionYear == '2026'
        result.stockHistoryList[0].transactionMonth == '2'
        result.stockHistoryList[1].transactionMonth == '3'
        result.stockHistoryList*.transactionCode == [TransactionCode.CREDIT, TransactionCode.DEBIT]
    }

    void 'a negative debit should add stock back and show as a credit with a positive quantity'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction reversal = buildTransaction('t2', debitType)
        Map transactionMap = [
                (receipt) : [buildEntry(receipt, 5)],
                (reversal): [buildEntry(reversal, -2)],
        ]

        when:
        StockHistoryRowDto row = computeRows(transactionMap).stockHistoryList[1]

        then:
        row.balance == 7
        row.quantity == 2
        row.isCredit
        !row.isDebit
    }

    void 'a negative credit should take stock away and show as a debit'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction correction = buildTransaction('t2', creditType)
        Map transactionMap = [
                (receipt)   : [buildEntry(receipt, 5)],
                (correction): [buildEntry(correction, -2)],
        ]

        when:
        StockHistoryResult result = computeRows(transactionMap)

        then:
        result.stockHistoryList[1].balance == 3
        result.stockHistoryList[1].quantity == 2
        result.stockHistoryList[1].isDebit
        !result.stockHistoryList[1].isCredit
        result.totalCredit == 3
    }

    void 'an inventory count should overwrite only the counted bin and lot'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction count = buildTransaction('t2', inventoryType)
        Map transactionMap = [
                (receipt): [buildEntry(receipt, 5, binA, 'LOT-1'), buildEntry(receipt, 3, binA, 'LOT-2')],
                (count)  : [buildEntry(count, 2, binA, 'LOT-1')],
        ]

        when:
        StockHistoryResult result = computeRows(transactionMap)

        then: 'LOT-1 is recounted at 2 while LOT-2 keeps its 3'
        result.stockHistoryList*.balance == [5, 8, 5]
        result.totalBalance == 5
        result.totalCount == 2

        and: 'a count row is neither a credit nor a debit'
        !result.stockHistoryList[2].isCredit
        !result.stockHistoryList[2].isDebit
    }

    void 'entries without a bin or a lot should share one default balance'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction count = buildTransaction('t2', inventoryType)
        Map transactionMap = [
                (receipt): [buildEntry(receipt, 4), buildEntry(receipt, 6, binB)],
                (count)  : [buildEntry(count, 1)],
        ]

        when:
        StockHistoryResult result = computeRows(transactionMap)

        then: 'the count replaces the unbinned 4 and leaves bin B alone'
        result.totalBalance == 7
    }

    void 'a product inventory should reset every balance and total before taking the new baseline'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction issue = buildTransaction('t2', debitType)
        Transaction baseline = buildTransaction('t3', productInventoryType)
        Map transactionMap = [
                (receipt) : [buildEntry(receipt, 10, binA)],
                (issue)   : [buildEntry(issue, 4, binA)],
                (baseline): [buildEntry(baseline, 6, binA), buildEntry(baseline, 1, binB)],
        ]

        when:
        StockHistoryResult result = computeRows(transactionMap)

        then:
        result.stockHistoryList*.balance == [10, 6, 6, 7]
        result.totalBalance == 7
        result.totalCount == 7
        result.totalCredit == 0
        result.totalDebit == 0

        and: 'only the first entry of the product inventory is flagged as the baseline'
        result.stockHistoryList*.isBaseline == [false, false, true, false]
    }

    void 'only the first entry of a transaction should show the transaction details'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction issue = buildTransaction('t2', debitType)
        Map transactionMap = [
                (receipt): [buildEntry(receipt, 1, binA), buildEntry(receipt, 2, binB)],
                (issue)  : [buildEntry(issue, 1, binA)],
        ]

        when:
        List<StockHistoryRowDto> rows = computeRows(transactionMap).stockHistoryList

        then:
        rows*.showDetails == [true, false, true]
        rows*.isSameTransaction == [false, true, false]
        rows*.transaction == [receipt, receipt, issue]
        rows*.binLocation == [binA, binB, binA]
        rows*.destinationBinLocation == [null, null, null]
    }

    void 'rows should carry the requisition and order ids resolved for their transaction'() {
        given:
        Transaction receipt = buildTransaction('t1', creditType)
        Transaction issue = buildTransaction('t2', debitType)
        Map transactionMap = [
                (receipt): [buildEntry(receipt, 5)],
                (issue)  : [buildEntry(issue, 1)],
        ]
        TransactionRefIds refIds = new TransactionRefIds(
                requisitionIdByTransactionId: [t2: 'req-1'],
                orderIdByTransactionId: [t1: 'po-1'],
        )

        when:
        List<StockHistoryRowDto> rows = computeRows(transactionMap, new LocalTransferContext(), refIds)
                .stockHistoryList

        then:
        rows*.orderId == ['po-1', null]
        rows*.requisitionId == [null, 'req-1']
    }

    void 'a bin to bin transfer should show as one internal row going from the source bin to the destination bin'() {
        given:
        Transaction receipt = buildTransaction('t0', creditType)
        Transaction transferOut = buildTransaction('t1', debitType)
        Transaction transferIn = buildTransaction('t2', creditType)
        Map transactionMap = [
                (receipt)    : [buildEntry(receipt, 10, binA)],
                (transferOut): [buildEntry(transferOut, 4, binA)],
                (transferIn) : [buildEntry(transferIn, 4, binB)],
        ]
        LocalTransferContext context = buildTransferContext(transferOut, transferIn, transactionMap)

        when:
        StockHistoryResult result = computeRows(transactionMap, context)

        then: 'the transfer in that directly follows its pair is folded into the transfer out row'
        result.stockHistoryList.size() == 2

        and:
        StockHistoryRowDto row = result.stockHistoryList[1]
        row.transaction == transferOut
        row.isInternal
        row.binLocation == binA
        row.destinationBinLocation == binB
        row.localTransferInfo.is(context.localTransferInfoByTransactionId['t1'])
        !row.isCredit
        !row.isDebit

        and: 'stock only moved between bins, so the balance holds while both totals count the move'
        row.balance == 10
        result.totalBalance == 10
        result.totalCredit == 14
        result.totalDebit == 4
    }

    void 'a transfer listed from its incoming side should still go from the other bin to this one'() {
        given:
        Transaction transferIn = buildTransaction('t2', creditType)
        Transaction transferOut = buildTransaction('t1', debitType)
        Map transactionMap = [
                (transferIn) : [buildEntry(transferIn, 4, binB)],
                (transferOut): [buildEntry(transferOut, 4, binA)],
        ]
        LocalTransferContext context = buildTransferContext(transferOut, transferIn, transactionMap)

        when:
        List<StockHistoryRowDto> rows = computeRows(transactionMap, context).stockHistoryList

        then:
        rows.size() == 1
        rows[0].transaction == transferIn
        rows[0].binLocation == binA
        rows[0].destinationBinLocation == binB
    }

    void 'a transfer whose other side holds no entries for this product should leave the other bin empty'() {
        given:
        Transaction transferOut = buildTransaction('t1', debitType)
        Transaction transferIn = buildTransaction('t2', creditType)
        Map transactionMap = [(transferOut): [buildEntry(transferOut, 4, binA)]]
        LocalTransferContext context = buildTransferContext(transferOut, transferIn, transactionMap)

        when:
        List<StockHistoryRowDto> rows = computeRows(transactionMap, context).stockHistoryList

        then:
        rows.size() == 1
        rows[0].isInternal
        rows[0].binLocation == binA
        rows[0].destinationBinLocation == null
    }

    void 'an empty ledger should produce no rows and zero totals'() {
        when:
        StockHistoryResult result = computeRows([:])

        then:
        result.stockHistoryList.empty
        result.totalBalance == 0
        result.totalCount == 0
        result.totalCredit == 0
        result.totalDebit == 0
    }

    void 'the transaction reference ids should only be recorded for the references a transaction has'() {
        given:
        Map capturedParams
        Transaction.metaClass.static.executeQuery = { String query, Map params ->
            capturedParams = params
            return [
                    ['t1', 'req-1', null] as Object[],
                    ['t2', null, 'po-1'] as Object[],
                    ['t3', null, null] as Object[],
            ]
        }
        List<Transaction> transactions = ['t1', 't2', 't3'].collect { buildTransaction(it, creditType) }

        when:
        TransactionRefIds refIds = StockHistoryAssembler.getTransactionRefIds(transactions)

        then:
        capturedParams.ids == ['t1', 't2', 't3']
        refIds.requisitionIdByTransactionId == [t1: 'req-1']
        refIds.orderIdByTransactionId == [t2: 'po-1']
    }

    void 'no queries should run when there are no transactions'() {
        given:
        [Transaction, Shipment, Requisition, Order].each { Class domainClass ->
            domainClass.metaClass.static.executeQuery = { String query, Map params ->
                throw new IllegalStateException("no query expected")
            }
        }

        when:
        TransactionRefIds refIds = StockHistoryAssembler.getTransactionRefIds([])
        StockHistoryDisplayContext displayContext = StockHistoryAssembler.buildStockHistoryDisplayContext([], refIds)
        LocalTransferContext transferContext = StockHistoryAssembler.buildLocalTransferContext([], [:])

        then:
        refIds.requisitionIdByTransactionId.isEmpty()
        displayContext.shipmentDtoById.isEmpty()
        displayContext.requisitionDtoById.isEmpty()
        displayContext.orderDtoById.isEmpty()
        transferContext.otherTransactionById.isEmpty()
    }

    void 'shipments should be classified as purchase or return order shipments from the types of all their orders'() {
        given:
        Collection<String> queriedShipmentIds
        Shipment.metaClass.static.executeQuery = { String query, Map params ->
            queriedShipmentIds = params.ids
            return [
                    ['s-po', 'PURCHASE_ORDER', OrderTypeCode.PURCHASE_ORDER, 'Purchase Order'] as Object[],
                    ['s-po', 'PURCHASE_ORDER', OrderTypeCode.PURCHASE_ORDER, 'Purchase Order'] as Object[],
                    ['s-return', Constants.RETURN_ORDER, OrderTypeCode.TRANSFER_ORDER, 'Return Order'] as Object[],
                    ['s-mixed', 'PURCHASE_ORDER', OrderTypeCode.PURCHASE_ORDER, 'Purchase Order'] as Object[],
                    ['s-mixed', Constants.RETURN_ORDER, OrderTypeCode.TRANSFER_ORDER, 'Return Order'] as Object[],
            ]
        }
        List<Transaction> transactions = [
                buildTransaction('t1', creditType, '2026-01-01', [incomingShipment: buildShipment('s-po')]),
                buildTransaction('t2', debitType, '2026-01-01', [outgoingShipment: buildShipment('s-po')]),
                buildTransaction('t3', creditType, '2026-01-01', [incomingShipment: buildShipment('s-return')]),
                buildTransaction('t4', debitType, '2026-01-01', [outgoingShipment: buildShipment('s-mixed')]),
                buildTransaction('t5', debitType, '2026-01-01', [outgoingShipment: buildShipment('s-none')]),
                buildTransaction('t6', creditType),
        ]

        when:
        Map<String, StockHistoryShipmentDto> dtos = StockHistoryAssembler
                .buildStockHistoryDisplayContext(transactions, new TransactionRefIds()).shipmentDtoById

        then: 'every shipment is looked up once, and transactions without a shipment add nothing'
        queriedShipmentIds as Set == ['s-po', 's-return', 's-mixed', 's-none'] as Set
        queriedShipmentIds.size() == 4

        and:
        dtos['s-po'].isFromPurchaseOrder
        dtos['s-po'].purchaseOrderTypeCode == OrderTypeCode.PURCHASE_ORDER
        !dtos['s-po'].isFromReturnOrder

        and:
        dtos['s-return'].isFromReturnOrder
        dtos['s-return'].returnOrderTypeCode == Constants.RETURN_ORDER
        dtos['s-return'].returnOrderTypeName == 'Return Order'
        !dtos['s-return'].isFromPurchaseOrder

        and: 'a shipment mixing order types is neither'
        !dtos['s-mixed'].isFromPurchaseOrder
        !dtos['s-mixed'].isFromReturnOrder
        dtos['s-mixed'].purchaseOrderTypeCode == null

        and: 'a shipment without orders gets no entry'
        !dtos.containsKey('s-none')
    }

    void 'requisitions and orders should be resolved into display values keyed by their id'() {
        given:
        Collection<String> queriedRequisitionIds
        Collection<String> queriedOrderIds
        Requisition.metaClass.static.executeQuery = { String query, Map params ->
            queriedRequisitionIds = params.ids
            // OBAF: loadRequisitionDtos also selects r.autoIssuanceRequested (OBAF auto-issuance feature), so a row has 4 columns
            return [['req-1', 'R-100', 'Ward restock', false] as Object[]]
        }
        Order.metaClass.static.executeQuery = { String query, Map params ->
            queriedOrderIds = params.ids
            return [
                    ['po-1', 'PO-1', 'Supplier order', 'PURCHASE_ORDER', 'Purchase Order',
                     OrderTypeCode.PURCHASE_ORDER] as Object[],
                    ['pa-1', 'PA-1', 'Putaway', Constants.PUTAWAY_ORDER, 'Putaway Order',
                     OrderTypeCode.TRANSFER_ORDER] as Object[],
            ]
        }
        TransactionRefIds refIds = new TransactionRefIds(
                requisitionIdByTransactionId: [t1: 'req-1', t2: 'req-1'],
                orderIdByTransactionId: [t3: 'po-1', t4: 'pa-1'],
        )

        when:
        StockHistoryDisplayContext context = StockHistoryAssembler.buildStockHistoryDisplayContext(
                [buildTransaction('t1', creditType)], refIds)

        then: 'a requisition shared by several transactions is queried once'
        queriedRequisitionIds as List == ['req-1']
        queriedOrderIds as Set == ['po-1', 'pa-1'] as Set

        and:
        context.requisitionDtoById['req-1'].requestNumber == 'R-100'
        context.requisitionDtoById['req-1'].name == 'Ward restock'

        and:
        with(context.orderDtoById['po-1']) {
            orderNumber == 'PO-1'
            name == 'Supplier order'
            orderTypeCode == 'PURCHASE_ORDER'
            orderTypeName == 'Purchase Order'
            !isTransferOrder
            !isPutawayOrder
        }
        context.orderDtoById['pa-1'].isTransferOrder
        context.orderDtoById['pa-1'].isPutawayOrder
    }

    void 'the local transfer context should pair both sides of a transfer found among the transactions'() {
        given:
        Transaction transferOut = saveTransaction('t1', debitType)
        Transaction transferIn = saveTransaction('t2', creditType)
        Transaction unrelated = saveTransaction('t3', creditType)
        LocalTransfer localTransfer = new LocalTransfer(sourceTransaction: transferOut, destinationTransaction: transferIn)
        localTransfer.id = 'lt-1'
        localTransfer.save(validate: false)
        List<TransactionEntry> outEntries = [buildEntry(transferOut, 4, binA)]
        Map transactionMap = [(transferOut): outEntries, (unrelated): [buildEntry(unrelated, 1)]]

        when: 'only the outgoing side is in the ledger'
        LocalTransferContext context = StockHistoryAssembler.buildLocalTransferContext(
                [transferOut, unrelated], transactionMap)

        then:
        context.isInternal('t1')
        context.isInternal('t2')
        !context.isInternal('t3')
        context.otherTransactionById['t1'] == transferIn
        context.otherTransactionById['t2'] == transferOut
        context.entriesByTransactionId['t1'] == outEntries

        and: 'the transaction types are only known for transactions in the ledger'
        LocalTransferInfo info = context.localTransferInfoByTransactionId['t1']
        context.localTransferInfoByTransactionId['t2'].is(info)
        info.sourceTransactionId == 't1'
        info.destinationTransactionId == 't2'
        info.sourceTransactionType == debitType
        info.destinationTransactionType == null
    }

    void 'assembling the page should fail for an unknown product'() {
        given:
        StockHistoryAssembler assembler = new StockHistoryAssembler(inventoryService: Mock(InventoryService))

        when:
        assembler.assembleStockHistoryPage(new StockCardCommand(), params)

        then:
        ProductException e = thrown()
        e.message == "Product with identifier '${expectedId}' could not be found"

        where:
        params                                  || expectedId
        [id: 'missing']                         || 'missing'
        [id: 'ignored', product: [id: 'other']] || 'other'
    }

    void 'assembling the page should load the product ledger for the warehouse inventory and build every part of the page'() {
        given:
        Product product = new Product(name: 'Paracetamol')
        product.id = 'prod-1'
        product.save(validate: false)
        Inventory inventory = new Inventory()
        Location warehouse = new Location(name: 'Depot', inventory: inventory)

        and: 'a receipt from a requisition and a bin transfer'
        Transaction receipt = saveTransaction('t1', creditType)
        Transaction transferOut = saveTransaction('t2', debitType)
        Transaction transferIn = saveTransaction('t3', creditType)
        LocalTransfer localTransfer = new LocalTransfer(sourceTransaction: transferOut, destinationTransaction: transferIn)
        localTransfer.id = 'lt-1'
        localTransfer.save(validate: false)
        List<TransactionEntry> entries = [
                buildEntry(receipt, 10, binA),
                buildEntry(transferOut, 6, binA),
                buildEntry(transferIn, 6, binB),
        ]

        and:
        Transaction.metaClass.static.executeQuery = { String query, Map params ->
            return [['t1', 'req-1', null] as Object[]]
        }
        Requisition.metaClass.static.executeQuery = { String query, Map params ->
            // OBAF: loadRequisitionDtos also selects r.autoIssuanceRequested (OBAF auto-issuance feature), so a row has 4 columns
            return [['req-1', 'R-100', 'Ward restock', false] as Object[]]
        }

        and:
        InventoryService inventoryService = Mock(InventoryService)
        StockHistoryAssembler assembler = new StockHistoryAssembler(inventoryService: inventoryService)
        StockCardCommand command = new StockCardCommand(warehouse: warehouse)

        when:
        StockHistoryPageModel page = assembler.assembleStockHistoryPage(command, [id: 'prod-1'])

        then:
        1 * inventoryService.getTransactionEntriesByInventoryAndProductForStockHistory(inventory, [product]) >> entries

        and:
        page.commandInstance.is(command)
        command.product == product
        command.inventory.is(inventory)

        and: 'the transfer is shown once, from bin A to bin B'
        page.stockHistory.stockHistoryList.size() == 2
        page.stockHistory.stockHistoryList[1].isInternal
        page.stockHistory.stockHistoryList[1].destinationBinLocation == binB
        page.stockHistory.totalBalance == 10

        and: 'the receipt links to its requisition'
        page.stockHistory.stockHistoryList[0].requisitionId == 'req-1'
        page.displayContext.requisitionDtoById['req-1'].requestNumber == 'R-100'
        page.displayContext.orderDtoById.isEmpty()
        page.displayContext.shipmentDtoById.isEmpty()
    }

    private StockHistoryResult computeRows(Map transactionMap,
                                           LocalTransferContext context = new LocalTransferContext(),
                                           TransactionRefIds refIds = new TransactionRefIds()) {
        return StockHistoryAssembler.computeStockHistoryRows(transactionMap, context, refIds)
    }

    private Transaction buildTransaction(String id, TransactionType type, String date = '2026-01-01',
                                         Map properties = [:]) {
        Transaction transaction = new Transaction([transactionType: type,
                                                   transactionDate: Date.parse('yyyy-MM-dd', date)] + properties)
        transaction.id = id
        return transaction
    }

    private Transaction saveTransaction(String id, TransactionType type) {
        return buildTransaction(id, type).save(validate: false)
    }

    private TransactionEntry buildEntry(Transaction transaction, Integer quantity, Location bin = null,
                                        String lotNumber = null) {
        TransactionEntry entry = new TransactionEntry(
                transaction: transaction,
                quantity: quantity,
                binLocation: bin,
                inventoryItem: new InventoryItem(lotNumber: lotNumber),
        )
        entry.id = "entry-${++entrySequence}"
        return entry
    }

    private static Shipment buildShipment(String id) {
        Shipment shipment = new Shipment()
        shipment.id = id
        return shipment
    }

    /**
     * Pairs a transfer out with its transfer in the way buildLocalTransferContext does, for the row computation
     * features that should not depend on the database lookup.
     */
    private static LocalTransferContext buildTransferContext(Transaction transferOut, Transaction transferIn,
                                                             Map<Transaction, List<TransactionEntry>> transactionMap) {
        LocalTransferContext context = new LocalTransferContext()
        transactionMap.each { Transaction transaction, List<TransactionEntry> entries ->
            context.entriesByTransactionId[transaction.id] = entries
        }
        context.otherTransactionById[transferOut.id] = transferIn
        context.otherTransactionById[transferIn.id] = transferOut
        LocalTransferInfo info = new LocalTransferInfo(
                sourceTransactionId: transferOut.id,
                sourceTransactionType: transferOut.transactionType,
                destinationTransactionId: transferIn.id,
                destinationTransactionType: transferIn.transactionType,
        )
        context.localTransferInfoByTransactionId[transferOut.id] = info
        context.localTransferInfoByTransactionId[transferIn.id] = info
        return context
    }
}
