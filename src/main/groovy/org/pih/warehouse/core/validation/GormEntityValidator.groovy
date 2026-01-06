package org.pih.warehouse.core.validation

/**
 * Base trait representing a Validator for a GORM entity/domain class.
 *
 * Validator classes can be created for entities when we need to perform complex logic during validation that involves
 * calling out to other beans and/or making database queries. By using a validator, we can let the entity class remain
 * small, with only simple, static checks in the constraints block.
 *
 * TODO: see if we can work this into the framework better so that we don't even need to call the validator in
 *       the constraints block. Maybe we can have the entity class extend some trait that will handle auto-wiring
 *       in the validator.
 */
trait GormEntityValidator {

    /**
     * @return true if the given post-validation object represents a succeeded validation, false otherwise.
     */
    boolean isValidationResultValid(Object validationResult) {
        if (validationResult instanceof Boolean) {
            return validationResult
        }
        if (validationResult instanceof List) {
            return validationResult.empty
        }
        throw new IllegalArgumentException("Validation results can only be a List<String> or a Boolean.")
    }
}
