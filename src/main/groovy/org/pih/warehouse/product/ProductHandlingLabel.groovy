package org.pih.warehouse.product

/**
 * A handling flag of a product.
 */
enum ProductHandlingLabel {

    COLD_CHAIN("product.coldChain.label"),
    CONTROLLED_SUBSTANCE("product.controlledSubstance.label"),
    HAZARDOUS_MATERIAL("product.hazardousMaterial.label"),
    RECONDITIONED("product.reconditioned.label")

    /** Code of the message naming */
    final String messageCode

    ProductHandlingLabel(String messageCode) {
        this.messageCode = messageCode
    }

    /**
     * @return the handling labels of the given product
     */
    static List<ProductHandlingLabel> of(Product product) {
        if (!product) {
            return []
        }

        List<ProductHandlingLabel> labels = []
        if (product.coldChain) {
            labels << COLD_CHAIN
        }
        if (product.controlledSubstance) {
            labels << CONTROLLED_SUBSTANCE
        }
        if (product.hazardousMaterial) {
            labels << HAZARDOUS_MATERIAL
        }
        if (product.reconditioned) {
            labels << RECONDITIONED
        }
        return labels
    }
}
