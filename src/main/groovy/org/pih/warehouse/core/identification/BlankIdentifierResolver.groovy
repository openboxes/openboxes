package org.pih.warehouse.core.identification

import groovy.util.logging.Slf4j
import org.grails.datastore.gorm.GormEntity

/**
 * Handles generating identifiers for all instances of an entity that don't yet have one.
 *
 * Used by the AssignIdentifierJob as a backup retry mechanism to ensure all entities have a valid id.
 *
 * Classes that implement this one will almost certainly also extend from IdentifierService. The only reason this trait
 * is kept separate from the base service is to allow services to opt out of providing the functionality if they're
 * not called by the AssignIdentifierJob.
 */
@Slf4j
trait BlankIdentifierResolver<T extends GormEntity> {

    /**
     * @return the domain-specific name/keyword that is used in identifier properties.
     * Ex: The "product" of "openboxes.identifier.product.format"
     */
    abstract String getIdentifierName()

    /**
     * @return A list of all entities that do not have an identifier assigned.
     */
    abstract List<T> getAllUnassignedEntities()

    /**
     * Sets the identifier field on the given entity instance.
     */
    abstract void setIdentifierOnEntity(String id, T entity)

    /**
     * Generates a new identifier for the entity.
     *
     * This implementation will almost certainly come from the IdentifierService.
     */
    abstract String generate(T entity)

    /**
     * Fetches all entries from the database that have a null identifier, and tries to generate one for them.
     */
    void generateForAllUnassignedIdentifiers() {
        List<T> entities = getAllUnassignedEntities()
        String entityType = getIdentifierName()
        for (T entity : entities) {
            try {
                String identifier = generate(entity)
                if (identifier == null) {
                    log.error("Failed to generate a unique identifier for ${entityType}. Terminating job early to save CPU in case we're running out of ids.")
                    return
                }

                log.info("Assigning identifier ${identifier} to ${entityType} with id: ${entity.id}")
                setIdentifierOnEntity(identifier, entity)

                if (!entity.save()) {
                    log.error("Unable to assign identifier to ${entityType} with id: ${entity.id}. Error: ${entity.errors}")
                }
            } catch (Exception e) {
                log.error("Unable to assign identifier to ${entityType} with id: ${entity.id}. Error: ${e.message}")
            }
        }
    }
}
