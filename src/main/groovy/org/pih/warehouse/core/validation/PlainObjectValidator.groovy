package org.pih.warehouse.core.validation

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors

/**
 * A simple object validator that exist outside of the context of the framework.
 *
 * For use when your object doesn't rely on Grails or Javax validation, or for when you don't want to trigger
 * framework validation.
 *
 * This is different from an {@link ObjectValidator} in that it cannot be hooked into Grails validation. You can only
 * use this validator directly via xValidator.validate(x) calls. There is no way to hook a PlainObjectValidator into an
 * object's x.validate() method like you can with the other {@link Validator} implementations. If you need to rely on
 * framework validation (such as when saving domain entities or binding request DTOs), use the other validator
 * implementations that are framework-aware.
 *
 * It is perfectly fine to have a PlainObjectValidator that operates on an {@link ObjectValidatable}, but know that
 * it will ignore any Grails constraints and Javax annotations on the object.
 */
abstract class PlainObjectValidator<T> extends Validator<T> {

    @Override
    Errors getErrors(T toValidate) {
        /*
         * This validator is intentionally framework unaware, so we don't want to rely on the existence of a Grails
         * "errors" field on the object. As such, we need to initialize our own Errors object.
         *
         * This also means that even if the object has an "errors" field, triggering this validator will not populate
         * the field. You must rely on the errors field in the returned ObjectValidationResult.
         *
         * Counterintuitively, BeanPropertyBindingResult is the recommended Errors implementation to use, even for
         * non-data binding errors.
         */
        return new BeanPropertyBindingResult(toValidate, toValidate?.class?.simpleName)
    }
}
