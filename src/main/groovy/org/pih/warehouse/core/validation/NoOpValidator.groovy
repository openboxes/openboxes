package org.pih.warehouse.core.validation

import org.springframework.validation.Errors

/**
 * Always returns valid.
 *
 * For use on simple Validatable objects that don't need a custom validator.
 * Exists so that we can make the compiler happy.
 */
trait NoOpValidator implements Validator<Object> {

    @Override
    ObjectValidationResult doValidate(Object toValidate) {
        return ObjectValidationResult.valid()
    }

    @Override
    Errors getErrors(Object toValidate) {
        return null
    }
}
