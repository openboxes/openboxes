package org.pih.warehouse.inventory

import grails.validation.ValidationException
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.product.Product

@Component
class InventoryItemManager {

    /**
     * Finds the inventory item for the given product and lot number, creating it if it doesn't exist.
     *
     * @param product The product to get the item for.
     * @param lotNumber The lot number of the item to get.
     * @param expirationDate The expiration date of the lot. Will only be used if we need to create a new item.
     * @param disableRefresh True if the creation of a new InventoryItem should NOT trigger an asynchronous
     *                       product availability refresh. Typically this is only true when callers want to
     *                       trigger the refresh themselves, manually.
     */
    InventoryItem getOrCreateInventoryItem(Product product,
                                           String lotNumber,
                                           Date expirationDate,
                                           boolean disableRefresh = false) {
        InventoryItem inventoryItem = getInventoryItem(product, lotNumber)
        if (inventoryItem) {
            return inventoryItem
        }

        return createInventoryItem(product, lotNumber, expirationDate, disableRefresh)
    }

    /**
     * Finds the inventory item for the given product and lot number.
     */
    InventoryItem getInventoryItem(Product product, String lotNumber) {
        // First check if an inventory item exists for the lotNumber as given. We do this check to ensure
        // that any pre-existing lots (from before we were sanitizing inputs) can still be found.
        InventoryItem inventoryItem = InventoryItem.createCriteria().get() {
            and {
                eq("product.id", product.id)
                if (lotNumber) {
                    or {
                        eq("lotNumber", lotNumber)
                    }
                } else {
                    or {
                        isNull("lotNumber")
                        eq("lotNumber", "")
                    }
                }
            }
        } as InventoryItem

        if (inventoryItem) {
            return inventoryItem
        }

        // Otherwise, sanitize the given lot number and look again (unless the give lot is already sanitized, which
        // means the item does not exist). We do this in two separate queries because we want an exact matching
        // lot number (the above query) to take priority.
        String sanitizedLotNumber = sanitizeLotNumber(lotNumber)
        if (lotNumber == sanitizedLotNumber) {
            return null
        }

        return InventoryItem.findByProductAndLotNumber(product, sanitizedLotNumber)
    }

    private InventoryItem createInventoryItem(Product product,
                                              String lotNumber,
                                              Date expirationDate,
                                              boolean disableRefresh = false) {
        InventoryItem inventoryItem = new InventoryItem(
                product: product,
                lotNumber: sanitizeLotNumber(lotNumber),
                expirationDate: expirationDate,
                disableRefresh: disableRefresh,
        )
        if (!inventoryItem.save()) {
            throw new ValidationException("Error saving inventory item", inventoryItem.errors)
        }
        return inventoryItem
    }

    private String sanitizeLotNumber(String lotNumber) {
        if (StringUtils.isEmpty(lotNumber)) {
            return lotNumber
        }

        return lotNumber.trim()
    }
}
