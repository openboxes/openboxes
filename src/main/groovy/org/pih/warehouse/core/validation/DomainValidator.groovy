package org.pih.warehouse.core.validation

import org.grails.datastore.mapping.validation.ValidationErrors
import org.grails.datastore.gorm.GormValidateable

/**
 * A validator for a Grails Domain class.
 */
trait DomainValidator<T extends GormValidateable> implements Validator<T> {

    ValidationErrors getErrors(T toValidate) {
        return toValidate.errors as ValidationErrors
    }
}
