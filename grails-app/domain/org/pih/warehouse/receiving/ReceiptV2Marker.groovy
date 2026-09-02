package org.pih.warehouse.receiving

import java.time.Instant

/**
 * Marks a receipt as created by the v2 receiving workflow.
 *
 * The two receiving workflows persist product-edited lines with different semantics: v2 lines consume their shipment
 * item's remaining quantity even when their product differs from the shipment item's, while old-workflow lines
 * deliberately do not. Reads that aggregate receipt items per shipment item (e.g. the packing list's received and
 * canceled quantities) must therefore know which rule a receipt was written under, and that fact cannot be recovered
 * later: old-workflow receipts can produce exactly the same data shape as v2 ones (added lines carrying an edited
 * product and a zero quantity shipped), and configuration (which locations use the v2 flow) changes over time while
 * the data lives forever. So the workflow is recorded explicitly when the receipt is started - as a standalone marker
 * row rather than a column, keeping the legacy {@link Receipt} domain untouched.
 *
 * Rows are insert-only: they are never updated, and are deleted only together with their receipt. Their foreign key
 * restricts deletes rather than cascading, so every flow that deletes a receipt (the receiving rollbacks) has to
 * delete its marker first - see ReceiptV2Service.deleteMarkersForReceipts.
 */
class ReceiptV2Marker {

    String id

    Receipt receipt

    Instant dateCreated

    static mapping = {
        table 'receipt_v2_marker'
        id generator: 'uuid'
        version false
    }

    static constraints = {
        receipt(unique: true)
    }
}
