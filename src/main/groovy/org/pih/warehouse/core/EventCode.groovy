/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

enum EventCode {

    CREATED,
    SCHEDULED,
    PICKED,
    PACKED,
    STAGING,
    LOADING,
    SHIPPED,
    IN_TRANSIT,
    CUSTOMS_ENTRY,
    CUSTOMS_HOLD,
    CUSTOMS_RELEASE,
    DELIVERED,
    RECEIVED,
    PARTIALLY_RECEIVED,
    CANCELLED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUBMITTED


    static list() {
        [
            SCHEDULED,
            SHIPPED,
            IN_TRANSIT,
            CUSTOMS_ENTRY,
            CUSTOMS_HOLD,
            CUSTOMS_RELEASE,
            DELIVERED,
            RECEIVED,
            PARTIALLY_RECEIVED,
            CANCELLED,
            PENDING_APPROVAL,
            APPROVED,
            REJECTED,
            SUBMITTED
        ]
    }

    static listInTransit() {
        [SHIPPED, IN_TRANSIT, CUSTOMS_ENTRY, CUSTOMS_HOLD, CUSTOMS_RELEASE]
    }

    static listPending() {
        [SCHEDULED]
    }

    static List<EventCode> listSystemEventTypeCodes() {
        return [
            CREATED,
            SHIPPED,
            RECEIVED,
            PARTIALLY_RECEIVED,
            REJECTED,
            SUBMITTED,
            PENDING_APPROVAL,
            APPROVED
        ]
    }

    static List<EventCode> listCustomEventTypeCodes() {
        return values() - listSystemEventTypeCodes()
    }

}

