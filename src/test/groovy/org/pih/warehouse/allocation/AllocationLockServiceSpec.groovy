package org.pih.warehouse.allocation

import grails.testing.gorm.DataTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import spock.lang.Specification

/**
 * OBLS-919: when a single transaction locks several products (a multi-line requisition), the locks must be
 * taken in a deterministic order so two concurrent allocations sharing products cannot deadlock.
 */
class AllocationLockServiceSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains Location, Product
    }

    void 'lockForAllocation(list) locks distinct products in ascending id order'() {
        given:
        AllocationLockService service = Spy(AllocationLockService)
        List<String> lockedOrder = []
        Location facility = new Location(id: 'f1')
        Product a = new Product(id: 'aaa')
        Product b = new Product(id: 'bbb')
        Product c = new Product(id: 'ccc')

        // Record the per-product lock calls instead of hitting the database.
        service.lockForAllocation(_ as Location, _ as Product) >> { Location f, Product p -> lockedOrder << p.id }

        when: 'products are supplied out of order, with a duplicate and a null'
        service.lockForAllocation(facility, [c, a, b, a, null])

        then: 'each distinct product is locked once, in ascending id order'
        lockedOrder == ['aaa', 'bbb', 'ccc']
    }

    void 'lockForAllocation(list) is a no-op for a null or empty product list'() {
        given:
        AllocationLockService service = Spy(AllocationLockService)

        when:
        service.lockForAllocation(new Location(id: 'f1'), (List<Product>) null)
        service.lockForAllocation(new Location(id: 'f1'), [])

        then: 'the per-product lock is never taken'
        0 * service.lockForAllocation(_ as Location, _ as Product)
    }
}
