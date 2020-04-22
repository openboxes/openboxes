package org.pih.warehouse.tableroapi

import grails.gorm.transactions.Transactional
import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.Table
import org.pih.warehouse.tablero.ColorNumber
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.tablero.IndicatorDatasets
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.core.Location
import org.joda.time.LocalDate

@Transactional
class IndicatorDataService {

    def dashboardService

    DataGraph getExpirationSummaryData(Location location, def params) {
        Integer querySize = params.querySize ? params.querySize.toInteger() - 1 : 5

        // expirationSummary lists every expired item based on its month
        List expirationSummary = [0] * querySize
        List listLabels = []
        List expirationAlerts = dashboardService.getExpirationAlerts(location)

        LocalDate date = LocalDate.now()

        expirationAlerts.each {
            Integer daysCounter = 0
            // We should count only items that will expire someday
            if(it.inventoryItem.expires != "never") {
                // If an item is already inspired, we don't count it
                if (it.daysToExpiry > 0) {
                    listLabels = []
                    // For loop verifies if item expires in querySize coming months
                    for (int i=0; i<=querySize; i++) {
                        // daysCounter += number of days of (i) month
                        daysCounter += date.plusMonths(i).dayOfMonth().getMaximumValue()

                        String monthLabel = date.plusMonths(i).toString("MMM", Locale.US)
                        listLabels.push(monthLabel)
                        // if item expires in daysCounter incoming days, we count it
                        if (it.daysToExpiry <= daysCounter ) {
                            expirationSummary[i] =  expirationSummary[i] ? expirationSummary[i] + 1 : 1
                        }
                    }
                }
            }
        }

        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Expiration(s)', expirationSummary)
        ]

        IndicatorData data = new IndicatorData(datasets, listLabels)

        DataGraph indicatorData = new DataGraph(data, 1, "Expiration summary", "line")

        return indicatorData
    }

    DataGraph getFillRate() {
        List listData = []
        List bar2Data  = []
        List listLabel = []
        Date today = new Date()
        today.clearTime()
        for(int i=5;i>=0;i--){
            def monthBegin = today.clone()
            def monthEnd = today.clone()
            monthBegin.set(month: today.month - i, date: 1)
            monthEnd.set(month: today.month - i + 1, date: 1)

            def query1 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ?""", [monthBegin, monthEnd])

            def query2 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ? and quantityCanceled > 0 and (cancelReasonCode = 'STOCKOUT' or cancelReasonCode = 'LOW_STOCK' or cancelReasonCode = 'COULD_NOT_LOCATE')""", [monthBegin, monthEnd])
            String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

            listLabel.push(monthLabel)
            listData.push(query1[0])
            bar2Data.push(query2[0])
        }

        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Line1 Dataset', listData, 'line'),
            new IndicatorDatasets('Line2 Dataset', [15, 15, 15, 15, 15, 15], 'line'),
            new IndicatorDatasets('Bar1 Dataset', listData),
            new IndicatorDatasets('Bar2 Dataset', bar2Data),
        ]

        IndicatorData data = new IndicatorData(datasets, listLabel)

        DataGraph indicatorData = new DataGraph(data, 1, "Fill rate", "line")

        return indicatorData
    }

    DataGraph getInventorySummaryData(def results) {
        def inStockCount = results.findAll {
            it.quantityOnHand > 0
        }.size()
        def lowStockCount = results.findAll {
            it.quantityOnHand > 0 && it.quantityOnHand <= it.minQuantity
        }.size()
        def reoderStockCount = results.findAll {
            it.quantityOnHand > it.minQuantity && it.quantityOnHand <= it.reorderQuantity
        }.size()
        def overStockCount = results.findAll {
            it.quantityOnHand > it.reorderQuantity && it.quantityOnHand <= it.maxQuantity
        }.size()
        def stockOutCount = results.findAll {
            it.quantityOnHand <= 0
        }.size()
        //def totalCount = results.size()

        def inventoryData = [
                    inStockCount    : inStockCount,
                    lowStockCount   : lowStockCount,
                    reoderStockCount: reoderStockCount,
                    overStockCount  : overStockCount,
                    stockOutCount   : stockOutCount,
                    //totalCount      : totalCount
                ]

        List listData = []
        for(item in inventoryData){
            listData.push(item.value? item.value : 0)
        }

        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Inventory Summary', listData)
        ]

        IndicatorData data = new IndicatorData(datasets, ['In stock', 'Above maximum', 'Below reorder', 'Below minimum', 'No longer in stock'])

        DataGraph indicatorData = new DataGraph(data, 1, "Inventory Summary", "horizontalBar")

        return indicatorData
    }

    DataGraph getSentStockMovements(Location location, def params) {
        Integer querySize = params.querySize? params.querySize.toInteger()-1 : 5
        Date today = new Date()
        today.clearTime()

        // queryLimit limits the query and avoid of getting data older than wanted
        Date queryLimit = today.clone()
        queryLimit.set(month: today.month - querySize, date: 1)

        List queryData = Shipment.executeQuery("""SELECT COUNT(s.id), s.destination, 
        MONTH(s.lastUpdated), YEAR(s.lastUpdated) FROM Shipment s WHERE s.origin = :location 
        AND s.currentStatus <> 'PENDING' AND s.lastUpdated > :limit 
        GROUP BY MONTH(s.lastUpdated), YEAR(s.lastUpdated), s.destination""",
        ['location': location, 'limit': queryLimit])
        // queryData gives an array of arrays [[count, destination, month, year], ...] of sent stock

        Map listRes = [:]
        List listLabel = []

        for (item in queryData) {
            // item[0]: item total counted
            // item[1]: item destination
            // item[2]: item month
            // item[3]: item year

            Location itemLocation = item[1]

            // If the destination is new, add it to the list with empty values for now
            if (listRes.get(itemLocation.name) == null) {
                listRes.put(itemLocation.name, new IndicatorDatasets(itemLocation.name, [0] * querySize))
            }

            for (int i = querySize; i >= 0; i--) {
                Date tmpDate = today.clone()
                tmpDate.set(month: today.month - i, date: 1)

                // If there is data, update the dataset in the proper position
                if (tmpDate.month == item[2] - 1 && tmpDate.year + 1900 == item[3]) {
                    Integer value = item[0]
                    IndicatorDatasets locationDataset = listRes.get(itemLocation.name)
                    locationDataset.data[querySize - i] = value
                }

                // If the list of labels is incomplete, add the label
                if (listLabel.size() <= querySize) {
                    String monthLabel = new java.text.DateFormatSymbols().months[tmpDate.month].substring(0,3)
                    listLabel.push(monthLabel)
                }
            }
        }
        List<IndicatorDatasets> datasets = (List<IndicatorDatasets>) listRes.values().toList()

        IndicatorData data = new IndicatorData(datasets, listLabel)

        DataGraph indicatorData = new DataGraph(data, 1, "Stock Movements Sent by Month", "bar")

        return indicatorData
    }

    DataGraph getReceivedStockData(Location location, def params) {
        Integer querySize = params.querySize? params.querySize.toInteger() - 1 : 5
        Date today = new Date()
        today.clearTime()

        Date queryLimit = today.clone()
        queryLimit.set(month: today.month - querySize, date: 1)

        List queryData = Shipment.executeQuery("""SELECT COUNT(s.id), s.origin, 
        MONTH(s.lastUpdated), YEAR(s.lastUpdated) FROM Shipment s WHERE s.destination = :location 
        AND s.currentStatus <> 'PENDING' AND s.lastUpdated > :limit 
        GROUP BY MONTH(s.lastUpdated), YEAR(s.lastUpdated), s.origin""",
        ['location': location, 'limit': queryLimit])

        Map listRes = [:]
        List listLabel = []

        for (item in queryData) {
            // item[0]: item total counted
            // item[1]: item origin
            // item[2]: item month
            // item[3]: item year

            Location itemLocation = item[1]

            // If the origin is new, add it to the list with empty values for now
            if (listRes.get(itemLocation.name) == null) {
                listRes.put(itemLocation.name, new IndicatorDatasets(itemLocation.name, [0] * querySize))
            }

            for (int i = querySize; i >= 0; i--) {
                Date tmpDate = today.clone()
                tmpDate.set(month: today.month - i, date: 1)

                // If there is data, update the dataset in the proper position
                if (tmpDate.month == item[2] - 1 && tmpDate.year + 1900 == item[3]) {
                    Integer value = item[0]
                    IndicatorDatasets locationDataset = listRes.get(itemLocation.name)
                    locationDataset.data[querySize - i] = value
                }

                // If the list of labels is incomplete, add the label
                if (listLabel.size() <= querySize) {
                    String monthLabel = new java.text.DateFormatSymbols().months[tmpDate.month].substring(0,3)
                    listLabel.push(monthLabel)
                }
            }
        }
        List<IndicatorDatasets> datasets = (List<IndicatorDatasets>) listRes.values().toList()

        IndicatorData data = new IndicatorData(datasets, listLabel)

        DataGraph indicatorData = new DataGraph(data, 1, "Stock Movements Received by Month", "bar")

        return indicatorData
    }

    NumberIndicator getOutgoingStock(Location location) {
        Date today = new Date()
        today.clearTime()
        def m4 = today - 4
        def m7 = today - 7

        def greenData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated > :day and r.origin = :location and r.status <> 'ISSUED'""",
        ['day': m4, 'location': location])

        def yellowData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :dayOne and r.dateCreated <= :dayTwo and r.origin = :location and r.status <> 'ISSUED'""",
        ['dayOne': m7, 'dayTwo': m4, 'location': location])

        def redData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated < :day and r.origin = :location and r.status <> 'ISSUED'""",
        ['day': m7, 'location': location])

        ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago')
        ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago')
        ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago')

        NumberIndicator indicatorData = new NumberIndicator(green, yellow, red)

        return indicatorData
    }

    NumberIndicator getIncomingStock(Location location) {

        def query = Shipment.executeQuery("""select s.currentStatus, count(s) from Shipment s where s.destination = :location and s.currentStatus <> 'RECEIVED' group by s.currentStatus""",
        ['location': location])

        // Initial state
        ColorNumber pending = new ColorNumber(0, 'Pending', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=PENDING')
        ColorNumber shipped = new ColorNumber(0, 'Shipped', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=SHIPPED')
        ColorNumber partiallyReceived = new ColorNumber(0, 'Partially Received', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=PARTIALLY_RECEIVED')

        // Changes each ColorNumber if found in query
        query.each {
            if (it[0].name == 'PENDING') {
                pending = new ColorNumber(it[1], 'Pending', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=PENDING')
            } else if (it[0].name == 'SHIPPED') {
                shipped = new ColorNumber(it[1], 'Shipped', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=SHIPPED')
            } else if (it[0].name == 'PARTIALLY_RECEIVED') {
                partiallyReceived = new ColorNumber(it[1], 'Partially Received', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=PARTIALLY_RECEIVED')
            }
        }

        NumberIndicator indicatorData = new NumberIndicator(pending, shipped, partiallyReceived, false)

        return indicatorData
    }

    def getDiscrepancy(Location location, def params) {
        Integer querySize = params.querySize? params.querySize.toInteger() - 1 : 5

        Date date = LocalDate.now().minusMonths(querySize).toDate()

        def results = ReceiptItem.executeQuery("""
            select 
                s.id,
                s.shipmentNumber, 
                s.name, 
                si.id,
                si.quantity,
                count(ri.id), 
                s.requisition.id
            from ShipmentItem as si
            left outer join si.receiptItems as ri
            join ri.receipt as r
            join r.shipment as s
            where 
                s.currentStatus = 'RECEIVED'
                and s.destination = :location 
                and r.actualDeliveryDate > :date 
            group by s.shipmentNumber, s.id, si.id, si.quantity
            having si.quantity <> sum(ri.quantityReceived)
        """, ['location': location, 'date': date])

        // Transform to map
        results = results.collect { [
                shipmentId: it[0],
                shipmentNumber: it[1],
                shipmentName: it[2]
            ]
        }

        // Find discrepancies by shipment
        Map discrepenciesByShipmentId =
                results.inject([:]) { map, row ->
                    // Initialize map entry for shipment id
                    if (!map[row.shipmentId])
                        map[row.shipmentId] = row << [count: 0]

                    // Each new shipment row in teh results should increment count
                    map[row.shipmentId].count += 1
                    return map
                }

        List<TableData> tableBody = discrepenciesByShipmentId.keySet().collect {
            def row = discrepenciesByShipmentId[it]
            return new TableData(row.shipmentNumber,
                    row.shipmentName,
                    row.count.toString(),
                    "/openboxes/stockMovement/show/${row.shipmentId}"
            )
        }

        Table indicatorData = new Table("Shipment", "Name", "Discrepancy", tableBody)

        return indicatorData
    }
}
