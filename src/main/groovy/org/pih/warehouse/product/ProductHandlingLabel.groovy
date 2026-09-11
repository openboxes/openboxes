package org.pih.warehouse.product

/**
 * Represents the different types of handling labels that a product can be associated with.
 *
 * Since these labels have logic associated with them baked into the code (via fields on the Product), we keep them
 * as an enum. Eventually these should be migrated to be a domain entity so that they can be fully customizable.
 */
enum ProductHandlingLabel {

    COLD_CHAIN("product.coldChain.label"),
    CONTROLLED_SUBSTANCE("product.controlledSubstance.label"),
    HAZARDOUS_MATERIAL("product.hazardousMaterial.label"),
    RECONDITIONED("product.reconditioned.label")

    /**
     * Localization code representing the display text for the label.
     * */
    final String labelTextCode

    ProductHandlingLabel(String labelTextCode) {
        this.labelTextCode = labelTextCode
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
