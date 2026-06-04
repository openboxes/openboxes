package org.pih.warehouse.core.validation

import grails.util.Holders
import grails.validation.ValidationException
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import javax.validation.ConstraintViolation
import javax.validation.Valid
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.core.GenericTypeResolver
import org.springframework.validation.Errors
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.AppUtil

/**
 * Marks a class as able to be validated by a Validator class.
 *
 * There are three supported validation methods:
 * 1) Via javax.validation.constraints.* annotations: for simple validations
 * 2) Via a custom *Validator component: for complex validations that depend on other components
 * 3) Via Grails' static "constraints" block: Uses Grails validation. Prefer the other options when possible.
 *
 * When X.validate() is called, constraints defined in any/all of the above approaches will be triggered. As such,
 * you can rely on more than one of the above validation solutions at the same time.
 *
 * This also allows us to use the @Valid annotation on controller action method params. Any controller action
 * method param that is annotated with @Valid will automatically throw an exception if any of its fields are invalid.
 *
 * For example: def someAction(@Valid XCommand requestBody) { ... }
 */
trait Validatable<V extends Validator> {

    /**
     * @return The Errors object. This will be populated with any validation errors that occur.
     */
    abstract Errors getErrors()

    /**
     * Enhances Grails validation by providing additional methods for validation.
     *
     * @param grailsValidationResult True if the object is considered valid according to the checks in Grails'
     *                               constraints block, false otherwise.
     * @param fieldsToValidate The subset of fields to validate. If null, will validate all fields.
     * @return True if the object is valid, false otherwise.
     */
    boolean performAdditionalValidation(boolean grailsValidationResult, List fieldsToValidate=null) {
        boolean javaxValid = performJavaxValidation(fieldsToValidate as Set<String>)

        V validator = validator()
        boolean validatorValid = validator ? validator.validate(this) : true

        // The object is only considered valid if all of the validation steps return valid
        boolean validOverall = grailsValidationResult && javaxValid && validatorValid

        // Automatically throw an exception for invalid objects that are annotated with @Valid within
        // a controller action. This is done to mirror Spring's behaviour.
        if (!validOverall && errorOnInvalid()) {
            // Spring would throw a org.springframework.web.bind.MethodArgumentNotValidException in this scenario
            // but it is much easier to throw a simple ValidationException here for now.
            throw new ValidationException("Validation failed for ${this.class.simpleName}", errors)
        }

        return validOverall
    }

    /**
     * Validates against any javax.validation.constraints.* annotations on the object.
     *
     * @return True if the object is valid, false otherwise
     */
    private boolean performJavaxValidation(Set<String> fieldsToValidate) {
        // Javax validations are quick, so the simplest thing to do is always perform them all, then
        // filter down to only the ones we need if we're only validating a subset of the fields.
        Set<ConstraintViolation> violations = javaxValidator().validate(this)
        if (fieldsToValidate != null) {
            violations = violations.findAll { it.propertyPath.toString() in fieldsToValidate }
        }

        // Add any validation failures to the base error object so that they will be detected by Grails.
        // Javax validations are always performed at the field level. There are no object level validations.
        for (violation in violations) {
            // Custom, localizable Javax message codes are expected to be wrapped in "{}", but our message localizer
            // doesn't work that way so we need to strip them out. Example: @NotNull(message="{default.notNull.label}")
            String messageTemplate = violation.messageTemplate
            String code = messageTemplate.startsWith("{") ? messageTemplate.replaceAll(/[{}]/, "") : null

            errors.rejectValue(
                    violation.propertyPath.toString(),  // Field name
                    code,                               // localization code
                    null,                               // We assume annotation-based error messages have no args
                    violation.message                   // Fallback default message for the annotation
            )
        }

        return violations.empty
    }

    private javax.validation.Validator javaxValidator() {
        // There are two javax Validator beans in a Spring application (we use number 2):
        // 1) mvcValidator: for use in Spring MVC data binding during web request processing
        // 2) defaultValidator: for use in Hibernate validation (JSR-303 Bean Validation using constraint annotations)
        return AppUtil.getBean(javax.validation.Validator, "defaultValidator")
    }

    /**
     * @return The Validator associated with this validatable object.
     */
    private V validator() {
        // Determines (statically but at runtime) the class type of the validator and uses that to fetch the bean.
        Class validatorClass = GenericTypeResolver.resolveTypeArgument(getClass(), Validatable.class)
        // Don't bother resolving the no-op validator since we know it does nothing. Having a null validator
        // is a valid use case (for example, when you want to use Javax annotations and no custom validator).
        return (!validatorClass || validatorClass == NoOpValidator) ? null : AppUtil.getBean((Class<V>) validatorClass)
    }

    /**
     * @return true if we should throw an exception if the validatable object is invalid.
     */
    private boolean errorOnInvalid() {
        try {
            return isAnnotatedWithValid()
        } catch (Exception ignored) {
            return false
        }
    }

    /**
     * Returns true if the object being validated is a controller action parameter that is annotated with
     * javax.validation @Valid. The goal is to mirror the same annotation behaviour that Spring uses.
     *
     * For example: def someAction(@Valid XCommand requestBody) { ... }
     */
    private boolean isAnnotatedWithValid() {
        GrailsWebRequest webRequest = RequestContextHolder.currentRequestAttributes() as GrailsWebRequest
        Class controllerClass = Holders.grailsApplication.getArtefactByLogicalPropertyName(
                "Controller", webRequest.controllerName)?.clazz
        if (!controllerClass) {
            return false
        }

        Method actionMethod = controllerClass.methods.find { it.name == webRequest.actionName }
        if (!actionMethod) {
            return false
        }

        return actionMethod.parameters.any { Parameter param ->
            param.type.isAssignableFrom(this.class) && param.isAnnotationPresent(Valid)
        }
    }
}
