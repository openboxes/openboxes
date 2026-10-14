package org.pih.warehouse.core

import org.springframework.stereotype.Component

import org.pih.warehouse.core.serialization.SerializationMapper

@Component
class DataGroupMapper implements SerializationMapper<DataGroup> {

    @Override
    Map<String, Object> serialize(DataGroup source) {
        // We don't want to add another nested key in the JSON for the group so simply render the group map itself.
        return source.group
    }
}
