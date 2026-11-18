package org.pih.warehouse.smoke.spec

import spock.lang.Unroll

import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.smoke.spec.base.SmokeSpec

class ProductPriceSpec extends SmokeSpec {

    @Unroll
    void "product price #price retains its precision after database reload"() {
        when:
        BigDecimal reloadedPrice = ProductPrice.withNewTransaction { status ->
            try {
                ProductPrice productPrice = new ProductPrice(price: price).save(flush: true, failOnError: true)
                String id = productPrice.id
                productPrice.discard()

                return ProductPrice.get(id).price
            } finally {
                status.setRollbackOnly()
            }
        }

        then:
        assert reloadedPrice == price

        where:
        price << [3.4666, 0.0001, 0.0000, 12.3400]
    }
}
