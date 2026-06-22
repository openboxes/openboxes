package org.pih.warehouse.databinding

import grails.web.databinding.DataBindingUtils

import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.product.Product

/**
 * Verifies ProductValueConverter end to end: when Grails data-binds a String into a Product-typed
 * field, the converter resolves it by id OR productCode (and to null for an unknown identifier).
 *
 * StockMovementItem is used because it has a `Product product` field, but the converter is
 * registered globally, so this exercises the exact path the API uses (bindData -> data binder ->
 * registered ValueConverter). The binding runs inside a session because the converter issues a
 * GORM query (Product.findByIdOrProductCode).
 */
class ProductValueConverterIntegrationSpec extends ApiSpec {

    void 'binds a Product-typed field from a productCode string'() {
        given:
        StockMovementItem item = new StockMovementItem()

        when:
        Product.withNewSession {
            DataBindingUtils.bindObjectToInstance(item, [product: product.productCode])
        }

        then:
        item.product?.id == product.id
    }

    void 'binds a Product-typed field from an id string'() {
        given:
        StockMovementItem item = new StockMovementItem()

        when:
        Product.withNewSession {
            DataBindingUtils.bindObjectToInstance(item, [product: product.id])
        }

        then:
        item.product?.id == product.id
    }

    void 'binds null when the identifier matches no product'() {
        given:
        StockMovementItem item = new StockMovementItem()

        when:
        Product.withNewSession {
            DataBindingUtils.bindObjectToInstance(item, [product: 'no-such-id-or-code'])
        }

        then:
        item.product == null
    }
}
