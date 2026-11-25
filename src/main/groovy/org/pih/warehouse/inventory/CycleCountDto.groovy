package org.pih.warehouse.inventory

import com.fasterxml.jackson.annotation.JsonProperty

import org.pih.warehouse.core.Person

import java.time.LocalDate

import org.pih.warehouse.core.dtos.DomainDto
import org.pih.warehouse.core.mapper.SmartMapper

class CycleCountDto implements DomainDto<CycleCount> {

    String status

    String requestId

    List<CycleCountItemDto> cycleCountItems

    CountDto initialCount

    CountDto verificationCount

    static CycleCountDto toDto(CycleCount cycleCount) {
        CycleCountRequest cycleCountRequest = cycleCount?.cycleCountRequest

        return new CycleCountDto(
                id: cycleCount.id,
                requestId: cycleCountRequest?.id,
                initialCount: new CountDto(
                        assignee: cycleCountRequest?.countAssignee,
                        deadline: cycleCountRequest?.countDeadline
                ),
                verificationCount: new CountDto(
                        assignee: cycleCountRequest?.recountAssignee,
                        deadline: cycleCountRequest?.recountDeadline
                ),
                status: cycleCount.status.toString(),
                cycleCountItems: SmartMapper.mapCollectionStatic(
                        sortItemsInEntryOrder(cycleCount.cycleCountItems), CycleCountItemDto)
        )
    }

    /**
     * Sorts the items in the order they were created, so custom items stay in the order the user entered them.
     * Count items go first, because the recount is merged with the count on the frontend
     * (mergeCycleCountItems in useResolveStep.js) and takes its order from it.
     * Within each count, custom items go to the bottom. Then the items are sorted by dateCreated,
     * and the id decides the order of the items saved in the same second.
     */
    private static List<CycleCountItem> sortItemsInEntryOrder(Collection<CycleCountItem> cycleCountItems) {
        return cycleCountItems.toSorted { CycleCountItem a, CycleCountItem b ->
            a.countIndex <=> b.countIndex
                    ?: a.custom <=> b.custom
                            ?: a.dateCreated <=> b.dateCreated
                                // Hibernate generated ids use UUIDHexGenerator.
                                // Newer ids are always > than previous ones
                                ?: a.id <=> b.id
        }
    }

    /**
     * @return The largest count index of all the cycle count items. Helps determine what count we're on.
     */
    @JsonProperty("maxCountIndex")
    Integer getMaxCountIndex() {
        return cycleCountItems.max{ it.countIndex }?.countIndex
    }
}

class CountDto {
    Person assignee
    LocalDate deadline
}
