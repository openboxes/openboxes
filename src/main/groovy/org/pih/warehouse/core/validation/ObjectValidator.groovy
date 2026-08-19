package org.pih.warehouse.core.validation

import org.springframework.validation.Errors

/**
 * A validator for an {@link ObjectValidatable} Grails object, such as a Request DTO / Command Object.
 *
 * Will be triggered automatically for command object instances in controller method args, or when calling
 * x.validate() on an instance of the target object.
 */
abstract class ObjectValidator<T extends ObjectValidatable> extends Validator<T> {

    @Override
    Errors getErrors(T toValidate) {
        return toValidate?.errors
    }
}
