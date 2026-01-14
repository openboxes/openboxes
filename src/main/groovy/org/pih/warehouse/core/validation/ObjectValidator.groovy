package org.pih.warehouse.core.validation

import grails.validation.Validateable
import org.grails.datastore.mapping.validation.ValidationErrors

/**
 * A validator for a non-domain validatable object (such as a Command Object).
 */
trait ObjectValidator<T extends Validateable> implements Validator {

    ValidationErrors getErrors(T toValidate) {
        return toValidate.errors as ValidationErrors
    }
}
