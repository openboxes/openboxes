package org.pih.warehouse.core.validation

import grails.validation.Validateable
import org.springframework.validation.Errors

/**
 * A validator for a non-domain validatable object (such as a Command Object).
 */
trait ObjectValidator<T extends Validateable> implements Validator<T> {

    @Override
    Errors getErrors(T toValidate) {
        return toValidate.errors
    }
}
