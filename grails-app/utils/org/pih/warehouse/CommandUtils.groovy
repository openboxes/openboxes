package org.pih.warehouse

import grails.util.GrailsNameUtils
import grails.util.Holders
import org.apache.commons.lang.ClassUtils
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

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

    /**
     * Util to build and return localized errors grouped by field in the format:
     * { fieldName1: ["some error"], fieldName2: ["some error 2"] }
     * @param errors
     * @return
     */
    static Map<String, List<String>> buildErrorsGroupedByField(Errors errors) {
        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        errors.allErrors
                .groupBy { FieldError error -> error.field }
                .collectEntries { [it.key, it.value.collect { error -> g.message(error: error)}] } as Map<String, List<String>>
    }
}
