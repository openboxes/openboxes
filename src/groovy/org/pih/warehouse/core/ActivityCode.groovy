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

enum ActivityCode {

    MANAGE_INVENTORY('MANAGE_INVENTORY'),   // FIXME should change to MANAGE_STOCK
    ADJUST_INVENTORY('ADJUST_INVENTORY'),   // FIXME should change to ADJUST_STOCK
    SEND_STOCK('SEND_STOCK'),               // FIXME should change to TRANSFER_STOCK
    RECEIVE_STOCK('RECEIVE_STOCK'),
    CONSUME_STOCK('CONSUME_STOCK'),
    ISSUE_STOCK('ISSUE_STOCK'),

    EXTERNAL('EXTERNAL'),

    APPROVE_REQUEST('APPROVE_REQUEST'),
    PLACE_REQUEST('PLACE_REQUEST'),
    FULFILL_REQUEST('FULFILL_REQUEST'),

    APPROVE_ORDER('APPROVE_ORDER'),
    PLACE_ORDER('PLACE_ORDER'),
    FULFILL_ORDER('FULFILL_ORDER'),

    // Activities for INTERNAL_LOCATION
    CROSS_DOCKING('CROSS_DOCKING'),
    PUTAWAY_STOCK('PUTAWAY_STOCK'),
    PICK_STOCK('PICK_STOCK'),
    PACK_STOCK('PACK_STOCK'),
    LOAD_STOCK('LOAD_STOCK'),
    HOLD_STOCK('HOLD_STOCK'),

    // Requisition reason codes
    SUBSTITUTE_REQUISITION_ITEM('SUBSTITUTE_REQUISITION_ITEM'),
    MODIFY_REQUISITION_ITEM('MODIFY_REQUISITION_ITEM'),
    MODIFY_PICKLIST_ITEM('MODIFY_PICKLIST_ITEM'),

    // Notifications
    ENABLE_NOTIFICATIONS('ENABLE_NOTIFICATIONS'),

    // Packing
    PACK_SHIPMENT('PACK_SHIPMENT'),

    // Static slotting
    STATIC_SLOTTING('STATIC_SLOTTING'),

    // Dynamic slotting
    DYNAMIC_SLOTTING('DYNAMIC_SLOTTING'),

    // Directed putaway
    DIRECTED_PUTAWAY('DIRECTED_PUTAWAY'),

    // Creates a dynamic receiving location during receipt
    DYNAMIC_RECEIVING('DYNAMIC_RECEIVING'),

    // Allows partial receipting of quantities
    PARTIAL_RECEIVING('PARTIAL_RECEIVING'),

    // Accounting (Budget Code, GL Account)
    REQUIRE_ACCOUNTING('REQUIRE_ACCOUNTING'),

    // Central purchasing
    ENABLE_CENTRAL_PURCHASING('ENABLE_CENTRAL_PURCHASING'),

    // Requires picking on mobile device
    REQUIRE_MOBILE_PICKING('REQUIRE_MOBILE_PICKING'),

    // Enable etrucknow integration features
    ENABLE_ETRUCKNOW_INTEGRATION('ENABLE_ETRUCKNOW_INTEGRATION'),

    // Order allocation strategies
    ORDER_ALLOCATION_STRATEGY_FEFO('ORDER_ALLOCATION_STRATEGY_FEFO'),
    ORDER_ALLOCATION_STRATEGY_FIFO('ORDER_ALLOCATION_STRATEGY_FIFO'),
    ORDER_ALLOCATION_STRATEGY_LIFO('ORDER_ALLOCATION_STRATEGY_LIFO'),
    ORDER_ALLOCATION_STRATEGY_LOCATION_ASCENDING('ORDER_ALLOCATION_STRATEGY_LOCATION_ASCENDING'),
    ORDER_ALLOCATION_STRATEGY_LOCATION_DESCENDING('ORDER_ALLOCATION_STRATEGY_LOCATION_DESCENDING'),
    ORDER_ALLOCATION_STRATEGY_QUANTITY_ASCENDING('ORDER_ALLOCATION_STRATEGY_QUANTITY_ASCENDING'),
    ORDER_ALLOCATION_STRATEGY_QUANTITY_DESCENDING('ORDER_ALLOCATION_STRATEGY_QUANTITY_DESCENDING'),
    ORDER_ALLOCATION_STRATEGY_BEST_AVAILABLE('ORDER_ALLOCATION_STRATEGY_BEST_AVAILABLE'),
    ORDER_ALLOCATION_STRATEGY_CUSTOM('ORDER_ALLOCATION_STRATEGY_CUSTOM'),

    // Putaway strategies
    PUTAWAY_STRATEGY_EMPTY_LOCATIONS('PUTAWAY_STRATEGY_EMPTY_LOCATIONS'),
    PUTAWAY_STRATEGY_SINGLE_SKU('PUTAWAY_STRATEGY_SINGLE_SKU'),
    PUTAWAY_STRATEGY_SINGLE_LPN('PUTAWAY_STRATEGY_SINGLE_LPN'),

    // Picking strategies
    PICKING_STRATEGY_AUTOMATIC_REALLOCATION('PICKING_STRATEGY_AUTOMATIC_REALLOCATION'),

    final String id

    ActivityCode(String id) { this.id = id }

    static list() {
        [
                MANAGE_INVENTORY,
                ADJUST_INVENTORY,
                APPROVE_ORDER,
                APPROVE_REQUEST,
                PLACE_ORDER,
                PLACE_REQUEST,
                FULFILL_ORDER,
                FULFILL_REQUEST,
                SEND_STOCK,
                RECEIVE_STOCK,
                CONSUME_STOCK,
                CROSS_DOCKING,
                PUTAWAY_STOCK,
                PICK_STOCK,
                PACK_STOCK,
                LOAD_STOCK,
                EXTERNAL,
                ENABLE_NOTIFICATIONS,
                PACK_SHIPMENT,
                DYNAMIC_RECEIVING,
                PARTIAL_RECEIVING,
                DYNAMIC_SLOTTING,
                STATIC_SLOTTING,
                DIRECTED_PUTAWAY,
                REQUIRE_ACCOUNTING,
                ENABLE_CENTRAL_PURCHASING,
                HOLD_STOCK,

                // Integration
                REQUIRE_MOBILE_PICKING,
                ENABLE_ETRUCKNOW_INTEGRATION,

                // Order allocation strategies
                ORDER_ALLOCATION_STRATEGY_FEFO,
                ORDER_ALLOCATION_STRATEGY_FIFO,
                ORDER_ALLOCATION_STRATEGY_LIFO,
                ORDER_ALLOCATION_STRATEGY_LOCATION_ASCENDING,
                ORDER_ALLOCATION_STRATEGY_LOCATION_DESCENDING,
                ORDER_ALLOCATION_STRATEGY_QUANTITY_ASCENDING,
                ORDER_ALLOCATION_STRATEGY_QUANTITY_DESCENDING,
                ORDER_ALLOCATION_STRATEGY_BEST_AVAILABLE,
                ORDER_ALLOCATION_STRATEGY_CUSTOM,

                // Putaway strategies
                PUTAWAY_STRATEGY_EMPTY_LOCATIONS,
                PUTAWAY_STRATEGY_SINGLE_SKU,
                PUTAWAY_STRATEGY_SINGLE_LPN,

                // Picking strategies
                PICKING_STRATEGY_AUTOMATIC_REALLOCATION,
        ]
    }
}
