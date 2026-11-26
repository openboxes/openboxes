package org.pih.warehouse.exporter

import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.reflection.PropertyFields
import org.pih.warehouse.core.serialization.SerializationMapper
import org.pih.warehouse.core.serialization.TabularSerializable

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
        // Write null rows as empty rows so that we preserve the number of rows given.
        if (objectToUnbind == null) {
            return [:]
        }

        // A Map is already in the "unbound" format so there's nothing to do.
        if (objectToUnbind instanceof Map) {
            return objectToUnbind as Map<String, Object>
        }

        // If a mapper exists that defines how the object should be serialized for export, use it to unbind the object.
        if (objectToUnbind instanceof TabularSerializable) {
            SerializationMapper serializationMapper = mapperComponentResolver.getSerializationMapper(objectToUnbind.class)
            if (serializationMapper) {
                try {
                    return serializationMapper.serializeTabular(objectToUnbind)
                }
                catch (UnsupportedOperationException ignore) {
                    // The mapper exists but does not actually specify how to format as tabular data.
                }
            }
        }

        // Otherwise, simply collect all of the object's properties (including inherited ones) as a Map.
        return PropertyFields.getPropertyFields(objectToUnbind.class).collectEntries {
            [it.name, objectToUnbind."${it.name}"]
        }
    }
}
