package org.pih.warehouse.inventory

import grails.validation.ValidationException
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.product.lot.ProductLot
import org.pih.warehouse.product.Product

@Component
class InventoryItemManager {

    /**
     * Finds the inventory item for the given product lot, creating it if it doesn't exist, and otherwise updating its
     * expiration date if it has changed.
     *
     * @param product The product associated with the lot.
     * @param lotNumber The lot number of the item.
     * @param expirationDate The expiration date of the lot. If the lot exists, it's expiration date will be updated
     *                       to this one if they differ.
     * @param disableRefresh True if the creation of a new InventoryItem should NOT trigger an asynchronous
     *                       product availability refresh. Typically this is only true when callers want to
     *                       trigger the refresh themselves, manually.
     */
    InventoryItem upsertInventoryItem(Product product,
                                      String lotNumber,
                                      Date expirationDate,
                                      boolean disableRefresh = false) {
        InventoryItem inventoryItem = getInventoryItem(product, lotNumber)
        if (!inventoryItem) {
            inventoryItem = createInventoryItem(product, lotNumber, expirationDate, disableRefresh)
        }
        if (expirationDate != inventoryItem.expirationDate) {
            inventoryItem = updateExpirationDate(inventoryItem, expirationDate)
        }
        return inventoryItem
    }

    /**
     * Finds the inventory item for the given product lot, creating it if it doesn't exist, and otherwise updating its
     * expiration date if it has changed.
     *
     * @param productLot The product + lot of the item to upsert.
     * @param disableRefresh True if the creation of a new InventoryItem should NOT trigger an asynchronous
     *                       product availability refresh. Typically this is only true when callers want to
     *                       trigger the refresh themselves, manually.
     */
    InventoryItem upsertInventoryItem(ProductLot productLot, boolean disableRefresh = false) {
        return upsertInventoryItem(productLot.product, productLot.lotNumber, productLot.expirationDate, disableRefresh)
    }

    /**
     * Finds the inventory items for the given product lots, creating them if they don't exist, and otherwise
     * updating their expiration dates if they have changed.
     *
     * @param productLot The product + lot of the items to upsert.
     * @param disableRefresh True if the creation of a new InventoryItem should NOT trigger an asynchronous
     *                       product availability refresh. Typically this is only true when callers want to
     *                       trigger the refresh themselves, manually.
     */
    InventoryItemByProductLot upsertInventoryItems(Collection<ProductLot> productLots, boolean disableRefresh = false) {
        InventoryItemByProductLot inventoryItemMap = new InventoryItemByProductLot()
        for (ProductLot productLot in productLots) {
            // We might have been given the same lot multiple times, so ignore any repeats that we've already processed
            if (inventoryItemMap.containsKey(productLot)) {
                // We could consider adding some validation here, such as checking that the expiration dates match
                // across repeat lots.
                continue
            }

            InventoryItem inventoryItem = upsertInventoryItem(productLot, disableRefresh)
            inventoryItemMap.put(productLot, inventoryItem)
        }
        return inventoryItemMap
    }

    /**
     * Finds the inventory item for the given product and lot number.
     */
    InventoryItem getInventoryItem(Product product, String lotNumber) {
        // First check if an inventory item exists for the lotNumber as given. We do this check to ensure
        // that any pre-existing lots (from before we were sanitizing inputs) can still be found.
        InventoryItem inventoryItem = InventoryItem.createCriteria().get() {
            and {
                eq("product", product)
                if (lotNumber) {
                    eq("lotNumber", lotNumber)
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

        // Otherwise, sanitize the given lot number and look again (unless the given lot is already sanitized, which
        // means the item does not exist). We do this in two separate queries because we want an exact matching
        // lot number (the above query) to take priority.
        String sanitizedLotNumber = sanitizeLotNumber(lotNumber)
        if (lotNumber == sanitizedLotNumber) {
            return null
        }

        return InventoryItem.findByProductAndLotNumber(product, sanitizedLotNumber)
    }

    /**
     * Finds the inventory items for the given product lots, matching each lot number as it is given.
     */
    List<InventoryItem> getInventoryItems(List<ProductLot> productLots) {
        if (!productLots) {
            return []
        }

        return InventoryItem.createCriteria().list {
            or {
                productLots.each { ProductLot productLot ->
                    and {
                        eq("product", productLot.product)
                        if (productLot.lotNumber) {
                            eq("lotNumber", productLot.lotNumber)
                        } else {
                            or {
                                isNull("lotNumber")
                                eq("lotNumber", "")
                            }
                        }
                    }
                }
            }
        } as List<InventoryItem>
    }

    /**
     * Updates the expiration date of an existing inventory item, clearing it when none is given. An inventory item
     * is a product lot shared by every depot, so the change reaches all of them.
     */
    InventoryItem updateExpirationDate(InventoryItem inventoryItem, Date expirationDate) {
        if (inventoryItem.expirationDate == expirationDate) {
            return inventoryItem
        }

        inventoryItem.expirationDate = expirationDate

        if (!inventoryItem.validate()) {
            throw new ValidationException("Error saving inventory item", inventoryItem.errors)
        }
        return inventoryItem
    }

    private InventoryItem createInventoryItem(Product product,
                                              String lotNumber,
                                              Date expirationDate,
                                              boolean disableRefresh = false) {
        InventoryItem inventoryItem = new InventoryItem(
                product: product,
                lotNumber: sanitizeLotNumber(lotNumber),
                expirationDate: expirationDate,
        )
        inventoryItem.disableRefresh = disableRefresh  // Transient fields can't be set via map constructor

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
