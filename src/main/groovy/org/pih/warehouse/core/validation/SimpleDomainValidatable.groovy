package org.pih.warehouse.core.validation

/**
 * Marks a domain entity as able to be validated.
 *
 * For use when we don't need a custom Validator component. Exists so that we can make the compiler happy.
 * If you do need a custom validator, implement DomainValidatable directly.
 */
trait SimpleDomainValidatable implements DomainValidatable<NoOpValidator> {

}
