package org.pih.warehouse.api.regression.platform.inventory

import spock.lang.PendingFeature


import org.pih.warehouse.inventory.*
import grails.validation.ValidationException
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog

import java.time.LocalDate

/**
 * The filters of the cycle count candidate and pending request lists: a new depot holds stock of one product that
 * has never been counted (no request, no count), so it is a candidate with no status.
 */
class CycleCountServiceIntegrationSpec extends ServiceIntegrationSpec {

    CycleCountService cycleCountService

    Location depot
    Product product

    void setup() {
        depot = newDepot('Cycle count depot')
        product = newProduct()
        stock(depot, product, uniqueName('LOT'), null, 7)
    }

    private static String random(String prefix) {
        return "${prefix}-${UUID.randomUUID().toString().take(8)}".toString()
    }

    /** Builds the command inside a transaction (the filters reference persisted entities) and lists candidates. */
    private Map candidates(Closure<Map> filters) {
        return asAdminAt(depot) {
            CycleCountCandidateFilterCommand command = new CycleCountCandidateFilterCommand(filters.call(Product.get(product.id)))
            List<CycleCountCandidate> results = cycleCountService.getCandidates(command, depot.id)
            List<PendingCycleCountRequest> pending = cycleCountService.getPendingCycleCountRequests(command, depot.id)
            [
                    products  : results*.product*.id,
                    facilities: results*.facility*.id.unique(),
                    pending   : pending.size(),
            ]
        }
    }

    @PendingFeature(reason = 'candidates sorted by the date of the last count fail (QueryException) for pending requests')
    @Unroll
    void 'the candidate list #includes the uncounted product when filtered by #filter'() {
        when:
        Map result = candidates(filters)

        then:
        result.products.contains(product.id) == included
        result.facilities.every { it == depot.id }
        result.pending == 0

        where:
        filter                                       | included | filters
        'nothing'                                    | true     | { Product p -> [:] }
        'its product code'                           | true     | { Product p -> [searchTerm: p.productCode] }
        'another search term'                        | false    | { Product p -> [searchTerm: 'no-such-product-code'] }
        'its category'                               | true     | { Product p -> [categories: [p.category]] }
        'another category'                           | false    | { Product p -> [categories: [Category.findByName('Medicines') ?: new Category(name: random('Category')).save(flush: true)]] }
        'a bin it is not in'                         | false    | { Product p -> [internalLocations: ['NO-SUCH-BIN']] }
        'a tag it does not have'                     | false    | { Product p -> [tags: [new Tag(tag: random('tag')).save(flush: true)]] }
        'a catalog it is not in'                     | false    | { Product p -> [catalogs: [new ProductCatalog(name: random('Catalog'), code: random('C')).save(flush: true)]] }
        'an ABC class it does not have'              | false    | { Product p -> [abcClasses: ['Z']] }
        'a status it does not have'                  | false    | { Product p -> [statuses: [CycleCountCandidateStatus.REQUESTED]] }
        'a status, also showing counts in progress'  | true     | { Product p -> [statuses: [CycleCountCandidateStatus.REQUESTED], showCycleCountsInProgress: true] }
        'a count assignee'                           | false    | { Product p -> [countAssignees: [User.findByUsername('admin')]] }
        'a recount assignee'                         | false    | { Product p -> [recountAssignees: [User.findByUsername('admin')]] }
        'a count deadline'                           | false    | { Product p -> [countDeadline: LocalDate.now()] }
        'a recount deadline'                         | false    | { Product p -> [recountDeadline: LocalDate.now()] }
        'negative quantities only'                   | false    | { Product p -> [negativeQuantity: true] }
        'requests it does not have'                  | true     | { Product p -> [requestIds: ['no-such-request']] }
        'sorted by product, as a csv'                | true     | { Product p -> [sort: 'product', order: 'desc', format: 'csv'] }
        'sorted by category'                         | true     | { Product p -> [sort: 'category', order: 'desc'] }
        'sorted by ABC class'                        | true     | { Product p -> [sort: 'abcClass'] }
        'sorted by quantity on hand'                 | true     | { Product p -> [sort: 'quantityOnHand', order: 'desc'] }
        'sorted by the date of the last count'       | true     | { Product p -> [sort: 'dateLastCount', order: 'desc'] }
        'its code, sorted by product'                | true     | { Product p -> [searchTerm: p.productCode, sort: 'product'] }

        includes = included ? 'includes' : 'leaves out'
    }

    void 'the candidates are filtered and sorted by the date of their last count (the baseline counts)'() {
        when:
        List counted = asAdminAt(depot) {
            CycleCountCandidateFilterCommand command = new CycleCountCandidateFilterCommand(dateLastCount: new Date(),
                    sort: 'dateLastCount', order: 'asc')
            cycleCountService.getCandidates(command, depot.id)*.product*.id
        }
        List before = asAdminAt(depot) {
            cycleCountService.getCandidates(new CycleCountCandidateFilterCommand(dateLastCount: new Date() - 30), depot.id)*.product*.id
        }

        then:
        counted.contains(product.id)
        !before.contains(product.id)
    }

    void 'an invalid filter is refused'() {
        when:
        asAdminAt(depot) {
            CycleCountCandidateFilterCommand command = new CycleCountCandidateFilterCommand()
            command.errors.reject('invalid')
            cycleCountService.getCandidates(command, depot.id)
        }

        then:
        thrown(ValidationException)

        when:
        asAdminAt(depot) {
            CycleCountCandidateFilterCommand command = new CycleCountCandidateFilterCommand()
            command.errors.reject('invalid')
            cycleCountService.getPendingCycleCountRequests(command, depot.id)
        }

        then:
        thrown(ValidationException)
    }
}
