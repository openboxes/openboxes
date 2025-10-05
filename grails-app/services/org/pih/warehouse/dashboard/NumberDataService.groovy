package org.pih.warehouse.dashboard

import grails.core.GrailsApplication
import grails.plugin.cache.Cacheable
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.joda.time.LocalDate
import org.pih.warehouse.DateUtil
import org.springframework.web.context.request.RequestContextHolder
import grails.web.servlet.mvc.GrailsParameterMap
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.inventory.InventoryItemService
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.putaway.PutawayTaskService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.ShipmentStatusCode
import util.ConfigHelper

class NumberDataService {

    def dataService
    LocationService locationService
    PutawayTaskService putawayTaskService
    GrailsApplication grailsApplication
    InventoryItemService inventoryItemService

    @Cacheable(value = "dashboardCache", key = { "getInventoryByLotAndBin-${location?.id}"})
    NumberData getInventoryByLotAndBin(Location location) {
        def binLocations = ProductAvailability.executeQuery("select count(*) from ProductAvailability pa where pa.location = :location and pa.quantityOnHand > 0",
                ['location': location])

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(binLocations[0], "${urlContextPath}/report/showBinLocationReport?location.id=" + location.id + "&status=inStock")
    }

    @Cacheable(value = "dashboardCache", key = { "getLostAndFoundInventoryItems-${location?.id}" })
    NumberData getLostAndFoundInventoryItems(Location location) {

        def lostAndFoundLocations = locationService.getLocationsSupportingActivity(ActivityCode.LOST_AND_FOUND)
        def locationIds = lostAndFoundLocations.findAll { it.parentLocation?.id == location.id }*.id

        Long countLostAndFound = 0
        if (locationIds) {
            def results = ProductAvailability.executeQuery("""
                SELECT COUNT(DISTINCT pa.id)
                FROM ProductAvailability pa
                WHERE pa.location = :facility
                  AND pa.binLocation.id IN (:locationIds)
            """, [facility: location, locationIds: locationIds])
            countLostAndFound = results[0] ?: 0
        }

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(
                countLostAndFound,
                "${urlContextPath}/report/showLostAndFoundReport?location.id=${location.id}"
        )
    }

    @Cacheable(value = "dashboardCache", key = { "getInProgressShipments-${location?.id}${user?.id}" })
    NumberData getInProgressShipments(User user, Location location) {

        OrderType returnOrderType = OrderType.get(Constants.RETURN_ORDER)

        def requisitionShipmentCount = Requisition.executeQuery("""
                    SELECT COUNT(*) FROM Requisition r 
                        JOIN r.shipments s 
                        WHERE r.origin = :location 
                        AND s.currentStatus = 'PENDING' 
                        AND r.status NOT IN (:statuses)
                        AND r.createdBy = :user
                """,
                [
                        'location': location,
                        'user': user,
                        'statuses' : [RequisitionStatus.PENDING_APPROVAL, RequisitionStatus.REJECTED],
                ]).get(0)

        def returnOrderShipmentCount = Order.executeQuery("""
                    SELECT COUNT(DISTINCT o.id) FROM Order o
                    LEFT JOIN o.orderItems oi
                    LEFT JOIN oi.shipmentItems si
                    LEFT JOIN si.shipment s
                    WHERE o.origin = :location
                    AND o.orderType = :orderType
                    AND s.currentStatus = 'PENDING' 
                    AND o.createdBy = :user
                """,
                ['location': location, 'user': user, 'orderType': returnOrderType]).get(0)

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(
                requisitionShipmentCount + returnOrderShipmentCount,
                "${urlContextPath}/stockMovement/list?direction=OUTBOUND&receiptStatusCode=PENDING&origin=" + location.id + "&createdBy=" + user.id
        )
    }

    @Cacheable(value = "dashboardCache", key = { "getInProgressPutaways-${location?.id}${user?.id}" })
    NumberData getInProgressPutaways(User user, Location location) {
        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderType = :orderType AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location",
                ['user': user, 'location': location, 'orderType': OrderType.findByCode(Constants.PUTAWAY_ORDER)])

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(incompletePutaways[0], "${urlContextPath}/order/list?orderType=PUTAWAY_ORDER&status=PENDING&orderedBy=" + user.id)
    }

    @Cacheable(value = "dashboardCache", key = { "getReceivingBin-${location?.id}" })
    NumberData getReceivingBin(Location location) {
        def receivingBin = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.product.id) from ProductAvailability pa 
            LEFT JOIN pa.location l 
            LEFT JOIN pa.binLocation bl
            WHERE l = :location AND pa.quantityOnHand > 0 
            AND bl.locationType.id = :locationType""",
                [
                    'location'    : location,
                    'locationType': Constants.RECEIVING_LOCATION_TYPE_ID,
                ])

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(receivingBin[0], "${urlContextPath}/report/showBinLocationReport?status=inStock&activityCode=RECEIVE_STOCK")
    }

    @Cacheable(value = "dashboardCache", key = { "getItemsInventoried-${location?.id}" })
    NumberData getItemsInventoried(Location location) {
        Date firstOfMonth = LocalDate.now().withDayOfMonth(1).toDate()
        return getItemsInventoriedInRange(location, firstOfMonth)
    }

    /**
     * Fetch the count of distinct products that have been inventoried in the given time range.
     * "Inventoried" means any operation that performs a full quantity count for the product.
     *
     * @param location The facility that we want the count at.
     * @param startDate The datetime in the past to start the check at. If null, will use the start of time.
     * @param endDate The datetime in the past to check up until. If null, will use the current time.
     */
    NumberData getItemsInventoriedInRange(Location location, Date startDate=null, Date endDate=null) {
        /**
        * After migrating from single product inventory transaction to baseline + adjustment transactions
        * there is a case when we might have only an adjustment if it's first inventory record
        * and we can't rely on CREDIT type code for this case and ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
        * is only used for baseline + adjustment and for manual stock adjustment
        */
        List<String> transactionTypeIds = [
                Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID,
                Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID,
                Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID
        ]

        List<TransactionEntry> itemsInventoried = TransactionEntry.executeQuery("""
            SELECT COUNT(distinct ii.product.id) from TransactionEntry te
            INNER JOIN te.inventoryItem ii
            INNER JOIN te.transaction t
            WHERE t.inventory = :inventory
            AND t.transactionType.id IN :transactionTypeIds
            AND (t.comment <> :commentToFilter OR t.comment IS NULL)
            AND t.transactionDate BETWEEN :startDate AND :endDate""",
                [
                        inventory          : location?.inventory,
                        transactionTypeIds : transactionTypeIds,
                        startDate          : startDate ?: DateUtil.EPOCH_DATE,
                        endDate            : endDate ?: new Date(),
                        commentToFilter    : Constants.INVENTORY_BASELINE_MIGRATION_TRANSACTION_COMMENT
                ])

        return new NumberData(itemsInventoried[0] as Double)
    }

    @Cacheable(value = "dashboardCache", key = { "getDefaultBin-${location?.id}" })
    NumberData getDefaultBin(Location location) {
        def productsInDefaultBin = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.product.id) FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand > 0
            AND pa.binLocation is null""",
                [
                    'location': location
                ])

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(productsInDefaultBin[0], "${urlContextPath}/report/showBinLocationReport?location.id=" + location.id + "&status=inStock")
    }

    @Cacheable(value = "dashboardCache", key = { "getProductWithNegativeInventory-${location?.id}" })
    NumberData getProductWithNegativeInventory(Location location) {

        def productsWithNegativeInventory = ProductAvailability.executeQuery("""
            SELECT pa.productCode, pa.product.name, pa.lotNumber, pa.binLocationName, pa.quantityOnHand
            FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand < 0
            ORDER BY pa.quantityOnHand ASC
            """,
                [
                        'location': location
                ])

        def numberOfProducts = productsWithNegativeInventory.size()

        String tooltipData = null

        if (numberOfProducts) {
            // Display only the first item in the tooltip
            // productsWithNegativeInventory[0][0] product code
            // productsWithNegativeInventory[0][1] Product name
            // productsWithNegativeInventory[0][2] Lot number
            // productsWithNegativeInventory[0][3] Bin location name
            // productsWithNegativeInventory[0][4] Quantity on hand
            tooltipData = """\
                Code: ${productsWithNegativeInventory[0][0]}
                Name: ${productsWithNegativeInventory[0][1]}
                Lot number: ${productsWithNegativeInventory[0][2]}
                Bin location: ${productsWithNegativeInventory[0][3]}
                Quantity: ${productsWithNegativeInventory[0][4]}"""
            tooltipData = tooltipData.stripIndent()
        }

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(numberOfProducts, "${urlContextPath}/report/showBinLocationReport?location.id=" + location.id, tooltipData)
    }

    @Cacheable(value = "dashboardCache", key = { "getExpiredProductsInStock-${location?.id}" })
    NumberData getExpiredProductsInStock(Location location) {
        Date today = LocalDate.now().toDate()
        def expiredProductsInStock = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.inventoryItem) FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand > 0
            AND pa.inventoryItem.expirationDate < :today
            """,
                [
                    'location': location,
                    'today' : today,
                ])

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(expiredProductsInStock[0], "${urlContextPath}/inventory/listExpiredStock?status=expired")
    }

    @Cacheable(value = "dashboardCache", key = { "getOpenStockRequests-${location?.id}" })
    NumberData getOpenStockRequests(Location location) {
        def openStockRequests = Requisition.executeQuery("""
            SELECT COUNT(distinct r.id) FROM Requisition r
            WHERE r.origin = :location
            AND r.sourceType = :sourceType
            AND r.status NOT IN (:statuses)
            """,
                [
                        'location': location,
                        'sourceType' : RequisitionSourceType.ELECTRONIC,
                        'statuses' : [
                                RequisitionStatus.CREATED,
                                RequisitionStatus.ISSUED,
                                RequisitionStatus.CANCELED,
                                RequisitionStatus.PENDING_APPROVAL,
                                RequisitionStatus.REJECTED,
                        ],
                ])

        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(openStockRequests[0], "${urlContextPath}/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC")
    }

    @Cacheable(value = "dashboardCache", key = { "getRequestsPendingApproval-${location?.id}-${currentUser?.id}" })
    NumberData getRequestsPendingApproval(Location location, User currentUser) {
        int requisitionCount = Requisition.createCriteria().get {
            projections {
                count('id')
            }
            eq("origin", location)
            eq("sourceType", RequisitionSourceType.ELECTRONIC)
            eq("status",  RequisitionStatus.PENDING_APPROVAL)
        }

        ApplicationTagLib g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')

        Map linkParams = [
            direction: StockMovementDirection.OUTBOUND,
            sourceType: RequisitionSourceType.ELECTRONIC,
        ]

        if(currentUser.hasRoles(location, [RoleType.ROLE_REQUISITION_APPROVER])) {
            linkParams.requisitionStatusCode = RequisitionStatus.PENDING_APPROVAL
        }

        String redirectLink = g.createLink(controller: "stockMovement", action: "list", params: linkParams)

        return new NumberData(requisitionCount, redirectLink)
    }

    @Cacheable(value = "dashboardCache", key = { "getInventoryValue-${location?.id}" })
    NumberData getInventoryValue(Location location) {
        def inventoryValue = ProductAvailability.executeQuery("""select sum (pa.quantityOnHand * p.pricePerUnit) 
                from ProductAvailability as pa
                inner join pa.product as p 
                where pa.location = :location""",
                ['location': location])

        return new NumberData(inventoryValue[0])
    }

    @Cacheable(value = "dashboardCache", key = { "getOpenPurchaseOrdersCount-${params?.value}" })
    NumberData getOpenPurchaseOrdersCount(Map params) {
        def openPurchaseOrdersCount = 0

        if (params.value) {
            def supplierIds = params.list('value').toList().collect { "'$it'" }.join(',')
            def pendingShipmentStatues = ShipmentStatusCode.listPending().collect { "'$it'" }.join(',')
            def pendingOrderStatuses = OrderStatus.listPending().collect { "'$it'" }.join(',')
            def openPurchaseOrders = dataService.executeQuery("""
                SELECT 
                    COUNT(DISTINCT o.id) AS openPurchaseOrdersCount
                FROM `order` AS o
                    LEFT OUTER JOIN order_item ON o.id = order_item.order_id
                    LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
                    LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
                    LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
                WHERE 
                    o.order_type_id = '${OrderTypeCode.PURCHASE_ORDER.name()}' AND 
                    o.origin_id in (${supplierIds}) AND 
                    (
                        (shipment.id IS NOT NULL AND shipment.current_status IN (${pendingShipmentStatues})) OR 
                        (shipment.id IS NULL AND o.status IN (${pendingOrderStatuses}))
                    )
            """)

            openPurchaseOrdersCount = openPurchaseOrders?.size() ? openPurchaseOrders[0].openPurchaseOrdersCount : 0
        }

        return new NumberData(openPurchaseOrdersCount)
    }

    @Cacheable(value = "dashboardCache", key = { "getOpenPutawayTasks-${location?.id}" })
    NumberData getOpenPutawayTasks(Location location) {
        def webRequest = RequestContextHolder.requestAttributes
        def grailsParams = new GrailsParameterMap([:], webRequest.request)
        grailsParams.status = PutawayTaskStatus.PENDING
        def tasks = putawayTaskService.search(location, null, null, null, grailsParams)
        String urlContextPath = ConfigHelper.contextPath
        return new NumberData(
                tasks?.size() ?: 0,
                "${urlContextPath}/order/list?orderType=PUTAWAY_ORDER&status=PENDING"
        )
    }

    @Cacheable(value = "dashboardCache", key = { "getInboundSortationItems-${location?.id}" })
    NumberData getInboundSortationItems(Location location) {
        Long countInbound = inventoryItemService.countByActivity(location, ActivityCode.INBOUND_SORTATION)
        String urlContextPath = ConfigHelper.contextPath
        // TODO: Must implement filtering by the activity code to ensure the report matches the dashboard count.
        return new NumberData(
                countInbound,
                "${urlContextPath}/report/showBinLocationReport?location.id=${location.id}&status=inStock&activityCode=INBOUND_SORTATION"
        )
    }

    @Cacheable(value = "dashboardCache", key = { "getAverageInboundSortationTime-${facility?.id}" })
    NumberData getAverageInboundSortationTime(Location facility) {
        // TODO: Replace mock with call to {{baseUrl}}/openboxes/api/facilities/1/putaway-metrics?period=today endpoint
        BigDecimal averageMinutes = putawayTaskService.getAveragePutawayCycleTime(facility, new Date()-1, new Date())
        return new NumberData(averageMinutes, null)

    }
}
