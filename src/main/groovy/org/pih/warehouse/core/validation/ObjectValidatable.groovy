package org.pih.warehouse.core.validation

import grails.validation.Validateable

/**
 * Marks a non-entity object (such as a Command Object) as able to be validated.
 *
 * Hooks into Grails validation by wrapping Validateable.validate() methods with additional validation.
 */
trait ObjectValidatable<V extends Validator> implements Validatable<V>, Validateable {

    @Override
    boolean validate(List fieldsToValidate, Map<String, Object> params, Closure<?>... adHocConstraintsClosures) {
        boolean grailsValid = super.validate(fieldsToValidate, params, adHocConstraintsClosures)
        return performAdditionalValidation(grailsValid, fieldsToValidate)
    }
}
