package org.pih.warehouse.api.regression.platform.builder

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder

/** Upstream's ProductTestBuilder plus a pricePerUnit() method (see RegressionLocationTestBuilder). */
@InheritConstructors
class RegressionProductTestBuilder extends ProductTestBuilder {

    RegressionProductTestBuilder pricePerUnit(BigDecimal pricePerUnit) {
        args.pricePerUnit = pricePerUnit
        return this
    }
}
