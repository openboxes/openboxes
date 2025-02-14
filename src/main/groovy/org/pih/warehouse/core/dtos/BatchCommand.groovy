package org.pih.warehouse.core.dtos

import grails.validation.Validateable
import grails.validation.ValidationException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors

/**
 *
 */
abstract class BatchCommand<BatchedCommand extends Validateable> implements Validateable {

    List<BatchedCommand> requests

    /**
     *
     */
    abstract String getValidationErrorMessage()

    /**
     *
     */
    void validateBatch() {
        if (!hasErrors()) {
            return
        }

        // We need to build the errors manually so that we can group them all together.
        Errors errorsToReturn = new BeanPropertyBindingResult(this, "requests")

        // Add any validation errors from the base batch command.
        errors.allErrors.each { error -> errorsToReturn.addError(error) }

        // Add any validation errors from each command being batched.
        requests.each { Validateable batchedCommand ->
            batchedCommand.errors.allErrors.each { error -> errorsToReturn.addError(error) }
        }

        throw new ValidationException(validationErrorMessage, errorsToReturn)
    }

    static constraints = {
        // Elements of a list need to be validated manually. Otherwise the base batch command will ignore their errors.
        requests(validator: { List<BatchedCommand> batch ->
            batch.each { BatchedCommand command -> command.validate() }
            if (batch.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
