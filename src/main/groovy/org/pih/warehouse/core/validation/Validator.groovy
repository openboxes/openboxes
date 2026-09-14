package org.pih.warehouse.core.validation

import grails.validation.ValidationException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

/**
 * Validates instances of some class.
 *
 * We suggest creating validator components for objects whose validation involves performing complex operations, such as
 * calling out to beans and/or making database queries. By breaking validation logic out into a separate, dedicated
 * component, we let our objects remain small and single purpose.
 *
 * This validator works in tandem with framework-aware objects such as Grails Domain classes and Request DTOs.
 * As long as the framework-aware object implements {@link Validatable}, the validation in this class will be triggered
 * alongside any validation defined in the static constraints block of the object and via javax constraint annotations.
 */
abstract class Validator<T> {

    /**
     * Contains the main validation logic for the validator. The returned ObjectValidationResult should contain
     * all validation errors that were triggered during validation.
     *
     * @param toValidate The object instance to be validated.
     * @return ObjectValidationResult the result of the validation. Contains validation errors if there are any.
     */
    protected abstract ObjectValidationResult doValidate(T toValidate)

    /**
     * Extracts the Errors object from the object to validate (or initializes a new Errors instance).
     * This Errors object will be populated with any validation errors that occur.
     */
    abstract Errors getErrors(T toValidate)

    /**
     * Validates the given object. The errors object associated with the object to validate will be populated
     * with any validation errors that occur.
     *
     * It's important to note that for framework-aware Validateable objects, if you call the validator directly
     * via xValidator.validate(x) ONLY the validator logic will be triggered. To trigger the full validation flow
     * of the framework (which includes Grails constraints and Javax annotations), you must use the object's
     * x.validate() method.
     *
     * @param toValidate The object instance to be validated.
     * @param errorOnFailure True if we should throw an exception if validation fails.
     * @return true if the object is valid, false otherwise.
     */
    ObjectValidationResult validate(T toValidate, boolean errorOnFailure = true) {
        // We do not clear errors before validating because we assume that will be handled by the framework.
        ObjectValidationResult results = doValidate(toValidate)
        if (results.valid) {
            return results
        }

        // If there are errors, we add them all to the "errors" field of the object being validated.
        // This ensures that the errors will be detected by Grails' object validation.
        Errors errors = getErrors(toValidate)
        for (ObjectError error in results.errors) {
            switch (error) {
                case FieldError:
                    errors.rejectValue((error as FieldError).field, error.code, error.arguments, error.code)
                    break
                case ObjectError:
                    errors.reject(error.code, error.arguments, error.code)
                    break
                case null:
                    break
                default:
                    throw new IllegalArgumentException("Unknown error type ${error.class}")
            }
        }

        if (errorOnFailure) {
            throw new ValidationException("Validation failed for ${toValidate?.class?.simpleName}", errors)
        }

        return results
    }

    /**
     * Mark a field of the object as invalid.
     *
     * @param fieldName The name of the field that failed validation
     * @param rejectedValue The value of the field that failed validation
     * @param errorCode The l10n message key containing the message to display when rendering the errors of the entity.
     * @param errorArgs Values to use for any args contained within the errorCode message
     */
    protected FieldError rejectField(String fieldName,
                                     Object rejectedValue,
                                     String errorCode,
                                     Object[] errorArgs=null) {
        return new FieldError(
                "Object",  // objectName will be set automatically when adding the errors to the object being validated.
                fieldName,
                rejectedValue,
                false,  // This is a validation failure. (A binding failure would be if we were given the wrong type.)
                [errorCode] as String[],
                errorArgs,
                errorCode)  // If we don't resolve the errorCode, display the code itself. This helps us catch typos.
    }

    /**
     * Mark the object itself as invalid. For use when not validating a specific field.
     *
     * @param errorCode The l10n message key containing the message to display when rendering the errors of the entity.
     * @param errorArgs Values to use for any args contained within the errorCode message
     */
    protected ObjectError rejectObject(String errorCode, Object[] errorArgs=null) {
        return new ObjectError(
                "Object",  // objectName will be set automatically when adding the errors to the object being validated.
                [errorCode] as String[],
                errorArgs,
                errorCode)  // If we don't resolve the errorCode, display the code itself. This helps us catch typos.
    }
}
