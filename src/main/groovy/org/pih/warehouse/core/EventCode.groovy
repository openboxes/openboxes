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
 * Enumerates the specific, discrete types of {@link Event} that can be triggered via interactions with
 * the application (even if those events don't end up altering state or status).
 *
 * Custom, implementation-specific events that are not triggered via application logic should be defined
 * by adding an {@link EventType} with the CUSTOM code.
 *
 * Avoid working directly with EventCode. Anything that references an EventCode should do so via it's EventType(s).
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
    PUTAWAY(false),
    PARTIALLY_PUTAWAY(false),

    /**
     * Custom, implementation-specific events that are not triggered by application logic. Custom events
     * can only be triggered manually by users, likely via some create event API call.
     *
     * By default, the system will contain no custom events. Custom events need to be configured by adding
     * an {@link EventType} to the database with the CUSTOM code.
     */
    CUSTOM(false)

    /**
     * Returns true if the event code represents an event that is core to the system, meaning it is triggered
     * by application logic.
     *
     * This field only exists for backwards compatability reasons from before we had the CUSTOM code. Going forward,
     * every event code that is not CUSTOM should be a system event.
     */
    boolean isSystemEvent

    EventCode(boolean isSystemEvent) {
        this.isSystemEvent = isSystemEvent
    }

    /**
     * Returns true if the event code represents a custom event, meaning there is no in-app functionality built
     * around it. Custom events are purely labels representing some state change that is managed outside of the app.
     */
    boolean isCustomEvent() {
        // This check on isSystemEvent is to support legacy behaviour from before we added the CUSTOM event.
        // Going forward, custom events should only represent the events that use the CUSTOM code.
        return !isSystemEvent || this == CUSTOM
    }

    static List<EventCode> listCustomEventTypeCodes() {
        return values().findAll { it.isCustomEvent() }
    }

    static List<EventCode> listReceiptEventTypeCodes() {
        return [
                PARTIALLY_RECEIVED,
                RECEIVED,
        ]
    }

    boolean isReceiptEvent() {
        return listReceiptEventTypeCodes().contains(this)
    }

    static List<EventCode> listPutawayEventTypeCodes() {
        return [
                PARTIALLY_PUTAWAY,
                PUTAWAY,
        ]
    }

    boolean isPutawayEvent() {
        return listPutawayEventTypeCodes().contains(this)
    }
}
