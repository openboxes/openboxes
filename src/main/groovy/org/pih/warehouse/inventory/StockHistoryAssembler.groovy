package org.pih.warehouse.inventory

import groovy.util.logging.Slf4j
import org.pih.warehouse.core.Location
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

/**
 * Assembles every piece of data the stock history page (showStockHistory.gsp /
 * printStockHistory.gsp) needs from the database.
 *
 * Lives outside InventoryService because it's strictly feature-scoped: every method
 * here exists to support that one screen, and the methods are tightly coupled to one
 * another (they share the LocalTransfer / TransactionRefIds / DTO contracts). Keeping
 * them together makes the dataflow easier to follow than scattering them across a
 * 3000-line domain service.
 *
 * Most of the methods here exist primarily so they can be tuned independently: each
 * one carries its own per-step timing log, and the original perf work that motivated
 * this whole refactor measured against those individual steps. The fasade entry point
 * is {@link #assembleStockHistoryPage} - controllers should call that.
 */
@Component
@Slf4j
class StockHistoryAssembler {

    @Autowired
    InventoryService inventoryService

    /**
     * One-shot entry point for the stock history page: returns everything both
     * templates need. The controller's only remaining job is choosing which template
     * to render.
     */
    StockHistoryPageModel assembleStockHistoryPage(StockCardCommand cmd, Map params) {
        StockCardCommand commandInstance = getStockHistoryCommand(cmd, params)

        // Group transaction entries by transaction
        Map<Transaction, List<TransactionEntry>> transactionMap = commandInstance?.getTransactionLogMap(false)

        // Get unique transactions list
        List<Transaction> transactions = transactionMap.keySet().toList()

        // Build a LocalTransferContext based on id keyed maps, so the main loop never has to call
        // transaction.getOtherTransaction() or transaction.localTransfer that would trigger a ton of N+1 queries
        // unable to resolve earlier due to the form, how the transaction.otherTransaction is implemented in the domain model.
        LocalTransferContext localTransferContext = buildLocalTransferContext(transactions, transactionMap)

        // Get requisitionId / orderId projections for the transactions
        // In order to fetch Requisition and Order separately, as the hasOne mapping on Picklist
        // would make the Picklist being eagerly loaded for every Requisition/Order if we were to just JOIN FETCH
        // the Requisition and Order on the main query level (getTransactionEntriesByInventoryAndProductForStockHistory).
        TransactionRefIds transactionRefIds = getTransactionRefIds(transactions)

        // Build id-keyed maps for the requisition, shipment, order dtos used by the view to avoid N+1 on those fields when
        // accessing them directly in the view.
        StockHistoryDisplayContext displayContext = buildStockHistoryDisplayContext(transactions, transactionRefIds)

        StockHistoryResult stockHistory = computeStockHistoryRows(
                transactionMap, localTransferContext, transactionRefIds)

        return new StockHistoryPageModel(
                commandInstance: commandInstance,
                stockHistory: stockHistory,
                displayContext: displayContext,
        )
    }

    /**
     * Fetches and populates a StockCardCommand for the stock history page.
     */
    StockCardCommand getStockHistoryCommand(StockCardCommand cmd, Map params) {
        cmd.product = Product.get(params?.product?.id ?: params.id)
        if (!cmd.product) {
            throw new ProductException("Product with identifier '${params?.product?.id ?: params.id}' could not be found")
        }

        cmd.inventory = cmd.warehouse?.inventory
        cmd.transactionEntryList = inventoryService.getTransactionEntriesByInventoryAndProductForStockHistory(
                cmd.inventory, [cmd.product])

        return cmd
    }

    /**
     * Pre-resolve everything stock history's main loop needs to know about LocalTransfer
     * pairing, in one query, keyed by transaction id. Avoids per-row lazy loads of
     * Transaction.getOtherTransaction() / transaction.localTransfer.
     */
    LocalTransferContext buildLocalTransferContext(
            List<Transaction> transactions,
            Map<Transaction, List<TransactionEntry>> transactionMap) {

        LocalTransferContext ctx = new LocalTransferContext()
        if (!transactions) {
            return ctx
        }

        List<LocalTransfer> localTransferList =
                LocalTransfer.findAllByDestinationTransactionInListOrSourceTransactionInList(
                        transactions, transactions)

        // Re-key entries by id so the loop can fetch the other side's entries without
        // calling transaction.getTransactionEntries() and triggering a lazy load of the whole collection.
        // Even though on first sight it looks like those entries must have been already loaded in the session
        // they are loaded, by Hibernate would anyway call SELECT on the entries when calling otherTransaction.transactionEntries.
        // This is because the transaction entries are filtered to the current product, so Hibernate can't be sure that the entries for the other transaction are already loaded
        // (and in fact they won't be, because of the product filter) and it has to trigger a lazy load of the whole collection.
        transactionMap.each { Transaction t, List<TransactionEntry> entries ->
            ctx.entriesByTransactionId[t.id] = entries
        }


        Map<String, Transaction> transactionsById = transactions.collectEntries { [(it.id): it] }
        // Build otherTransaction map and pre-resolve the source/destination transaction types for the GSP.
        // Again, this is to avoid lazy loads of transaction.localTransfer / transaction.getOtherTransaction() in the loop.
        localTransferList.each { LocalTransfer localTransfer ->
            String sourceTransactionId = localTransfer.sourceTransaction.id
            String destinationTransactionId = localTransfer.destinationTransaction.id

            ctx.otherTransactionById[sourceTransactionId] = localTransfer.destinationTransaction
            ctx.otherTransactionById[destinationTransactionId] = localTransfer.sourceTransaction

            LocalTransferInfo info = new LocalTransferInfo(
                    sourceTransactionId: sourceTransactionId,
                    sourceTransactionType: transactionsById[sourceTransactionId]?.transactionType,
                    destinationTransactionId: destinationTransactionId,
                    destinationTransactionType: transactionsById[destinationTransactionId]?.transactionType,
            )
            ctx.localTransferInfoByTransactionId[sourceTransactionId] = info
            ctx.localTransferInfoByTransactionId[destinationTransactionId] = info
        }

        return ctx
    }

    /**
     * Read t.requisition_id / t.order_id straight off the Transaction row via an HQL
     * projection. We deliberately don't dereference transaction.requisition?.id /
     * transaction.order?.id in Groovy: those calls would trigger an additional SQL query per transaction
     * to fetch the Requisition / Order entity, which would also trigger the eager one-to-one load of Picklist and all its fields.
     */
    static TransactionRefIds getTransactionRefIds(List<Transaction> transactions) {
        TransactionRefIds refIds = new TransactionRefIds()
        if (!transactions) {
            return refIds
        }

        Transaction.executeQuery("""
            SELECT t.id, t.requisition.id, t.order.id
            FROM Transaction t
            WHERE t.id IN :ids
        """, [ids: transactions.id]).each { Object[] row ->
            String transactionId = row[0] as String
            if (row[1]) {
                refIds.requisitionIdByTransactionId[transactionId] = row[1] as String
            }
            if (row[2]) {
                refIds.orderIdByTransactionId[transactionId] = row[2] as String
            }
        }

        return refIds
    }

    /**
     * Build the stock history rows by iterating over the transactions and their entries.
     */
    static StockHistoryResult computeStockHistoryRows(
            Map<Transaction, List<TransactionEntry>> transactionMap,
            LocalTransferContext localTransferContext,
            TransactionRefIds transactionRefIds) {

        StockHistoryResult result = new StockHistoryResult()
        Map<String, Integer> balance = [:]
        Map<String, Integer> count = [:]
        Transaction previousTransaction = null

        transactionMap.each { Transaction transaction, List transactionEntries ->
            boolean isTransactionInternal = localTransferContext.isInternal(transaction.id)
            Transaction otherTransaction = localTransferContext.otherTransactionById[transaction.id]
            // skip current transaction if it is internal and is connected to previous transaction
            if (isTransactionInternal && otherTransaction != null
                    && otherTransaction.id == previousTransaction?.id) {
                return
            }

            TransactionCode currentTransactionCode = transaction?.transactionType?.transactionCode

            // For PRODUCT INVENTORY transactions we just need to clear the balance completely and start over
            if (currentTransactionCode == TransactionCode.PRODUCT_INVENTORY) {
                balance = [:]
                count = [:]
                result.totalCredit = 0
                result.totalDebit = 0
            }

            transactionEntries.eachWithIndex { TransactionEntry transactionEntry, i ->

                boolean isBaseline = false
                boolean isCredit = false
                boolean isDebit = false

                String index = (transactionEntry.binLocation?.name ?: "DefaultBin") + "-" + (transactionEntry?.inventoryItem?.lotNumber ?: "DefaultLot")

                if (!balance[index]) {
                    balance[index] = 0
                    count[index] = 0
                }

                if (isTransactionInternal) {
                    result.totalDebit += transactionEntry?.quantity
                    result.totalCredit += transactionEntry?.quantity
                } else {
                    switch (currentTransactionCode) {
                        case TransactionCode.DEBIT:
                            balance[index] -= transactionEntry?.quantity
                            result.totalDebit += transactionEntry?.quantity
                            isDebit = transactionEntry?.quantity > 0
                            isCredit = transactionEntry.quantity < 0
                            break
                        case TransactionCode.CREDIT:
                            balance[index] += transactionEntry?.quantity
                            result.totalCredit += transactionEntry?.quantity
                            isDebit = transactionEntry.quantity < 0
                            isCredit = transactionEntry?.quantity >= 0
                            break
                        case TransactionCode.INVENTORY:
                            balance[index] = transactionEntry?.quantity
                            count[index] = transactionEntry?.quantity
                            break
                        case TransactionCode.PRODUCT_INVENTORY:
                            balance[index] += transactionEntry?.quantity
                            count[index] += transactionEntry?.quantity
                            isBaseline = i == 0
                            break
                    }
                }

                // Normalize quantity (inventory transactions were all converted to CREDIT so some may have negative quantity)
                Integer quantity = (transactionEntry.quantity > 0) ? transactionEntry.quantity : -transactionEntry.quantity

                String transactionYear = (transaction.transactionDate.year + 1900).toString()
                String transactionMonth = (transaction.transactionDate.month).toString()

                List otherTransactionEntries = isTransactionInternal && otherTransaction
                        ? (localTransferContext.entriesByTransactionId[otherTransaction.id] ?: [])
                        : []
                Location otherBinLocation = otherTransactionEntries ? otherTransactionEntries[0].binLocation : null

                Location sourceBinLocation = transactionEntry.binLocation
                Location destinationBinLocation = null

                if (isTransactionInternal) {
                    if (currentTransactionCode == TransactionCode.DEBIT) {
                        destinationBinLocation = otherBinLocation
                    } else {
                        sourceBinLocation = otherBinLocation
                        destinationBinLocation = transactionEntry.binLocation
                    }
                }
                result.stockHistoryList << new StockHistoryRowDto(
                        transactionYear: transactionYear,
                        transactionMonth: transactionMonth,
                        transactionCode: currentTransactionCode,
                        transaction: transaction,
                        // Access requisition id via pre-computed map to avoid additional fetch on Requisition
                        // that would also trigger the Picklist to be loaded eagerly
                        requisitionId: transactionRefIds.requisitionIdByTransactionId[transaction.id],
                        // Access order id via pre-computed map to avoid additional fetch on Order
                        // that would also trigger the Picklist to be loaded eagerly
                        orderId: transactionRefIds.orderIdByTransactionId[transaction.id],
                        localTransferInfo: isTransactionInternal
                                ? localTransferContext.localTransferInfoByTransactionId[transaction.id]
                                : null,
                        binLocation: sourceBinLocation,
                        destinationBinLocation: destinationBinLocation,
                        inventoryItem: transactionEntry.inventoryItem,
                        comments: transactionEntry.comments,
                        quantity: quantity,
                        balance: balance.values().sum() as Integer,
                        isDebit: isDebit,
                        isCredit: isCredit,
                        isInternal: isTransactionInternal,
                        isBaseline: isBaseline,
                        isSameTransaction: (previousTransaction?.id == transaction?.id),
                        showDetails: (i == 0),
                )

                previousTransaction = transaction
            }

            result.totalBalance = balance.values().sum() as int
            result.totalCount = count.values().sum() as int
        }

        return result
    }

    /**
     * Bulk-resolve all the scalar projections the stock history GSP needs for its per-row
     * link / label cells (shipment PO/RO classification, requisition fields, order +
     * orderType fields) in a single call.
     */
    static StockHistoryDisplayContext buildStockHistoryDisplayContext(
            List<Transaction> transactions,
            TransactionRefIds transactionRefIds) {

        StockHistoryDisplayContext context = new StockHistoryDisplayContext()
        if (!transactions) {
            return context
        }

        Set<String> shipmentIds = transactions.collectMany {
            [it.incomingShipment?.id, it.outgoingShipment?.id]
        }.findAll().toSet()

        context.shipmentDtoById = loadShipmentDtos(shipmentIds)
        context.requisitionDtoById = loadRequisitionDtos(
                transactionRefIds.requisitionIdByTransactionId.values().toSet())
        context.orderDtoById = loadOrderDtos(
                transactionRefIds.orderIdByTransactionId.values().toSet())
        return context
    }

    /**
     * Bulk-resolve "is this shipment a PO/RO?" plus the order-type label fields used by
     * the stock history view. Replaces per-row Shipment.getOrders() traversals (which
     * pulled every shipmentItem and every orderItem of every shipment in the page) that caused N+1.
     */
    private static Map<String, StockHistoryShipmentDto> loadShipmentDtos(Set<String> shipmentIds) {
        Map<String, StockHistoryShipmentDto> result = [:]
        if (!shipmentIds) {
            return result
        }
        long startTime = System.currentTimeMillis()

        List<Object[]> rows = Shipment.executeQuery("""
            SELECT DISTINCT s.id, ot.code, ot.orderTypeCode, ot.name
            FROM Shipment s
            JOIN s.shipmentItems si
            JOIN si.orderItems oi
            JOIN oi.order o
            JOIN o.orderType ot
            WHERE s.id IN :ids
        """, [ids: shipmentIds])

        rows.groupBy { it[0] as String }.each { String shipmentId, List<Object[]> shipmentRows ->
            StockHistoryShipmentDto dto = new StockHistoryShipmentDto()
            // Mimic the isFromPurchaseOrder transient behavior by checking the orderTypeCode of every order on the shipment
            dto.isFromPurchaseOrder = shipmentRows.every { it[2] == OrderTypeCode.PURCHASE_ORDER }
            // Mimic the isFromReturnOrder transient behavior
            dto.isFromReturnOrder = shipmentRows.every { it[1] == Constants.RETURN_ORDER }
            if (dto.isFromPurchaseOrder) {
                dto.purchaseOrderTypeCode = OrderTypeCode.PURCHASE_ORDER
            }
            if (dto.isFromReturnOrder) {
                Object[] shipmentRow = shipmentRows[0]
                dto.returnOrderTypeCode = shipmentRow[1] as String
                dto.returnOrderTypeName = shipmentRow[3] as String
            }
            result[shipmentId] = dto
        }

        log.info("loadShipmentDtos(): " + (System.currentTimeMillis() - startTime) + " ms")
        return result
    }

    /**
     * Bulk-resolve requisition fields (id/requestNumber/name) for the stock history
     * those are the only fields used from the Requisition, and pulling whole Requisition via
     * createAlias would always trigger the eager one-to-one inverse load on Picklist,
     * so we read them straight off the Requisition table via HQL projection.
     */
    private static Map<String, StockHistoryRequisitionDto> loadRequisitionDtos(Set<String> requisitionIds) {
        Map<String, StockHistoryRequisitionDto> result = [:]
        if (!requisitionIds) {
            return result
        }
        long startTime = System.currentTimeMillis()

        Requisition.executeQuery("""
            SELECT r.id, r.requestNumber, r.name
            FROM Requisition r
            WHERE r.id IN :ids
        """, [ids: requisitionIds]).each { Object[] row ->
            String id = row[0] as String
            result[id] = new StockHistoryRequisitionDto(
                    id: id,
                    requestNumber: row[1] as String,
                    name: row[2] as String,
            )
        }

        log.info("loadRequisitionDtos(): " + (System.currentTimeMillis() - startTime) + " ms")
        return result
    }

    /**
     * Bulk-resolve scalar order + orderType fields (plus precomputed isTransferOrder /
     * isPutawayOrder flags) for the stock history view. Same reason as for Requisition:
     * skipping Order entity materialisation skips the eager one-to-one inverse on Picklist.
     */
    private static Map<String, StockHistoryOrderDto> loadOrderDtos(Set<String> orderIds) {
        Map<String, StockHistoryOrderDto> result = [:]
        if (!orderIds) {
            return result
        }
        long startTime = System.currentTimeMillis()

        Order.executeQuery("""
            SELECT o.id, o.orderNumber, o.name, ot.code, ot.name, ot.orderTypeCode
            FROM Order o
            JOIN o.orderType ot
            WHERE o.id IN :ids
        """, [ids: orderIds]).each { Object[] row ->
            String id = row[0] as String
            String orderTypeCode = row[3] as String
            OrderTypeCode orderTypeEnum = row[5] as OrderTypeCode
            result[id] = new StockHistoryOrderDto(
                    id: id,
                    orderNumber: row[1] as String,
                    name: row[2] as String,
                    orderTypeCode: orderTypeCode,
                    orderTypeName: row[4] as String,
                    isTransferOrder: orderTypeEnum == OrderTypeCode.TRANSFER_ORDER,
                    isPutawayOrder: orderTypeCode == Constants.PUTAWAY_ORDER,
            )
        }

        log.info("loadOrderDtos(): " + (System.currentTimeMillis() - startTime) + " ms")
        return result
    }
}
