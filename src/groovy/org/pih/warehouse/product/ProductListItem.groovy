package org.pih.warehouse.product

class ProductListItem {

    Product product

    ProductListItem(Product product) {
        this.product = product
    }

    Map toJson() {
        [
                id                  : product.id,
                productCode         : product.productCode,
                name                : product.name,
                description         : product.description,
                category            : product.category?.name,
                productFamily       : product.productFamily,
                unitOfMeasure       : product.unitOfMeasure,
                pricePerUnit        : product.pricePerUnit,
                dateCreated         : product.dateCreated,
                lastUpdated         : product.lastUpdated,
                updatedBy           : product.updatedBy?.name,
                color               : product.color,
                handlingIcons       : product.handlingIcons,
                lotAndExpiryControl : product.lotAndExpiryControl,
                active              : product.active,
                displayName         : product.displayName,
                glAccount           : product.glAccount,
                productCatalogs     : product.productCatalogs,
        ]
    }
}
