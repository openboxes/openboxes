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

public enum RequisitionStatus {
	CREATED(1),
    EDITING(2),
    REVIEWING(3),
	PICKING(4),
	PICKED(5),
	PENDING(6),
	FULFILLED(7),
    CONFIRMING(8),
	ISSUED(9),
	RECEIVED(10),
	CANCELED(11)
	
	int sortOrder

	RequisitionStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(RequisitionStatus a, RequisitionStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	/* remove OPEN, FULFILLED */
	static list() {
		[ CREATED, EDITING, REVIEWING, PICKING, PICKED, PENDING, CONFIRMING, ISSUED, RECEIVED, CANCELED ]
	}

    static listAll() {
        [ CREATED, EDITING, REVIEWING, PICKING, PICKED, PENDING, CONFIRMING, FULFILLED, ISSUED, RECEIVED, CANCELED ]
    }
	
	String toString() { return name() }

}
