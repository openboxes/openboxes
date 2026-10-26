package org.pih.warehouse.regression.obaf.backorder

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import org.pih.warehouse.allocation.BackorderService
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * BackorderService.validateBackorderReferences: an inbound shipment carrying sales-order
 * references is rejected only when a reference does not resolve to an order. An inbound item with
 * no demand left on its order is still accepted; the putaway strategy sends it to storage.
 */
class BackorderServiceSpec extends Specification implements ServiceUnitTest<BackorderService>, DataTest {

    Product product = product('BS')
    Requisition so1 = new Requisition(requestNumber: 'SO-1')

    void setupSpec() {
        mockDomain Shipment
    }

    void setup() {
        Requisition.metaClass.static.findByRequestNumber = { String n -> n == 'SO-1' ? so1 : null }
    }

    void cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(Requisition)
    }

    Shipment shipmentWith(ShipmentItem... items) {
        return new Shipment(name: 'inbound', shipmentItems: items as LinkedHashSet)
    }

    ShipmentItem item(String ref) {
        ShipmentItem si = new ShipmentItem(product: product, quantity: 3, backorderReference: ref)
        si.id = nextId('si')
        return si
    }

    void 'a resolvable reference passes'() {
        given:
        Shipment s = shipmentWith(item('SO-1'))

        when:
        service.validateBackorderReferences(s)

        then:
        !s.errors.hasErrors()
    }

    void 'an unknown backorder reference is rejected with backorder.notFound.message'() {
        given:
        Shipment s = shipmentWith(item('SO-404'))

        when:
        service.validateBackorderReferences(s)

        then:
        s.errors.globalErrors*.code == ['backorder.notFound.message']
        s.errors.globalErrors[0].arguments as List == ['SO-404']
    }

    void 'each unknown reference is rejected once, alongside a resolvable one'() {
        given:
        Shipment s = shipmentWith(item('SO-1'), item('SO-404'), item('SO-404'), item('SO-405'))

        when:
        service.validateBackorderReferences(s)

        then:
        s.errors.globalErrors*.code == ['backorder.notFound.message'] * 2
        s.errors.globalErrors*.arguments.collect { it as List }.flatten() as Set == ['SO-404', 'SO-405'] as Set
    }

    void 'an inbound item beyond what its order still needs is not rejected (it goes to storage at putaway)'() {
        given: 'SO-1 has no demand lines at all, so nothing on the shipment can be matched'
        so1.requisitionItems = [] as LinkedHashSet
        Shipment s = shipmentWith(item('SO-1'), item('SO-1'))

        when:
        service.validateBackorderReferences(s)

        then:
        !s.errors.hasErrors()
    }

    void 'a hard-linked item is not re-validated, even with a stale reference; unreferenced items pass'() {
        given:
        ShipmentItem hard = item('SO-404')
        hard.backorderItem = new RequisitionItem()
        ShipmentItem plain = item(null)
        Shipment s = shipmentWith(hard, plain)

        when:
        service.validateBackorderReferences(s)

        then:
        !s.errors.hasErrors()
    }
}
