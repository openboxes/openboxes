package org.pih.warehouse.product

import org.springframework.stereotype.Component

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.validation.GormEntityValidator
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

@Component
class ProductValidator implements GormEntityValidator {

    def validateActive(Product product) {
        if (product.active) {
            return true
        }

        def validationResult = validateCannotDeactivateProductWhenAnyLocationHasAnyStock(product)
        if (!isValidationResultValid(validationResult)) {
            return validationResult
        }

        validationResult = validateCannotDeactivateProductWhenInPendingDepotShipment(product)
        if (!isValidationResultValid(validationResult)) {
            return validationResult
        }

        validationResult = validateCannotDeactivateProductWhenInActiveStocklist(product)
        if (!isValidationResultValid(validationResult)) {
            return validationResult
        }
    }

    private def validateCannotDeactivateProductWhenAnyLocationHasAnyStock(Product product) {
        // TODO: move to some service/repository
        List<Location> locationsWithProductInStock = ProductAvailability.createCriteria().list {
            projections {
                groupProperty("location")
            }
            eq("product", product)
            gt("quantityOnHand", 0)
        } as List<Location>

        return locationsWithProductInStock ? ["invalid.inStock", locationsWithProductInStock] : true
    }

    private def validateCannotDeactivateProductWhenInPendingDepotShipment(Product product) {
        // TODO: move to some service/repository
        List<Location> managedInventoryDepotLocations = Location.createCriteria().list {
            locationType {
                eq("locationTypeCode", LocationTypeCode.DEPOT)
            }
        } as List<Location>
        managedInventoryDepotLocations = managedInventoryDepotLocations.findAll { it.supports(ActivityCode.MANAGE_INVENTORY) }

        List<Location> locationsWithShipment = ShipmentItem.createCriteria().list {
            createAlias("shipment", "shipmentAlias")
            projections {
                groupProperty("shipmentAlias.destination")
            }
            eq("product", product)
            inList("shipmentAlias.currentStatus", [ShipmentStatusCode.SHIPPED, ShipmentStatusCode.PARTIALLY_RECEIVED])
            inList("shipmentAlias.destination", managedInventoryDepotLocations)
        } as List<Location>

        return locationsWithShipment ? ["invalid.inShipment", locationsWithShipment] : true
    }

    private def validateCannotDeactivateProductWhenInActiveStocklist(Product product) {
        // TODO: move to some service/repository
        int numActiveStocklistsForProduct = RequisitionItem.createCriteria().count {
            eq("product", product)
            requisition {
                eq("isTemplate", true)
                eq("isPublished", true)
            }
        }

        return numActiveStocklistsForProduct > 0 ? ["invalid.inStocklist"] : true
    }
}
