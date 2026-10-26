// Companion spec: extra features for upstream's ProductServiceSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.product

import spock.lang.PendingFeature

import org.pih.warehouse.product.*
import grails.testing.gorm.DataTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ApiException

import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.product.ProductType

@Unroll
class ProductServiceMoreSpec extends Specification implements DataTest {

    @Shared
    ProductService service

    void setupSpec() {
        mockDomain Product
    }

    void setup() {
        service = new ProductService()
    }

    @PendingFeature(reason = 'OBAF builds ApiException with a map constructor: MethodSelectionException')
    void 'getNdcResults asks for the Hipaaspace API key when none is configured'() {
        given:
        service.grailsApplication = grailsApplication

        when:
        service.getNdcResults('search', 'aspirin')

        then:
        ApiException e = thrown()
        e.message.contains('hipaaspace.api.key')
    }

    @PendingFeature(reason = 'OBAF builds ApiException with a map constructor: MethodSelectionException')
    void 'findGoogleProducts asks for the Google API key when none is configured'() {
        given:
        service.grailsApplication = grailsApplication

        when:
        service.findGoogleProducts([searchTerms: 'aspirin', startIndex: 0, spellingEnabled: false])

        then:
        ApiException e = thrown()
        e.message.contains('google.api.key')
    }
}
