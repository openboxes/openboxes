/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import grails.gorm.transactions.NotTransactional

@Transactional
class InventoryLevelService {

    def productAvailabilityService
    def inventorySnapshotService

    // Associations are resolved explicitly (by id or natural key) rather than via data binding, so we don't
    // expose the raw domain bindings; everything else is bound.
    private static final List<String> NON_BINDABLE_FIELDS = [
        'id', 'inventory', 'product', 'productId', 'productCode',
        'internalLocation', 'preferredBinLocation', 'replenishmentLocation'
    ]

    // @NotTransactional to avoid two open sessions with the per-item withNewTransaction
    @NotTransactional
    List<Map> bulkUpsert(Location facility, List<Map> items, boolean deferRefresh) {
        List<Map> results = []
        items.eachWithIndex { Map json, int index ->
            try {
                InventoryLevel.withNewTransaction { status ->
                    InventoryLevel inventoryLevel =
                        (json.identifier ? InventoryLevel.findByIdentifier(json.identifier) : null) ?: new InventoryLevel()
                    boolean isNew = !inventoryLevel.id

                    inventoryLevel.inventory = facility.inventory
                    bindInventoryLevelData(inventoryLevel, json)

                    if (!inventoryLevel.validate()) {
                        status.setRollbackOnly()
                        results << [
                            index       : index,
                            status      : 'error',
                            errorMessage: 'Validation failed',
                            errors      : inventoryLevel.errors.allErrors.collect {
                                [field: it.field, code: it.code, message: it.defaultMessage ?: it.code]
                            }
                        ]
                        return
                    }

                    inventoryLevel.save()

                    if (!deferRefresh && inventoryLevel.product?.id) {
                        productAvailabilityService.triggerRefreshProductAvailability(
                            facility.id, [inventoryLevel.product.id], true)
                        inventorySnapshotService.triggerRefreshInventorySnapshot(
                            facility.id, [inventoryLevel.product.id], true)
                    }

                    results << [
                        index           : index,
                        status          : 'ok',
                        action          : isNew ? 'created' : 'updated',
                        inventoryLevelId: inventoryLevel.id,
                        productId       : inventoryLevel.product?.id
                    ]
                }
            } catch (Exception e) {
                log.error("bulkUpsert: error at index ${index}: ${e.message}", e)
                results << [
                    index       : index,
                    status      : 'error',
                    errorMessage: e.message
                ]
            }
        }
        return results
    }

    private InventoryLevel bindInventoryLevelData(InventoryLevel inventoryLevel, Map json) {
        String productKey = json.productId ?: json.productCode
        if (productKey) {
            inventoryLevel.product = Product.findByIdOrProductCode(productKey, productKey)
        }

        DataBindingUtils.bindObjectToInstance(
            inventoryLevel, new SimpleMapDataBindingSource(json), null, NON_BINDABLE_FIELDS, null)

        if (json.containsKey('internalLocation')) {
            inventoryLevel.internalLocation = resolveLocation(json.internalLocation)
        }
        if (json.containsKey('preferredBinLocation')) {
            inventoryLevel.preferredBinLocation = resolveLocation(json.preferredBinLocation)
        }
        if (json.containsKey('replenishmentLocation')) {
            inventoryLevel.replenishmentLocation = resolveLocation(json.replenishmentLocation)
        }

        return inventoryLevel
    }

    private Location resolveLocation(def value) {
        if (!value) {
            return null
        }
        String key = value instanceof Map ? (value.id ?: value.locationNumber) : value
        return key ? Location.findByIdOrLocationNumber(key, key) : null
    }
}
