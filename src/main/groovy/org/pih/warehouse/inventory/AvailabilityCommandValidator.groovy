package org.pih.warehouse.inventory

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class AvailabilityCommandValidator extends ObjectValidator<AvailabilityCommand> {

    @Override
    protected ObjectValidationResult doValidate(AvailabilityCommand command) {
        return new ObjectValidationResult(
                validateProductLotsAreValid(command),
        )
    }

    /**
     * Elements of a list are not validated by default, so manually validate every element in the list. If any of the
     * elements have validation errors, propagate the failure up to the command.
     */
    private ObjectError validateProductLotsAreValid(AvailabilityCommand command) {
        command.productLots.each { ProductLotRequest productLot -> productLot.validate() }

        return command.productLots.any { it.hasErrors() } ?
                rejectField("productLots", command.productLots, "availabilityCommand.productLots.invalid") :
                null
    }
}
