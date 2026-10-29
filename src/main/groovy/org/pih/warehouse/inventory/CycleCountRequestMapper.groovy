package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.serialization.SerializationMapper

@Component
class CycleCountRequestMapper implements SerializationMapper<CycleCountRequest> {

    @Override
    Map<String, Object> serialize(CycleCountRequest source) {
        return [
                id: source.id,
                facility: source.facility?.id,
                product: source.product,
                status: source.status.toString(),
                requestType: source.requestType.toString(),
                initialCount: [
                        deadline: source.countDeadline,
                        assignee: source.countAssignee
                ],
                verificationCount: [
                        deadline: source.recountDeadline,
                        assignee: source.recountAssignee
                ],
                inventoryItemsCount: source.inventoryItemsCount,
                blindCount: source.blindCount,
                dateCreated: source.dateCreated,
                lastUpdated: source.lastUpdated,
        ]
    }
}
