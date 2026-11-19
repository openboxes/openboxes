/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.replenishment

import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.Product

/**
 * A pick of a replenishment list: a picklist item of an open transfer order line with a destination bin
 * (see the replenishment_task view). Picked from {@link #location} into an outbound container, staged, and later put
 * away into {@link #destinationLocation}. Read only: changes are made to the picklist item.
 */
class ReplenishmentTask {

    String id
    String identifier

    Order order
    String orderNumber
    OrderStatus orderStatus
    OrderType orderType
    Location facility
    Date dateRequested
    Person requestedBy

    OrderItem orderItem
    Product product

    Location location
    Location destinationLocation
    Location outboundContainer
    Location stagingLocation
    InventoryItem inventoryItem
    BigDecimal quantityRequired = 0
    BigDecimal quantityPicked = 0
    Person assignee
    Date dateAssigned
    Date dateStarted
    Person pickedBy
    Date datePicked
    Person stagedBy
    Date dateStaged
    String reasonCode
    PickTaskStatus status = PickTaskStatus.PENDING

    Date dateCreated
    Date lastUpdated

    static constraints = {
        order nullable: false
        orderNumber nullable: true
        orderStatus nullable: true
        orderType nullable: true
        facility nullable: true
        dateRequested nullable: true
        requestedBy nullable: true
        orderItem nullable: true
        product nullable: false
        location nullable: true
        destinationLocation nullable: true
        outboundContainer nullable: true
        stagingLocation nullable: true
        inventoryItem nullable: false
        quantityRequired nullable: false
        quantityPicked nullable: true
        assignee nullable: true
        dateAssigned nullable: true
        dateStarted nullable: true
        pickedBy nullable: true
        datePicked nullable: true
        stagedBy nullable: true
        dateStaged nullable: true
        reasonCode nullable: true
        status nullable: false
    }

    static mapping = {
        table 'replenishment_task'
        version false // A view: no optimistic locking
    }

    Map toJson() {
        String locationTypeCode = location?.locationType?.locationTypeCode
        [
                id                 : id,
                identifier         : identifier,
                orderId            : order?.id,
                orderItemId        : orderItem?.id,
                orderNumber        : orderNumber,
                orderStatus        : orderStatus?.name(),
                orderType          : orderType?.code,
                dateRequested      : dateRequested,
                requestedBy        : requestedBy?.name,
                facility           : facility?.toBaseJson(),
                product            : product?.toJson(),
                location           : location?.toJson(locationTypeCode),
                destinationLocation: destinationLocation?.toJson(destinationLocation?.locationType?.locationTypeCode),
                outboundContainer  : outboundContainer?.toJson(locationTypeCode),
                stagingLocation    : stagingLocation?.toJson(locationTypeCode),
                inventoryItem      : inventoryItem?.toJson(),
                quantityRequired   : quantityRequired,
                quantityPicked     : quantityPicked,
                assignee           : assignee,
                dateAssigned       : dateAssigned,
                dateStarted        : dateStarted,
                pickedBy           : pickedBy,
                datePicked         : datePicked,
                stagedBy           : stagedBy,
                dateStaged         : dateStaged,
                reasonCode         : reasonCode,
                status             : status?.name(),
                dateCreated        : dateCreated,
                lastUpdated        : lastUpdated,
        ]
    }
}
