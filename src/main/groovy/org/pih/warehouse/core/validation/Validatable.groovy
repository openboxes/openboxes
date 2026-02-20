package org.pih.warehouse.core.validation

import org.springframework.core.GenericTypeResolver

import org.pih.warehouse.core.AppUtil

/**
 * Marks an object as able to be validated by a Validator class.
 *
 * The validation performed by these validators works in tandem with any constraints block defined in the validatable
 * object as long as it is a Grails Domain (which implement org.grails.datastore.gorm.GormValidateable) or a non-domain
 * class that implements grails.validation.Validateable.
 */
trait Validatable<V extends Validator> {

    /**
     * A Grails GORM built-in for classes that implement Validateable or GormValidateable.
     * This logic will run before any validation in the constraints block of the validatable object.
     */
    def beforeValidate() {
        validator.validate(this)
    }

    V getValidator() {
        // Determines (statically but at runtime) the class type of the validator and uses that to fetch the bean.
        return AppUtil.getBean((Class<V>) GenericTypeResolver.resolveTypeArgument(getClass(), Validatable.class))
    }
}
