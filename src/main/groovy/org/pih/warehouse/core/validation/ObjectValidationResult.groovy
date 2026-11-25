package org.pih.warehouse.core.validation

import org.springframework.validation.ObjectError

/**
 * Represents the result of validating some object by collecting the errors that were raised during validation.
 *
 * It's important to note that this is a simple wrapper on a list of ObjectError. The only reason this class exists
 * is because it's more intuitive for our {@link Validator} implementations to return a list of errors than it is for
 * them to need to remember to set the errors on the {@link Validatable} object themselves. As such, we rely on
 * the {@link Validator} implementation to take this result and apply the errors to the ValidationErrors field of
 * the {@link Validatable} object being validated.
 */
class ObjectValidationResult {

    private static final ObjectValidationResult VALID = new ObjectValidationResult(Collections.emptyList())

    List<ObjectError> errors = []

    /**
     * @param errors Any number of {@link ObjectError}s or collections of them. Nulls are ignored.
     */
    ObjectValidationResult(Object... errors) {
        List flattenedErrors = errors?.flatten() ?: []
        Object invalidObject = flattenedErrors.find { it != null && !(it instanceof ObjectError) }
        if (invalidObject != null) {
            throw new IllegalArgumentException(
                    "Expected ObjectError or Collection<ObjectError> but got ${invalidObject.getClass().name}")
        }
        addErrors(flattenedErrors as Collection<ObjectError>)
    }

    void addError(ObjectError error) {
        if (error == null) {
            return
        }
        this.errors.add(error)
    }

    void addErrors(Collection<ObjectError> errors) {
        if (!errors) {
            return
        }
        // For convenience, we allow for nulls in the given list of errors, which we treat as not being an error.
        Collection<ObjectError> nonNullErrors = errors.findAll {it != null}
        this.errors.addAll(nonNullErrors)
    }

    void addErrors(ObjectError... errors) {
        addErrors(errors?.toList())
    }

    boolean isValid() {
        return errors.isEmpty()
    }

    static ObjectValidationResult valid() {
        return VALID
    }
}
