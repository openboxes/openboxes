package org.pih.warehouse

/**
 *  A warehouse event represents an operation within the scope of a
 *  warehouse.  Examples include shipments made from the warehouse
 *  as well as transfers/deliveries made to the warehouse.
 *
 *  Subclasses:  ShipmentEvent, DeliveryEvent, DonationEvent
 *
 *  These subclasses will encapsulate the information needed for
 *  specific events.  These objects are not to be stored in the
 *  database -- they are strictly for convenience as well as to
 *  make the API a little easier to understand.
 */
class InventoryEvent {

    String name		    // shipment, receipt, delivery, donation, correction
    String description

    static constraints = {
    }
}
