package org.pih.warehouse.core.dtos

import grails.validation.Validateable
import grails.validation.ValidationException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors

class BatchCommandUtils {

    private static String DEFAULT_BATCH_FIELD_NAME = 'commands'

    /**
     * Validates a batch command, throwing an error if the root level batch object or any of the commands
     * being batched fail validation.
     *
     * @param batch the root level command to validate
     * @param batchPropertyName the name of the field containing the list of batched commands
     */
    static void validateBatch(Validateable batch, String batchPropertyName=DEFAULT_BATCH_FIELD_NAME) {
        if (!batch.hasErrors()) {
            return
        }

        // We need to build the errors manually so that we can group them all together.
        Errors errors = new BeanPropertyBindingResult(batch, batchPropertyName)

        // Add any validation errors from the base batch command.
        batch.errors.allErrors.each { error -> errors.addError(error) }

        // Add any validation errors from each command being batched.
        batch.properties.get(batchPropertyName).each { Validateable batchedCommand ->
            batchedCommand.errors.allErrors.each { error -> errors.addError(error) }
        }

        throw new ValidationException("Invalid batch request", errors)
    }
}
