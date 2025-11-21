package org.pih.warehouse.picking

import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType

class PickTask {

    String id
    String identifier

    Requisition requisition
    String requisitionNumber
    DeliveryTypeCode deliveryTypeCode
    Date dateRequested
    Integer priority = 0
    Person requestedBy
    RequisitionStatus requisitionStatus
    Location facility
    RequisitionType requisitionType

    RequisitionItem requisitionItem
    Product product

    Location location
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
    String reasonCode
    PickTaskStatus status = PickTaskStatus.PENDING

    Date dateCreated
    Date lastUpdated

    static constraints = {
        requisition nullable: false
        requisitionNumber nullable: true
        deliveryTypeCode nullable: true
        dateRequested nullable: true
        priority nullable: true
        requestedBy nullable: true
        requisitionStatus nullable: true
        facility nullable: true
        requisitionType nullable: true
        requisitionItem nullable: true
        product nullable: false
        location nullable: true
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
        reasonCode nullable: true
        status nullable: false
    }

    static mapping = {
        table 'pick_task'
        version false
    }

    Map toJson() {
        [
                id              : id,
                identifier      : identifier,
                requisitionId   : requisition?.id,
                requisitionNumber : requisition?.requestNumber,
                deliveryTypeCode: deliveryTypeCode?.name(),
                dateRequested   : dateRequested,
                priority        : priority,
                requestedBy     : requestedBy?.name,
                requisitionStatus: requisitionStatus,
                facility        : facility?.toBaseJson(),
                requisitionType : requisitionType.name(),
                product         : product?.toJson(),
                location        : location?.toJson(location?.locationType?.locationTypeCode),
                outboundContainer: outboundContainer?.toJson(location?.locationType?.locationTypeCode),
                stagingLocation : stagingLocation?.toJson(location?.locationType?.locationTypeCode),
                inventoryItem   : inventoryItem?.toJson(),
                quantityRequired: quantityRequired,
                quantityPicked  : quantityPicked,
                assignee        : assignee,
                dateAssigned    : dateAssigned,
                dateStarted     : dateStarted,
                pickedBy        : pickedBy?.name,
                datePicked      : datePicked,
                reasonCode      : reasonCode,
                status          : status?.name(),
                dateCreated     : dateCreated,
                lastUpdated     : lastUpdated,
        ]
    }
}