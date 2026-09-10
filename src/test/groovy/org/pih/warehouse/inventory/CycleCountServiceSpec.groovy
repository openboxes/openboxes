package org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

import java.time.LocalDate

@Unroll
class CycleCountServiceSpec extends Specification implements DataTest {

    CycleCountService cycleCountService

    CycleCountProductAvailabilityService cycleCountProductAvailabilityServiceMock

    CycleCountTransactionService cycleCountTransactionServiceMock

    void setupSpec() {
        mockDomains(CycleCount, CycleCountItem, CycleCountRequest, InventoryItem, Product, Category, Location,
                Person, User, Transaction, TransactionSource)
    }

    void setup() {
        cycleCountService = new CycleCountService()

        // The service stamps createdBy/updatedBy from the logged-in user; give it one.
        User currentUser = new User(username: "tester")
        currentUser.save(validate: false, flush: true)
        new AuthService().setCurrentUser(currentUser)

        cycleCountProductAvailabilityServiceMock = Mock(CycleCountProductAvailabilityService)
        cycleCountService.cycleCountProductAvailabilityService = cycleCountProductAvailabilityServiceMock

        cycleCountTransactionServiceMock = Mock(CycleCountTransactionService)
        cycleCountService.cycleCountTransactionService = cycleCountTransactionServiceMock
    }

    // ---------------------------------------------------------------------------------------------
    // Fixture helpers
    // ---------------------------------------------------------------------------------------------

    private Location createFacility(String name = "Main facility") {
        Location facility = new Location(name: name)
        facility.save(validate: false, flush: true)
        return facility
    }

    private Location createBinLocation(String name) {
        Location binLocation = new Location(name: name)
        binLocation.save(validate: false, flush: true)
        return binLocation
    }

    private Product createProduct(String productCode, String name = "Product ${productCode}") {
        Product product = new Product(productCode: productCode, name: name)
        product.save(validate: false, flush: true)
        return product
    }

    private InventoryItem createInventoryItem(Product product, String lotNumber = null, Date expirationDate = null) {
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: lotNumber, expirationDate: expirationDate)
        inventoryItem.save(validate: false, flush: true)
        return inventoryItem
    }

    private Person createPerson(String firstName) {
        Person person = new Person(firstName: firstName, lastName: "Counter")
        person.save(validate: false, flush: true)
        return person
    }

    private CycleCountRequest createRequest(Map args = [:]) {
        CycleCountRequest request = new CycleCountRequest(
                facility: args.facility,
                product: args.product,
                status: args.status ?: CycleCountRequestStatus.CREATED,
                requestType: CycleCountRequestType.MANUAL_REQUEST,
                blindCount: args.containsKey("blindCount") ? args.blindCount : true,
                countAssignee: args.countAssignee,
                recountAssignee: args.recountAssignee,
                cycleCount: args.cycleCount,
        )
        request.save(validate: false, flush: true)
        return request
    }

    private CycleCount createCycleCountWithItems(Location facility, List<CycleCountItem> items,
                                                 CycleCountStatus status = CycleCountStatus.COUNTING) {
        CycleCount cycleCount = new CycleCount(facility: facility, status: status, dateLastRefreshed: new Date())
        items.each {
            // Save before adding so each item already has an id. Unsaved items compare by object
            // identity (see CycleCountItem.compareTo), which makes the sorted item set behave
            // unpredictably in the test environment once items are saved and reloaded. Adding
            // unsaved items - the production behavior - is still covered by the startCycleCount
            // and startRecount tests.
            it.save(validate: false, flush: true)
            cycleCount.addToCycleCountItems(it)
        }
        cycleCount.save(validate: false, flush: true)
        // The save hook recomputes the status from the items; tests that need a specific
        // status set it explicitly afterwards.
        cycleCount.status = status
        return cycleCount
    }

    private CycleCountItem buildItem(Map args) {
        return new CycleCountItem(
                facility: args.facility,
                cycleCount: args.cycleCount,
                inventoryItem: args.inventoryItem,
                location: args.binLocation,
                product: args.product ?: args.inventoryItem?.product,
                countIndex: args.containsKey("countIndex") ? args.countIndex : 0,
                status: args.status ?: CycleCountItemStatus.COUNTING,
                quantityOnHand: args.containsKey("quantityOnHand") ? args.quantityOnHand : 10,
                quantityCounted: args.containsKey("quantityCounted") ? args.quantityCounted : null,
                custom: args.containsKey("custom") ? args.custom : false,
                assignee: args.assignee,
                dateCounted: args.dateCounted,
        )
    }

    private AvailableItem availableItem(InventoryItem inventoryItem, Location binLocation, BigDecimal quantityOnHand) {
        return new AvailableItem(inventoryItem: inventoryItem, binLocation: binLocation, quantityOnHand: quantityOnHand)
    }

    // ---------------------------------------------------------------------------------------------
    // getInventoryItemsCount
    // ---------------------------------------------------------------------------------------------

    void 'getInventoryItemsCount should count available items when the count has not been started'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        CycleCountRequest request = createRequest(facility: facility, product: product)

        and: 'product availability reports two items'
        InventoryItem inventoryItem = createInventoryItem(product)
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, product) >> [
                availableItem(inventoryItem, createBinLocation("A1"), 5),
                availableItem(inventoryItem, createBinLocation("A2"), 3),
        ]

        expect:
        assert cycleCountService.getInventoryItemsCount(request) == 2
    }

    void 'getInventoryItemsCount should be null-safe when availability returns nothing for an unstarted count'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        CycleCountRequest request = createRequest(facility: facility, product: product)
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, product) >> null

        expect:
        assert cycleCountService.getInventoryItemsCount(request) == null
    }

    void 'getInventoryItemsCount should only count items of the most recent count when the count is started'() {
        given: 'a count whose initial count had two items but whose recount has one'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: inventoryItem, binLocation: createBinLocation("A1"), countIndex: 0),
                buildItem(facility: facility, inventoryItem: inventoryItem, binLocation: createBinLocation("A2"), countIndex: 0),
                buildItem(facility: facility, inventoryItem: inventoryItem, binLocation: createBinLocation("A1"), countIndex: 1,
                        status: CycleCountItemStatus.INVESTIGATING),
        ])
        CycleCountRequest request = createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        Integer count = cycleCountService.getInventoryItemsCount(request)

        then: 'the recount (count index 1) is the most recent count, and it has a single item'
        assert count == 1

        and: 'availability is never consulted once a count exists'
        0 * cycleCountProductAvailabilityServiceMock.getAvailableItems(*_)
    }

    // ---------------------------------------------------------------------------------------------
    // createRequests / updateRequests
    // ---------------------------------------------------------------------------------------------

    void 'createRequests should persist a request per command with manual-request defaults'() {
        given:
        Location facility = createFacility()
        Product productA = createProduct("AB12")
        Product productB = createProduct("CD34")
        CycleCountRequestBatchCommand command = new CycleCountRequestBatchCommand(requests: [
                new CycleCountRequestCommand(facility: facility, product: productA, blindCount: true),
                new CycleCountRequestCommand(facility: facility, product: productB, blindCount: false),
        ])

        when:
        List<CycleCountRequest> requests = cycleCountService.createRequests(command)

        then:
        assert requests.size() == 2
        assert CycleCountRequest.count() == 2
        assert requests.every { it.status == CycleCountRequestStatus.CREATED }
        assert requests.every { it.requestType == CycleCountRequestType.MANUAL_REQUEST }

        and: 'the blind count flag is taken from each individual command, not shared'
        assert requests[0].blindCount
        assert !requests[1].blindCount
    }

    void 'createRequests should stop at the first invalid request'() {
        given: 'the second request is invalid (no facility)'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        CycleCountRequestBatchCommand command = new CycleCountRequestBatchCommand(requests: [
                new CycleCountRequestCommand(facility: facility, product: product, blindCount: true),
                new CycleCountRequestCommand(facility: null, product: product, blindCount: true),
        ])

        when:
        cycleCountService.createRequests(command)

        then: 'the batch fails before returning anything'
        thrown(ValidationException)
        // The service validates and saves one request at a time, so the valid first request has
        // already been saved when the second one fails. Whether that first save actually reaches
        // the database is up to the surrounding transaction's rollback, which a unit test
        // does not cover.
    }

    void 'updateRequests should apply the #description assignment to the matching field pair'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        CycleCountRequest request = createRequest(facility: facility, product: product)
        Person assignee = createPerson("Alice")
        LocalDate deadline = LocalDate.of(2026, 9, 1)

        CycleCountRequestUpdateCommand update = new CycleCountRequestUpdateCommand(cycleCountRequest: request)
        update.assignments = [(countIndexKey): [assignee: assignee, deadline: deadline]]

        when:
        List<CycleCountRequest> updated = cycleCountService.updateRequests(
                new CycleCountRequestUpdateBulkCommand(commands: [update]))

        then:
        assert updated.size() == 1
        assert updated[0].countAssignee == expectedCount("assignee", assignee)
        assert updated[0].countDeadline == expectedCount("deadline", deadline)
        assert updated[0].recountAssignee == expectedRecount("assignee", assignee)
        assert updated[0].recountDeadline == expectedRecount("deadline", deadline)

        where:
        countIndexKey           | description
        Constants.COUNT_INDEX   | "count"
        Constants.RECOUNT_INDEX | "recount"

        expectedCount = { String field, def value -> countIndexKey == Constants.COUNT_INDEX ? value : null }
        expectedRecount = { String field, def value -> countIndexKey == Constants.RECOUNT_INDEX ? value : null }
    }

    void 'updateRequests should leave assignments untouched when no assignment matches a count index'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        Person existingAssignee = createPerson("Existing")
        CycleCountRequest request = createRequest(facility: facility, product: product, countAssignee: existingAssignee)
        CycleCountRequestUpdateCommand update = new CycleCountRequestUpdateCommand(cycleCountRequest: request)
        update.assignments = [:]

        when:
        cycleCountService.updateRequests(new CycleCountRequestUpdateBulkCommand(commands: [update]))

        then: 'the pre-existing count assignee survives an empty update'
        assert request.countAssignee == existingAssignee
        assert request.recountAssignee == null
    }

    void 'updateRequests should surface a validation failure instead of silently skipping the save'() {
        given: 'a request that can no longer pass validation (it has no facility)'
        Product product = createProduct("AB12")
        CycleCountRequest request = createRequest(facility: null, product: product)
        CycleCountRequestUpdateCommand update = new CycleCountRequestUpdateCommand(cycleCountRequest: request)
        update.assignments = [(Constants.COUNT_INDEX): [assignee: createPerson("Alice"), deadline: LocalDate.of(2026, 9, 1)]]

        when:
        cycleCountService.updateRequests(new CycleCountRequestUpdateBulkCommand(commands: [update]))

        then:
        thrown(ValidationException)
    }

    // ---------------------------------------------------------------------------------------------
    // startCycleCount
    // ---------------------------------------------------------------------------------------------

    void 'startCycleCount should create count items from product availability for a new count'() {
        given:
        Location facility = createFacility()
        Location binA = createBinLocation("A1")
        Location binB = createBinLocation("B1")
        Product product = createProduct("AB12")
        InventoryItem lot1 = createInventoryItem(product, "LOT-1")
        InventoryItem lot2 = createInventoryItem(product, "LOT-2")
        Person assignee = createPerson("Alice")
        CycleCountRequest request = createRequest(facility: facility, product: product, countAssignee: assignee)

        and: 'availability has one item with stock and one with zero quantity on hand'
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, product) >> [
                availableItem(lot1, binA, 7),
                availableItem(lot2, binB, 0),
        ]

        when:
        CycleCountDto dto = cycleCountService.startCycleCount(
                new CycleCountStartCommand(cycleCountRequest: request), facility)

        then: 'a cycle count is persisted and linked 1:1 to its request'
        assert CycleCount.count() == 1
        assert request.cycleCount != null
        assert request.status == CycleCountRequestStatus.IN_PROGRESS

        and: 'every availability entry becomes a ready-to-count initial-count item assigned to the count assignee'
        List<CycleCountItem> items = request.cycleCount.cycleCountItems as List
        assert items.size() == 2
        assert items.every { it.countIndex == 0 }
        assert items.every { it.status == CycleCountItemStatus.READY_TO_COUNT }
        assert items.every { it.assignee == assignee }
        assert items.every { !it.custom }

        and: 'each item carries the quantity on hand and bin location that availability reported'
        assert items.find { it.inventoryItem == lot1 }.quantityOnHand == 7
        assert items.find { it.inventoryItem == lot1 }.location == binA
        assert items.find { it.inventoryItem == lot2 }.quantityOnHand == 0
        assert items.find { it.inventoryItem == lot2 }.location == binB
        assert items.every { it.dateCounted != null }

        and: 'items with stock start uncounted, but zero-stock items are pre-filled with a counted quantity of zero'
        assert items.find { it.inventoryItem == lot1 }.quantityCounted == null
        assert items.find { it.inventoryItem == lot2 }.quantityCounted == 0

        and: 'the returned DTO reflects the persisted state'
        assert dto.id == request.cycleCount.id
        assert dto.cycleCountItems.size() == 2
    }

    void 'startCycleCount should be idempotent and refresh initial-count assignees when the count already exists'() {
        given: 'an in-progress count whose initial count was assigned to Alice, plus a recount item owned by Carol'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        Person alice = createPerson("Alice")
        Person bob = createPerson("Bob")
        Person carol = createPerson("Carol")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, assignee: alice)
        CycleCountItem recountItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("B1"), countIndex: 1, assignee: carol,
                status: CycleCountItemStatus.INVESTIGATING)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, recountItem])

        and: 'the request has since been reassigned to Bob'
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, countAssignee: bob)

        when:
        CycleCountDto dto = cycleCountService.startCycleCount(
                new CycleCountStartCommand(cycleCountRequest: request), facility)

        then: 'no new count and no new items are created'
        assert CycleCount.count() == 1
        assert cycleCount.cycleCountItems.size() == 2

        and: 'availability is not consulted again'
        0 * cycleCountProductAvailabilityServiceMock.getAvailableItems(*_)

        and: 'only the initial-count item is reassigned; the recount item keeps its assignee'
        assert countItem.assignee == bob
        assert recountItem.assignee == carol

        and: 'the existing count is returned, linked to its request'
        assert dto.id == cycleCount.id
        assert dto.requestId == request.id
    }

    void 'startCycleCount should batch-start one count per request'() {
        given:
        Location facility = createFacility()
        Product productA = createProduct("AB12")
        Product productB = createProduct("CD34")
        InventoryItem itemA = createInventoryItem(productA)
        InventoryItem itemB = createInventoryItem(productB)
        CycleCountRequest requestA = createRequest(facility: facility, product: productA)
        CycleCountRequest requestB = createRequest(facility: facility, product: productB)
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, productA) >>
                [availableItem(itemA, createBinLocation("A1"), 1)]
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, productB) >>
                [availableItem(itemB, createBinLocation("B1"), 2)]

        when:
        List<CycleCountDto> dtos = cycleCountService.startCycleCount(new CycleCountStartBatchCommand(
                facility: facility,
                requests: [
                        new CycleCountStartCommand(cycleCountRequest: requestA),
                        new CycleCountStartCommand(cycleCountRequest: requestB),
                ]))

        then:
        assert dtos.size() == 2
        assert CycleCount.count() == 2
        assert requestA.cycleCount != null
        assert requestB.cycleCount != null
        assert dtos*.id.containsAll([requestA.cycleCount.id, requestB.cycleCount.id])
    }

    // ---------------------------------------------------------------------------------------------
    // startRecount
    // ---------------------------------------------------------------------------------------------

    void 'startRecount should create investigating recount items from fresh product availability'() {
        given: 'a counted cycle count with one initial-count item'
        Location facility = createFacility()
        Location binA = createBinLocation("A1")
        Location binB = createBinLocation("B1")
        Product product = createProduct("AB12")
        InventoryItem lot1 = createInventoryItem(product, "LOT-1")
        InventoryItem lot2 = createInventoryItem(product, "LOT-2")
        Person recounter = createPerson("Rita")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: lot1, binLocation: binA,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 10, quantityCounted: 8)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem])
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, recountAssignee: recounter)

        and: 'availability has since gained a second lot that did not exist at count time'
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, product) >> [
                availableItem(lot1, binA, 10),
                availableItem(lot2, binB, 4),
        ]

        when:
        CycleCountDto dto = cycleCountService.startRecount(facility,
                new CycleCountStartRecountCommand(cycleCountRequest: request, countIndex: 1))

        then: 'the recount contains an investigating item per current availability entry, assigned to the recounter'
        List<CycleCountItem> recountItems = cycleCount.cycleCountItems.findAll { it.countIndex == 1 } as List
        assert recountItems.size() == 2
        assert recountItems.every { it.status == CycleCountItemStatus.INVESTIGATING }
        assert recountItems.every { it.assignee == recounter }
        assert recountItems*.inventoryItem.containsAll([lot1, lot2])

        and: 'recount quantities on hand come from the fresh availability, not the initial count'
        assert recountItems.find { it.inventoryItem == lot1 }.quantityOnHand == 10
        assert recountItems.find { it.inventoryItem == lot2 }.quantityOnHand == 4

        and: 'the initial count is untouched'
        assert countItem.countIndex == 0
        assert countItem.quantityCounted == 8

        and:
        assert dto.maxCountIndex == 1
    }

    void 'startRecount should be idempotent and refresh recount assignees when the recount already exists'() {
        given: 'a count that already has recount items assigned to Rita'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        Person rita = createPerson("Rita")
        Person sam = createPerson("Sam")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, assignee: rita)
        CycleCountItem recountItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("B1"), countIndex: 1, assignee: rita,
                status: CycleCountItemStatus.INVESTIGATING)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, recountItem])

        and: 'the request has since been reassigned to Sam'
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, recountAssignee: sam)

        when:
        cycleCountService.startRecount(facility,
                new CycleCountStartRecountCommand(cycleCountRequest: request, countIndex: 1))

        then: 'no new items appear and availability is not consulted'
        assert cycleCount.cycleCountItems.size() == 2
        0 * cycleCountProductAvailabilityServiceMock.getAvailableItems(*_)

        and: 'recount items are reassigned but the initial count keeps its assignee'
        assert recountItem.assignee == sam
        assert countItem.assignee == rita
    }

    void 'startRecount should carry a custom count item forward as a regular recount item'() {
        given: 'an initial count with one availability-backed item and one custom-added item'
        Location facility = createFacility()
        Location binA = createBinLocation("A1")
        Location binB = createBinLocation("B1")
        Product product = createProduct("AB12")
        InventoryItem trackedLot = createInventoryItem(product, "LOT-1")
        InventoryItem foundLot = createInventoryItem(product, "LOT-FOUND")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: trackedLot, binLocation: binA,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityCounted: 10)
        CycleCountItem customItem = buildItem(facility: facility, inventoryItem: foundLot, binLocation: binB,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 0, quantityCounted: 3, custom: true)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, customItem])
        Person recounter = createPerson("Rita")
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, recountAssignee: recounter)

        and: 'availability still only knows about the tracked lot'
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, product) >> [
                availableItem(trackedLot, binA, 10),
        ]

        when:
        cycleCountService.startRecount(facility,
                new CycleCountStartRecountCommand(cycleCountRequest: request, countIndex: 1))

        then: 'the custom item is carried into the recount alongside the availability-backed item'
        List<CycleCountItem> recountItems = cycleCount.cycleCountItems.findAll { it.countIndex == 1 } as List
        assert recountItems.size() == 2
        CycleCountItem carried = recountItems.find { it.inventoryItem == foundLot }
        assert carried != null

        and: 'the carried item starts a fresh investigation: uncounted, keeping the quantity on hand it was given'
        assert carried.quantityCounted == null
        assert carried.quantityOnHand == 0
        assert carried.status == CycleCountItemStatus.INVESTIGATING

        and: 'the carried item is intentionally no longer custom, so it cannot be deleted from the recount'
        assert !carried.custom

        and: 'the recount assignee is only applied to availability-backed items, not carried custom ones'
        assert recountItems.find { it.inventoryItem == trackedLot }.assignee == recounter
        assert carried.assignee == null
    }

    void 'startRecount should not duplicate a custom item that availability already produced for the recount'() {
        given: 'a custom item whose product, bin and lot now ALSO exist in product availability'
        Location facility = createFacility()
        Location binA = createBinLocation("A1")
        Product product = createProduct("AB12")
        InventoryItem foundLot = createInventoryItem(product, "LOT-FOUND")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: foundLot, binLocation: binA,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 0, quantityCounted: 3, custom: true)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem])
        CycleCountRequest request = createRequest(facility: facility, product: product, cycleCount: cycleCount)

        and: 'the custom row has since been recorded as real stock, so availability returns it'
        cycleCountProductAvailabilityServiceMock.getAvailableItems(facility, product) >> [
                availableItem(foundLot, binA, 3),
        ]

        when:
        cycleCountService.startRecount(facility,
                new CycleCountStartRecountCommand(cycleCountRequest: request, countIndex: 1))

        then: 'exactly one recount item exists for that product, bin and lot - not two'
        List<CycleCountItem> recountItems = cycleCount.cycleCountItems.findAll { it.countIndex == 1 } as List
        assert recountItems.size() == 1
        assert recountItems[0].inventoryItem == foundLot
    }

    // ---------------------------------------------------------------------------------------------
    // submitCount
    // ---------------------------------------------------------------------------------------------

    void 'submitCount should fail when quantities are outdated and the caller asked to fail'() {
        given: 'a counting cycle count'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 10)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        and: 'the refresh reports that quantities changed under the count'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh refreshResult =
                new CycleCountProductAvailabilityService.CycleCountItemsForRefresh()
        refreshResult.itemsToUpdate = [(item): 12 as BigDecimal]
        cycleCountProductAvailabilityServiceMock.refreshProductAvailability(cycleCount) >> refreshResult

        when:
        cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: true,
                failOnOutdatedQuantity: true,
                requireRecountOnDiscrepancy: false))

        then:
        IllegalArgumentException e = thrown()
        assert e.message == "Quantity on hand for a cycle count item is no longer up to date"
    }

    void 'submitCount should tolerate outdated quantities when the caller did not ask to fail'() {
        given: 'a counting cycle count'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 10)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        and: 'the refresh reports that quantities changed under the count'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh refreshResult =
                new CycleCountProductAvailabilityService.CycleCountItemsForRefresh()
        refreshResult.itemsToUpdate = [(item): 12 as BigDecimal]
        cycleCountProductAvailabilityServiceMock.refreshProductAvailability(cycleCount) >> refreshResult

        when:
        cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: true,
                failOnOutdatedQuantity: false,
                requireRecountOnDiscrepancy: false))

        then:
        notThrown(IllegalArgumentException)
    }

    void 'submitCount should not refresh quantities unless asked to'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 10)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: false,
                failOnOutdatedQuantity: true,
                requireRecountOnDiscrepancy: false))

        then:
        0 * cycleCountProductAvailabilityServiceMock.refreshProductAvailability(*_)
    }

    void 'submitCount should mark a discrepancy item #expectedItemStatus when requireRecountOnDiscrepancy=#requireRecount'() {
        given: 'a count with one matching item and one discrepancy'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem matchingItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 10)
        CycleCountItem discrepancyItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A2"), countIndex: 0, quantityOnHand: 10, quantityCounted: 7)
        CycleCount cycleCount = createCycleCountWithItems(facility, [matchingItem, discrepancyItem])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        CycleCountDto dto = cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: false,
                failOnOutdatedQuantity: false,
                requireRecountOnDiscrepancy: requireRecount))

        then: 'an accurate item is always approved'
        assert matchingItem.status == CycleCountItemStatus.APPROVED

        and: 'the discrepancy is only held back for recount when the caller requires it'
        assert discrepancyItem.status == expectedItemStatus

        and: 'the count status follows the item statuses'
        assert dto.status == expectedCountStatus.toString()

        where:
        requireRecount | expectedItemStatus              | expectedCountStatus
        true           | CycleCountItemStatus.COUNTED    | CycleCountStatus.COUNTED
        false          | CycleCountItemStatus.APPROVED   | CycleCountStatus.COMPLETED
    }

    void 'submitCount should not create transactions when the count is held for recount'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem discrepancyItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 7)
        CycleCount cycleCount = createCycleCountWithItems(facility, [discrepancyItem])
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, status: CycleCountRequestStatus.IN_PROGRESS)

        when:
        cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: false,
                failOnOutdatedQuantity: false,
                requireRecountOnDiscrepancy: true))

        then: 'no adjustment transactions are committed and the request stays in progress'
        0 * cycleCountTransactionServiceMock.createTransactions(*_)
        assert request.status == CycleCountRequestStatus.IN_PROGRESS
    }

    void 'submitCount should complete the request and commit transactions when every item is resolved'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 10)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, status: CycleCountRequestStatus.IN_PROGRESS)

        when:
        CycleCountDto dto = cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: false,
                failOnOutdatedQuantity: false,
                requireRecountOnDiscrepancy: true))

        then: 'the adjustment pipeline runs once for the counted products with the refresh flag passed through'
        1 * cycleCountTransactionServiceMock.createTransactions(cycleCount, [product], false)

        and:
        assert dto.status == CycleCountStatus.COMPLETED.toString()
        assert request.status == CycleCountRequestStatus.COMPLETED
    }

    void 'submitCount should baseline every counted product even when the refresh removes its items'() {
        // Guards the OBPIH-7525 fix: the list of counted products must be captured BEFORE the
        // refresh runs, because the refresh can drop items whose stock has fallen to zero - and
        // those products still need their baseline transaction.
        given: 'a count over two products'
        Location facility = createFacility()
        Product productKept = createProduct("AB12")
        Product productRemoved = createProduct("CD34")
        InventoryItem keptItem = createInventoryItem(productKept)
        InventoryItem removedItem = createInventoryItem(productRemoved)
        CycleCountItem kept = buildItem(facility: facility, inventoryItem: keptItem,
                binLocation: createBinLocation("A1"), countIndex: 0, quantityOnHand: 10, quantityCounted: 10)
        CycleCountItem removed = buildItem(facility: facility, inventoryItem: removedItem,
                binLocation: createBinLocation("A2"), countIndex: 0, quantityOnHand: 0, quantityCounted: 0)
        CycleCount cycleCount = createCycleCountWithItems(facility, [kept, removed])
        createRequest(facility: facility, product: productKept, cycleCount: cycleCount,
                status: CycleCountRequestStatus.IN_PROGRESS)

        and: 'the refresh drops the zero-stock product from the count'
        cycleCountProductAvailabilityServiceMock.refreshProductAvailability(cycleCount) >> {
            cycleCount.removeFromCycleCountItems(removed)
            return new CycleCountProductAvailabilityService.CycleCountItemsForRefresh()
        }

        when:
        cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: true,
                failOnOutdatedQuantity: false,
                requireRecountOnDiscrepancy: false))

        then: 'the transaction pipeline still receives BOTH products, not just the surviving one'
        1 * cycleCountTransactionServiceMock.createTransactions(cycleCount,
                { List<Product> products ->
                    products.size() == 2 && products.toSet() == [productKept, productRemoved].toSet()
                }, true)
    }

    void 'submitCount should treat a count whose items were all canceled as completed'() {
        // Documents today's behavior: recomputeStatus() treats canceled items as resolved, so a
        // count whose items were ALL canceled comes out COMPLETED, never CANCELED. As a result the
        // cancellation branch of the close-out logic can never run from submitCount, and a fully
        // canceled count still creates inventory transactions for its products.
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem canceledItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, status: CycleCountItemStatus.CANCELED,
                quantityOnHand: 10, quantityCounted: 10)
        CycleCount cycleCount = createCycleCountWithItems(facility, [canceledItem])
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, status: CycleCountRequestStatus.IN_PROGRESS)

        when:
        CycleCountDto dto = cycleCountService.submitCount(new CycleCountSubmitCountCommand(
                cycleCount: cycleCount,
                facility: facility,
                refreshQuantityOnHand: false,
                failOnOutdatedQuantity: false,
                requireRecountOnDiscrepancy: false))

        then:
        assert dto.status == CycleCountStatus.COMPLETED.toString()
        assert request.status == CycleCountRequestStatus.COMPLETED

        and:
        1 * cycleCountTransactionServiceMock.createTransactions(cycleCount, [product], false)
    }

    // ---------------------------------------------------------------------------------------------
    // updateCycleCountItems
    // ---------------------------------------------------------------------------------------------

    void 'updateCycleCountItem should move the item to countIndex #expectedIndex and #expectedStatus when recount=#recount'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, status: CycleCountItemStatus.READY_TO_COUNT,
                quantityOnHand: 10)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item], CycleCountStatus.REQUESTED)
        item.save(validate: false)

        when:
        CycleCountItemDto dto = cycleCountService.updateCycleCountItem(new CycleCountUpdateItemCommand(
                cycleCountItem: item,
                recount: recount,
                quantityCounted: 9))

        then:
        assert item.countIndex == expectedIndex
        assert item.status == expectedStatus
        assert item.quantityCounted == 9
        assert dto.quantityVariance == -1

        and: 'the parent count status is recomputed from its items'
        assert cycleCount.status == expectedCountStatus

        where:
        recount | expectedIndex | expectedStatus                     | expectedCountStatus
        false   | 0             | CycleCountItemStatus.COUNTING      | CycleCountStatus.COUNTING
        true    | 1             | CycleCountItemStatus.INVESTIGATING | CycleCountStatus.INVESTIGATING
    }

    void 'updateCycleCountItem should keep the date counted supplied by the command'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        Date recordedDate = new Date(1200000000000L)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0)
        createCycleCountWithItems(facility, [item])
        item.save(validate: false, flush: true)

        when:
        cycleCountService.updateCycleCountItem(new CycleCountUpdateItemCommand(
                cycleCountItem: item, recount: false, dateCounted: recordedDate))

        then:
        assert item.dateCounted == recordedDate
    }

    void 'updateCycleCountItem should default the date counted to now when the command carries none'() {
        // The update copies every field from the command onto the item, so an update that carries
        // no count date clears the one that was there - and the service then stamps the current
        // time. In other words, keeping an existing date requires sending it back with the update.
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0)
        createCycleCountWithItems(facility, [item])
        item.save(validate: false, flush: true)

        when:
        cycleCountService.updateCycleCountItem(new CycleCountUpdateItemCommand(cycleCountItem: item, recount: false))

        then:
        assert item.dateCounted != null
        assert Math.abs(item.dateCounted.time - new Date().time) < 5000
    }

    void 'updateCycleCountItems should update every item in the batch and preserve order'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem first = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0)
        CycleCountItem second = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A2"), countIndex: 0)
        createCycleCountWithItems(facility, [first, second])
        [first, second]*.save(validate: false, flush: true)

        when:
        List<CycleCountItemDto> dtos = cycleCountService.updateCycleCountItems([
                new CycleCountUpdateItemCommand(cycleCountItem: first, recount: false, quantityCounted: 1),
                new CycleCountUpdateItemCommand(cycleCountItem: second, recount: false, quantityCounted: 2),
        ])

        then:
        assert dtos.size() == 2
        assert dtos[0].id == first.id
        assert dtos[1].id == second.id
        assert first.quantityCounted == 1
        assert second.quantityCounted == 2
    }

    // ---------------------------------------------------------------------------------------------
    // createCycleCountItem
    // ---------------------------------------------------------------------------------------------

    void 'createCycleCountItem should create a custom item with zero quantity on hand'() {
        given: 'a count whose only item is already resolved, so the count reads as completed'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product, "LOT-1")
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: inventoryItem, binLocation: createBinLocation("A1"),
                        countIndex: 0, status: CycleCountItemStatus.APPROVED),
        ], CycleCountStatus.COMPLETED)

        when:
        CycleCountItemDto dto = cycleCountService.createCycleCountItem(new CycleCountItemCommand(
                cycleCount: cycleCount,
                facility: facility,
                inventoryItem: inventoryItem,
                binLocation: createBinLocation("B1"),
                quantityCounted: 5,
                recount: false))

        then: 'the item is custom (user-added), starts from zero stock, and joins the current count'
        assert dto.custom
        assert dto.quantityOnHand == 0
        assert dto.quantityCounted == 5
        assert dto.countIndex == 0
        assert dto.status == CycleCountItemStatus.COUNTING
        assert cycleCount.cycleCountItems.size() == 2

        and: 'adding the item reopens the count - its status is recomputed from the items'
        assert cycleCount.status == CycleCountStatus.COUNTING
    }

    void 'createCycleCountItem should mark recount additions as investigating at count index 1'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product, "LOT-1")
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: inventoryItem, binLocation: createBinLocation("A1"),
                        countIndex: 1, status: CycleCountItemStatus.INVESTIGATING),
        ])

        when:
        CycleCountItemDto dto = cycleCountService.createCycleCountItem(new CycleCountItemCommand(
                cycleCount: cycleCount,
                facility: facility,
                inventoryItem: inventoryItem,
                binLocation: createBinLocation("B1"),
                quantityCounted: 2,
                recount: true))

        then:
        assert dto.countIndex == 1
        assert dto.status == CycleCountItemStatus.INVESTIGATING
    }

    void 'createCycleCountItem should reuse an existing inventory item with the same product and lot'() {
        given: 'an inventory item for the lot already exists in the database'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem persistedLot = createInventoryItem(product, "LOT-1")
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: persistedLot, binLocation: createBinLocation("A1"),
                        countIndex: 0, status: CycleCountItemStatus.COUNTING),
        ])

        and: 'the command carries an unsaved duplicate of that lot (as batch binding produces)'
        InventoryItem unsavedDuplicate = new InventoryItem(product: product, lotNumber: "LOT-1")

        when:
        CycleCountItemDto dto = cycleCountService.createCycleCountItem(new CycleCountItemCommand(
                cycleCount: cycleCount,
                facility: facility,
                inventoryItem: unsavedDuplicate,
                binLocation: createBinLocation("B1"),
                quantityCounted: 1,
                recount: false))

        then: 'no second inventory item is created for the same product and lot'
        assert InventoryItem.count() == 1
        assert dto.inventoryItem.id == persistedLot.id
    }

    void 'createCycleCountItem should persist a brand new inventory item when the lot does not exist yet'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem knownLot = createInventoryItem(product, "LOT-1")
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: knownLot, binLocation: createBinLocation("A1"),
                        countIndex: 0, status: CycleCountItemStatus.COUNTING),
        ])
        InventoryItem newLot = new InventoryItem(product: product, lotNumber: "LOT-NEW")

        when:
        cycleCountService.createCycleCountItem(new CycleCountItemCommand(
                cycleCount: cycleCount,
                facility: facility,
                inventoryItem: newLot,
                binLocation: createBinLocation("B1"),
                quantityCounted: 1,
                recount: false))

        then:
        assert InventoryItem.count() == 2
        assert InventoryItem.findByProductAndLotNumber(product, "LOT-NEW") != null
    }

    void 'createCycleCountItem should reject an invalid discrepancy reason code'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product, "LOT-1")
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: inventoryItem, binLocation: createBinLocation("A1"),
                        countIndex: 0, status: CycleCountItemStatus.COUNTING),
        ])

        when: 'the reason code is not one of the sanctioned cycle count reason codes'
        cycleCountService.createCycleCountItem(new CycleCountItemCommand(
                cycleCount: cycleCount,
                facility: facility,
                inventoryItem: inventoryItem,
                binLocation: createBinLocation("B1"),
                quantityCounted: 1,
                discrepancyReasonCode: ReasonCode.STOCKOUT,
                recount: false))

        then:
        thrown(ValidationException)
    }

    void 'createCycleCountItems should share one new inventory item between batch rows with the same product and lot'() {
        // The service saves a newly created inventory item immediately so that a later row in the
        // same batch can find and reuse it instead of creating a duplicate - this test covers
        // exactly that scenario.
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem knownLot = createInventoryItem(product, "LOT-1")
        CycleCount cycleCount = createCycleCountWithItems(facility, [
                buildItem(facility: facility, inventoryItem: knownLot, binLocation: createBinLocation("A1"),
                        countIndex: 0, status: CycleCountItemStatus.COUNTING),
        ])

        and: 'two batch rows carry separate unsaved instances of the same brand-new lot'
        InventoryItem newLotRowOne = new InventoryItem(product: product, lotNumber: "LOT-NEW")
        InventoryItem newLotRowTwo = new InventoryItem(product: product, lotNumber: "LOT-NEW")

        when:
        List<CycleCountItemDto> dtos = cycleCountService.createCycleCountItems([
                new CycleCountItemCommand(cycleCount: cycleCount, facility: facility,
                        inventoryItem: newLotRowOne, binLocation: createBinLocation("B1"),
                        quantityCounted: 1, recount: false),
                new CycleCountItemCommand(cycleCount: cycleCount, facility: facility,
                        inventoryItem: newLotRowTwo, binLocation: createBinLocation("B2"),
                        quantityCounted: 2, recount: false),
        ])

        then: 'both rows resolve to a single persisted inventory item for the new lot'
        assert dtos.size() == 2
        assert InventoryItem.findAllByProductAndLotNumber(product, "LOT-NEW").size() == 1
        assert dtos[0].inventoryItem.id == dtos[1].inventoryItem.id

        and: 'results come back in batch order'
        assert dtos[0].quantityCounted == 1
        assert dtos[1].quantityCounted == 2
    }

    // ---------------------------------------------------------------------------------------------
    // deleteCycleCountItem
    // ---------------------------------------------------------------------------------------------

    void 'deleteCycleCountItem should delete a custom item'() {
        given: 'a count holding one regular and one custom item'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem regularItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, custom: false)
        CycleCountItem customItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("B1"), countIndex: 0, custom: true)
        createCycleCountWithItems(facility, [regularItem, customItem])

        when:
        cycleCountService.deleteCycleCountItem(customItem.id)

        then: 'the custom item is gone and the regular one survives'
        assert CycleCountItem.get(customItem.id) == null
        assert CycleCountItem.get(regularItem.id) != null
    }

    void 'deleteCycleCountItem should refuse to delete a system-generated item'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem systemItem = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, custom: false)
        createCycleCountWithItems(facility, [systemItem])

        when:
        cycleCountService.deleteCycleCountItem(systemItem.id)

        then:
        IllegalArgumentException e = thrown()
        assert e.message == "Only custom cycle count items can be deleted"
        assert CycleCountItem.get(systemItem.id) != null
    }

    void 'deleteCycleCountItem should silently ignore an unknown item id'() {
        // Documents today's behavior: deleting an item that does not exist is treated as already
        // done rather than as an error.
        when:
        cycleCountService.deleteCycleCountItem("no-such-id")

        then:
        noExceptionThrown()
    }

    // ---------------------------------------------------------------------------------------------
    // deleteCycleCountRequests
    // ---------------------------------------------------------------------------------------------

    void 'deleteCycleCountRequests should delete the request and cascade to its count and items'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        CycleCountRequest request = createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        cycleCountService.deleteCycleCountRequests([request.id])

        then:
        assert CycleCountRequest.get(request.id) == null
        assert CycleCount.get(cycleCount.id) == null
        assert CycleCountItem.get(item.id) == null
    }

    void 'deleteCycleCountRequests should delete a request that has no count yet'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        CycleCountRequest request = createRequest(facility: facility, product: product)

        when:
        cycleCountService.deleteCycleCountRequests([request.id])

        then:
        assert CycleCountRequest.get(request.id) == null
    }

    void 'deleteCycleCountRequests should fail fast on an unknown request id'() {
        when:
        cycleCountService.deleteCycleCountRequests(["no-such-id"])

        then:
        thrown(ObjectNotFoundException)
    }

    // ---------------------------------------------------------------------------------------------
    // deleteCycleCountWithAssociations
    // ---------------------------------------------------------------------------------------------

    void 'deleteCycleCountWithAssociations should remove the count, its items, its request and its transactions'() {
        given: 'a completed count that produced transactions grouped under a transaction source'
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, status: CycleCountItemStatus.APPROVED)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item], CycleCountStatus.COMPLETED)
        CycleCountRequest request = createRequest(
                facility: facility, product: product, cycleCount: cycleCount, status: CycleCountRequestStatus.COMPLETED)

        TransactionSource transactionSource = new TransactionSource(cycleCount: cycleCount)
        transactionSource.save(validate: false, flush: true)
        Transaction baseline = new Transaction(transactionSource: transactionSource)
        baseline.save(validate: false, flush: true)
        Transaction adjustment = new Transaction(transactionSource: transactionSource)
        adjustment.save(validate: false, flush: true)

        when:
        cycleCountService.deleteCycleCountWithAssociations(cycleCount.id)

        then: 'nothing that pointed at the count survives'
        assert Transaction.get(baseline.id) == null
        assert Transaction.get(adjustment.id) == null
        assert TransactionSource.get(transactionSource.id) == null
        assert CycleCountItem.get(item.id) == null
        assert CycleCount.get(cycleCount.id) == null
        assert CycleCountRequest.get(request.id) == null
    }

    void 'deleteCycleCountWithAssociations should fail fast on an unknown cycle count id'() {
        when:
        cycleCountService.deleteCycleCountWithAssociations("no-such-id")

        then:
        thrown(ObjectNotFoundException)
    }

    // ---------------------------------------------------------------------------------------------
    // refreshCycleCount
    // ---------------------------------------------------------------------------------------------

    void 'refreshCycleCount should fail fast on an unknown cycle count id'() {
        when:
        cycleCountService.refreshCycleCount("no-such-id", false, 0)

        then:
        thrown(ObjectNotFoundException)
    }

    void 'refreshCycleCount should refuse to refresh a count in status #status'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0, status: CycleCountItemStatus.APPROVED)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item], status)
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        cycleCountService.refreshCycleCount(cycleCount.id, false, 0)

        then:
        IllegalArgumentException e = thrown()
        assert e.message == "Cycle count cannot be refreshed when in state: ${status}"

        and:
        0 * cycleCountProductAvailabilityServiceMock.refreshProductAvailability(*_)

        where:
        status << [CycleCountStatus.COMPLETED, CycleCountStatus.CANCELED, CycleCountStatus.READY_TO_REVIEW]
    }

    void 'refreshCycleCount should delegate to the availability refresh for an active count in status #status'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem inventoryItem = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: inventoryItem,
                binLocation: createBinLocation("A1"), countIndex: 0)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item], status)
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        CycleCountDto dto = cycleCountService.refreshCycleCount(cycleCount.id, true, 1)

        then: 'the refresh is delegated with the exact flags the caller provided'
        1 * cycleCountProductAvailabilityServiceMock.refreshProductAvailability(cycleCount, true, 1)

        and:
        assert dto.id == cycleCount.id

        where:
        status << [CycleCountStatus.REQUESTED, CycleCountStatus.COUNTING,
                   CycleCountStatus.COUNTED, CycleCountStatus.INVESTIGATING]
    }

    // ---------------------------------------------------------------------------------------------
    // Export: CSV of cycle count candidates
    // ---------------------------------------------------------------------------------------------

    void 'getCycleCountCsv should render one row per candidate after the header'() {
        given:
        Category category = new Category(name: "Medicines")
        category.save(validate: false)
        Product product = createProduct("AB12", "Ibuprofen 200mg")
        product.category = category
        CycleCountCandidate candidate = new CycleCountCandidate(
                product: product,
                abcClass: "A",
                internalLocations: "A1,B2",
                dateLastCount: new Date(1200000000000L),
                quantityOnHand: 42,
        )

        when:
        String csv = cycleCountService.getCycleCountCsv([candidate]).out.toString()
        List<String> lines = csv.readLines()

        then:
        assert lines.size() == 2
        assert lines[0] == 'Code,Product,Product Family,Category,Formularies,ABC Classification,Bin Location,Tag,Last Counted,QoH'

        and: 'the full data row is pinned, so a swapped or dropped column cannot slip through'
        // Note: values containing commas are escaped twice - once by the explicit escape call in the
        // service, once by the CSV printer itself - hence the doubled quotes around the bin list.
        assert lines[1] == 'AB12,Ibuprofen 200mg,,Medicines,,A,"""A1,B2""",,10/01/2008,42'
    }

    void 'getCycleCountCsv should render safe defaults for a sparse candidate'() {
        given: 'a candidate with no product details, no locations and no count history'
        CycleCountCandidate candidate = new CycleCountCandidate(product: createProduct("AB12"))

        when:
        String csv = cycleCountService.getCycleCountCsv([candidate]).out.toString()
        List<String> lines = csv.readLines()

        then: 'the row renders empty strings for missing text and zero for missing quantity'
        assert lines.size() == 2
        assert lines[1] == 'AB12,Product AB12,,,,,,,,0'
    }

    void 'getCycleCountCsv should render only the header when there are no candidates'() {
        expect:
        assert cycleCountService.getCycleCountCsv(candidates).out.toString().readLines().size() == 1

        where:
        candidates << [[], null]
    }

    // ---------------------------------------------------------------------------------------------
    // Export: count form XLS
    // ---------------------------------------------------------------------------------------------

    void 'getCountFormXls should render one row per cycle count item with formatted fields'() {
        given:
        Location facility = createFacility()
        Location bin = createBinLocation("A1")
        Product product = createProduct("AB12", "Ibuprofen 200mg")
        InventoryItem lot = createInventoryItem(product, "LOT-1", Date.parse("yyyy-MM-dd", "2027-01-31"))
        Person assignee = createPerson("Alice")
        CycleCountItem item = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 0, quantityCounted: 0, assignee: assignee,
                dateCounted: Date.parse("yyyy-MM-dd", "2026-08-15"))
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        List<Map> rows = cycleCountService.getCountFormXls([CycleCountDto.toDto(cycleCount)])

        then:
        assert rows.size() == 1
        assert rows[0]["Product Code"] == "AB12"
        assert rows[0]["Product Name"] == "Ibuprofen 200mg"
        assert rows[0]["Lot Number"] == "LOT-1"
        assert rows[0]["Expiration Date"] == "01/31/2027"
        assert rows[0]["Bin Location"] == "A1"
        assert rows[0]["User Counted"] == assignee.name

        and: 'a counted quantity of zero renders as 0, not as blank - blank means "not counted yet"'
        assert rows[0]["Quantity Counted"] == 0
    }

    void 'getCountFormXls should render blanks for uncounted items and missing optional fields'() {
        given:
        Location facility = createFacility()
        Product product = createProduct("AB12")
        InventoryItem noLot = createInventoryItem(product)
        CycleCountItem item = buildItem(facility: facility, inventoryItem: noLot, binLocation: null,
                countIndex: 0, quantityCounted: null)
        CycleCount cycleCount = createCycleCountWithItems(facility, [item])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        List<Map> rows = cycleCountService.getCountFormXls([CycleCountDto.toDto(cycleCount)])

        then:
        assert rows[0]["Quantity Counted"] == ""
        assert rows[0]["Expiration Date"] == ""
        assert rows[0]["Bin Location"] == null
        assert rows[0]["User Counted"] == ""
        assert rows[0]["Date Counted"] == ""
    }

    // ---------------------------------------------------------------------------------------------
    // Export: recount form XLS
    // ---------------------------------------------------------------------------------------------

    void 'getRecountFormXls should merge the count and recount of the same lot and bin into one row'() {
        given: 'an item counted at index 0 and recounted at index 1'
        Location facility = createFacility()
        Location bin = createBinLocation("A1")
        Product product = createProduct("AB12", "Ibuprofen 200mg")
        InventoryItem lot = createInventoryItem(product, "LOT-1")
        Person counter = createPerson("Alice")
        Person recounter = createPerson("Rita")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 10, quantityCounted: 7,
                assignee: counter, dateCounted: Date.parse("yyyy-MM-dd", "2026-08-15"))
        CycleCountItem recountItem = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 1, status: CycleCountItemStatus.INVESTIGATING, quantityOnHand: 10, quantityCounted: 9,
                assignee: recounter, dateCounted: Date.parse("yyyy-MM-dd", "2026-08-16"))
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, recountItem])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        List<Map> rows = cycleCountService.getRecountFormXls([CycleCountDto.toDto(cycleCount)])

        then: 'one merged row carries the count columns and the recount columns side by side'
        assert rows.size() == 1
        assert rows[0]["Product Code"] == "AB12"
        assert rows[0]["Lot Number"] == "LOT-1"
        assert rows[0]["Quantity Counted"] == 7
        assert rows[0]["Difference"] == -3
        assert rows[0]["Date Counted"] == "08/15/2026"
        assert rows[0]["Quantity Recounted"] == 9
        assert rows[0]["Date Recounted"] == "08/16/2026"

        and: 'the row is identified by the recount item, which drives any subsequent import'
        assert rows[0]["Cycle count item id"] == recountItem.id
    }

    void 'getRecountFormXls should render a zero count difference as blank'() {
        // Documents today's behavior: the "Difference" column treats a value of 0 like a missing
        // value, so a difference of exactly zero shows up blank - the same as a line that was
        // never counted.
        given:
        Location facility = createFacility()
        Location bin = createBinLocation("A1")
        Product product = createProduct("AB12")
        InventoryItem lot = createInventoryItem(product, "LOT-1")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 5, quantityCounted: 5)
        CycleCountItem recountItem = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 1, status: CycleCountItemStatus.INVESTIGATING, quantityOnHand: 5, quantityCounted: 5)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, recountItem])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        List<Map> rows = cycleCountService.getRecountFormXls([CycleCountDto.toDto(cycleCount)])

        then:
        assert rows[0]["Quantity Counted"] == 5
        assert rows[0]["Difference"] == ""
    }

    void 'getRecountFormXls currently fails when an initial count item has no recount partner'() {
        // Documents today's behavior: when a lot that was counted disappears from stock before the
        // recount starts, the export is left with a first-count row that has no recount partner,
        // and building the merged row fails on the missing recount side. This is an edge case the
        // export does not survive today.
        given: 'an initial count over two lots but a recount over only one of them'
        Location facility = createFacility()
        Location bin = createBinLocation("A1")
        Product product = createProduct("AB12")
        InventoryItem keptLot = createInventoryItem(product, "LOT-1")
        InventoryItem goneLot = createInventoryItem(product, "LOT-GONE")
        CycleCountItem keptCount = buildItem(facility: facility, inventoryItem: keptLot, binLocation: bin,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 5, quantityCounted: 5)
        CycleCountItem goneCount = buildItem(facility: facility, inventoryItem: goneLot, binLocation: bin,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 2, quantityCounted: 2)
        CycleCountItem keptRecount = buildItem(facility: facility, inventoryItem: keptLot, binLocation: bin,
                countIndex: 1, status: CycleCountItemStatus.INVESTIGATING, quantityOnHand: 5, quantityCounted: 5)
        CycleCount cycleCount = createCycleCountWithItems(facility, [keptCount, goneCount, keptRecount])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        cycleCountService.getRecountFormXls([CycleCountDto.toDto(cycleCount)])

        then:
        thrown(NullPointerException)
    }

    void 'getRecountFormXls should list a custom recount item as its own row with empty count columns'() {
        given: 'a recount where the user custom-added a lot that was never part of the initial count'
        Location facility = createFacility()
        Location bin = createBinLocation("A1")
        Product product = createProduct("AB12")
        InventoryItem knownLot = createInventoryItem(product, "LOT-1")
        InventoryItem foundLot = createInventoryItem(product, "LOT-FOUND")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: knownLot, binLocation: bin,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 10, quantityCounted: 10)
        CycleCountItem recountItem = buildItem(facility: facility, inventoryItem: knownLot, binLocation: bin,
                countIndex: 1, status: CycleCountItemStatus.INVESTIGATING, quantityOnHand: 10, quantityCounted: 10)
        CycleCountItem customRecountItem = buildItem(facility: facility, inventoryItem: foundLot, binLocation: bin,
                countIndex: 1, status: CycleCountItemStatus.INVESTIGATING, quantityOnHand: 0, quantityCounted: 4,
                custom: true)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, recountItem, customRecountItem])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        List<Map> rows = cycleCountService.getRecountFormXls([CycleCountDto.toDto(cycleCount)])

        then: 'the custom find is its own row, with recount data but no count-side data'
        assert rows.size() == 2
        Map customRow = rows.find { it["Lot Number"] == "LOT-FOUND" }
        assert customRow != null
        assert customRow["Quantity Recounted"] == 4
        assert customRow["Quantity Counted"] == ""
        assert customRow["Counted by"] == ""
        assert customRow["Date Counted"] == ""
    }

    void 'getRecountFormXls should keep zero recounted quantities visible'() {
        // A recounted quantity of 0 is a real answer ("there is nothing on the shelf") and must not
        // render as blank, which would read as "not recounted yet".
        given:
        Location facility = createFacility()
        Location bin = createBinLocation("A1")
        Product product = createProduct("AB12")
        InventoryItem lot = createInventoryItem(product, "LOT-1")
        CycleCountItem countItem = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 0, status: CycleCountItemStatus.COUNTED, quantityOnHand: 3, quantityCounted: 0)
        CycleCountItem recountItem = buildItem(facility: facility, inventoryItem: lot, binLocation: bin,
                countIndex: 1, status: CycleCountItemStatus.INVESTIGATING, quantityOnHand: 3, quantityCounted: 0)
        CycleCount cycleCount = createCycleCountWithItems(facility, [countItem, recountItem])
        createRequest(facility: facility, product: product, cycleCount: cycleCount)

        when:
        List<Map> rows = cycleCountService.getRecountFormXls([CycleCountDto.toDto(cycleCount)])

        then:
        assert rows[0]["Quantity Counted"] == 0
        assert rows[0]["Quantity Recounted"] == 0
    }
}
