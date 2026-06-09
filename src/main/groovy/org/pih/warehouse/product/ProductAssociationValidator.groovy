package org.pih.warehouse.product

import org.pih.warehouse.core.validation.DomainValidator
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

@Component
class ProductAssociationValidator implements DomainValidator<ProductAssociation> {

    @Override
    ObjectValidationResult doValidate(ProductAssociation productAssociation) {
        return new ObjectValidationResult(
                validateDuplicates(productAssociation),
        )
    }

    private ObjectError validateSelfAssociation(ProductAssociation productAssociation) {
        if (productAssociation.product?.id == productAssociation.associatedProduct?.id) {
            return rejectField("associatedProduct", productAssociation.associatedProduct, "productAssociation.associatedProduct.invalid.selfAssociation")
        }
        return null
    }

    private ObjectError validateDuplicateAssociation(ProductAssociation productAssociation) {
        int amountOfExistingAssociations = ProductAssociation.createCriteria().count {
            and {
                eq("product", productAssociation.product)
                eq("associatedProduct", productAssociation.associatedProduct)
                eq("code", productAssociation.code)
                // Exclude current association from the search if it already exists in the database
                // (i.e. we are updating existing association, not creating a new one)
                if (productAssociation.id) {
                    ne("id", productAssociation.id)
                }
            }
        }
        if (amountOfExistingAssociations > 0) {
            return rejectField("associatedProduct", productAssociation.associatedProduct, "productAssociation.associatedProduct.invalid.duplicateAssociation")
        }
        return null
    }

    private ObjectError validateDuplicateMutualAssociation(ProductAssociation productAssociation) {
        if (!productAssociation.mutualAssociation) {
            return null
        }
        int amountOfExistingAssociations = ProductAssociation.createCriteria().count {
            or {
                and {
                    eq("product", productAssociation.product)
                    eq("associatedProduct", productAssociation.associatedProduct)
                    eq("code", productAssociation.code)
                    // Exclude product association and its mutual association from the search not to treat it as duplicate of itself
                    if (productAssociation.id || productAssociation.mutualAssociation?.id) {
                        not {
                            inList("id", [productAssociation.id, productAssociation.mutualAssociation?.id].findAll { it })
                        }
                    }
                }
                and {
                    eq("product", productAssociation.associatedProduct)
                    eq("associatedProduct", productAssociation.product)
                    eq("code", productAssociation.code)
                    // Exclude product association and its mutual association from the search not to treat it as duplicate of itself
                    if (productAssociation.id || productAssociation.mutualAssociation?.id) {
                        not {
                            inList("id", [productAssociation.id, productAssociation.mutualAssociation?.id].findAll { it })
                        }
                    }
                }
            }
        }
        if (amountOfExistingAssociations > 0) {
            return rejectField("associatedProduct", productAssociation.associatedProduct, "productAssociation.associatedProduct.invalid.duplicateMutualAssociation")
        }
        return null
    }

    List<ObjectError> validateDuplicates(ProductAssociation productAssociation) {
        return [
                validateSelfAssociation(productAssociation),
                validateDuplicateAssociation(productAssociation),
                validateDuplicateMutualAssociation(productAssociation),
        ]
    }
}
