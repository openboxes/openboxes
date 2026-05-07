package org.pih.warehouse.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.DomainValidator
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode

@Component
class ProductValidator implements DomainValidator<Product> {

    private static String ACTIVE_FIELD_NAME = "active"

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Autowired
    RequisitionService requisitionService

    @Autowired
    ShipmentService shipmentService

    @Override
    ObjectValidationResult doValidate(Product product) {
        return new ObjectValidationResult(
                validateActive(product),
        )
    }

    List<ObjectError> validateActive(Product product) {
        if (product.active) {
            return Collections.emptyList()
        }

        return [
                validateCannotDeactivateProductWhenManagedLocationHasItInStock(product),
                validateCannotDeactivateProductWhenInNotYetReceivedShipmentToManagedLocation(product),
                validateCannotDeactivateProductWhenInActiveStocklist(product),
        ]
    }

    private ObjectError validateCannotDeactivateProductWhenManagedLocationHasItInStock(Product product) {
        List<Location> locations = productAvailabilityService.getActiveLocationsWithProductInStockAndActivityCode(
                product, ActivityCode.MANAGE_INVENTORY)

        return locations ?
                rejectField(ACTIVE_FIELD_NAME, product.active, "product.active.invalid.inStock", locations) :
                null
    }

    private ObjectError validateCannotDeactivateProductWhenInNotYetReceivedShipmentToManagedLocation(Product product) {
        List<Location> locations = shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(
                product,
                ActivityCode.MANAGE_INVENTORY,
                [ShipmentStatusCode.SHIPPED, ShipmentStatusCode.PARTIALLY_RECEIVED])

        return locations ?
                rejectField(ACTIVE_FIELD_NAME, product.active, "product.active.invalid.inShipment", locations) :
                null
    }

    private ObjectError validateCannotDeactivateProductWhenInActiveStocklist(Product product) {
        boolean productInStockList = requisitionService.isProductInStockList(product)

        return productInStockList ?
                rejectField(ACTIVE_FIELD_NAME, product.active, "product.active.invalid.inStocklist") :
                null
    }
}
