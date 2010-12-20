package org.pih.warehouse.core;

/**
 * Similar to transaction types.  We want to remodel this such that there are 
 * a small collection of fixed, enumerated event types, which the system codes 
 * around.  Any user-configurable event types wrap/link to one of these 
 * underlying event types.
 * 
 *  REQUEST_SENT -> Order placed
 *	REQUEST_RECV -> Order received
 * 	REQUEST_WAIT -> Order awaiting approval; Checking stock; Packing shipment...
 * 	SHIPMENT_SENT -> Order shipped
 *	SHIPMENT_WAIT -> Shipment in transit, Shipment clearing customs, etc
 *	SHIPMENT_RECV -> Shipment received 
 * 
 * @author jmiranda
 */

public enum EventTypes {

	REQUEST_SENT('Request Sent'),
	REQUEST_WAIT('Request Pending'),
	REQUEST_RECV('Request Received'),
	SHIPMENT_SENT('Shipment Sent'),
	SHIPMENT_WAIT('Shipment Pending'),
	SHIPMENT_RECV('Shipment Received');

	String name

	EventTypes(String name) { this.name = name; }

	static list() {
		[ REQUEST_SENT, REQUEST_WAIT, REQUEST_RECV, SHIPMENT_SENT, SHIPMENT_WAIT, SHIPMENT_RECV ]
	}
}

