package org.pih.warehouse.regression.platform.product

import spock.lang.PendingFeature

import org.pih.warehouse.product.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.User

class ProductEventsSpec extends Specification implements DataTest {

    Product primary
    Product obsolete
    Product other
    User merger

    void setupSpec() {
        mockDomains(Product, ProductMergeLogger, User)
    }

    void setup() {
        primary = new Product(name: 'Gloves', productCode: 'G1').save(validate: false)
        obsolete = new Product(name: 'Old gloves', productCode: 'G0').save(validate: false)
        other = new Product(name: 'Masks', productCode: 'M1').save(validate: false)
        merger = new User(username: 'jo').save(validate: false, flush: true)
    }

    private void logMerge(Product into, Product from, String comments) {
        new ProductMergeLogger(primaryProduct: into, obsoleteProduct: from, dateMerged: new Date(), comments: comments,
                createdBy: merger).save(validate: false, flush: true)
    }

    @PendingFeature(reason = 'OBAF calls last() on an empty merge event list: NoSuchElementException')
    void 'a product without merges has no events or summary'() {
        when:
        ProductEvents events = ProductEvents.createFromProduct(other)

        then:
        events.product.is(other)
        events.productMergeEvents == []
        events.productMergeSummary == ''
        events.otherProductId == null
    }

    void 'the primary product of a merge lists the obsolete products it absorbed once each'() {
        given:
        logMerge(primary, obsolete, 'duplicate')
        logMerge(primary, obsolete, 'logged twice')

        when:
        ProductEvents events = ProductEvents.createFromProduct(primary)

        then:
        events.productMergeEvents.size() == 1
        with(events.productMergeEvents[0]) {
            primaryProduct == primary
            obsoleteProduct == obsolete
            mergedBy == merger
        }
        events.productMergeSummary == 'Obsolete product: G0 Old gloves\n'
        events.otherProductId == obsolete.id
    }

    void 'the obsolete product of a merge points at its primary product'() {
        given:
        logMerge(primary, obsolete, 'duplicate')

        when:
        ProductEvents events = ProductEvents.createFromProduct(obsolete)

        then:
        events.productMergeEvents*.primaryProduct == [primary]
        events.productMergeSummary == 'Primary product: G1 Gloves\n'
        events.otherProductId == primary.id
    }
}
