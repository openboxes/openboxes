package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.pih.warehouse.core.*


class RequisitionTests extends GrailsUnitTestCase {

    void testDefaultValues(){
       def requisition = new Requisition()
       assert requisition.dateRequested <= new Date()
       //def tomorrow = new Date().plus(1)
       //tomorrow.clearTime()
       def today = new Date()
	   today.clearTime()
	   
	   assert requisition.requestedDeliveryDate >= today
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

	/*
    void testDateRequestedCannotBeGreaterThanToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date().plus(1))
        assertFalse requisition.validate()
        assert requisition.errors["dateRequested"]
    }
    */

     void testDateRequestedCanNeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date())
        requisition.validate()
        assertNull requisition.errors["dateRequested"]
    }

     void testDateRequestedCanBeLessThanToday() {
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

	/*
    void testRequestedDeliveryDateCannotBeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(requestedDeliveryDate:new Date())
        requisition.validate()
        assert requisition.errors["requestedDeliveryDate"]
    }
    */

    @Test
    void compareTo_shouldSortByOriginTypeCommodityClassDateCreated() {
        def justin = new Person(id:"1", firstName:"Justin", lastName:"Miranda")
        def boston = new Location(id: "bos", name:"Boston")
        def miami = new Location(id: "mia", name:"Miami")
        def today = new Date()
        def tomorrow = new Date().plus(1)
        def yesterday = new Date().minus(1)
        def requisition1 = new Requisition(id: "1", destination: boston, origin: miami)
        def requisition2 = new Requisition(id: "2", destination: miami, origin: boston)

        def requisition3 = new Requisition(id: "3", destination: boston, origin: miami, dateRequested: today)
        def requisition4 = new Requisition(id: "4", destination: boston, origin: miami, dateRequested: tomorrow)

        def requisition5 = new Requisition(id: "5", destination: boston, origin: miami, dateRequested: today, type: RequisitionType.WARD_STOCK, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)
        def requisition6 = new Requisition(id: "6", destination: boston, origin: miami, dateRequested: today, type: RequisitionType.WARD_ADHOC, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)
        def requisition7 = new Requisition(id: "7", destination: miami, origin: boston, dateRequested: today, type: RequisitionType.WARD_NON_STOCK, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)

        def requisition8 = new Requisition(id: "8", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.WARD_NON_STOCK, commodityClass: CommodityClass.MEDICATION, dateCreated: today)
        def requisition9 = new Requisition(id: "9", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.WARD_NON_STOCK, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)
        def requisition10 = new Requisition(id: "10", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.WARD_NON_STOCK, commodityClass: CommodityClass.MEDICATION, dateCreated: today)

        def requisition11 = new Requisition(id: "11", dateCreated: tomorrow)
        def requisition12 = new Requisition(id: "12", dateCreated: yesterday)
        def requisition13 = new Requisition(id: "13", dateCreated: today)

        def equal = 0, firstWins = 1, secondWins = -1
        assertEquals equal, requisition1 <=> requisition1
        assertEquals firstWins, requisition1 <=> requisition2
        assertEquals firstWins, requisition3 <=> requisition4


        assertEquals([requisition7,requisition5,requisition6], [requisition5,requisition6,requisition7].sort())
        assertEquals([requisition9,requisition8,requisition10], [requisition8,requisition9,requisition10].sort())
        assertEquals([requisition11,requisition13,requisition12], [requisition11,requisition12,requisition13].sort())

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
        status:  RequisitionStatus.CREATED,
        recipientProgram: "prog",
        origin: boston,
        destination: miami,
        requisitionItems: [requisitionItem]
      )
      def json = requisition.toJson()
      assert json.id == requisition.id
      assert json.requestedById == peter.id
      assert json.requestedByName == peter.getName()
      assert json.dateRequested == today.format("MM/dd/yyyy")
      assert json.requestedDeliveryDate == tomorrow.format("MM/dd/yyyy")
      assert json.name == requisition.name
      assert json.version == requisition.version
      assert json.lastUpdated == requisition.lastUpdated.format("dd/MMM/yyyy hh:mm a")
      assert json.status == "CREATED"
      assert json.recipientProgram == requisition.recipientProgram
      assert json.originId == requisition.origin.id
      assert json.originName == requisition.origin.name
      assert json.destinationId == requisition.destination.id
      assert json.destinationName == requisition.destination.name
      assert json.requisitionItems.size() == 1
      assert json.requisitionItems[0].id == requisitionItem.id

    }

}
