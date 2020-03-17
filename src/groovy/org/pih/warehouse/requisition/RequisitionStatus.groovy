/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.requisition

import org.pih.warehouse.inventory.StockMovementStatusCode

enum RequisitionStatus {
    CREATED(1),
    EDITING(2),
    VERIFYING(3),
    PICKING(4),
    PICKED(5),
    PENDING(5),
    CHECKING(6),
    ISSUED(7),
    RECEIVED(8),
    CANCELED(9),
    DELETED(10),
    ERROR(11),
    // Removed
    OPEN(0),
    FULFILLED(0),
    REVIEWING(0),
    CONFIRMING(0)

    int sortOrder

    RequisitionStatus(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(RequisitionStatus a, RequisitionStatus b) {
        return a.sortOrder <=> b.sortOrder
    }
    /* remove OPEN, FULFILLED */

    static list() {
        [CREATED, EDITING, VERIFYING, PICKING, PICKED, CHECKING, ISSUED, CANCELED]
    }

    static listPending() {
        [CREATED, CHECKING, EDITING, PICKED, PICKING, VERIFYING]
    }

    static listCompleted() {
        [ISSUED, RECEIVED]
    }

    static listCanceled() {
        [CANCELED, DELETED]
    }

    static listAll() {
        [CREATED, EDITING, VERIFYING, PICKING, PICKED, PENDING, CHECKING, FULFILLED, ISSUED, RECEIVED, CANCELED, DELETED, ERROR]
    }

    static toStockMovementStatus(RequisitionStatus requisitionStatus) {
        switch(requisitionStatus) {
            case RequisitionStatus.EDITING:
                return StockMovementStatusCode.REQUESTING
            case RequisitionStatus.VERIFYING:
                return StockMovementStatusCode.REQUESTED
            case RequisitionStatus.ISSUED:
                return StockMovementStatusCode.DISPATCHED
            default:
                return StockMovementStatusCode.valueOf(requisitionStatus.toString())
        }

    }

    static fromStockMovementStatus(StockMovementStatusCode stockMovementStatus) {
        switch(stockMovementStatus) {
            case StockMovementStatusCode.REQUESTING:
                return RequisitionStatus.EDITING
            case StockMovementStatusCode.REQUESTED:
                return RequisitionStatus.VERIFYING
            case StockMovementStatusCode.VALIDATED:
                return RequisitionStatus.VERIFYING
            case StockMovementStatusCode.DISPATCHED:
                return RequisitionStatus.ISSUED
            default:
                return RequisitionStatus.valueOf(stockMovementStatus.toString())
        }
    }

    String toString() { return name() }



}
