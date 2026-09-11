package org.pih.warehouse.exporter

import java.lang.reflect.Field

import org.springframework.stereotype.Component

import org.pih.warehouse.core.http.HttpSerializable
import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Takes in a List of strongly typed Exportable objects and "unbinds" them to a List of Map of bulk data.
 */
@Component
class BulkDataUnbinder {

    final private MapperComponentResolver mapperComponentResolver

    BulkDataUnbinder(final MapperComponentResolver mapperComponentResolver) {
        this.mapperComponentResolver = mapperComponentResolver
    }

    /**
     * "Unbinds" the given collection of objects, converting them from a strongly-typed class to a simple List of Map
     * that is keyed on field name.
     *
     * Note that we do not format the fields here. Each BulkDataWriter formats fields differently (Excel can handle
     * number, date and booleans whereas CSV needs strings for everything) so we leave it to the writers to format
     * the fields themselves.
     *
     * @param objectsToUnbind The collection of objects to convert to a List of Map
     * @return The result of unbinding the bulk data.
     */
    List<Map<String, Object>> unbindData(Collection<Object> objectsToUnbind) {
        List<Map<String, Object>> unboundObjects = []

        for (objectToUnbind in objectsToUnbind) {
            unboundObjects.add(unbindObject(objectToUnbind))
        }
        return unboundObjects
    }

    private Map<String, Object> unbindObject(Object objectToUnbind) {
        // If the object explicitly defines how it should be serialized, unbind via that approach.
        ResponseMapper responseMapper = mapperComponentResolver.getResponseMapper(objectToUnbind.class)
        if (responseMapper) {
            return responseMapper.asExportRow(objectToUnbind)
        }

        if (objectToUnbind instanceof HttpSerializable) {
            return objectToUnbind.asExportRow()
        }

        // Otherwise, simply collect all fields declared on the object as a Map.
        return getDeclaredFields(objectToUnbind.class).collectEntries { [it.name, objectToUnbind."${it.name}"]
        }
    }

    /**
     * Collects the declared fields of the given class, including any fields declared in superclasses (except Object).
     */
    private List<Field> getDeclaredFields(Class clazz) {
        List<Field> fields = []
        for (Class current = clazz; current != Object; current = current.superclass) {
            fields.addAll(current.declaredFields)
        }
        // We exclude synthetic fields since those are injected by the compiler (e.g. Groovy metaClasses)
        return fields.findAll { !it.synthetic }
    }
}
