package org.pih.warehouse

import grails.util.GrailsNameUtils
import grails.util.Holders
import org.apache.commons.lang.ClassUtils

class CommandUtils {
    static String getTypePropertyName(Class type) {
        String shortTypeName = ClassUtils.getShortClassName(type)
        return shortTypeName.substring(0, 1).toLowerCase(Locale.ENGLISH) + shortTypeName.substring(1)
    }

    static List<Map> getAvailableProperties(String entityName) {
        Map properties =
                Holders.grailsApplication.mappingContext.getPersistentEntity(entityName)?.propertiesByName
        return properties.collect { name, property  ->
           [
                name: name,
                naturalName: GrailsNameUtils.getNaturalName(name),
                typePropertyName: getTypePropertyName(property?.type)
            ]
        }
    }
}
