package org.pih.warehouse.api.regression.platform.shipping

import spock.lang.PendingFeature

import groovy.sql.Sql


import org.pih.warehouse.shipping.*
import grails.util.GrailsWebMockUtil
import grails.validation.ValidationException
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import grails.util.Holders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Shared

import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.product.Category
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt

/**
 * The shipment service against a real database: shipments between two depots, packed into containers, sent and
 * received with the stock transactions that come with it, and imported from or exported to a packing list.
 * Every feature stocks its own products so that pending shipments of one feature do not take stock from another.
 */
class ShipmentServiceIntegrationSpec extends IntegrationSpec {

    ShipmentService shipmentService
    javax.sql.DataSource dataSource
    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    AuthService authService

    @Shared Map<String, String> ids = [:]

    private static final long DAY = 24L * 60 * 60 * 1000

    /**
     * Runs the closure in a new transaction. The current user (kept per thread) is loaded again in the new session,
     * so that the audit fields set on save refer to an instance of this session.
     */
    private <T> T tx(Closure<T> closure) {
        return Shipment.withNewTransaction {
            if (AuthService.currentUser) {
                authService.currentUser = AuthService.currentUser
            }
            return closure.call()
        }
    }

    private Location origin() { Location.get(ids.origin) }
    private Location destination() { Location.get(ids.destination) }
    private User admin() { User.get(ids.admin) }


    /** A depot of the main organization with the given extra fields, saved at once. */
    private static Location newDepot(Map fields) {
        Location location = new Location([locationType: LocationType.get('2'), organization: Location.get('1').organization,
                                          active: true] + fields)
        location.inventory = new Inventory(warehouse: location)
        return location.save(failOnError: true, flush: true)
    }

    void setup() {
        if (!ids) {
            Map<String, String> created = [:]
            tx {
                String suffix = UUID.randomUUID().toString().take(8)
                created.suffix = suffix
                created.origin = new LocationTestBuilder().asFacility().name("Shipping origin ${suffix}").build(true).id
                created.destination = new LocationTestBuilder().asFacility().name("Shipping destination ${suffix}").build(true).id
                created.admin = User.findByUsername('admin').id
                created.category = new CategoryTestBuilder().name("Shipped ${suffix}").build(true).id
            }
            ids = created
        }
        GrailsWebMockUtil.bindMockWebRequest(Holders.grailsApplication.mainContext as WebApplicationContext)
    }

    void cleanup() {
        if (ids) {
            dropDuplicateReceiptTransactions()
        }
        RequestContextHolder.resetRequestAttributes()
        authService.currentUser = null
        authService.currentLocation = null
    }

    /**
     * Receiving and then synchronizing a shipment's transactions leaves its receipt
     * with two inbound transactions. Every later Shipment marshalling (GET /api/shipments in any spec of the same
     * database) then answers 500 ("More than one row with the given identifier ... Transaction"). The feature that
     * does it still runs unchanged; afterwards the extra transactions of this spec's receipts are removed.
     */
    private void dropDuplicateReceiptTransactions() {
        // Plain SQL: loading such a receipt or transaction through Hibernate is what fails
        Sql sql = new Sql(dataSource)
        try {
            List<String> extra = []
            Set<String> seen = [] as Set
            sql.eachRow("""select t.id, t.receipt_id from transaction t
                           join receipt r on t.receipt_id = r.id join shipment s on r.shipment_id = s.id
                           where s.destination_id in (?, ?) or s.origin_id in (?, ?)
                           order by t.date_created, t.id""".toString(),
                    [ids.destination, ids.origin, ids.destination, ids.origin]) { row ->
                if (!seen.add((String) row.receipt_id)) {
                    extra << (String) row.id
                }
            }
            extra.each { String id ->
                sql.execute('delete from transaction_entry where transaction_id = ?', [id])
                sql.execute('delete from transaction where id = ?', [id])
            }
        } finally {
            sql.close()
        }
    }

    /** A new product with the given quantity counted at the origin ten days ago, without a bin. */
    private Map stock(int quantity, String lotNumber = null) {
        Map result = tx {
            authService.currentUser = admin()
            Product product = new ProductTestBuilder().randomizedName().category(Category.get(ids.category)).build(true)
            InventoryItem lot = InventoryItem.build(product: product,
                    lotNumber: lotNumber ?: "L-${UUID.randomUUID().toString().take(6)}", expirationDate: null)
            Transaction count = new Transaction(inventory: origin().inventory,
                    transactionType: TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID),
                    transactionDate: new Date(System.currentTimeMillis() - 10 * DAY),
                    transactionNumber: "SH-${UUID.randomUUID().toString().take(12)}", disableRefresh: true)
            count.addToTransactionEntries(new TransactionEntry(inventoryItem: lot, quantity: quantity))
            count.save(failOnError: true)
            return [product: product.id, lot: lot.id, productCode: product.productCode, lotNumber: lot.lotNumber]
        }
        tx { productAvailabilityService.refreshProductsAvailability(ids.origin, [result.product], true) }
        return result
    }

    /** A pending shipment from the origin to the destination with the given quantity of each stocked lot. */
    private String shipment(String name, Map<Map, Integer> items, Date expectedShippingDate = new Date() - 3) {
        return tx {
            authService.currentUser = admin()
            Shipment shipment = new Shipment(name: name, origin: origin(), destination: destination(),
                    expectedShippingDate: expectedShippingDate,
                    shipmentType: ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID))
            items.each { Map stock, Integer quantity ->
                InventoryItem lot = InventoryItem.get(stock.lot)
                shipment.addToShipmentItems(new ShipmentItem(product: lot.product, inventoryItem: lot,
                        lotNumber: lot.lotNumber, quantity: quantity))
            }
            shipmentService.saveShipment(shipment)
            assert !shipment.hasErrors()
            return shipment.id
        }
    }

    /** Records that the shipment was scheduled: an event that leaves it pending. */
    private void schedule(String shipmentId) {
        tx {
            authService.currentUser = admin()
            Shipment shipment = Shipment.get(shipmentId)
            shipmentService.createShipmentEvent(shipment, new Date() - 1, EventCode.SCHEDULED, origin())
            shipment.save(failOnError: true)
        }
    }

    private int quantityAt(Location location, String lotId) {
        return inventoryService.getQuantity(location.inventory, InventoryItem.get(lotId))
    }

    void 'getShipmentInstance loads a shipment or makes a new one of the given type'() {
        given:
        Map aspirin = stock(5)
        String shipmentId = shipment("Instance ${ids.suffix}", [(aspirin): 1])

        expect:
        tx { shipmentService.getShipmentInstance(shipmentId).name } == "Instance ${ids.suffix}"
        tx { shipmentService.getShipmentInstance(null).id } == null
        tx { shipmentService.getShipmentInstance(null, ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID).name).shipmentType.id } ==
                Constants.DEFAULT_SHIPMENT_TYPE_ID

        when:
        tx { shipmentService.getShipmentInstance('no-such-shipment') }

        then:
        Exception missing = thrown()
        missing.message == 'No shipment found with shipmentId no-such-shipment'

        when:
        tx { shipmentService.getShipmentInstance(null, 'Teleport') }

        then:
        Exception badType = thrown()
        badType.message == 'Teleport is not a valid shipment type'
    }

    void 'the shipment queries find shipments by location, name, status and terms'() {
        given:
        Map aspirin = stock(10)
        String name = "Query ${ids.suffix}"
        String shipmentId = shipment(name, [(aspirin): 2])
        schedule(shipmentId)
        String number = tx { Shipment.get(shipmentId).shipmentNumber }

        expect:
        tx {
            Location origin = origin()
            Location destination = destination()
            Shipment shipment = Shipment.get(shipmentId)
            assert number
            assert shipmentService.getShipmentsByLocation(origin)*.id.contains(shipmentId)
            assert shipmentService.getShipmentsByLocation(null, destination, ShipmentStatusCode.PENDING)*.id.contains(shipmentId)
            assert !shipmentService.getShipmentsByLocation(origin, null, ShipmentStatusCode.SHIPPED)*.id.contains(shipmentId)
            assert shipmentService.getShipmentsByName(name)*.id == [shipmentId]
            assert shipmentService.getShipmentsByNameAndDestination(name, destination)*.id == [shipmentId]
            assert shipmentService.getShipmentsByNameAndDestination(name, origin) == []
            assert shipmentService.getPendingShipments(origin)*.id.contains(shipmentId)
            assert shipmentService.getPendingInboundShipments(destination)*.id.contains(shipmentId)
            assert shipmentService.getIncomingShipments(destination)*.id.contains(shipmentId)
            assert shipmentService.getOutgoingShipments(origin)*.id.contains(shipmentId)
            assert shipmentService.getShipmentsByDestination(destination)*.id.contains(shipmentId)
            assert shipmentService.getRecentOutgoingShipments(ids.origin, 5, 5)*.id.contains(shipmentId)
            assert shipmentService.getRecentIncomingShipments(ids.destination, 5, 5) != null
            assert shipmentService.getShipments(name, shipment.shipmentType, origin, destination, ShipmentStatusCode.PENDING,
                    null, null, new Date() - 1, new Date() + 1, 10)*.id == [shipmentId]
            assert shipmentService.getShipments(number, null, null, null, null, null, null, null, null, 10)*.id == [shipmentId]
            assert shipmentService.getAllShipments('name', 'asc')*.id.contains(shipmentId)
            assert shipmentService.shipments*.id.contains(shipmentId)
            Map byStatus = shipmentService.getShipmentsByStatus([shipment])
            assert byStatus[ShipmentStatusCode.PENDING].objectList*.id == [shipmentId]
            assert byStatus[ShipmentStatusCode.SHIPPED] == []
            assert shipmentService.productMap.size() > 0
            assert shipmentService.getPendingShipmentItemsWithProduct(destination, Product.get(aspirin.product))*.quantity == [2]

            Product product = Product.get(aspirin.product)
            assert shipmentService.getOutgoingQuantityByProduct(origin, [product]) == [(product): 2]
            assert shipmentService.getIncomingQuantityByProduct(destination, [product]) == [(product): 2]
            assert shipmentService.getQuantityForShipping(origin)[InventoryItem.get(aspirin.lot)] == 2
            assert shipmentService.getQuantityForReceiving(destination)[InventoryItem.get(aspirin.lot)] == 2
            assert shipmentService.getQuantityAllocated(origin, null, InventoryItem.get(aspirin.lot)) == 2
            assert shipmentService.getDuplicatedShipmentItemsQuantity(shipment, null, InventoryItem.get(aspirin.lot)) == 2
            assert shipmentService.getShipmentWorkflow(shipmentId) == ShipmentWorkflow.findByShipmentType(shipment.shipmentType)
            assert shipmentService.getShipmentWorkflow(new Shipment()) == null
            assert shipmentService.getShipmentWorkflow('no-such-shipment') == null
            true
        }
    }

    void 'containers are created, nested, moved, copied, sorted and deleted'() {
        given:
        Map aspirin = stock(20)
        Map gauze = stock(20)
        String shipmentId = shipment("Containers ${ids.suffix}", [(aspirin): 4, (gauze): 3])

        when: 'two pallets and a box on the first pallet are added'
        tx { shipmentService.createContainers(shipmentId, null, Constants.PALLET_CONTAINER_TYPE_ID, "Pallet 1\nPallet 2") }
        String pallet1 = tx { Shipment.get(shipmentId).containers.find { it.name == 'Pallet 1' }.id }
        String pallet2 = tx { Shipment.get(shipmentId).containers.find { it.name == 'Pallet 2' }.id }
        tx { shipmentService.createContainers(shipmentId, pallet1, Constants.BOX_CONTAINER_TYPE_ID, "Box A") }
        String boxA = tx { Shipment.get(shipmentId).containers.find { it.name == 'Box A' }.id }

        then:
        tx { Container.get(boxA).parentContainer.id } == pallet1
        tx { Shipment.get(shipmentId).findAllParentContainers()*.name.sort() } == ['Pallet 1', 'Pallet 2']

        when: 'the items are packed'
        String aspirinItem = tx { Shipment.get(shipmentId).shipmentItems.find { it.inventoryItem.id == aspirin.lot }.id }
        String gauzeItem = tx { Shipment.get(shipmentId).shipmentItems.find { it.inventoryItem.id == gauze.lot }.id }
        tx { shipmentService.moveShipmentItemToContainer(aspirinItem, boxA) }
        tx { shipmentService.moveShipmentItemToContainer(gauzeItem, pallet2) }

        then:
        tx { ShipmentItem.get(aspirinItem).container.id } == boxA
        tx { shipmentService.findShipmentItem(Shipment.get(shipmentId), Container.get(pallet2), InventoryItem.get(gauze.lot)).id } == gauzeItem
        tx {
            shipmentService.findShipmentItem(Shipment.get(shipmentId), Container.get(boxA), Product.get(aspirin.product),
                    aspirin.lotNumber).id
        } == aspirinItem
        tx {
            shipmentService.findShipmentItem(Shipment.get(shipmentId), Container.get(boxA), Product.get(aspirin.product),
                    aspirin.lotNumber, InventoryItem.get(aspirin.lot)).id
        } == aspirinItem

        when: 'the second pallet is put on the first one, which is not allowed for a pallet holding a box'
        tx { shipmentService.moveContainerToContainer(pallet1, pallet2) }

        then:
        ShipmentException nested = thrown()
        nested.message == 'Moving a container with subcontainers into another container is currently not supported.'

        when: 'a box cannot take a container either'
        tx { shipmentService.moveContainerToContainer(pallet2, boxA) }

        then:
        ShipmentException intoBox = thrown()
        intoBox.message == 'Moving a container into a sub-container in currently not supported.'

        when: 'the containers are sorted and the box is copied twice'
        tx { shipmentService.sortContainers([pallet2, pallet1]) }
        tx { shipmentService.copyContainer(Container.get(boxA), 2) }

        then:
        tx { Container.get(pallet2).sortOrder } == 0
        tx { Container.get(pallet1).sortOrder } == 1
        tx { Shipment.get(shipmentId).containers.findAll { it.name == 'Box A' }.size() } == 3
        tx { Shipment.get(shipmentId).shipmentItems.findAll { it.inventoryItem.id == aspirin.lot }*.quantity } == [4, 4, 4]

        when: 'a container with items cannot be deleted without its items'
        tx { shipmentService.deleteContainers(shipmentId, [pallet2], false) }

        then:
        ShipmentException notEmpty = thrown()
        notEmpty.message == 'Cannot delete container that contains items'

        when: 'the second pallet goes to the trash with the item on it'
        tx { shipmentService.moveContainerToContainer(pallet2, 'trash') }

        then:
        tx { Container.get(pallet2) } == null
        tx { ShipmentItem.get(gauzeItem) } == null
        tx { Shipment.get(shipmentId).shipmentItems.size() } == 3

        when: 'an item goes to the trash'
        tx { shipmentService.moveShipmentItemToContainer(aspirinItem, 'trash') }

        then:
        tx { ShipmentItem.get(aspirinItem) } == null

        when: 'all containers are deleted with their items'
        tx { shipmentService.deleteAllContainers(shipmentId, true) }

        then:
        tx { Shipment.get(shipmentId).containers.size() } == 0
        tx { Shipment.get(shipmentId).shipmentItems.size() } == 0
    }

    void 'a container is moved to another shipment with its items'() {
        given:
        Map aspirin = stock(10)
        String fromId = shipment("Move from ${ids.suffix}", [(aspirin): 2])
        String toId = shipment("Move to ${ids.suffix}", [:])
        String containerId = tx {
            Shipment from = Shipment.get(fromId)
            Container container = from.addNewContainer(ContainerType.get(Constants.BOX_CONTAINER_TYPE_ID))
            container.name = 'Box to move'
            shipmentService.saveContainer(container)
            from.shipmentItems.each { it.container = container }
            from.save(flush: true)
            return container.id
        }

        when:
        tx {
            shipmentService.moveContainer(Container.get(containerId), Shipment.get(toId))
            Shipment.get(toId).save(flush: true)
        }

        then:
        tx { Shipment.get(toId).shipmentItems*.quantity } == [2]
        tx { Shipment.get(toId).containers*.id } == [containerId]
        tx { Shipment.get(fromId).shipmentItems.size() } == 0
    }

    void 'items are added within the stock available at the origin and moved between containers'() {
        given:
        Map aspirin = stock(10)
        String shipmentId = shipment("Items ${ids.suffix}", [:])

        when:
        tx { shipmentService.addToShipmentItems(shipmentId, null, aspirin.lot, 6) }

        then:
        tx { Shipment.get(shipmentId).shipmentItems*.quantity } == [6]
        tx { shipmentService.validatePicklist(Shipment.get(shipmentId)) }
        tx { shipmentService.validateShipmentItem(Shipment.get(shipmentId).shipmentItems.first()) }

        when: 'an unknown lot is added'
        tx { shipmentService.addToShipmentItems(shipmentId, null, 'no-such-lot', 1) }

        then:
        ValidationException unknown = thrown()
        unknown.errors.globalErrors*.code == ['shipmentItem.inventoryItem.required']

        when: 'part of the item is moved into a new box and the rest stays unpacked'
        String boxId = tx {
            Shipment shipment = Shipment.get(shipmentId)
            Container box = shipment.addNewContainer(ContainerType.get(Constants.BOX_CONTAINER_TYPE_ID))
            box.name = 'Box'
            shipmentService.saveContainer(box)
            shipment.save(flush: true)
            return box.id
        }
        boolean moved = tx {
            ShipmentItem shipmentItem = Shipment.get(shipmentId).shipmentItems.first()
            boolean result = shipmentService.moveItem(shipmentItem, [(boxId): 4])
            Shipment.get(shipmentId).save(flush: true)
            return result
        }
        boolean tooMany = tx {
            shipmentService.moveItem(Shipment.get(shipmentId).shipmentItems.find { !it.container }, [(boxId): 99])
        }

        then:
        moved
        !tooMany
        tx { Shipment.get(shipmentId).shipmentItems.collect { [it.container?.id, it.quantity] } as Set } ==
                [[null, 2], [boxId, 4]] as Set

        when: 'the bins of the picks are cleared and an item is deleted'
        tx { shipmentService.clearPicklist(Shipment.get(shipmentId)) }
        tx { shipmentService.deleteShipmentItem(Shipment.get(shipmentId).shipmentItems.find { !it.container }) }

        then:
        tx { Shipment.get(shipmentId).shipmentItems*.quantity } == [4]
    }

    void 'the add to shipment command shows the stock of the products and adds the chosen quantities'() {
        given:
        Map aspirin = stock(9)
        String shipmentId = shipment("Command ${ids.suffix}", [(aspirin): 1])
        schedule(shipmentId)

        when:
        ItemListCommand command = tx {
            ItemListCommand result = shipmentService.getAddToShipmentCommand([aspirin.product], origin())
            result.items.each { item -> item.shipment = Shipment.get(shipmentId); item.quantity = 2 }
            assert shipmentService.addToShipment(result)
            return result
        }

        then:
        command.items.collect { [it.lotNumber, it.quantityOnHand, it.quantityShipping] } == [[aspirin.lotNumber, 9, 1]]
        tx { Shipment.get(shipmentId).shipmentItems*.quantity } == [3]
    }

    void 'sending debits the origin and receiving credits the destination'() {
        given:
        Map aspirin = stock(10)
        String shipmentId = shipment("Send ${ids.suffix}", [(aspirin): 4])

        when: 'the shipment is sent'
        tx {
            authService.currentUser = admin()
            shipmentService.sendShipment(Shipment.get(shipmentId), 'on its way', admin(), origin(), new Date() - 2)
        }

        then:
        tx { Shipment.get(shipmentId).hasShipped() }
        tx { Shipment.get(shipmentId).comments*.comment } == ['on its way']
        tx { quantityAt(origin(), aspirin.lot) } == 6
        tx { Transaction.findAllByOutgoingShipment(Shipment.get(shipmentId))*.transactionType*.id } ==
                [Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID]
        tx { shipmentService.getPendingInboundShipmentItems(destination())*.shipment*.id.contains(shipmentId) }
        tx { shipmentService.getPendingInboundShipmentItems(destination(), Product.get(aspirin.product))*.quantity } == [4]
        tx { shipmentService.getPendingInboundShipmentItems(destination(), [Product.get(aspirin.product)])*.quantity } == [4]

        when: 'it is sent again'
        tx { shipmentService.sendShipment(Shipment.get(shipmentId), null, admin(), origin(), new Date() - 1) }

        then:
        ValidationException again = thrown()
        again.errors.globalErrors*.code.contains('shipment.invalid.alreadyShipped')

        when: 'it is received at the origin'
        tx {
            shipmentService.findOrCreateReceipt(Shipment.get(shipmentId))
            shipmentService.receiveShipment(shipmentId, null, ids.admin, ids.origin, true)
        }

        then:
        ShipmentException wrongPlace = thrown()
        wrongPlace.message.startsWith('Shipment must be received by the destination location')

        when: 'it is received at the destination'
        tx {
            authService.currentUser = admin()
            Receipt receipt = shipmentService.findOrCreateReceipt(Shipment.get(shipmentId))
            receipt.actualDeliveryDate = new Date() - 1
            shipmentService.validateReceipt(receipt)
            shipmentService.receiveShipment(shipmentId, 'arrived', ids.admin, ids.destination, true)
        }

        then:
        tx { Shipment.get(shipmentId).wasReceived() }
        tx { quantityAt(destination(), aspirin.lot) } == 4
        tx { Transaction.findAllByReceipt(Shipment.get(shipmentId).receipt)*.transactionType*.id } ==
                [Constants.TRANSFER_IN_TRANSACTION_TYPE_ID]
        tx { shipmentService.synchronizeTransactions(Shipment.get(shipmentId)) }

        when: 'it is received twice'
        tx { shipmentService.receiveShipment(shipmentId, null, ids.admin, ids.destination, true) }

        then:
        ShipmentException twice = thrown()
        twice.message == 'Shipment has already been received.'
    }

    void 'rolling back the shipping of a shipment takes back its debit'() {
        given:
        Map aspirin = stock(10)
        String shipmentId = shipment("Rollback ${ids.suffix}", [(aspirin): 4])
        tx {
            authService.currentUser = admin()
            shipmentService.sendShipment(Shipment.get(shipmentId), null, admin(), origin(), new Date() - 2)
        }

        expect:
        tx { quantityAt(origin(), aspirin.lot) } == 6

        when: 'the shipping is rolled back'
        tx {
            authService.currentUser = admin()
            shipmentService.rollbackLastEvent(Shipment.get(shipmentId))
        }

        then:
        tx { Shipment.get(shipmentId).hasShipped() } == false
        tx { quantityAt(origin(), aspirin.lot) } == 10
        tx { Transaction.findAllByOutgoingShipment(Shipment.get(shipmentId)) } == []

        when: 'there is nothing left to roll back'
        tx { shipmentService.rollbackLastEvent(Shipment.get(shipmentId)) }

        then:
        RuntimeException nothing = thrown()
        nothing.message == 'Cannot rollback shipment status because there are no recent events'
    }

    void 'sendShipment refuses a ship date in the future'() {
        given:
        Map aspirin = stock(3)
        String shipmentId = shipment("Future ${ids.suffix}", [(aspirin): 1])

        when:
        tx { shipmentService.sendShipment(Shipment.get(shipmentId), null, admin(), origin(), new Date() + 2) }

        then:
        ValidationException exception = thrown()
        exception.errors.globalErrors*.code == ['shipment.invalid.invalidShipDate']
        tx { Shipment.get(shipmentId).hasShipped() } == false
    }

    void 'bulk receipt receives shipped shipments and both bulk actions need shipments'() {
        given:
        Map aspirin = stock(8)
        String shipmentId = shipment("Bulk ${ids.suffix}", [(aspirin): 3], new Date() - 6)
        tx {
            authService.currentUser = admin()
            shipmentService.sendShipment(Shipment.get(shipmentId), null, admin(), origin(), new Date() - 4, false)
        }

        expect: 'sending without debiting leaves the stock and synchronizing creates the debit'
        tx { quantityAt(origin(), aspirin.lot) } == 8
        tx { shipmentService.synchronizeTransactions(Shipment.get(shipmentId)) }
        tx { quantityAt(origin(), aspirin.lot) } == 5

        when:
        tx {
            authService.currentUser = admin()
            shipmentService.receiveShipments([shipmentId], 'all in', ids.admin, ids.destination, true)
        }

        then:
        tx { Shipment.get(shipmentId).wasReceived() }
        tx { quantityAt(destination(), aspirin.lot) } == 3

        when:
        shipmentService.receiveShipments([], null, null, null, true)

        then:
        IllegalArgumentException noShipments = thrown()
        noShipments.message == 'Must select at least one shipment in order to use bulk receipt'

        when:
        shipmentService.rollbackShipments(null)

        then:
        IllegalArgumentException noRollback = thrown()
        noRollback.message == 'Must select at least one shipment in order to use bulk rollback'
    }

    void 'markAsReceived and the maintenance helpers keep the status of a shipment in step with its events'() {
        given:
        Map aspirin = stock(3)
        String shipmentId = shipment("Mark ${ids.suffix}", [(aspirin): 1])

        when:
        tx {
            authService.currentUser = admin()
            shipmentService.markAsReceived(Shipment.get(shipmentId), destination())
        }

        then:
        tx { Shipment.get(shipmentId).wasReceived() }
        tx { shipmentService.bulkUpdateShipments() }
        tx {
            shipmentService.refreshCurrentStatus(shipmentId)
            true
        }
        tx { shipmentService.shipmentsWithInvalidStatus.every { it.id != shipmentId } }
        tx { shipmentService.fixShipmentsWithInvalidStatus() } >= 0

        when:
        tx {
            Shipment shipment = Shipment.get(shipmentId)
            shipmentService.deleteEvent(shipment, shipment.mostRecentEvent)
        }

        then:
        tx { Shipment.get(shipmentId).events.size() } == 0
    }

    void 'creating stock transactions needs depots with an inventory'() {
        given:
        Map aspirin = stock(3)
        String shipmentId = shipment("No inventory ${ids.suffix}", [(aspirin): 1])

        when:
        tx {
            Shipment shipment = Shipment.get(shipmentId)
            shipment.destination = new Location(name: 'Nowhere')
            shipmentService.createInboundTransaction(shipment)
        }

        then:
        IllegalStateException inbound = thrown()
        inbound.message == 'Destination Nowhere must have an inventory in order to receive stock'
    }

    private static byte[] packingList(List<List> rows) {
        HSSFWorkbook workbook = new HSSFWorkbook()
        HSSFSheet sheet = workbook.createSheet()
        sheet.createRow(0).createCell(0).setCellValue('Pallet')
        rows.eachWithIndex { List values, int index ->
            HSSFRow row = sheet.createRow(index + 1)
            values.eachWithIndex { Object value, int column ->
                if (value instanceof Number) {
                    row.createCell(column).setCellValue(value as double)
                } else if (value instanceof Date) {
                    row.createCell(column).setCellValue(value as Date)
                } else if (value != null) {
                    row.createCell(column).setCellValue(value as String)
                }
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        workbook.write(outputStream)
        return outputStream.toByteArray()
    }

    void 'a packing list is imported into pallets and boxes and exported again'() {
        given:
        Map aspirin = stock(10)
        Map gauze = stock(10)
        String shipmentId = shipment("Packing ${ids.suffix}", [:])
        byte[] spreadsheet = packingList([
                ['Pallet 1', 'Box 1', aspirin.productCode, 'Aspirin', aspirin.lotNumber, null, 4, 'EA', null],
                ['Pallet 1', null, gauze.productCode, 'Gauze', gauze.lotNumber, new Date() + 90, 2.4, 'EA', null],
                [null, null, null, 'blank row', null, null, 0, null, null],
        ])

        when:
        boolean imported = tx { shipmentService.importPackingList(shipmentId, new ByteArrayInputStream(spreadsheet)) }

        then:
        imported
        tx {
            Shipment.get(shipmentId).shipmentItems.collect {
                [it.inventoryItem.id, it.quantity, it.container.name]
            } as Set
        } == [[aspirin.lot, 4, 'Box 1'], [gauze.lot, 2, 'Pallet 1']] as Set

        when:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        tx { shipmentService.exportPackingList(shipmentId, outputStream) }
        HSSFSheet exported = new HSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0)

        then:
        exported.lastRowNum == 2
        (1..2).collect { exported.getRow(it).getCell(2).stringCellValue } as Set == [aspirin.productCode, gauze.productCode] as Set

        when: 'the exported list is parsed back'
        List parsed = tx { shipmentService.parsePackingList(new ByteArrayInputStream(outputStream.toByteArray())) }

        then:
        parsed.collect { [it.productCode, it.quantity] } as Set == [[aspirin.productCode, 4d], [gauze.productCode, 2d]] as Set
    }

    void 'validatePackingList refuses #problem'() {
        given:
        Map aspirin = stock(5)
        List items = rows(aspirin)

        when:
        tx { shipmentService.validatePackingList(items, origin()) }

        then:
        RuntimeException exception = thrown()
        exception.message.startsWith(message(aspirin))

        where:
        problem                     | rows                                                                     | message
        'duplicates'                | { Map a -> [item(a, 'P', 'B', 1), item(a, 'P', 'B', 1)] }               | { Map a -> 'Found duplicates' }
        'an unknown product'        | { Map a -> [[productCode: 'NO-SUCH-CODE', quantity: 1]] }                | { Map a -> 'Cannot find product with product code NO-SUCH-CODE' }
        'a box without a pallet'    | { Map a -> [item(a, null, 'B', 1)] }                                     | { Map a -> "You must enter a valid Pallet if using the Box column for item ${a.productCode}" }
        'more than the stock'       | { Map a -> [item(a, 'P', null, 6)] }                                     | { Map a -> "Quantity to ship exceeds quantity on hand for item ${a.productCode}" }
    }

    private static Map item(Map stock, String pallet, String box, int quantity) {
        return [palletName: pallet, boxName: box, productCode: stock.productCode, lotNumber: stock.lotNumber,
                quantity: quantity]
    }

    void 'parsePackingList reports the row and column of a cell it cannot read'() {
        given:
        byte[] spreadsheet = packingList([['Pallet', 'Box', 'CODE', 'Name', 'Lot', 'not a date', 1, 'EA', null]])

        when:
        shipmentService.parsePackingList(new ByteArrayInputStream(spreadsheet))

        then:
        RuntimeException exception = thrown()
        exception.message.startsWith('Error parsing XLS file at row 2 column 6')
    }

    void 'shipment items are created from an order and the shipment of a stock transfer gets a tracking number'() {
        given:
        Map aspirin = stock(5)
        String orderId = tx {
            authService.currentUser = admin()
            org.pih.warehouse.order.Order order = new org.pih.warehouse.order.Order(
                    name: "Return ${ids.suffix}", orderNumber: "RET-${ids.suffix}", origin: origin(), destination: destination(),
                    orderType: org.pih.warehouse.order.OrderType.findByCode(org.pih.warehouse.order.OrderTypeCode.TRANSFER_ORDER.name()),
                    status: org.pih.warehouse.order.OrderStatus.PENDING, orderedBy: admin(), dateOrdered: new Date(),
                    description: 'returned goods')
            order.addToOrderItems(new org.pih.warehouse.order.OrderItem(product: Product.get(aspirin.product),
                    inventoryItem: InventoryItem.get(aspirin.lot), quantity: 2, quantityPerUom: 1))
            order.save(failOnError: true)
            return order.id
        }

        when:
        String shipmentId = tx {
            authService.currentUser = admin()
            authService.currentLocation = destination()
            StockTransfer stockTransfer = new StockTransfer(id: orderId, origin: origin(), destination: destination(),
                    trackingNumber: 'TRACK-1', driverName: 'Sam', comments: 'handle with care', description: 'returned goods')
            shipmentService.createOrUpdateShipment(stockTransfer).id
        }

        then:
        tx {
            Shipment shipment = Shipment.get(shipmentId)
            [shipment.shipmentNumber, shipment.driverName, shipment.additionalInformation, shipment.shipmentType.id,
             shipment.shipmentItems*.quantity, shipment.referenceNumbers*.identifier]
        } == ["RET-${ids.suffix}".toString(), 'Sam', 'handle with care', Constants.DEFAULT_SHIPMENT_TYPE_ID, [2], ['TRACK-1']]
        tx { Shipment.get(shipmentId).name } == tx {
            "${origin().locationNumber ?: origin().name} - ${destination().locationNumber ?: destination().name} - TRACK-1 - returned goods"
                    .replace(' ', '')
        }

        when: 'the tracking number is changed and then removed'
        String changed = tx {
            Shipment shipment = Shipment.get(shipmentId)
            String identifier = shipmentService.createOrUpdateTrackingNumber(shipment, 'TRACK-2').identifier
            shipment.save(flush: true)
            return identifier
        }
        tx {
            Shipment shipment = Shipment.get(shipmentId)
            shipmentService.createOrUpdateTrackingNumber(shipment, null)
            shipment.save(flush: true)
        }

        then:
        changed == 'TRACK-2'
        tx { Shipment.get(shipmentId).referenceNumbers.size() } == 0

        when: 'the order is shipped through ship order items'
        String orderShipmentId = shipment("Order shipment ${ids.suffix}", [:])
        tx {
            org.pih.warehouse.order.Order order = org.pih.warehouse.order.Order.get(orderId)
            org.pih.warehouse.order.ShipOrderCommand command = new org.pih.warehouse.order.ShipOrderCommand(
                    order: order, shipment: Shipment.get(orderShipmentId))
            command.shipOrderItems.add(new org.pih.warehouse.order.ShipOrderItemCommand(orderItem: order.orderItems.first(),
                    quantityToShip: 2))
            shipmentService.updateOrCreateOrderBasedShipmentItems(command)
        }

        then:
        tx {
            Shipment shipment = Shipment.get(orderShipmentId)
            [shipment.name, shipment.description, shipment.shipmentItems*.quantity]
        } == ["Return ${ids.suffix}".toString(), "RET-${ids.suffix}".toString(), [2]]
    }

    void 'comments, status codes and destinations of shipped products are available'() {
        given:
        Map aspirin = stock(5)
        String shipmentId = shipment("Comments ${ids.suffix}", [(aspirin): 1])
        String managedId = tx {
            Location managed = newDepot(name: "Managed ${ids.suffix}",
                    supportedActivities: [ActivityCode.MANAGE_INVENTORY.id] as Set)
            Shipment shipment = Shipment.get(shipmentId)
            shipment.destination = managed
            shipment.save(failOnError: true)
            return managed.id
        }
        tx {
            authService.currentUser = admin()
            shipmentService.sendShipment(Shipment.get(shipmentId), null, admin(), origin(), new Date() - 1)
        }

        expect:
        tx { shipmentService.addShipmentComment(shipmentId, 'hello', admin(), admin()).comment } == 'hello'
        tx { Shipment.get(shipmentId).comments*.comment } == ['hello']
        tx {
            shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(Product.get(aspirin.product),
                    ActivityCode.MANAGE_INVENTORY, [ShipmentStatusCode.SHIPPED])*.id
        } == [managedId]
        tx {
            shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(Product.get(aspirin.product),
                    ActivityCode.MANAGE_INVENTORY, [ShipmentStatusCode.RECEIVED])
        } == []
        shipmentService.getShipmentStatusCodes(['RECEIVED'])*.id == ShipmentStatusCode.list()*.name().findAll { it != 'RECEIVED' }
        shipmentService.getShipmentStatusCodes().every { it.label && it.variant }
    }

    @PendingFeature(reason = 'shipment item lookups by activity miss depots that take their activities from their type')
    void 'destinations of shipped products take the supported activities of their type when they have none of their own'() {
        given:
        Map aspirin = stock(5)
        Map<String, String> depots = tx {
            [inheriting: newDepot(name: "Inheriting ${ids.suffix}").id,
             own       : newDepot(name: "Own activities ${ids.suffix}",
                     supportedActivities: [ActivityCode.PLACE_REQUEST.id] as Set).id]
        }
        depots.each { String key, String depotId ->
            String shipmentId = shipment("Activities ${key} ${ids.suffix}", [(aspirin): 1])
            tx {
                authService.currentUser = admin()
                Shipment shipment = Shipment.get(shipmentId)
                shipment.destination = Location.get(depotId)
                shipment.save(failOnError: true)
                shipmentService.sendShipment(shipment, null, admin(), origin(), new Date() - 1)
            }
        }

        expect: 'the depot type supports adjusting stock'
        tx { LocationType.get('2').supportedActivities.contains(ActivityCode.ADJUST_INVENTORY.id) }

        and: 'only the depot without activities of its own inherits that activity'
        tx {
            shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(Product.get(aspirin.product),
                    ActivityCode.ADJUST_INVENTORY, [ShipmentStatusCode.SHIPPED])*.id
        } == [depots.inheriting]
        tx {
            shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(Product.get(aspirin.product),
                    ActivityCode.PLACE_REQUEST, [ShipmentStatusCode.SHIPPED])*.id
        }.contains(depots.own)
    }

    @PendingFeature(reason = 'the lookup throws an SQL error when no location supports the activity')
    void 'an activity that no location supports gives no destinations'() {
        given:
        Map aspirin = stock(5)
        String shipmentId = shipment("No activity ${ids.suffix}", [(aspirin): 1])
        tx {
            authService.currentUser = admin()
            shipmentService.sendShipment(Shipment.get(shipmentId), null, admin(), origin(), new Date() - 1)
        }

        expect:
        tx {
            shipmentService.getDestinationsWithActivityCodeAndProductInShipmentWithStatus(Product.get(aspirin.product),
                    ActivityCode.NONE, [ShipmentStatusCode.SHIPPED])
        } == []
    }
}
