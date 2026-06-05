package org.pih.warehouse.core.validation

import org.grails.datastore.gorm.GormValidateable

/**
 * Marks a domain entity as able to be validated by our custom validation logic.
 *
 * Hooks into Grails validation by wrapping GormValidateable.validate() methods with additional validation.
 *
 * We need to perform some AST Transformations on this trait in order for it to play nice with Grails.
 * See {@link DomainValidatableASTTransformation} for further details.
 *
 * @param <V> Optional. The {@link Validator} component containing additional validation to perform on this object.
 */
trait DomainValidatable<V extends Validator> implements Validatable<V>, GormValidateable {

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
