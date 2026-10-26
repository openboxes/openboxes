package org.pih.warehouse.regression.platform.shipping

import spock.lang.PendingFeature

import org.pih.warehouse.shipping.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Person

class ContainerSpec extends Specification implements DataTest {

    ContainerType palletType
    ContainerType boxType
    Shipment shipment

    void setupSpec() {
        mockDomains(Container, ContainerType, Shipment, ShipmentItem, Person)
    }

    void setup() {
        palletType = new ContainerType(name: 'Pallet').save(validate: false)
        boxType = new ContainerType(name: 'Box')
        boxType.id = Constants.BOX_CONTAINER_TYPE_ID
        boxType.save(validate: false)
        shipment = new Shipment(name: 'S-1').save(validate: false, flush: true)
    }

    void 'containers sort by sort order, unsorted ones first, then by id'() {
        given:
        Container unsorted = new Container(name: 'u')
        Container first = new Container(name: 'a', sortOrder: 1)
        Container second = new Container(name: 'b', sortOrder: 2)
        Container sameOrderX = new Container(name: 'x', sortOrder: 3)
        Container sameOrderY = new Container(name: 'y', sortOrder: 3)
        sameOrderX.id = 'x'
        sameOrderY.id = 'y'

        expect:
        [second, sameOrderY, unsorted, sameOrderX, first].sort() == [unsorted, first, second, sameOrderX, sameOrderY]
        unsorted.compareTo(first) == -1
        first.compareTo(unsorted) == 1
    }

    void 'a copy keeps the description and size but not the shipment'() {
        given:
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee')
        Container pallet = new Container(name: 'Pallet 1', containerNumber: 'P1', description: 'Drugs', recipient: recipient,
                sortOrder: 2, height: 1.5f, width: 1f, length: 1.2f, volumeUnits: 'm3', weight: 40f, weightUnits: 'kg',
                containerType: palletType, shipment: shipment)

        when:
        Container copy = pallet.copyContainer()

        then:
        copy.name == 'Pallet 1'
        copy.containerNumber == 'P1'
        copy.recipient == recipient
        copy.weight == 40f
        copy.containerType == palletType
        copy.shipment == null
        pallet.optionValue == 'Pallet-Pallet 1'
        pallet.toString() == 'Pallet 1'
    }

    void 'boxes are added to a pallet once per name and items are added to the shipment'() {
        given:
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee')
        Container pallet = new Container(name: 'Pallet 1', containerType: palletType, shipment: shipment,
                recipient: recipient).save(validate: false)
        shipment.addToContainers(pallet)

        when:
        Container box = pallet.findOrCreateBox('Box A')
        Container again = pallet.findOrCreateBox('box a')
        Container other = pallet.findOrCreateBox('Box B')

        then:
        box.is(again)
        box.name == 'Box A'
        box.containerType == boxType
        box.parentContainer == pallet
        box.sortOrder == 0
        other.sortOrder == 1
        pallet.containers.size() == 2
        shipment.containers.containsAll([box, other])
        pallet.findContainerByName('missing') == null

        when:
        ShipmentItem item = pallet.addNewItem()

        then:
        item.container == pallet
        item.recipient == recipient
        shipment.shipmentItems.contains(item)
        pallet.shipmentItemsFromSession == [item] as Set || pallet.shipmentItemsFromSession as List == [item]
    }

    void 'a container lists its saved shipment items'() {
        given:
        Container pallet = new Container(name: 'Pallet 1', containerType: palletType, shipment: shipment).save(validate: false)
        ShipmentItem item = new ShipmentItem(container: pallet, shipment: shipment).save(validate: false, flush: true)
        new ShipmentItem(shipment: shipment).save(validate: false, flush: true)

        expect:
        pallet.shipmentItems == [item]
    }

    @PendingFeature(reason = 'OBAF has POUNDS_PER_KILOGRAM and KILOGRAMS_PER_POUND swapped')
    void 'weights convert between kilograms and pounds and add up the inner containers'() {
        given:
        Container kilos = new Container(name: 'k', weight: 10f, weightUnits: 'kg')
        Container pounds = new Container(name: 'p', weight: 10f, weightUnits: 'lbs')
        Container outer = new Container(name: 'outer', containers: [kilos, pounds] as Set)
        Container empty = new Container(name: 'empty')

        expect:
        kilos.totalWeightInKilograms() == 10f
        Math.abs(pounds.totalWeightInKilograms() - 4.5359237f) < 0.0001
        Math.abs(kilos.totalWeightInPounds() - 22.0462262f) < 0.0001
        pounds.totalWeightInPounds() == 10f
        Math.abs(outer.totalWeightInKilograms() - 14.5359237f) < 0.0001
        Math.abs(outer.totalWeightInPounds() - 32.0462262f) < 0.0001
        empty.totalWeightInKilograms() == 0
        empty.totalWeightInPounds() == 0
    }
}
