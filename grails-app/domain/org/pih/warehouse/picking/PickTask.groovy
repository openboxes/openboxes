package org.pih.warehouse.picking

import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class PickTask {

    String id
    String identifier

    Product product
    InventoryItem inventoryItem
    Requisition requisition
    RequisitionItem requisitionItem
    Location location
    Location facility

    BigDecimal quantity = 0
    BigDecimal quantityPicked = 0

    Person pickedBy
    Person requestedBy

    Date dateRequested
    Date datePicked
    Date dateCreated
    Date lastUpdated

    DeliveryTypeCode deliveryTypeCode
    String reasonCode
    Integer priority = 0
    String requisitionStatus

    static constraints = {
        product nullable: false
        inventoryItem nullable: false
        requisition nullable: false
        requisitionItem nullable: true
        location nullable: true
        facility nullable: true
        pickedBy nullable: true
        requestedBy nullable: true
        dateRequested nullable: true
        datePicked nullable: true
        reasonCode nullable: true
        quantity nullable: false
        quantityPicked nullable: true
        deliveryTypeCode nullable: true
        requisitionStatus nullable: true
        priority nullable: true
    }

    static mapping = {
        table 'pick_task'
        version false
    }

    Map toJson() {
        [
                id              : id,
                identifier      : identifier,
                facility        : facility?.toBaseJson(),
                requisitionId   : requisition?.id,
                requestNumber   : requisition?.requestNumber,
                inventoryItem   : inventoryItem?.toJson(),
                product         : product?.toJson(),
                quantity        : quantity,
                quantityPicked  : quantityPicked,
                deliveryTypeCode: deliveryTypeCode?.name(),
                requestedBy     : requestedBy?.name,
                pickedBy        : pickedBy?.name,
                dateRequested   : dateRequested,
                datePicked      : datePicked,
                dateCreated     : dateCreated,
                lastUpdated     : lastUpdated,
                reasonCode      : reasonCode,
                location        : location?.toJson(location?.locationType?.locationTypeCode),
                requisitionStatus: requisitionStatus,
                priority        : priority
        ]
    }
}