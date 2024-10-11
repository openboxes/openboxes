package org.pih.warehouse.data

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.IdentifierGeneratorParams
import org.pih.warehouse.product.ProductSupplier

class ProductSupplierIdentifierService extends IdentifierService {

    @Override
    String getEntityKey() {
        return "productSupplier"
    }

    @Override
    protected Integer countDuplicates(String code) {
        return ProductSupplier.countByCode(code)
    }

    /**
     * Generates a new product supplier code.
     */
    String generate(String productCode, String organizationCode) {
        return generate(IdentifierGeneratorParams.builder()
                .customKeys([
                        'productCode': productCode,
                        'organizationCode': organizationCode,
                ])
                .build())
    }
}
