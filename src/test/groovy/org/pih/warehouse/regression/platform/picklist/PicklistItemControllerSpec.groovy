package org.pih.warehouse.regression.platform.picklist

import org.pih.warehouse.picklist.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class PicklistItemControllerSpec extends Specification implements ControllerUnitTest<PicklistItemController>, DataTest {

    void setupSpec() {
        mockDomains(Picklist, PicklistItem, Requisition, RequisitionItem, Product)
    }

    void setup() {
        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code }]
    }

    void 'delete removes the item and returns to the pick page of its requisition'() {
        given:
        Requisition requisition = new Requisition(name: 'Ward request').save(validate: false)
        RequisitionItem requisitionItem = new RequisitionItem(requisition: requisition,
                product: new Product(name: 'Paracetamol', productCode: 'PC1').save(validate: false)).save(validate: false)
        Picklist picklist = new Picklist(requisition: requisition)
        PicklistItem item = new PicklistItem(requisitionItem: requisitionItem, quantity: 2)
        picklist.addToPicklistItems(item)
        picklist.save(validate: false)
        item.save(validate: false)
        params.id = item.id

        when:
        controller.delete()

        then:
        flash.message == 'default.deleted.message'
        response.redirectedUrl == "/requisition/pick/${requisition.id}?requisitionItem.id=${requisitionItem.id}"
    }

    void 'delete reports a missing item'() {
        given:
        params.id = 'missing'

        when:
        controller.delete()

        then:
        flash.message == 'default.not.found.message'
        response.redirectedUrl.startsWith('/requisition/pick')
    }
}
