package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.core.*


class RequisitionTests extends GrailsUnitTestCase {

    void testDefaultValues(){
       def requisition = new Requisition()
       assert requisition.dateRequested <= new Date()
       def tomorrow = new Date().plus(1)
       tomorrow.clearTime()
       assert requisition.requestedDeliveryDate >= tomorrow
    }

    void testNotNullableConstraints() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:null,requestedDeliveryDate:null)
        assertFalse requisition.validate()
        assertEquals "nullable", requisition.errors["origin"]
        assertEquals "nullable", requisition.errors["destination"]
        assertEquals "nullable", requisition.errors["requestedBy"]
        assertEquals "nullable", requisition.errors["dateRequested"]
        assertEquals "nullable", requisition.errors["requestedDeliveryDate"]
    }

    void testDateRequestedCannotBeGreaterThanToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date().plus(1))
        assertFalse requisition.validate()
        assert requisition.errors["dateRequested"]
    }

     void testDateRequestedCanbeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date())
        requisition.validate()
        assertNull requisition.errors["dateRequested"]
    }

     void testDateRequestedCanbeLessThanToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date().minus(6))
        requisition.validate()
        assertNull requisition.errors["dateRequested"]
    }

    void testRequestedDeliveryDateGreaterThanToday() {
        mockForConstraintsTests(Requisition)
        def tomorrow = new Date().plus(1)
        tomorrow.clearTime()
        def requisition = new Requisition(requestedDeliveryDate: tomorrow)
        requisition.validate()
        assertNull requisition.errors["requestedDeliveryDate"]
    }

    void testRequestedDeliveryDateCannotBeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(requestedDeliveryDate:new Date())
        requisition.validate()
        assert requisition.errors["requestedDeliveryDate"]
    }
    void testToJson(){
      def peter = new Person(id:"person1", firstName:"peter", lastName:"zhao")
      def boston = new Location(id: "l1", name:"boston")
      def miami = new Location(id: "l2", name:"miami")
      def today = new Date()
      def tomorrow = new Date().plus(1)
      def requisitionItem = new RequisitionItem(id:"item1")
      def requisition = new Requisition(
        id: "1234",
        requestedBy: peter,
        dateRequested: today,
        requestedDeliveryDate: tomorrow,
        name: "test",
        version: 3,
        lastUpdated: today,
        status:  RequisitionStatus.OPEN,
        recipientProgram: "prog",
        origin: boston,
        destination: miami,
        requisitionItems: [requisitionItem]
      )
      def json = requisition.toJson()
      assert json.id == requisition.id
      assert json.requestedById == peter.id
      assert json.requestedByName == peter.getName()
      assert json.dateRequested == today
      assert json.requestedDeliveryDate == tomorrow
      assert json.name == requisition.name
      assert json.version == requisition.version
      assert json.lastUpdated == requisition.lastUpdated
      assert json.status == "OPEN"
      assert json.recipientProgram == requisition.recipientProgram
      assert json.originId == requisition.origin.id
      assert json.originName == requisition.origin.name
      assert json.destinationId == requisition.destination.id
      assert json.destinationName == requisition.destination.name
      assert json.requisitionItems.size() == 1
      assert json.requisitionItems[0].id == requisitionItem.id

    }

}
