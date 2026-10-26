package org.pih.warehouse.api.regression.platform.requisition

import spock.lang.PendingFeature



import spock.lang.Ignore
import org.pih.warehouse.requisition.*
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.validation.ValidationException
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Shared

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * The requisition service against a real database: requests from a ward depot to a fulfilling depot, a published
 * stock list, the issue of a picked request as a stock transfer, status transitions and the CSV exports.
 */
class RequisitionServiceIntegrationSpec extends IntegrationSpec {

    RequisitionService requisitionService
    InventoryService inventoryService
    AuthService authService

    @Shared Map<String, String> ids = [:]

    private static final long DAY = 24L * 60 * 60 * 1000

    /**
     * Runs the closure in a new transaction. The current user (kept per thread) is loaded again in the new session,
     * so that the audit fields set on save refer to an instance of this session.
     */
    private <T> T tx(Closure<T> closure) {
        return Requisition.withNewTransaction {
            if (AuthService.currentUser) {
                authService.currentUser = AuthService.currentUser
            }
            return closure.call()
        }
    }

    private Location fulfilling() { Location.get(ids.fulfilling) }
    private Location requesting() { Location.get(ids.requesting) }
    private Product gauze() { Product.get(ids.gauze) }
    private Product aspirin() { Product.get(ids.aspirin) }
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
                User admin = User.findByUsername('admin')
                authService.currentUser = admin
                String suffix = UUID.randomUUID().toString().take(8)
                Location fulfilling = newDepot(name: "Fulfilling ${suffix}", locationNumber: "FUL-${suffix}")
                Location requesting = newDepot(name: "Requesting ${suffix}", locationNumber: "REQ-${suffix}")
                Category drugs = new CategoryTestBuilder().name("A drugs ${suffix}").build(true)
                Category supplies = new CategoryTestBuilder().name("B supplies ${suffix}").build(true)
                Product gauze = new ProductTestBuilder().name("Gauze ${suffix}").category(supplies).build(true)
                Product aspirin = new ProductTestBuilder().name("Aspirin ${suffix}").category(drugs).build(true)
                InventoryItem aspirinLot = InventoryItem.build(product: aspirin, lotNumber: "RL-${suffix}", expirationDate: null)
                Transaction count = new Transaction(inventory: fulfilling.inventory,
                        transactionType: TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID),
                        transactionDate: new Date(System.currentTimeMillis() - 5 * DAY), transactionNumber: "RQC-${suffix}",
                        disableRefresh: true)
                count.addToTransactionEntries(new TransactionEntry(inventoryItem: aspirinLot, quantity: 50))
                count.save(failOnError: true)

                Requisition stocklist = new Requisition(name: "Stock list ${suffix}", origin: fulfilling, destination: requesting,
                        requestedBy: admin, isTemplate: true, isPublished: true, status: RequisitionStatus.CREATED)
                stocklist.addToRequisitionItems(new RequisitionItem(product: gauze, quantity: 10, orderIndex: 0))
                stocklist.addToRequisitionItems(new RequisitionItem(product: aspirin, quantity: 20, orderIndex: 1))
                requisitionService.saveTemplateRequisition(stocklist)

                created.putAll(suffix: suffix, fulfilling: fulfilling.id, requesting: requesting.id, gauze: gauze.id,
                        aspirin: aspirin.id, aspirinLot: aspirinLot.id, stocklist: stocklist.id, category: drugs.id, admin: admin.id,
                        gauzeCode: gauze.productCode, aspirinCode: aspirin.productCode)
            }
            ids = created
        }
        GrailsWebMockUtil.bindMockWebRequest(Holders.grailsApplication.mainContext as WebApplicationContext)
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
        authService.currentUser = null
        authService.currentLocation = null
    }

    /** A request for the given quantities, saved through the service so that it gets a request number. */
    private String request(String name, RequisitionStatus status, Map<String, Integer> quantities, Date dateRequested = new Date()) {
        return tx {
            authService.currentUser = admin()
            Requisition requisition = new Requisition(name: name, origin: fulfilling(), destination: requesting(),
                    requestedBy: admin(), status: status, dateRequested: dateRequested, type: RequisitionType.DEFAULT,
                    commodityClass: CommodityClass.MEDICATION)
            quantities.each { String productId, Integer quantity ->
                requisition.addToRequisitionItems(new RequisitionItem(product: Product.get(productId), quantity: quantity,
                        orderIndex: requisition.requisitionItems?.size() ?: 0))
            }
            requisitionService.saveRequisition(requisition)
            assert !requisition.hasErrors()
            return requisition.id
        }
    }

    private static GrailsParameterMap params(Map values) {
        return new GrailsParameterMap(values, GrailsWebRequest.lookup().currentRequest)
    }

    void 'the stock list and its items are found by origin and destination'() {
        expect:
        tx {
            assert requisitionService.requisitionTemplates*.id.contains(ids.stocklist)
            assert requisitionService.getRequisitionTemplates(fulfilling())*.id == [ids.stocklist]
            assert requisitionService.getRequisitionTemplates(fulfilling(), requesting())*.id == [ids.stocklist]
            assert requisitionService.getRequisitionTemplates(requesting(), fulfilling()) == []
            assert requisitionService.getRequisitionTemplatesItems([Requisition.get(ids.stocklist)])*.product*.id ==
                    [ids.aspirin, ids.gauze]
            assert requisitionService.isProductInStockList(gauze())
            assert !requisitionService.isProductInStockList(new ProductTestBuilder().randomizedName().category(Category.get(ids.category)).build(true))
            assert requisitionService.getAllRequisitionTemplates(new Requisition(isTemplate: true, isPublished: true,
                    origin: fulfilling()), params([:]))*.id == [ids.stocklist]
            true
        }
    }

    void 'saveTemplateRequisition refuses an invalid stock list'() {
        when:
        tx { requisitionService.saveTemplateRequisition(new Requisition(isTemplate: true)) }

        then:
        ValidationException exception = thrown()
        exception.errors.getFieldError('name')
    }

    void 'the request statistics count the requests by status, for the user and since a date'() {
        given:
        request("Statistics created ${ids.suffix}", RequisitionStatus.CREATED, [(ids.gauze): 1])
        request("Statistics pending ${ids.suffix}", RequisitionStatus.PENDING, [(ids.gauze): 1])
        request("Statistics canceled ${ids.suffix}", RequisitionStatus.CANCELED, [(ids.gauze): 1], new Date() - 10)

        when:
        Map all = tx { requisitionService.getRequisitionStatistics(requesting(), null, admin()) }
        Map recent = tx { requisitionService.getRequisitionStatistics(null, fulfilling(), null, new Date() - 2) }
        Map withoutCanceled = tx {
            requisitionService.getRequisitionStatistics(requesting(), fulfilling(), null, null, [RequisitionStatus.CANCELED])
        }

        then:
        all[RequisitionStatus.CREATED] >= 1
        all[RequisitionStatus.CANCELED] >= 1
        all.ALL == all.findAll { it.key != 'ALL' && it.key != 'MINE' }.values().sum()
        all.MINE == all.ALL
        recent[RequisitionStatus.CANCELED] == null
        recent[RequisitionStatus.PENDING] >= 1
        withoutCanceled[RequisitionStatus.CANCELED] == null
        withoutCanceled.ALL == all.ALL - all[RequisitionStatus.CANCELED]
    }

    void 'getRequisitions filters requests by the fields of an example and sorts them'() {
        given:
        String name = "Listed ${ids.suffix}"
        String requisitionId = request(name, RequisitionStatus.PENDING, [(ids.gauze): 2])
        String requestNumber = tx { Requisition.get(requisitionId).requestNumber }
        String today = new Date().format('d/MMM/yyyy')

        expect:
        requestNumber
        tx {
            Requisition example = new Requisition(destination: requesting(), origin: fulfilling(), status: RequisitionStatus.PENDING,
                    requestedBy: admin(), createdBy: admin(), updatedBy: admin(), type: RequisitionType.DEFAULT,
                    commodityClass: CommodityClass.MEDICATION)
            assert requisitionService.getRequisitions(example, params([q: requestNumber]))*.id == [requisitionId]
            ['origin', 'destination', 'createdBy', 'updatedBy', 'requestedBy', 'name'].each { String sort ->
                assert requisitionService.getRequisitions(new Requisition(), params([q: name, sort: sort, order: 'asc']))*.id ==
                        [requisitionId]
            }
            authService.currentUser = admin()
            assert requisitionService.getRequisitions(new Requisition(), params([q: name, relatedToMe: 'true',
                    requestedDateRange: "${today}-${new Date().plus(1).format('d/MMM/yyyy')}", commodityClassIsNull: '']))*.id ==
                    [requisitionId]
            assert requisitionService.getRequisitions(new Requisition(), params([q: name]), [fulfilling()], [requesting()])*.id ==
                    [requisitionId]
            assert requisitionService.getRequisitions(new Requisition(), params([q: name]), [requesting()], []) == []
            assert requisitionService.requisitions*.id.contains(requisitionId)
            true
        }
    }

    void 'saveRequisition from a map binds the request and its items and replaces the items'() {
        when:
        String requisitionId = tx {
            authService.currentUser = admin()
            Requisition requisition = requisitionService.saveRequisition([name: "Bound ${ids.suffix}", destination: requesting(),
                    requestedBy: admin(), status: RequisitionStatus.CREATED,
                    requisitionItems: [[product: gauze(), quantity: 3], [product: aspirin(), quantity: 4]]], fulfilling())
            return requisition.id
        }

        then:
        tx {
            Requisition requisition = Requisition.get(requisitionId)
            [requisition.origin.id, requisition.requestNumber != null,
             requisition.requisitionItems.collect { [it.product.id, it.quantity] } as Set]
        } == [ids.fulfilling, true, [[ids.gauze, 3], [ids.aspirin, 4]] as Set]

        when: 'it is saved again with one item kept'
        tx {
            Requisition requisition = Requisition.get(requisitionId)
            String keptId = requisition.requisitionItems.find { it.product.id == ids.gauze }.id
            requisitionService.saveRequisition([id: requisitionId, name: "Bound again ${ids.suffix}",
                    requisitionItems: [[id: keptId, quantity: 5]]], fulfilling())
        }

        then:
        tx {
            Requisition requisition = Requisition.get(requisitionId)
            [requisition.name, requisition.requisitionItems.collect { [it.product.id, it.quantity] }]
        } == ["Bound again ${ids.suffix}".toString(), [[ids.gauze, 5]]]
    }

    void 'a request is cloned, canceled, restored, cleared and deleted'() {
        given:
        String requisitionId = request("Lifecycle ${ids.suffix}", RequisitionStatus.PENDING, [(ids.gauze): 2, (ids.aspirin): 3])

        when:
        String cloneId = tx { requisitionService.cloneRequisition(Requisition.get(requisitionId)).id }

        then:
        tx {
            Requisition clone = Requisition.get(cloneId)
            [clone.name, clone.isPublished, clone.requisitionItems.collect { [it.product.id, it.quantity] } as Set]
        } == ["Copy of Lifecycle ${ids.suffix}".toString(), false, [[ids.gauze, 2], [ids.aspirin, 3]] as Set]

        when:
        tx { requisitionService.cancelRequisition(Requisition.get(requisitionId)) }

        then:
        tx { Requisition.get(requisitionId).status } == RequisitionStatus.CANCELED
        tx { requisitionService.getCanceledRequisitionItems(requesting(), gauze())*.requisition*.id.contains(requisitionId) }

        when:
        tx { requisitionService.undoCancelRequisition(Requisition.get(requisitionId)) }

        then:
        tx { Requisition.get(requisitionId).status } == RequisitionStatus.PENDING
        tx { requisitionService.getPendingRequisitionItems(gauze())*.requisition*.id.contains(requisitionId) }
        tx { requisitionService.getPendingRequisitionItems(fulfilling(), gauze())*.requisition*.id.contains(requisitionId) }

        when:
        tx { requisitionService.clearRequisition(Requisition.get(cloneId)) }
        tx { requisitionService.deleteRequisition(Requisition.get(requisitionId)) }

        then:
        tx { Requisition.get(cloneId).requisitionItems.size() } == 0
        tx { Requisition.get(requisitionId) } == null
    }

    void 'issueRequisition transfers the picked stock once'() {
        given:
        String requisitionId = request("Issued ${ids.suffix}", RequisitionStatus.CHECKING, [(ids.aspirin): 6])
        tx {
            Requisition requisition = Requisition.get(requisitionId)
            Picklist picklist = new Picklist(requisition: requisition, name: 'Pick')
            picklist.addToPicklistItems(new PicklistItem(inventoryItem: InventoryItem.get(ids.aspirinLot), quantity: 6,
                    requisitionItem: requisition.requisitionItems.first()))
            picklist.save(failOnError: true)
        }

        when:
        String transactionId = tx {
            authService.currentUser = admin()
            requisitionService.issueRequisition(Requisition.get(requisitionId), admin(), Person.get(ids.admin), 'issued').id
        }

        then:
        tx { Requisition.get(requisitionId).status } == RequisitionStatus.ISSUED
        tx { Transaction.get(transactionId).transactionType.id } == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID
        tx { inventoryService.getQuantity(fulfilling().inventory, InventoryItem.get(ids.aspirinLot)) } == 44
        tx { inventoryService.getQuantity(requesting().inventory, InventoryItem.get(ids.aspirinLot)) } == 6
        tx { requisitionService.getIssuedRequisitionItems(fulfilling(), aspirin(), new Date() - 1, new Date() + 1, null)*.requisition*.id } ==
                [requisitionId]
        tx {
            requisitionService.getIssuedRequisitionItems(fulfilling(), aspirin(), null, null, [ReasonCode.STOCKOUT.name()])*.requisition*.id
        } == [requisitionId]
        tx { requisitionService.getIssuedTransactionEntries(fulfilling(), aspirin(), new Date() - 1, new Date() + 1)*.quantity } == [6]
        tx { requisitionService.getCanceledRequisitionItems(fulfilling(), [], new Date() - 1, new Date() + 1, 10, 0)*.requisition*.id } ==
                [requisitionId]

        when: 'it is issued again'
        tx { requisitionService.issueRequisition(Requisition.get(requisitionId), admin(), null, null) }

        then:
        ValidationException again = thrown()
        again.errors.globalErrors*.code == ['Cannot create multiple outbound transaction for the same requisition']
    }

    void 'comments are added to a request and taken off again'() {
        given:
        String requisitionId = request("Comments ${ids.suffix}", RequisitionStatus.CREATED, [(ids.gauze): 1])

        when:
        String commentId = tx {
            Comment comment = requisitionService.saveComment(new Comment(comment: 'please hurry', sender: admin()))
            requisitionService.addCommentToRequisition(comment, Requisition.get(requisitionId))
            return comment.id
        }

        then:
        tx { Requisition.get(requisitionId).comments*.comment } == ['please hurry']

        when:
        tx {
            Requisition requisition = Requisition.get(requisitionId)
            requisitionService.deleteComment(Comment.get(commentId), requisition)
            requisition.save(flush: true)
        }

        then:
        tx { Requisition.get(requisitionId).comments.size() } == 0
    }

    void 'the request count of the fiscal year counts the requests of the location'() {
        given:
        request("Fiscal ${ids.suffix}", RequisitionStatus.CREATED, [(ids.gauze): 1])

        expect:
        tx { requisitionService.getRequisitionCountInCurrentFiscalYear(fulfilling()) } ==
                tx { Requisition.countByOriginAndIsTemplate(fulfilling(), false) }
    }

    void 'buildRequisitionItem creates the request of an import row and adds the product to it'() {
        given:
        String requestNumber = "IMP-${ids.suffix}"
        Map row = [productCode: ids.gauzeCode, quantity: '7', requestedDeliveryDate: org.joda.time.LocalDate.now().plusDays(10),
                   origin: "FUL-${ids.suffix}", destination: "REQ-${ids.suffix}", requestNumber: requestNumber]

        when:
        List result = tx {
            authService.currentUser = admin()
            RequisitionItem item = requisitionService.buildRequisitionItem(row)
            [item.quantity, item.requisition.requestNumber, item.requisition.origin.id, item.requisition.destination.id]
        }

        then:
        result == [7, requestNumber, ids.fulfilling, ids.requesting]

        when: 'the row asks for nothing'
        tx { requisitionService.buildRequisitionItem(row + [quantity: '0']) }

        then:
        IllegalArgumentException noQuantity = thrown()
        noQuantity.message == 'Requested quantity should be greater than 0'

        when: 'the delivery date is too soon'
        tx { requisitionService.buildRequisitionItem(row + [requestedDeliveryDate: org.joda.time.LocalDate.now().plusDays(2)]) }

        then:
        IllegalArgumentException tooSoon = thrown()
        tooSoon.message == 'Delivery date must be after seven days from now'

        when: 'the product is unknown'
        tx { requisitionService.buildRequisitionItem(row + [productCode: 'NO-SUCH-CODE']) }

        then:
        IllegalArgumentException unknown = thrown()
        unknown.message == 'Product not found for NO-SUCH-CODE'
    }

    void 'the exports list the requests and their items'() {
        given:
        String requisitionId = request("Exported ${ids.suffix}", RequisitionStatus.PENDING, [(ids.gauze): 2, (ids.aspirin): 3])

        when:
        List<String> csv = tx {
            Requisition requisition = Requisition.get(requisitionId)
            [requisitionService.exportRequisitions([requisition]), requisitionService.exportRequisitionItems([requisition])]
        }

        then:
        csv[0].readLines().size() == 2
        csv[0].contains('Requisition Number')
        csv[0].contains("Exported ${ids.suffix}")
        csv[1].readLines().size() == 3
        csv[1].contains(ids.gauzeCode)
        csv[1].contains(ids.aspirinCode)
    }

    @PendingFeature(reason = 'pending requisition items miss PENDING, VERIFYING, PICKING and PICKED requisitions')
    void 'the pending items of a location include the requests that are requested, being verified, picked or are pending'() {
        given:
        Map<RequisitionStatus, String> requests = [RequisitionStatus.CREATED, RequisitionStatus.REQUESTED,
                                                   RequisitionStatus.VERIFYING,
                                                   RequisitionStatus.PICKING, RequisitionStatus.PICKED,
                                                   RequisitionStatus.PENDING, RequisitionStatus.CHECKING,
                                                   RequisitionStatus.ISSUED, RequisitionStatus.RECEIVED,
                                                   RequisitionStatus.CANCELED].collectEntries { RequisitionStatus status ->
            [(status): request("Pending ${status} ${ids.suffix}", status, [(ids.gauze): 1])]
        }

        expect:
        tx { requests.collectEntries { status, id -> [(status): Requisition.get(id).status] } } ==
                requests.collectEntries { status, id -> [(status): status] }

        when:
        Set<String> pending = tx { requisitionService.getPendingRequisitionItems(fulfilling())*.requisition*.id as Set }

        then:
        requests.findAll { status, id -> id in pending }.keySet() == [RequisitionStatus.CREATED, RequisitionStatus.REQUESTED,
                RequisitionStatus.VERIFYING, RequisitionStatus.PICKING, RequisitionStatus.PICKED, RequisitionStatus.PENDING,
                RequisitionStatus.CHECKING] as Set
    }

    @PendingFeature(reason = 'the fiscal-year requisition count picks the wrong year around the start month')
    void 'the fiscal year that starts this month has begun'() {
        given:
        request("Fiscal month ${ids.suffix}", RequisitionStatus.CREATED, [(ids.gauze): 1])
        String start = Holders.config.openboxes.dashboard.yearTypes.fiscalYear.start
        String end = Holders.config.openboxes.dashboard.yearTypes.fiscalYear.end
        String firstOfMonth = String.format('%02d/01', new Date().month + 1)
        Holders.config.merge([openboxes: [dashboard: [yearTypes: [fiscalYear: [start: firstOfMonth, end: firstOfMonth]]]]])

        when:
        def count = tx { requisitionService.getRequisitionCountInCurrentFiscalYear(fulfilling()) }

        then:
        count > 0
        count == tx { Requisition.countByOriginAndIsTemplate(fulfilling(), false) }

        cleanup:
        Holders.config.merge([openboxes: [dashboard: [yearTypes: [fiscalYear: [start: start, end: end]]]]])
    }
}
