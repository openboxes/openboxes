package org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

import org.pih.warehouse.core.*
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem


class RequisitionTests extends Specification implements DomainUnitTest<Requisition> {

    void setup() {
        config.openboxes.anonymize.enabled = false
    }

    void calculatePercentageCompleted_shouldBeNotCompleted() {
        given:
        def requisition = new Requisition()
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)

        when:
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 0))
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 0))

        then:
        0 == requisition.calculatePercentageCompleted()

        when:
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 999))
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 999))

        then:
        0 == requisition.calculatePercentageCompleted()
    }

    void calculatePercentageCompleted_shouldBeHalfCompleted() {
        given:
        def requisition = new Requisition()
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 1000))
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 0))

        expect:
        50 == requisition.calculatePercentageCompleted()
    }

    void calculatePercentageCompleted_shouldBeCompleted() {
        given:
        def requisition = new Requisition()
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 1000, quantityCanceled: 1000))

        expect:
        100 == requisition.calculatePercentageCompleted()
    }

    void newInstance_shouldCopyRequisitionAndRequisitionItems() {
        given:
        def origin = new Location(name: "HUM")
        def destination = new Location(name: "Boston")
        def requestedBy = new User(username: "jmiranda")
        def requisition = new Requisition(id:  "1", origin: origin, destination: destination, requestedBy: requestedBy,
                type: RequisitionType.ADHOC, commodityClass: CommodityClass.MEDICATION,
                dateRequested: new Date(), requestedDeliveryDate: new Date())

        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem)

        requisition.addToRequisitionItems(new RequisitionItem(id: "1"))
        requisition.addToRequisitionItems(new RequisitionItem(id: "2"))

        when:
        def requisitionClone = requisition.newInstance()

        then:
        requisitionClone != null
        "1" != requisitionClone.id
        requisitionClone != requisition
        origin == requisitionClone.origin
        destination == requisitionClone.destination
        RequisitionType.ADHOC == requisitionClone.type
        CommodityClass.MEDICATION == requisitionClone.commodityClass
        new Date().clearTime() == requisitionClone.dateRequested.clearTime()
        new Date().clearTime() == requisitionClone.requestedDeliveryDate.clearTime()
        requisitionClone.requestedBy == null
        2 == requisitionClone.requisitionItems.size()
    }

    void newInstance_shouldReturnEmptyRequisition() {
        given:
        def requisition = new Requisition()
        mockDomain(Requisition, [requisition])
        def requisitionClone = requisition.newInstance()

        expect:
        requisitionClone != requisition
        0 == requisitionClone.requisitionItems.size()
    }


    void testDefaultValues(){
       def requisition = new Requisition()
       assert requisition.dateRequested <= new Date()
       //def tomorrow = new Date().plus(1)
       //tomorrow.clearTime()
       def today = new Date()
	   today.clearTime()

	   assert requisition.requestedDeliveryDate >= today
    }

    @Ignore("To fix")
    void testNotNullableConstraints() {
        given:
        def requisition = new Requisition(dateRequested:null,requestedDeliveryDate:null)

        when:
        requisition.validate()

        then:
        "nullable" == requisition.errors["origin"]
        "nullable" == requisition.errors["destination"]
        "nullable" == requisition.errors["requestedBy"]
        "nullable" == requisition.errors["dateRequested"]
        "nullable" == requisition.errors["requestedDeliveryDate"]
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
         given:
         def requisition = new Requisition(dateRequested:new Date())

         when:
         requisition.validate()

         then:
         requisition.errors["dateRequested"] == null
    }

     void testDateRequestedCanBeLessThanToday() {
         given:
         def requisition = new Requisition(dateRequested:new Date().minus(6))

         when:
         requisition.validate()

         then:
         requisition.errors["dateRequested"] == null
    }

    void testRequestedDeliveryDateGreaterThanToday() {
        given:
        def tomorrow = new Date().plus(1)
        tomorrow.clearTime()
        def requisition = new Requisition(requestedDeliveryDate: tomorrow)

        when:
        requisition.validate()

        then:
        requisition.errors["requestedDeliveryDate"] == null
    }

	/*
    void testRequestedDeliveryDateCannotBeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(requestedDeliveryDate:new Date())
        requisition.validate()
        assert requisition.errors["requestedDeliveryDate"]
    }
    */

    void compareTo_shouldSortByOriginTypeCommodityClassDateCreated() {
        given:
        // def justin = new Person(id:"1", firstName:"Justin", lastName:"Miranda")
        def boston = new Location(id: "bos", name:"Boston")
        def miami = new Location(id: "mia", name:"Miami")
        def today = new Date()
        def tomorrow = new Date().plus(1)
        def yesterday = new Date().minus(1)
        def requisition1 = new Requisition(id: "1", destination: boston, origin: miami)
        def requisition2 = new Requisition(id: "2", destination: miami, origin: boston)

        def requisition3 = new Requisition(id: "3", destination: boston, origin: miami, dateRequested: today)
        def requisition4 = new Requisition(id: "4", destination: boston, origin: miami, dateRequested: tomorrow)

        def requisition5 = new Requisition(id: "5", destination: boston, origin: miami, dateRequested: today, type: RequisitionType.STOCK, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)
        def requisition6 = new Requisition(id: "6", destination: boston, origin: miami, dateRequested: today, type: RequisitionType.ADHOC, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)
        def requisition7 = new Requisition(id: "7", destination: miami, origin: boston, dateRequested: today, type: RequisitionType.NON_STOCK, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)

        def requisition8 = new Requisition(id: "8", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.NON_STOCK, commodityClass: CommodityClass.MEDICATION, dateCreated: today)
        def requisition9 = new Requisition(id: "9", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.NON_STOCK, commodityClass: CommodityClass.CONSUMABLES, dateCreated: today)
        def requisition10 = new Requisition(id: "10", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.NON_STOCK, commodityClass: CommodityClass.MEDICATION, dateCreated: today)

        def requisition11 = new Requisition(id: "11", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.NON_STOCK, dateCreated: tomorrow)
        def requisition12 = new Requisition(id: "12", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.NON_STOCK,dateCreated: yesterday)
        def requisition13 = new Requisition(id: "13", destination: miami, origin: boston, dateRequested: tomorrow, type: RequisitionType.NON_STOCK, dateCreated: today)

        // def equal1 = 0,
        def firstWins = 1 //, secondWins = -1

        expect:
        // assertEquals equal1, requisition1 <=> requisition1
        firstWins == (requisition1 <=> requisition2)
        firstWins == (requisition3 <=> requisition4)


        [requisition7,requisition5,requisition6] == [requisition5,requisition6,requisition7].sort()
        [requisition9,requisition8,requisition10] == [requisition8,requisition9,requisition10].sort()
        [requisition11,requisition13,requisition12] == [requisition11,requisition12,requisition13].sort()

    }

    @Ignore("To fix")
    void testToJson(){
      given:
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

      when:
      def json = requisition.toJson()

      then:
      json.id == requisition.id
      json.requestedById == peter.id
      json.requestedByName == peter.getName()
      json.dateRequested == today.format("MM/dd/yyyy")
      json.requestedDeliveryDate == tomorrow.format("MM/dd/yyyy HH:mm XXX")
      json.name == requisition.name
      json.version == requisition.version
      json.lastUpdated == requisition.lastUpdated.format("dd/MMM/yyyy hh:mm a")
      json.status == "CREATED"
      json.recipientProgram == requisition.recipientProgram
      json.originId == requisition.origin.id
      json.originName == requisition.origin.name
      json.destinationId == requisition.destination.id
      json.destinationName == requisition.destination.name
      json.requisitionItems.size() == 1
      json.requisitionItems[0].id == requisitionItem.id

    }

    @Ignore("To fix")
    void shouldContainRequestApprovalFields() {
        given:
        Requisition requisition = new Requisition()
        Person peter = new Person(id:"person1", firstName:"peter", lastName:"zhao")
        requisition.dateApproved = new Date()
        requisition.dateRejected = new Date()
        requisition.approvalRequired = false
        requisition.approvedBy = peter

        when:
        requisition.validate()

        then:
        false == requisition.approvalRequired
        null == requisition.errors?.dateApproved
        null == requisition.errors?.dateRejected
        peter == requisition.approvedBy
    }

    void shouldContainEvent() {
        given:
        mockDomain(Event)
        mockDomain(EventType)
        Person peter = new Person(id:"person1", firstName:"peter", lastName:"zhao")
        Location boston = new Location(id: "l1", name:"boston")
        Location miami = new Location(id: "l2", name:"miami")
        Date today = new Date()
        Date tomorrow = new Date().plus(1)
        RequisitionItem requisitionItem = new RequisitionItem(id:"item1")
        Requisition requisition = new Requisition(
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
        mockDomain(Requisition, [requisition])

        EventType eventType = new EventType(id: "321", name: "Approve", dateCreated: today, lastUpdated: today, eventCode: EventCode.APPROVED)
        Event event = new Event(id: "4321", eventType: eventType, dateCreated: today, lastUpdated: today)

        expect:
        requisition != null

        when:
        requisition.addToEvents(event)

        then:
        true == requisition.validate()
        1 == requisition.events.size()

    }
}
