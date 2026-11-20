package org.pih.warehouse.inventory

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.PlainObjectValidator
import org.pih.warehouse.requisition.RequisitionSourceType

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
     * Validates the stock movement only when the request is submitted, since the later statuses
     * (e.g. approved or picking) are set at the origin.
     */
    void validateForStatusTransition(StockMovement stockMovement, String status) {
        if (!(status in [StockMovementStatusCode.REQUESTED.name(), StockMovementStatusCode.PENDING_APPROVAL.name()])) {
            return
        }

        validate(stockMovement)
    }

    /**
     * Whether the user is logged in at the requesting location of the e-request.
     */
    private ObjectError validateCurrentLocationIsDestination(StockMovement stockMovement) {
        if (!isElectronicRequest(stockMovement)) {
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
     * A new request carries the source type itself, an existing one takes it from its requisition.
     */
    private boolean isElectronicRequest(StockMovement stockMovement) {
        return stockMovement.sourceType == RequisitionSourceType.ELECTRONIC || stockMovement.electronicType
    }

    /**
     * The saved requisition is checked first, since the stock movement may already have another destination
     * bound from the request body. A new request has no requisition yet, so the stock movement destination is used.
     */
    private Location getRequestingLocation(StockMovement stockMovement) {
        return stockMovement.requisition ? stockMovement.requisition.destination : stockMovement.destination
    }
}
