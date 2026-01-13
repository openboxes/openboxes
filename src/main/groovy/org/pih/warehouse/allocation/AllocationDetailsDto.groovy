package org.pih.warehouse.allocation

class AllocationDetailsDto {
    String requisitionItemId
    Integer quantityRequired
    Integer quantityAllocated
    Integer quantityRemaining
    AllocationStatus status
    List<AllocationDto> allocations
}
