package org.pih.warehouse.core.validation

import grails.validation.Validateable

/**
 * Marks a Grails Validateable object (such as a Request DTO / Command Object) as able to be validated by our
 * custom validation flow. This is achieved by hooking our custom validation into the object's validate() method.
 *
 * This allows us to use the @Valid annotation. Any ObjectValidatable in a controller method declaration that
 * is annotated with @Valid will automatically throw an exception if any of its fields are invalid.
 *
 * For example: "def someAction(@Valid XCommand request) { ... }" will throw an error if the XCommand is invalid.
 *
 * @param <V> Optional. The {@link ObjectValidator} component containing additional validation to perform on this
 *            object. If not provided, will only validate via Grails constraints and Javax annotations.
 */
trait ObjectValidatable<V extends ObjectValidator> implements Validatable<V>, Validateable {

    @Override
    boolean validate(List fieldsToValidate, Map<String, Object> params, Closure<?>... adHocConstraintsClosures) {
        boolean grailsValid = super.validate(fieldsToValidate, params, adHocConstraintsClosures)
        return performAdditionalValidation(grailsValid, fieldsToValidate)
    }
}
