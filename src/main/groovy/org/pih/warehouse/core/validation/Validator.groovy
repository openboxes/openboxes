package org.pih.warehouse.core.validation

import org.grails.datastore.mapping.validation.ValidationErrors
import org.springframework.core.GenericTypeResolver
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

/**
 * Validates instances of some class.
 *
 * We can use validators for objects whose validation involves performing complex operations such as calling out to
 * beans and/or making database queries. By using a validator, we let our objects remain small and single purpose.
 *
 * This validator works in tandem with Grails Domain classes and non-domain classes that implement {@link Validatable}.
 * The validation in this class is in addition to any validation defined in the static constraints block of the object.
 */
trait Validator<T> implements org.springframework.validation.Validator {

    /**
     * Contains the main validation logic for the validator. The returned ObjectValidationResult should contain
     * all validation errors that were triggered during validation.
     *
     * Do not call this method directly! To validate an object, call {@link #validate(T)} instead.
     *
     * @param toValidate The object instance to be validated.
     * @return ObjectValidationResult the result of the validation. Contains validation errors if there are any.
     */
    abstract ObjectValidationResult doValidate(T toValidate)

    /**
     * Extracts the Errors object from the object to validate (or initializes a new Errors instance).
     * This Errors object will be populated with any validation errors that occur.
     */
    abstract ValidationErrors getErrors(T toValidate)

    private Class<T> getClassOfValidatableObject() {
        // Determines (at runtime) the type of the class level generic "T".
        return (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), Validator.class)
    }

    @Override
    boolean supports(Class<?> clazz) {
        return classOfValidatableObject.isAssignableFrom(clazz)
    }

    @Override
    void validate(Object toValidate, Errors errors) {
        if (!supports(toValidate.class)) {
            throw new IllegalArgumentException("Validator ${getClass()} does not support validating class: ${toValidate.class}")
        }

        validate(classOfValidatableObject.cast(toValidate))
    }

    /**
     * Validates the given object. The errors object associated with the object to validate will be populated
     * with any validation errors that occur.
     *
     * @return true if the object is valid, false otherwise.
     */
    boolean validate(T toValidate) {

        // TODO: We may need to clear the ValidationErrors here before proceeding with validation. It's possible
        //       that if we don't do this, calling validate a second time will still return errors, even if we
        //       modify the fields to have valid values.

        ObjectValidationResult results = doValidate(toValidate)
        if (results.valid) {
            return true
        }

        // If there are errors, we add them all to the "errors" field of the object being validated.
        // This ensures that the errors will be detected by Grails' object validation.
        ValidationErrors errors = getErrors(toValidate)
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
        return !errors.hasErrors()
    }

    /**
     * Mark a field of the object as invalid.
     *
     * @param fieldName The name of the field that failed validation
     * @param rejectedValue The value of the field that failed validation
     * @param errorCode The l10n message key containing the message to display when rendering the errors of the entity.
     * @param errorArgs Values to use for any args contained within the errorCode message
     */
    FieldError rejectField(String fieldName, Object rejectedValue, String errorCode, Object[] errorArgs=null) {
        return new FieldError(
                "Object",  // objectName will be set automatically when adding the errors to the object being validated.
                fieldName,
                rejectedValue,
                // This is kind of a hack. At the start of the validation flow, Grails clears all errors where
                // bindingFailure == false. They do this so that if you validate an invalid field, then change the
                // field to a valid value and re-validate, it won't return the previous validation error. Because our
                // Validators get called in beforeValidate, we have to set bindingFailure to true, otherwise we'd lose
                // the validation result. Grails intends bindingFailure to be false for validation errors, but we don't
                // have a choice here.
                true,
                [errorCode] as String[],
                errorArgs,
                errorCode)  // If we don't resolve the errorCode, display the code itself. This helps us catch typos.
    }

    /**
     * Mark the object itself as invalid. For use when not validating a specific field.
     *
     * @param field The name of the field that failed validation
     * @param errorCode The l10n message key containing the message to display when rendering the errors of the entity.
     * @param errorArgs Values to use for any args contained within the errorCode message
     */
    ObjectError rejectObject(Errors errors, String field, String errorCode, Object[] errorArgs=null) {
        errors.rejectValue(field, errorCode, errorArgs, null)
        return new ObjectError(
                "Object",  // objectName will be set automatically when adding the errors to the object being validated.
                [errorCode] as String[],
                errorArgs,
                errorCode)  // If we don't resolve the errorCode, display the code itself. This helps us catch typos.
    }
}
