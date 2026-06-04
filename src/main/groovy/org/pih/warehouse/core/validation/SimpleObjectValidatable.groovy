package org.pih.warehouse.core.validation

/**
 * Marks a non-entity object (such as a Command Object) as able to be validated.
 *
 * For use when we don't need a custom Validator component. Exists so that we can make the compiler happy.
 * If you do need a custom validator, implement ObjectValidatable directly.
 */
trait SimpleObjectValidatable implements ObjectValidatable<NoOpValidator> {
}
