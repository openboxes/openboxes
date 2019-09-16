/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package util

import org.pih.warehouse.inventory.InventoryStatus

class InventoryUtil {

    static String getStatusMessage(inventoryStatus, minQuantity, reorderQuantity, maxQuantity, currentQuantity) {
        def statusMessage = ""
        if (inventoryStatus == InventoryStatus.SUPPORTED || !inventoryStatus) {
            if (currentQuantity <= 0) {
                statusMessage = "STOCK_OUT"
            } else {
                if (minQuantity && minQuantity > 0 && currentQuantity <= minQuantity) {
                    statusMessage = "LOW_STOCK"
                } else if (reorderQuantity && reorderQuantity > 0 && currentQuantity <= reorderQuantity) {
                    statusMessage = "REORDER"
                } else if (maxQuantity && maxQuantity > 0 && currentQuantity > maxQuantity) {
                    statusMessage = "OVERSTOCK"
                } else if (currentQuantity > 0) {
                    statusMessage = "IN_STOCK"
                } else {
                    statusMessage = "OBSOLETE"
                }
            }
        } else if (inventoryStatus == InventoryStatus.NOT_SUPPORTED) {
            statusMessage = "NOT_SUPPORTED"
        } else if (inventoryStatus == InventoryStatus.SUPPORTED_NON_INVENTORY) {
            statusMessage = "SUPPORTED_NON_INVENTORY"
        } else {
            statusMessage = "UNAVAILABLE"
        }
        return statusMessage
    }
}
