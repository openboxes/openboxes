package unit.org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import spock.lang.Specification

import org.pih.warehouse.requisition.RequisitionType

class RequisitionSpec extends Specification implements DomainUnitTest<Requisition> {

    void 'validate should return true for a valid requisition'() {
        when:
        Location location = new Location()
        Product product1 = new Product(name: 'Advil 200mg')
        Product product2 = new Product(name: 'Tylenol 325mg')
        RequisitionItem item1 = new RequisitionItem(product: product1, quantity: 10)
        RequisitionItem item2 = new RequisitionItem(product: product2, quantity: 20)
        Person person = new Person()

        Requisition requisition = new Requisition(
                name: 'testRequisition',
                commodityClass: CommodityClass.MEDICATION,
                type:  RequisitionType.NON_STOCK,
                origin: location,
                destination: location,
                requestedBy: person,
                dateRequested: new Date(),
                requestedDeliveryDate: new Date() + 1,
        )

        requisition.addToRequisitionItems(item1)
        requisition.addToRequisitionItems(item2)

        then:
        assert requisition.validate()
    }
}
