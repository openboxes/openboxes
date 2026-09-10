package org.pih.warehouse.product

/**
 * The display form of one of a product's handling flags (cold chain, controlled substance, hazardous
 * material, reconditioned): the Font Awesome icon representing it and the color to render it in,
 * ex: "fa-snowflake" in "#3bafda" for a cold chain product.
 */
class ProductHandlingIconDto {

    String icon
    String color

    static List<ProductHandlingIconDto> from(Product product) {
        if (!product) {
            return []
        }

        List<ProductHandlingIconDto> handlingIcons = []
        if (product.coldChain) {
            handlingIcons << new ProductHandlingIconDto(icon: "fa-snowflake", color: "#3bafda")
        }
        if (product.controlledSubstance) {
            handlingIcons << new ProductHandlingIconDto(icon: "fa-exclamation-circle", color: "#db1919")
        }
        if (product.hazardousMaterial) {
            handlingIcons << new ProductHandlingIconDto(icon: "fa-exclamation-triangle", color: "#ffa500")
        }
        if (product.reconditioned) {
            handlingIcons << new ProductHandlingIconDto(icon: "fa-prescription-bottle", color: "#a9a9a9")
        }
        return handlingIcons
    }
}
