package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.product.Product

class OutboundDemand {
    Product product
    DeliveryTypeCode deliveryTypeCode
    DemandTypeCode demandTypeCode
    Integer quantity
}