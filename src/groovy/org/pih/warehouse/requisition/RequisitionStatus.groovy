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

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum RequisitionStatus {

	NEW(1),
	REQUESTED(2),
	OPEN(3),
	PICKED(4),
	FULFILLED(5),
	SHIPPED(6),
	RECEIVED(7),
	CANCELED(8)
	
	int sortOrder

	RequisitionStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(RequisitionStatus a, RequisitionStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ NEW, REQUESTED, OPEN, PICKED, FULFILLED, SHIPPED, RECEIVED, CANCELED ]
	}
	
	String toString() { return name() }

}
