// Companion spec: extra features for upstream's RequisitionSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.requisition

import org.pih.warehouse.requisition.*
import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.requisition.RequisitionType

@Unroll
class RequisitionMoreSpec extends Specification implements DomainUnitTest<Requisition> {

    private static final TimeZone ZONE = TimeZone.getTimeZone('Asia/Kolkata')

    TimeZone originalTimeZone
    Locale originalLocale

    Object originalAnonymize

    void setup() {
        originalAnonymize = config.openboxes.anonymize.enabled
        config.openboxes.anonymize.enabled = false
        originalTimeZone = TimeZone.default
        originalLocale = Locale.default
        TimeZone.default = ZONE
        Locale.default = Locale.US
    }

    void cleanup() {
        config.openboxes.anonymize.enabled = originalAnonymize
        TimeZone.default = originalTimeZone
        Locale.default = originalLocale
    }

    void 'validate should reject a requisition without #field'() {
        given:
        Location location = new Location()
        Requisition requisition = new Requisition(
                name: 'testRequisition',
                origin: location,
                destination: location,
                requestedBy: new Person(),
        )
        requisition[field] = null

        expect:
        !requisition.validate()
        requisition.errors.getFieldError(field).code == 'nullable'

        where:
        field << ['name', 'origin', 'destination', 'requestedBy', 'dateRequested', 'requestedDeliveryDate']
    }

    void 'requisition item partitions should separate original, initial, additional, complete and incomplete items'() {
        given:
        RequisitionItem approved = item('approved', RequisitionItemType.ORIGINAL)
        approved.quantity = 10
        approved.quantityApproved = 10
        RequisitionItem pending = item('pending', RequisitionItemType.ORIGINAL)
        pending.quantity = 10
        RequisitionItem canceled = item('canceled', RequisitionItemType.ORIGINAL)
        canceled.quantity = 10
        canceled.quantityCanceled = 4
        RequisitionItem modification = item('modification', RequisitionItemType.QUANTITY_CHANGE)
        modification.quantity = 6
        modification.parentRequisitionItem = canceled
        RequisitionItem addition = item('addition', RequisitionItemType.ADDITION)
        addition.quantity = 1
        domain.requisitionItems = [approved, pending, canceled, modification, addition] as Set

        expect:
        domain.requisitionItemCount == 3
        names(domain.originalRequisitionItems) == ['approved', 'canceled', 'pending']
        names(domain.initialRequisitionItems) == ['addition', 'approved', 'canceled', 'pending']
        names(domain.additionalRequisitionItems) == ['modification']
        names(domain.completeRequisitionItems) == ['approved']
        names(domain.incompleteRequisitionItems) == ['addition', 'canceled', 'pending']
        domain.calculatePercentageCompleted() == 25
    }

    void 'calculatePercentageCompleted should be zero for a requisition without items'() {
        expect:
        domain.calculatePercentageCompleted() == 0
        domain.requisitionItemCount == null
    }

    void 'status predicates should be open #open, pending #pending and requested #requested for status #status'() {
        given:
        domain.status = status

        expect:
        domain.isOpen() == open
        domain.isPending() == pending
        domain.isRequested() == requested

        where:
        status                             || open  | pending | requested
        RequisitionStatus.CREATED          || true  | true    | false
        RequisitionStatus.EDITING          || true  | true    | false
        RequisitionStatus.VERIFYING        || false | true    | true
        RequisitionStatus.PICKING          || false | true    | true
        RequisitionStatus.PENDING          || false | true    | true
        RequisitionStatus.PICKED           || false | false   | false
        RequisitionStatus.CHECKING         || false | false   | false
        RequisitionStatus.ISSUED           || false | false   | true
        RequisitionStatus.RECEIVED         || false | false   | true
        RequisitionStatus.PENDING_APPROVAL || false | false   | false
        RequisitionStatus.CANCELED         || false | false   | false
        null                               || false | false   | false
    }

    void 'isWardRequisition should be #ward for type #type'() {
        given:
        domain.type = type

        expect:
        domain.isWardRequisition() == ward

        where:
        type                        || ward
        RequisitionType.NON_STOCK   || true
        RequisitionType.STOCK       || true
        RequisitionType.ADHOC       || true
        RequisitionType.DEFAULT     || false
        null                        || false
    }

    void 'isElectronicType should be #electronic for source type #sourceType'() {
        given:
        domain.sourceType = sourceType

        expect:
        domain.isElectronicType() == electronic

        where:
        sourceType                        || electronic
        RequisitionSourceType.ELECTRONIC  || true
        RequisitionSourceType.PAPER       || false
        null                              || false
    }

    void 'getRecentComment should return the newest comment'() {
        given:
        domain.comments = [
                comment('middle', 2000L),
                comment('newest', 3000L),
                comment('oldest', 1000L),
        ] as Set

        expect:
        domain.recentComment.comment == 'newest'
    }

    void 'getRecentComment should return null without comments'() {
        expect:
        domain.recentComment == null
    }

    void 'getMostRecentEvent should return the event with the latest event date'() {
        given:
        domain.events = [
                new Event(eventDate: new Date(2000L)),
                new Event(eventDate: new Date(3000L)),
                new Event(eventDate: new Date(1000L)),
        ] as Set

        expect:
        domain.mostRecentEvent.eventDate == new Date(3000L)
    }

    void 'getMostRecentEvent should return null without events'() {
        expect:
        domain.mostRecentEvent == null
    }

    void 'getShipment should return null without shipments and the only shipment otherwise'() {
        given:
        Shipment shipment = new Shipment(name: 'only')

        expect:
        domain.shipment == null

        when:
        domain.shipments = [shipment] as Set

        then:
        domain.shipment.is(shipment)
    }

    void 'getShipment should throw when the requisition has more than one shipment'() {
        given:
        domain.requestNumber = 'R-100'
        domain.shipments = [new Shipment(name: 'first'), new Shipment(name: 'second')] as Set

        when:
        domain.shipment

        then:
        IllegalStateException exception = thrown()
        exception.message == 'There are too many shipments associated with requisition R-100'
    }

    void 'getTotalCost should sum quantity times unit price for the priced items'() {
        given:
        domain.requisitionItems = [
                new RequisitionItem(quantity: 4, product: new Product(pricePerUnit: 2.5)),
                new RequisitionItem(quantity: 3, product: new Product(pricePerUnit: 1.0)),
                new RequisitionItem(quantity: 100, product: new Product(pricePerUnit: null)),
        ] as Set

        expect:
        domain.totalCost == 13.0
    }

    void 'getTotalCost should be zero when no item has a unit price'() {
        given:
        domain.requisitionItems = [new RequisitionItem(quantity: 4, product: new Product())] as Set

        expect:
        domain.totalCost == 0
    }

    void 'getQuantityByProduct should sum the quantities requested for the product'() {
        given:
        Product gauze = product('p1', 'Gauze')
        Product tape = product('p2', 'Tape')
        domain.requisitionItems = [
                new RequisitionItem(product: gauze, quantity: 4),
                new RequisitionItem(product: tape, quantity: 7),
                new RequisitionItem(product: gauze, quantity: 6),
        ] as Set

        expect:
        domain.getQuantityByProduct(gauze) == 10
        domain.getQuantityByProduct(tape) == 7
        domain.getQuantityByProduct(product('p3', 'Other')) == 0
    }

    void 'getSortedStocklistItems should sort a stock list by category, product name and order index'() {
        given:
        Category drugs = new Category(name: 'Drugs')
        Category supplies = new Category(name: 'Supplies')
        domain.isTemplate = true
        domain.requisitionItems = [
                stocklistItem('tape', supplies, 'Tape', 0),
                stocklistItem('gauze-2', supplies, 'Gauze', 2),
                stocklistItem('gauze-1', supplies, 'Gauze', 1),
                stocklistItem('advil', drugs, 'Advil', 5),
        ] as Set

        expect:
        domain.sortedStocklistItems*.comment == ['advil', 'gauze-1', 'gauze-2', 'tape']
        domain.requisitionItemsByCategory*.comment == ['advil', 'gauze-1', 'gauze-2', 'tape']
    }

    void 'getSortedStocklistItems should only be used with a stock list'() {
        given:
        domain.isTemplate = false

        when:
        domain.sortedStocklistItems

        then:
        thrown(IllegalStateException)
    }

    void 'getRequisitionItemsByDateCreated and getRequisitionItemsByOrderIndex should sort the items'() {
        given:
        RequisitionItem first = new RequisitionItem(comment: 'first', orderIndex: 2)
        first.dateCreated = new Date(1000L)
        RequisitionItem second = new RequisitionItem(comment: 'second', orderIndex: 0)
        second.dateCreated = new Date(2000L)
        RequisitionItem third = new RequisitionItem(comment: 'third', orderIndex: 1)
        third.dateCreated = new Date(3000L)
        domain.requisitionItems = [third, first, second] as Set

        expect:
        domain.requisitionItemsByDateCreated*.comment == ['first', 'second', 'third']
        domain.requisitionItemsByOrderIndex*.comment == ['second', 'third', 'first']
    }

    void 'shouldSendApprovalNotification should be #expected for status #status when the destination supports #activities'() {
        given:
        Location destination = new Location(name: 'Ward')
        destination.supportedActivities = activities*.id as Set
        domain.destination = destination
        domain.status = status

        expect:
        domain.shouldSendApprovalNotification() == expected

        where:
        status                             | activities                                                  || expected
        RequisitionStatus.PENDING_APPROVAL | [ActivityCode.ENABLE_FULFILLER_APPROVAL_NOTIFICATIONS]     || true
        RequisitionStatus.PENDING_APPROVAL | [ActivityCode.ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS]     || false
        RequisitionStatus.APPROVED         | [ActivityCode.ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS]     || true
        RequisitionStatus.REJECTED         | [ActivityCode.ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS]     || true
        RequisitionStatus.ISSUED           | [ActivityCode.ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS]     || true
        RequisitionStatus.APPROVED         | [ActivityCode.ENABLE_FULFILLER_APPROVAL_NOTIFICATIONS]     || false
        RequisitionStatus.PICKING          | [ActivityCode.ENABLE_FULFILLER_APPROVAL_NOTIFICATIONS,
                                              ActivityCode.ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS]     || false
    }

    void 'toJson should describe the requisition with dates formatted in the default time zone'() {
        given:
        Person requestor = new Person(firstName: 'Ann', lastName: 'Lee')
        requestor.id = 'person-1'
        Location origin = new Location(name: 'Depot')
        origin.id = 'origin-1'
        Location destination = new Location(name: 'Ward')
        destination.id = 'destination-1'
        Requisition template = new Requisition(name: 'Stock list')
        template.id = 'template-1'
        RequisitionItem second = new RequisitionItem(orderIndex: 2, quantity: 1)
        second.id = 'item-2'
        RequisitionItem first = new RequisitionItem(orderIndex: 1, quantity: 1)
        first.id = 'item-1'
        // OBAF: RequisitionItem.getStatus() checks isReduced()/isIncreased() first, which read requisition.status,
        // so each item needs its requisition
        [first, second].each { it.requisition = domain }

        domain.id = 'req-1'
        domain.name = 'Ward request'
        domain.description = 'weekly'
        domain.requestedBy = requestor
        domain.origin = origin
        domain.destination = destination
        domain.status = RequisitionStatus.PICKING
        domain.type = RequisitionType.STOCK
        domain.sourceType = RequisitionSourceType.ELECTRONIC
        domain.recipientProgram = 'Malaria'
        domain.requisitionTemplate = template
        domain.requisitionItems = [second, first] as Set
        domain.dateRequested = zoned(2024, Calendar.MARCH, 5, 23, 30)
        domain.requestedDeliveryDate = zoned(2024, Calendar.MARCH, 6, 9, 15)
        domain.lastUpdated = zoned(2024, Calendar.MARCH, 7, 14, 5)

        when:
        Map json = domain.toJson()

        then:
        json.id == 'req-1'
        json.name == 'Ward request'
        json.description == 'weekly'
        json.requestedById == 'person-1'
        json.requestedByName == 'Ann Lee'
        json.dateRequested == '03/05/2024'
        json.requestedDeliveryDate == '03/06/2024 09:15 +05:30'
        json.lastUpdated == '07/Mar/2024 02:05 PM'
        json.status == 'PICKING'
        json.type == 'STOCK'
        json.sourceType == 'ELECTRONIC'
        json.originId == 'origin-1'
        json.originName == 'Depot'
        json.destinationId == 'destination-1'
        json.destinationName == 'Ward'
        json.recipientProgram == 'Malaria'
        json.requisitionTemplate.id == 'template-1'
        json.requisitionTemplate.name == 'Stock list'
        json.requisitionItems*.id == ['item-1', 'item-2']
    }

    void 'toJson should leave optional fields empty'() {
        given:
        domain.dateRequested = zoned(2024, Calendar.JANUARY, 1, 0, 0)
        domain.requestedDeliveryDate = zoned(2024, Calendar.JANUARY, 2, 0, 0)

        when:
        Map json = domain.toJson()

        then:
        json.requestedById == null
        json.lastUpdated == null
        json.status == null
        json.type == null
        json.sourceType == null
        json.originName == null
        json.requisitionTemplate == null
        json.requisitionItems == null
    }

    void 'toStocklistJson should describe the stock list'() {
        given:
        domain.id = 'stocklist-1'
        domain.name = 'Ward stock'
        domain.origin = new Location(name: 'Depot')
        domain.destination = new Location(name: 'Ward')
        domain.requestedBy = new Person(firstName: 'Ann', lastName: 'Lee')
        domain.createdBy = new User(firstName: 'Bob', lastName: 'Ray')
        domain.updatedBy = new User(firstName: 'Cy', lastName: 'Do')
        domain.dateCreated = zoned(2024, Calendar.FEBRUARY, 3, 10, 0)
        domain.lastUpdated = zoned(2024, Calendar.NOVEMBER, 21, 10, 0)
        domain.isPublished = true
        domain.requisitionItems = [
                item('a', RequisitionItemType.ORIGINAL),
                item('b', RequisitionItemType.ORIGINAL),
                item('c', RequisitionItemType.SUBSTITUTION),
        ] as Set

        expect:
        domain.toStocklistJson() == [
                id                  : 'stocklist-1',
                name                : 'Ward stock',
                origin              : 'Depot',
                destination         : 'Ward',
                requisitionItemCount: 2,
                requestedBy         : 'Ann Lee',
                createdBy           : 'Bob Ray',
                updatedBy           : 'Cy Do',
                dateCreated         : 'Feb 03, 2024',
                lastUpdated         : 'Nov 21, 2024',
                isPublished         : true,
                // OBAF: the stock list json also carries the allocation settings OBAF added to Requisition
                autoAllocationRequested  : null,
                partialAllocationAllowed : null,
                partialIssuanceAllowed   : null,
                allocationSourceStrategy : null,
                autoIssuanceRequested    : null,
                negativeAllocationAllowed: false,
        ]
    }

    void 'toString should return the id'() {
        given:
        domain.id = 'req-42'

        expect:
        domain.toString() == 'req-42'
    }

    private static Date zoned(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance(ZONE)
        calendar.clear()
        calendar.set(year, month, day, hour, minute, 0)
        return calendar.time
    }

    private static RequisitionItem item(String name, RequisitionItemType type) {
        return new RequisitionItem(comment: name, requisitionItemType: type)
    }

    private static List<String> names(Collection<RequisitionItem> items) {
        return items*.comment.sort()
    }

    private static Comment comment(String text, long createdAt) {
        Comment comment = new Comment(comment: text)
        comment.dateCreated = new Date(createdAt)
        return comment
    }

    private static Product product(String id, String name) {
        Product product = new Product(name: name)
        product.id = id
        return product
    }

    private static RequisitionItem stocklistItem(String name, Category category, String productName, Integer orderIndex) {
        Product product = new Product(name: productName, category: category)
        return new RequisitionItem(comment: name, product: product, orderIndex: orderIndex)
    }
}
