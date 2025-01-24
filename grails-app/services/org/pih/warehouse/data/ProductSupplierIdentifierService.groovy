package org.pih.warehouse.data

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.IdentifierGeneratorContext
import org.pih.warehouse.product.ProductSupplier

@Transactional
class ProductSupplierIdentifierService extends IdentifierService<ProductSupplier> {

    @Override
    String getIdentifierName() {
        return "productSupplier"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return ProductSupplier.countByCode(id)
    }

    /**
     * Generates a new product supplier code.
     */
    String generate(ProductSupplier productSupplier, String productCode, String organizationCode) {
        return generate(productSupplier, IdentifierGeneratorContext.builder()
                .customProperties([
                        'productCode': productCode,
                        'organizationCode': organizationCode,
                ])
                .build())
    }
}
