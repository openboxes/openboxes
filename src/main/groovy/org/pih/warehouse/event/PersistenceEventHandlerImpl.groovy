package org.pih.warehouse.event

import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.hibernate.event.spi.PostInsertEvent
import org.hibernate.event.spi.PostInsertEventListener
import org.hibernate.event.spi.PreDeleteEvent
import org.hibernate.event.spi.PreDeleteEventListener
import org.hibernate.event.spi.PreUpdateEvent
import org.hibernate.event.spi.PreUpdateEventListener
import org.hibernate.persister.entity.EntityPersister

@Slf4j
class PersistenceEventHandlerImpl implements PreUpdateEventListener, PostInsertEventListener, PreDeleteEventListener {

    def eventService

    public static final String INSERT_EVENT = "INSERT"
    public static final String DELETE_EVENT = "DELETE"
    public static final String PRE_UPDATE_EVENT = "PRE_UPDATE"

    public static Map<String, Map> publishEventForEntitiesAndFields = Holders.config.openboxes.publishEventForEntitiesAndFields as Map<String, Map>

    @Override
    void onPostInsert(PostInsertEvent event) {

    }

    @Override
    boolean requiresPostCommitHanding(EntityPersister persister) {
        return false
    }

    @Override
    boolean onPreDelete(PreDeleteEvent event) {
        return false
    }

    @Override
    boolean onPreUpdate(PreUpdateEvent event) {
        def entity = event.entity
        String entityName = getClassName(entity.class)
        if(isEntityEnabledForPersistenceEvent(entityName)){
            handleUpdate(entity, PRE_UPDATE_EVENT, entityName)
        }
        return false
    }

    static Set<String> entitiesEnabledForPublishEvent(){
        Set<String> entitiesEnabled = publishEventForEntitiesAndFields.keySet()
        return entitiesEnabled
    }

    static List<String> fieldsEnabledForEventTypeAndEntity(String eventType, String entityName){
        Map eventsEnabled = publishEventForEntitiesAndFields[entityName] as Map
        List<String> fields = eventsEnabled?.get(eventType)?.fields as List<String>
        return fields
    }

    static String getAppEventName(String eventType, String entityName){
        Map eventsEnabled = publishEventForEntitiesAndFields[entityName] as Map
        String eventName = eventsEnabled?.get(eventType)?.eventName
        return eventName

    }

    static boolean isEntityEnabledForPersistenceEvent(String entityName){
        return entitiesEnabledForPublishEvent()?.contains(entityName)
    }

    /**
     * @entityName Entity class name like Shipment, Product
     * @eventType Type of event like PRE_UPDATE, INSERT, Most of cases eventType here would be PRE_UPDATE
     * This method is specifically to handle update request.
     * Here we are checking that configured Entity fields are updated or not. we updated than needs to call Webhooks call or other events.
     * For example Shipment status changed from Shipped to Received. then system will fire an event to process business logic for this field.
     * */
    protected void handleUpdate(def entity, String eventType, String entityName) {

        // By default, we don't log verbose properties
        Map<String, Object> newMap = [:]
        Map<String, Object> oldMap = [:]

        List<String> dirtyFields = entity.getDirtyPropertyNames()
        List<String> fieldsToCheck = fieldsEnabledForEventTypeAndEntity(eventType, entityName)
        if (dirtyFields && fieldsToCheck) {
            dirtyFields?.each { String fieldName ->
                if(fieldsToCheck.contains(fieldName) || fieldsToCheck.contains("ALL")) {
                    oldMap[fieldName] = entity.getPersistentValue(fieldName)
                    newMap[fieldName] = entity[fieldName]
                }
            }
        }
        // Adding logic fire event for these updated field.
        eventService.event(getAppEventName(eventType, entityName), entity, oldMap, newMap)
    }

    static String getClassName(def className){
        GrailsNameUtils.getShortName(className)
    }

    /**
     * Helper method to make a map of the current property values
     *
     * @param propertyNames
     * @param domain
     * @return
     */
    static Map<String, Object> makeMap(Collection<String> propertyNames, def domain) {
        propertyNames.collectEntries { [it, domain.metaClass.getProperty(domain, it)] }
    }

/**
 * Get the original or persistent or original value for the given domain.property. This method includes
 * some special case handling for hasMany properties, which don't follow normal rules.
 *
 * By default, getPersistentValue() is used to obtain the value.
 * If the value is always NULL, you can set AuditLogConfig.usePersistentDirtyPropertyValue = false
 * In this case, DirtyCheckable.html#getOriginalValue() is used.
 *
 * @see GormEntity#getPersistentValue(java.lang.String)
 * @see org.grails.datastore.mapping.dirty.checking.DirtyCheckable#getOriginalValue(java.lang.String)
 */
    static Object getOriginalValue(def domain, String propertyName) {
        PersistentEntity entity = getPersistentEntity(domain)
        PersistentProperty property = entity.getPropertyByName(propertyName)
        ((GormEntity) domain).getOriginalValue(propertyName)
    }

    /**
     * Return the grails domain class for the given domain object.
     *
     * @param domain the domain instance
     */
    static PersistentEntity getPersistentEntity(domain) {
        Holders.grailsApplication.mappingContext.getPersistentEntity(domain.getClass().name)
    }

}
