package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product

class ReplenishmentControllerSpec extends Specification implements ControllerUnitTest<ReplenishmentController>, DataTest {

    void setupSpec() {
        mockDomains(Location, Order, OrderItem, Picklist, PicklistItem, Product, User)
    }

    void 'index and create open the react page'() {
        when:
        params.id = 'o1'
        controller.index()

        then:
        response.redirectedUrl.startsWith('/replenishment/create')

        when:
        response.reset()
        controller.create()

        then:
        view == '/common/react'
    }

    void 'print groups the picked lines by zone and handling'() {
        given:
        Location zoneA = new Location(name: 'Zone A').save(validate: false)
        Location binA = new Location(name: 'A-01', zone: zoneA).save(validate: false)
        Location binNoZone = new Location(name: 'B-01').save(validate: false)
        Product cold = new Product(name: 'Vaccine', coldChain: true).save(validate: false)
        Product controlled = new Product(name: 'Morphine', controlledSubstance: true).save(validate: false)
        Product hazardous = new Product(name: 'Acid', hazardousMaterial: true).save(validate: false)
        Product general = new Product(name: 'Gauze').save(validate: false)
        Order order = new Order(orderNumber: 'TR-1', createdBy: new User(username: 'u').save(validate: false),
                dateCreated: new Date(126, 0, 15)).save(validate: false)
        order.dateCreated = new Date(126, 0, 15)
        OrderItem coldItem = new OrderItem(product: cold, order: order).save(validate: false)
        OrderItem controlledItem = new OrderItem(product: controlled, order: order).save(validate: false)
        OrderItem hazardousItem = new OrderItem(product: hazardous, order: order).save(validate: false)
        OrderItem generalItem = new OrderItem(product: general, order: order).save(validate: false)
        Picklist picklist = new Picklist(order: order)
        picklist.addToPicklistItems(new PicklistItem(orderItem: coldItem, binLocation: binA, quantity: 1))
        picklist.addToPicklistItems(new PicklistItem(orderItem: controlledItem, binLocation: binA, quantity: 1))
        picklist.addToPicklistItems(new PicklistItem(orderItem: hazardousItem, binLocation: binNoZone, quantity: 1))
        picklist.addToPicklistItems(new PicklistItem(orderItem: generalItem, binLocation: binNoZone, quantity: 2))
        picklist.save(validate: false)
        params.id = order.id

        when:
        def model = controller.print()

        then:
        model.headerItems.orderNumber == 'TR-1'
        model.headerItems.dateCreated == order.dateCreated.format('MM/dd/yyyy')
        model.itemsMap.keySet() as List == ['Zone A', null]
        model.itemsMap['Zone A'].lineItems.coldChain == [coldItem]
        model.itemsMap['Zone A'].lineItems.controlledSubstance == [controlledItem]
        model.itemsMap['Zone A'].lineItems.generalGoods == []
        model.itemsMap[null].lineItems.hazardousMaterial == [hazardousItem]
        model.itemsMap[null].lineItems.generalGoods == [generalItem]
        model.itemsMap[null].pickListItems[generalItem]*.quantity == [2]
    }
}
