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
import org.pih.warehouse.api.UpsertResult
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import grails.gorm.transactions.NotTransactional
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

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
    List<UpsertResult> bulkUpsert(Location facility, List<Map> items, boolean deferRefresh) {
        return items.withIndex().collect { Map json, int index -> upsert(facility, json, deferRefresh).withIndex(index) }
    }

    // @NotTransactional to avoid two open sessions with the per-item withNewTransaction
    @NotTransactional
    UpsertResult upsert(Location facility, Map json, boolean deferRefresh) {
        try {
            return InventoryLevel.withNewTransaction { status ->
                InventoryLevel inventoryLevel =
                    (json.identifier ? InventoryLevel.findByIdentifierAndInventory(json.identifier, facility.inventory) : null) ?: new InventoryLevel()
                boolean isNew = !inventoryLevel.id

                inventoryLevel.inventory = facility.inventory
                bindInventoryLevelData(inventoryLevel, json)

                if (!inventoryLevel.validate()) {
                    status.setRollbackOnly()
                    return UpsertResult.error('Validation failed', toErrorMaps(inventoryLevel.errors))
                }

                if (!inventoryLevel.save()) {
                    status.setRollbackOnly()
                    return UpsertResult.error('Save failed', toErrorMaps(inventoryLevel.errors))
                }

                if (!deferRefresh && inventoryLevel.product?.id) {
                    productAvailabilityService.triggerRefreshProductAvailability(
                        facility.id, [inventoryLevel.product.id], true)
                    inventorySnapshotService.triggerRefreshInventorySnapshot(
                        facility.id, [inventoryLevel.product.id], true)
                }

                return UpsertResult.ok(isNew, inventoryLevel.id, inventoryLevel.product?.id)
            }
        } catch (Exception e) {
            log.error("upsert: ${e.message}", e)
            return UpsertResult.error(e.message)
        }
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

    private static List<Map> toErrorMaps(Errors errors) {
        errors.allErrors.collect {
            [field  : it instanceof FieldError ? ((FieldError) it).field : null,
             code   : it.code,
             message: it.defaultMessage ?: it.code].findAll { k, v -> v != null }
        }
    }
}
