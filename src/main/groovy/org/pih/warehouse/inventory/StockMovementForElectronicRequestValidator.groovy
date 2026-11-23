package org.pih.warehouse.inventory

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.PlainObjectValidator

/**
 * Validates that an e-request is saved or submitted by a user logged in at its requesting location (the destination).
 * Other stock movements (e.g. inbounds or outbounds) are not validated.
 */
@Component
class StockMovementForElectronicRequestValidator extends PlainObjectValidator<StockMovement> {

    @Override
    protected ObjectValidationResult doValidate(StockMovement stockMovement) {
        return new ObjectValidationResult(
                validateCurrentLocationIsDestination(stockMovement),
        )
    }

    /**
     * Whether the user is logged in at the requesting location of the e-request.
     */
    private ObjectError validateCurrentLocationIsDestination(StockMovement stockMovement) {
        if (!stockMovement.electronicType) {
            return null
        }

        Location requestingLocation = getRequestingLocation(stockMovement)
        if (requestingLocation?.id == AuthService.currentLocation?.id) {
            return null
        }

        return rejectField("destination", requestingLocation,
                "stockMovement.request.isDifferentLocation.message", [requestingLocation?.name])
    }

    /**
     * The saved requisition is checked first, since the stock movement may already have another destination
     * bound from the request body. A new request has no requisition yet, so the stock movement destination is used.
     */
    private Location getRequestingLocation(StockMovement stockMovement) {
        return stockMovement.requisition?.destination ?: stockMovement.destination
    }
}
