package org.pih.warehouse.core.validation

import org.springframework.validation.Errors

/**
 * A validator for a {@link DomainValidatable} Grails Domain entity.
 *
 * Will be triggered when calling x.validate() on an instance of the target domain.
 */
abstract class DomainValidator<T extends DomainValidatable> extends Validator<T> {

    @Override
    Errors getErrors(T toValidate) {
        return toValidate?.errors
    }
}
