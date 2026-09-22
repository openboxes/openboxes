package org.pih.warehouse.inventory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.EntityToDtoMapper
import org.pih.warehouse.core.mapper.MapperConfig
import org.pih.warehouse.location.BinLocationDto
import org.pih.warehouse.location.FacilityDto
import org.pih.warehouse.product.ProductSimpleDtoMapper

@Component
class CycleCountItemDtoMapper implements EntityToDtoMapper<CycleCountItem, CycleCountItemDto> {

    @Autowired
    ProductSimpleDtoMapper productSimpleDtoMapper

    @Override
    CycleCountItemDto doMap(CycleCountItem source, MapperConfig config) {
        return new CycleCountItemDto(
                id: source.id,
                facility: FacilityDto.from(source.facility),
                product: productSimpleDtoMapper.map(source.product),
                inventoryItem: source.inventoryItem,
                binLocation: BinLocationDto.from(source.location),
                countIndex: source.countIndex,
                status: source.status,
                quantityOnHand: source.quantityOnHand,
                quantityCounted: source.quantityCounted,
                quantityVariance: source.quantityVariance,
                discrepancyReasonCode: source.discrepancyReasonCode,
                dateCounted: source.dateCounted,
                dateCreated: source.dateCreated,
                comment: source.comment,
                custom: source.custom,
                assignee: source.assignee,
        )
    }
}
