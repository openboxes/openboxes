package org.pih.warehouse.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.GormEntityValidator
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode

@Component
class ProductValidator implements GormEntityValidator {

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Autowired
    RequisitionService requisitionService

    @Autowired
    ShipmentService shipmentService

    def validateActive(Product product) {
        if (product.active) {
            return true
        }

        def validationResult = validateCannotDeactivateProductWhenManagedLocationHasItInStock(product)
        if (!isValidationResultValid(validationResult)) {
            return validationResult
        }

        validationResult = validateCannotDeactivateProductWhenInPendingShipmentToManagedLocation(product)
        if (!isValidationResultValid(validationResult)) {
            return validationResult
        }

        validationResult = validateCannotDeactivateProductWhenInActiveStocklist(product)
        if (!isValidationResultValid(validationResult)) {
            return validationResult
        }
    }

    private def validateCannotDeactivateProductWhenManagedLocationHasItInStock(Product product) {
        List<Location> locations = productAvailabilityService.getActiveLocationsWithProductInStockAndActivityCode(
                product, ActivityCode.MANAGE_INVENTORY)

        return locations ? ["invalid.inStock", locations] : true
    }

    private def validateCannotDeactivateProductWhenInPendingShipmentToManagedLocation(Product product) {
        List<Location> locations =  shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(
                product,
                ActivityCode.MANAGE_INVENTORY,
                [ShipmentStatusCode.SHIPPED, ShipmentStatusCode.PARTIALLY_RECEIVED])


        return locations ? ["invalid.inShipment", locations] : true
    }

    private def validateCannotDeactivateProductWhenInActiveStocklist(Product product) {
        boolean productInStockList = requisitionService.isProductInStockList(product)

        return productInStockList ? ["invalid.inStocklist"] : true
    }
}
