/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.allocation

import org.hibernate.SessionFactory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

/**
 * OBLS-919: Serializes the allocation decision for a single facility/product so that two allocations cannot
 * both reserve the same stock.
 *
 * Reading availability live from transactions (StockMovementService.getAvailableItemsFromTransactions) fixes
 * the reported *sequential* failure (the automatic allocation job processing two pending requisitions
 * back-to-back). This lock closes the remaining *true concurrency* window - e.g. the job racing a manual
 * allocation of the same product at the same instant.
 *
 * Mechanism: a pessimistic row lock (SELECT ... FOR UPDATE) on a dedicated allocation_lock row keyed by
 * (location, product), taken on the CURRENT transaction's connection. Because the lock lives on the
 * transaction's own connection, InnoDB holds it until that transaction commits or rolls back and releases it
 * automatically - the read of availability and the subsequent write of the picklist therefore happen under
 * the same lock, and there is no manual release or "must be called outside a transaction" footgun.
 *
 * CONTRACT: must be called from within the allocation transaction (before availability is read), so the lock
 * spans the read-then-reserve. It is a no-op if facility or product is null.
 *
 * When a single transaction needs to lock several products (a multi-line requisition), acquire them all up
 * front via {@link #lockForAllocation(Location, List)}, which locks in a deterministic order (by product id)
 * so two concurrent allocations touching the same products cannot deadlock by taking them in opposite orders.
 *
 * Requires MySQL/InnoDB (uses INSERT IGNORE to make the anchor row idempotent).
 */
class AllocationLockService {

    // Never open a new transaction of its own - it must join the caller's transaction/connection so the
    // FOR UPDATE lock is held for the life of the allocation transaction.
    static transactional = false

    SessionFactory sessionFactory

    /**
     * Take exclusive locks for all of the given products at a facility, in a deterministic order (by product
     * id), for the remainder of the current transaction. Null/duplicate products are ignored. Locking in a
     * stable order guarantees two concurrent transactions requesting an overlapping set of products acquire
     * them in the same order, so they queue instead of deadlocking.
     */
    void lockForAllocation(Location facility, List<Product> products) {
        products?.findAll { it?.id }
                ?.unique { it.id }
                ?.sort { it.id }
                ?.each { Product product -> lockForAllocation(facility, product) }
    }

    /**
     * Take an exclusive lock for (facility, product) for the remainder of the current transaction.
     */
    void lockForAllocation(Location facility, Product product) {
        if (!facility?.id || !product?.id) {
            return
        }

        def session = sessionFactory.currentSession

        // Ensure the anchor row exists so FOR UPDATE has a row to lock. INSERT IGNORE is a no-op (and does not
        // fail) when the (location_id, product_id) unique row already exists.
        session.createSQLQuery("""
            INSERT IGNORE INTO allocation_lock (id, version, location_id, product_id, date_created, last_updated)
            VALUES (:id, 0, :locationId, :productId, now(), now())
        """)
                .setString("id", UUID.randomUUID().toString())
                .setString("locationId", facility.id)
                .setString("productId", product.id)
                .executeUpdate()

        // Take the exclusive row lock. Held until the surrounding transaction commits or rolls back.
        session.createSQLQuery("""
            SELECT id FROM allocation_lock
            WHERE location_id = :locationId AND product_id = :productId
            FOR UPDATE
        """)
                .setString("locationId", facility.id)
                .setString("productId", product.id)
                .uniqueResult()
    }
}
