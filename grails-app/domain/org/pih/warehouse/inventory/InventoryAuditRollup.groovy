package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class InventoryAuditRollup implements Serializable {

    Product product
    Location facility
    Transaction transaction
    String transactionNumber
    Date transactionDate
    String facilityName
    String productCode
    BigDecimal quantityAdjusted
    VarianceTypeCode varianceTypeCode
    String abcClass
    BigDecimal pricePerUnit

    static mapping = {
        version false
    }

    static constraints = {
        id composite: ['facility', 'product', 'transaction']
    }

    static transients = []

}
