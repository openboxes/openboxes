package org.pih.warehouse.outboundOrder

class AllocationDetailsDto {
    String requisitionItemId
    Integer quantityRequired
    Integer quantityAllocated
    Integer quantityRemaining
    AllocationStatus status
    List<AllocationDto> allocations
}
