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

import grails.util.Holders
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.core.StatusType

enum RequisitionStatus {

    CREATED(1, null, StatusType.SUCCESS),
    EDITING(2, PENDING, StatusType.PRIMARY),
    PENDING_APPROVAL(3, null, StatusType.WARNING),
    APPROVED(4, null, StatusType.SUCCESS),
    REJECTED(5, null, StatusType.DANGER),
    VERIFYING(6, PENDING, StatusType.WARNING),
    PICKING(7, PENDING, StatusType.WARNING),
    PICKED(8, PENDING, StatusType.PRIMARY),
    PENDING(9,null, StatusType.PRIMARY),
    CHECKING(10, PENDING, StatusType.WARNING),
    ISSUED(11, null, StatusType.SUCCESS),
    RECEIVED(12, null, StatusType.SUCCESS),
    CANCELED(13, null, StatusType.DANGER),
    DELETED(14, null, StatusType.DANGER),
    ERROR(15, null, StatusType.DANGER),
    // for Outbound Stock Movement mapping
    DISPATCHED(0, null, StatusType.SUCCESS),
    REQUESTED(0, null, StatusType.PRIMARY),
    // Removed
    OPEN(0, null, StatusType.SUCCESS),
    FULFILLED(0, null, StatusType.SUCCESS),
    REVIEWING(0, null, StatusType.PRIMARY),
    CONFIRMING(0, null, StatusType.PRIMARY)

    int sortOrder
    RequisitionStatus displayStatusCode
    StatusType variant

    RequisitionStatus() { }

    RequisitionStatus(int sortOrder) { this.sortOrder = sortOrder }

    RequisitionStatus(int sortOrder, RequisitionStatus displayStatusCode) {
        this.sortOrder = sortOrder
        this.displayStatusCode = displayStatusCode
    }

    RequisitionStatus(int sortOrder, RequisitionStatus displayStatusCode, StatusType variant) {
        this.sortOrder = sortOrder
        this.displayStatusCode = displayStatusCode
        this.variant = variant
    }
    RequisitionStatus getDisplayStatus() {
        return this.displayStatusCode?:this
    }

    static int compare(RequisitionStatus a, RequisitionStatus b) {
        return a.sortOrder <=> b.sortOrder
    }
    /* remove OPEN, FULFILLED */

    static list() {
        [CREATED, EDITING, VERIFYING, PICKING, PICKED, CHECKING, ISSUED, CANCELED, PENDING, REQUESTED, PENDING_APPROVAL, APPROVED, REJECTED]
    }

    // Default options for outbound list, without supporting request approval
    static listOutboundOptions() {
        [CREATED, EDITING, VERIFYING, PICKING, PICKED, CHECKING, ISSUED, CANCELED]
    }

    // Options for outbounds when current location is supporting request approval (Added approved)
    static listOutboundOptionsWhenApprovalRequired() {
        [CREATED, EDITING, APPROVED, PICKING, PICKED, CHECKING, ISSUED, CANCELED]
    }

    // Options for request list when current location is supporting request approval (Added approved, rejected and waiting for approval)
    // statuses only when approvals are supported: waiting for approval, approved, rejected,
    static listRequestOptionsWhenApprovalRequired() {
        [PENDING_APPROVAL, APPROVED, PICKING, PICKED, CHECKING, REJECTED]
    }

    // Options for request list when current location is supporting request approval, but the user is not Approver
    // Those options are similar to options when location supports request approval and the user is approver,
    // but without “waiting for approval” and “rejected”
    static listRequestOptionsWhenNonApprover() {
        [APPROVED, PICKING, PICKED, CHECKING]
    }

    // Default options for requests list
    // Only outbounds are: created and editing
    // These statuses are only for returns, hence outbound only, not requests: dispatched, requested, pending, canceled
    // Issued requests no longer show in requests list but only on outbounds list
    static listRequestOptions() {
        [VERIFYING, PICKING, PICKED, CHECKING]
    }

    static listPending() {
        [CREATED, EDITING, VERIFYING, PICKING, PICKED, CHECKING, PENDING, REQUESTED, APPROVED, PENDING_APPROVAL]
    }

    static listCompleted() {
        [ISSUED, RECEIVED]
    }

    static listCanceled() {
        [CANCELED, DELETED]
    }

    static listApproval() {
        [APPROVED, REJECTED, PENDING_APPROVAL]
    }

    static listAll() {
        [CREATED, EDITING, VERIFYING, APPROVED, PENDING_APPROVAL, PICKING, PICKED, PENDING, CHECKING, FULFILLED, ISSUED, RECEIVED, CANCELED, DELETED, ERROR]
    }

    static toStockMovementStatus(RequisitionStatus requisitionStatus) {
        switch(requisitionStatus) {
            case RequisitionStatus.EDITING:
                return StockMovementStatusCode.REQUESTING
            case RequisitionStatus.VERIFYING:
                return StockMovementStatusCode.REQUESTED
            case RequisitionStatus.CHECKING:
                return StockMovementStatusCode.PACKED
            case RequisitionStatus.ISSUED:
                return StockMovementStatusCode.DISPATCHED
            case null:
                return StockMovementStatusCode.REQUESTING
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
            case StockMovementStatusCode.PENDING_APPROVAL:
                return RequisitionStatus.PENDING_APPROVAL
            case StockMovementStatusCode.PACKED:
                return RequisitionStatus.CHECKING
            case StockMovementStatusCode.VALIDATED:
                return RequisitionStatus.VERIFYING
            case StockMovementStatusCode.DISPATCHED:
                return RequisitionStatus.ISSUED
            default:
                return RequisitionStatus.valueOf(stockMovementStatus.toString())
        }
    }

    static getStatusSortOrderFormula() {
        return "(case status " +
            "when '${CREATED}' then ${CREATED.sortOrder} " +
            "when '${EDITING}' then ${EDITING.sortOrder} " +
            "when '${PENDING_APPROVAL}' then ${PENDING_APPROVAL.sortOrder} " +
            "when '${APPROVED}' then ${APPROVED.sortOrder} " +
            "when '${VERIFYING}' then ${VERIFYING.sortOrder} " +
            "when '${PICKING}' then ${PICKING.sortOrder} " +
            "when '${PICKED}' then ${PICKED.sortOrder} " +
            "when '${PENDING}' then ${PENDING.sortOrder} " +
            "when '${CHECKING}' then ${CHECKING.sortOrder} " +
            "when '${ISSUED}' then ${ISSUED.sortOrder} " +
            "when '${RECEIVED}' then ${RECEIVED.sortOrder} " +
            "when '${CANCELED}' then ${CANCELED.sortOrder} " +
            "when '${DELETED}' then ${DELETED.sortOrder} " +
            "when '${PENDING_APPROVAL}' then ${PENDING_APPROVAL.sortOrder} " +
            "when '${APPROVED}' then ${APPROVED.sortOrder} " +
            "when '${REJECTED}' then ${REJECTED.sortOrder} " +
            "else 0 " +
            "end)"
    }

    static Closure mapToOption = { RequisitionStatus status ->
        def g = Holders.grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        [
                id: status.name(),
                value: status.name(),
                label: "${g.message(code: 'enum.RequisitionStatus.' + status.name())}",
        ]
    }

    String toString() { return name() }

}
