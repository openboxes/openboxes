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

public enum RequisitionType {
	WARD_STOCK(0),
    WARD_ADHOC(1),
	WARD_NON_STOCK(2),
	DEPOT_STOCK(3),
    DEPOT_ADHOC(4),
	DEPOT_NON_STOCK(5),
	DEPOT_TO_DEPOT(6)

	int sortOrder

	RequisitionType(int sortOrder) {
		[
			this.sortOrder = sortOrder
		]
	}

	static int compare(RequisitionType a, RequisitionType b) {
		return a.sortOrder <=> b.sortOrder
	}

	static list() {
		[
			WARD_STOCK,
			WARD_NON_STOCK,
			WARD_ADHOC,
			DEPOT_TO_DEPOT,
			DEPOT_STOCK,
			DEPOT_NON_STOCK
		]
	}

	static listStockTypes() {
		[WARD_STOCK, DEPOT_STOCK]
	}

	String toString() {
		return name()
	}
}
