package org.pih.warehouse.data

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.product.ProductSupplier

class ProductSupplierIdentifierService extends IdentifierService {

    @Override
    String getPropertyKey() {
        return "productSupplier"
    }

    @Override
    protected Integer countDuplicates(String code) {
        return ProductSupplier.countByCode(code)
    }
}
