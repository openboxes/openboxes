package org.pih.warehouse.core.validation

import org.grails.datastore.gorm.GormValidateable

/**
 * Marks a domain entity as able to be validated by our custom validation flow.
 * This is achieved by hooking our custom validation into the entity's validate() method.
 *
 * @param <V> Optional. The {@link DomainValidator} component containing additional validation to perform on this
 *            object. If not provided, will only validate via Grails constraints and Javax annotations.
 */
trait DomainValidatable<V extends DomainValidator> implements Validatable<V>, GormValidateable {

    @Override
    boolean validate() {
        return performAdditionalValidation(super.validate())
    }

    @Override
    boolean validate(List fields) {
        return performAdditionalValidation(super.validate(fields), fields)
    }

    @Override
    boolean validate(Map arguments) {
        return performAdditionalValidation(super.validate(arguments))
    }
}
