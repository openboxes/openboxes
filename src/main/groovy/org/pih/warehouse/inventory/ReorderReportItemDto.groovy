package org.pih.warehouse.inventory

import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Product

class ReorderReportItemDto {
    String inventoryStatus
    Product product
    Set<Tag> tags
    InventoryLevel inventoryLevel
    Integer monthlyDemand
    Integer quantityAvailableToPromise
    Integer quantityToOrder
    BigDecimal unitCost
    BigDecimal expectedReorderCost
}
