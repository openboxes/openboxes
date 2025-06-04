package org.pih.warehouse.core.dtos

import grails.validation.Validateable

/**
 * A general purpose command for performing bulk operations.
 *
 * We define a "bulk" operation as a method of grouping together a list of operations that all perform the same type
 * of action (ex: creating an entity). A bulk operation allows us to perform all of these actions together in a single
 * API call, which is useful for performance and for transaction rollbacks in the case of errors.
 */
class BulkCommand<T extends Validateable> implements Validateable {

    /**
     * The name of the field used to hold the collection of bulk commands.
     */
    static final String BULK_FIELD_NAME = 'commands'

    /**
     * The bulk command objects to be operated on.
     */
    Collection<T> commands = []

    static constraints = {
        commands(validator: { Collection<T> commands ->
            // The individual commands are not automatically validated so we have to do it manually.
            commands.each { it.validate() }
            if (commands.any { it.hasErrors() }) {
                return false
            }
        })
    }
}
