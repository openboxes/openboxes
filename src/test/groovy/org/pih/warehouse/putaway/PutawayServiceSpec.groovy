package org.pih.warehouse.putaway

import grails.testing.gorm.DataTest
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class PutawayServiceSpec extends Specification implements DataTest {

    PutawayService service

    Location facility
    Location receivingBin
    Location otherBin
    Product product
    InventoryItem inventoryItem

    void setupSpec() {
        mockDomains(Category, Location, Product, InventoryItem, Order, OrderItem, PutawayTask)
    }

    void setup() {
        service = new PutawayService()

        Category category = new Category(name: "Medicines")
        category.id = "category-1"

        facility = new Location(name: "Facility")
        facility.id = "facility-1"

        receivingBin = new Location(name: "Receiving")
        receivingBin.id = "bin-receiving"

        otherBin = new Location(name: "Sortation")
        otherBin.id = "bin-other"

        product = new Product(productCode: "AB12", name: "Ibuprofen 200mg", category: category)
        product.id = "product-1"

        inventoryItem = new InventoryItem(product: product, lotNumber: "LOT-1")
        inventoryItem.id = "item-1"
    }

    void 'mergeCandidatesWithOpenTasks should exclude a candidate covered by a #status putaway task'() {
        given:
        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask openTask = buildTask(receivingBin, inventoryItem, product, 30.0G, status)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [openTask])

        then:
        result.size() == 1
        result[0].putawayStatus != PutawayStatus.READY

        where:
        status << [PutawayTaskStatus.PENDING, PutawayTaskStatus.STARTED, PutawayTaskStatus.IN_PROGRESS]
    }

    void 'a STARTED task should not leave the item available as a putaway candidate'() {
        given: 'stock is still sitting in the receiving bin because start() transfers nothing'
        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask startedTask = buildTask(receivingBin, inventoryItem, product, 30.0G, PutawayTaskStatus.STARTED)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [startedTask])

        then: 'the READY row is gone and only the in-flight task remains'
        result.size() == 1
        result[0].putawayStatus == PutawayStatus.IN_PROGRESS
        result[0].quantity == 30.0G
    }

    void 'only PENDING, STARTED and IN_PROGRESS should count as open putaway task statuses'() {
        expect:
        PutawayTaskStatus.toSet(StatusCategory.OPEN) as Set ==
                [PutawayTaskStatus.PENDING, PutawayTaskStatus.STARTED, PutawayTaskStatus.IN_PROGRESS] as Set

        and: 'closed statuses are excluded, so completed and canceled work never suppresses a candidate'
        !PutawayTaskStatus.toSet(StatusCategory.OPEN).contains(PutawayTaskStatus.COMPLETED)
        !PutawayTaskStatus.toSet(StatusCategory.OPEN).contains(PutawayTaskStatus.CANCELED)
    }

    void 'a partially tasked bin should still offer the untasked remainder'() {
        given:
        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask openTask = buildTask(receivingBin, inventoryItem, product, 10.0G, PutawayTaskStatus.PENDING)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [openTask])

        then:
        result.size() == 2
        result.find { it.putawayStatus == PutawayStatus.READY }.quantity == 20.0G
        result.find { it.putawayStatus == PutawayStatus.PENDING }.quantity == 10.0G
    }

    void 'tasks covering the full bin quantity should leave no candidate'() {
        given:
        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask openTask = buildTask(receivingBin, inventoryItem, product, 30.0G, PutawayTaskStatus.PENDING)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [openTask])

        then:
        !result.any { it.putawayStatus == PutawayStatus.READY }
    }

    void 'several open tasks on the same item should aggregate rather than only the first matching'() {
        given:
        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask firstTask = buildTask(receivingBin, inventoryItem, product, 10.0G, PutawayTaskStatus.PENDING)
        PutawayTask secondTask = buildTask(receivingBin, inventoryItem, product, 15.0G, PutawayTaskStatus.STARTED)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [firstTask, secondTask])

        then:
        result.find { it.putawayStatus == PutawayStatus.READY }.quantity == 5.0G
    }

    void 'a task in a different bin should not suppress the candidate'() {
        given:
        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask openTask = buildTask(otherBin, inventoryItem, product, 30.0G, PutawayTaskStatus.PENDING)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [openTask])

        then:
        result.find { it.putawayStatus == PutawayStatus.READY }.quantity == 30.0G
    }

    void 'a task for a different lot of the same product should not suppress the candidate'() {
        given:
        InventoryItem otherLot = new InventoryItem(product: product, lotNumber: "LOT-2")
        otherLot.id = "item-2"

        PutawayItem readyItem = buildReadyItem(receivingBin, inventoryItem, product, 30.0G)
        PutawayTask openTask = buildTask(receivingBin, otherLot, product, 30.0G, PutawayTaskStatus.PENDING)

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([readyItem], [openTask])

        then:
        result.find { it.putawayStatus == PutawayStatus.READY }.quantity == 30.0G
    }

    void 'a split remainder should report the remaining quantity not the original'() {
        given: 'a partial complete cancels the parent of 20 and leaves a PENDING child of 10'
        Order order = buildOrder()

        OrderItem parentItem = buildOrderItem(order, 20.0G, OrderItemStatusCode.CANCELED)
        parentItem.id = "order-item-parent"

        OrderItem remainingItem = buildOrderItem(order, 10.0G, OrderItemStatusCode.PENDING)
        remainingItem.id = "order-item-remaining"
        remainingItem.parentOrderItem = parentItem

        PutawayTask openTask = buildTask(receivingBin, inventoryItem, product, 10.0G, PutawayTaskStatus.PENDING)
        openTask.putawayOrderItem = remainingItem

        when:
        List<PutawayItem> result = service.mergeCandidatesWithOpenTasks([], [openTask])

        then:
        result.size() == 1
        result[0].quantity == 10.0G
        result[0].id == "order-item-remaining"
    }

    void 'getPutawayItemStatus should map #orderItemStatusCode to #expectedStatus'() {
        expect:
        PutawayItem.getPutawayItemStatus(orderItemStatusCode) == expectedStatus

        where:
        orderItemStatusCode                | expectedStatus
        OrderItemStatusCode.PENDING        | PutawayStatus.PENDING
        OrderItemStatusCode.STARTED        | PutawayStatus.IN_PROGRESS
        OrderItemStatusCode.IN_PROGRESS    | PutawayStatus.IN_PROGRESS
        OrderItemStatusCode.COMPLETED      | PutawayStatus.COMPLETED
        OrderItemStatusCode.CANCELED       | PutawayStatus.CANCELED
        OrderItemStatusCode.BACKORDER      | null
    }

    private PutawayItem buildReadyItem(Location bin, InventoryItem item, Product prod, BigDecimal quantity) {
        return new PutawayItem(
                putawayStatus: PutawayStatus.READY,
                product: prod,
                inventoryItem: item,
                currentLocation: bin,
                currentFacility: facility,
                quantity: quantity,
        )
    }

    private PutawayTask buildTask(Location bin, InventoryItem item, Product prod, BigDecimal quantity,
                                  PutawayTaskStatus status) {
        Order order = buildOrder()
        OrderItem orderItem = buildOrderItem(order, quantity, toOrderItemStatusCode(status))
        orderItem.id = "order-item-${bin.id}-${item.id}-${quantity}"
        orderItem.originBinLocation = bin
        orderItem.product = prod
        orderItem.inventoryItem = item

        PutawayTask task = new PutawayTask(
                facility: facility,
                location: bin,
                product: prod,
                inventoryItem: item,
                quantity: quantity,
                status: status,
        )
        task.putawayOrderItem = orderItem
        return task
    }

    private Order buildOrder() {
        Order order = new Order(origin: facility, destination: facility)
        order.id = "order-1"
        return order
    }

    private OrderItem buildOrderItem(Order order, BigDecimal quantity, OrderItemStatusCode statusCode) {
        return new OrderItem(
                order: order,
                product: product,
                inventoryItem: inventoryItem,
                originBinLocation: receivingBin,
                quantity: quantity,
                orderItemStatusCode: statusCode,
        )
    }

    private static OrderItemStatusCode toOrderItemStatusCode(PutawayTaskStatus status) {
        switch (status) {
            case PutawayTaskStatus.STARTED:
                return OrderItemStatusCode.STARTED
            case PutawayTaskStatus.IN_PROGRESS:
                return OrderItemStatusCode.IN_PROGRESS
            default:
                return OrderItemStatusCode.PENDING
        }
    }
}
