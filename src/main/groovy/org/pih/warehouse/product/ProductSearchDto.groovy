package org.pih.warehouse.product

import grails.util.Holders

class ProductSearchDto {

    String id

    Boolean active

    String name

    String displayName

    String productCode

    Boolean coldChain = Boolean.FALSE

    Boolean controlledSubstance = Boolean.FALSE

    Boolean hazardousMaterial = Boolean.FALSE

    Boolean reconditioned = Boolean.FALSE

    Boolean lotAndExpiryControl = Boolean.FALSE

    String productColor

    Boolean exactMatch = Boolean.FALSE

    String unitOfMeasure

    def getApplicationTagLib() {
        return Holders.grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
    }

    def getHandlingIcons() {
        def g = getApplicationTagLib()
        def handlingIcons = []
        if (this.coldChain) handlingIcons.add([icon: "fa-snowflake", color: "#3bafda", label: "${g.message(code: 'product.coldChain.label')}"])
        if (this.controlledSubstance) handlingIcons.add([icon: "fa-exclamation-circle", color: "#db1919", label: "${g.message(code: 'product.controlledSubstance.label')}"])
        if (this.hazardousMaterial) handlingIcons.add([icon: "fa-exclamation-triangle", color: "#ffa500", label: "${g.message(code: 'product.hazardousMaterial.label')}"])
        if (this.reconditioned) handlingIcons.add([icon: "fa-prescription-bottle", color: "#a9a9a9", label: "${g.message(code: 'product.reconditioned.label')}"])
        return handlingIcons
    }

    Map toJson() {
        [
                id                 : id,
                productCode        : productCode,
                active             : active,
                name               : name,
                displayName        : displayName,
                color              : productColor,
                handlingIcons      : handlingIcons,
                lotAndExpiryControl: lotAndExpiryControl,
                exactMatch         : exactMatch,
                unitOfMeasure      : unitOfMeasure
        ]
    }
}
