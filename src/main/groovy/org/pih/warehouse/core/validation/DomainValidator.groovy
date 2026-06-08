package org.pih.warehouse.core.validation

import org.grails.datastore.gorm.GormValidateable
import org.springframework.validation.Errors

/**
 * A validator for a Grails Domain class.
 */
trait DomainValidator<T extends GormValidateable> implements Validator<T> {

    @Override
    Errors getErrors(T toValidate) {
        return toValidate.errors
    }
}
