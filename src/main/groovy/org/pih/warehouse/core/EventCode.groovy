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

/**
 * Represents a particular category of {@link Event} that can occur during the course of a state-changing operation.
 *
 * EventCodes should be broad, categorical, and general purpose. Custom, feature-specific types of events should be
 * created as an {@Link EventType} so that the system remains flexible to a dynamic range of use cases.
 */
enum EventCode {

    CREATED(true),
    SCHEDULED(false),
    PICKED(false),
    PACKED(false),
    STAGING(false),
    LOADING(false),
    SHIPPED(true),
    IN_TRANSIT(false),
    CUSTOMS_ENTRY(false),
    CUSTOMS_HOLD(false),
    CUSTOMS_RELEASE(false),
    DELIVERED(false),
    RECEIVED(true),
    PARTIALLY_RECEIVED(true),
    CANCELLED(false),
    PENDING_APPROVAL(true),
    APPROVED(true),
    REJECTED(true),
    SUBMITTED(true),

    /**
     * Returns true if the event code represents a system event, meaning there is internal functionality built
     * into the app relating to it. As such, triggering this event likely changes state in the app.
     */
    boolean isSystemEvent

    EventCode(boolean isSystemEvent) {
        this.isSystemEvent = isSystemEvent
    }

    /**
     * Returns true if the event code represents a custom event, meaning there is no in-app functionality built
     * around it. The event is purely a label representing some state change that is managed outside of the app.
     */
    boolean isCustomEvent() {
        return !isSystemEvent
    }

    static List<EventCode> listCustomEventTypeCodes() {
        return values().findAll { it.isCustomEvent() }
    }
}
