package org.pih.warehouse.inventory.product.availability

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

/**
 * A simple convenience wrapper on a map of available items that is keyed using AvailableItemKey.
 *
 * We use this map structure quite often when working with available items so we provide this class
 * as a way to avoid some boilerplate code.
 */
class AvailableItemMap {
    Map<AvailableItemKey, AvailableItem> map = [:]

    AvailableItem get(AvailableItemKey key) {
        return map.get(key)
    }

    AvailableItem get(Location binLocation, InventoryItem inventoryItem) {
        return get(new AvailableItemKey(binLocation, inventoryItem))
    }

    List<AvailableItem> getAllByProduct(Product product) {
        return map.findAll { key, value -> key.isForProduct(product) }.values().asList()
    }

    boolean contains(AvailableItem availableItem) {
        return map.containsKey(new AvailableItemKey(availableItem))
    }

    AvailableItem put(AvailableItem availableItem) {
        return map.put(new AvailableItemKey(availableItem), availableItem)
    }

    void putAll(List<AvailableItem> availableItems) {
        for (AvailableItem availableItem in availableItems) {
            put(availableItem)
        }
    }

    int size() {
        return map.size()
    }

    Collection<AvailableItem> values() {
        return map.values()
    }

    Set<AvailableItemKey> keys() {
        return map.keySet()
    }

    Set<Map.Entry<AvailableItemKey, AvailableItem>> entrySet() {
        return map.entrySet()
    }

    boolean isEmpty() {
        return map.isEmpty()
    }
}
