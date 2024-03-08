package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class ProductSupplierAttributeService {

    ProductSupplierAttributeUpdateResponse updateAttributes(ProductSupplierAttributeBatchCommand command) {
        ProductSupplierAttributeUpdateResponse response = new ProductSupplierAttributeUpdateResponse()
        command.productAttributes.each { ProductSupplierAttributeCommand productSupplierAttributeCommand ->
            List<ProductAttribute> existingAttributes = productSupplierAttributeCommand.productSupplier?.attributes
            // Search for the existing product attribute - if we find one, then we would want to edit/remove an existing one
            ProductAttribute productAttribute = existingAttributes.find { it.attribute?.id == productSupplierAttributeCommand.attribute?.id }

            // If value is specified and we didn't find an existing product attribute - create a brand new, otherwise edit an existing one
            if (productSupplierAttributeCommand.value) {
                if (!productAttribute) {
                    productAttribute = new ProductAttribute(
                            attribute: productSupplierAttributeCommand.attribute,
                            value: productSupplierAttributeCommand.value,
                            productSupplier: productSupplierAttributeCommand.productSupplier
                    )
                    productSupplierAttributeCommand.productSupplier.product.addToAttributes(productAttribute)
                    productAttribute.save()
                    response.createdAttributes.add(productAttribute)
                    return
                }
                // Update an existing product attribute
                productAttribute.value = productSupplierAttributeCommand.value
                productAttribute.productSupplier = productSupplierAttributeCommand.productSupplier
                response.updatedAttributes.add(productAttribute)
                return
            }
            // If value is not specified and the attribute is active, remove the product attribute
            if (productAttribute?.attribute?.active) {
                productSupplierAttributeCommand.productSupplier?.product?.removeFromAttributes(productAttribute)
                productAttribute.delete()
                response.deletedAttributes.add(productAttribute.id)
            }
        }
        return response
    }
}
