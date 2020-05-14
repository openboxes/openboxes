package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.GraphData
import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.Table
import org.pih.warehouse.tablero.ColorNumber
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.tablero.IndicatorDatasets
import org.pih.warehouse.tablero.NumberTableData
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.core.Location
import org.joda.time.LocalDate

class IndicatorDataService {

    def dashboardService

    GraphData getExpirationSummaryData(Location location, def params) {
        // querySize = value of the date filter (1 month, 3 months, etc.)
        // Here it represents the last month we want to show
        // Add + 1 to include today (expired items) as the first point
        Integer querySize = params.querySize ? params.querySize.toInteger() + 1 : 7

        LocalDate date = LocalDate.now()

        List expirationSummary = [0] * querySize

        List linksExpirationSummary = [""] * querySize
        List listLabels = []

        // Fill labels and links
        for (int i = 0; i < querySize; i++) {
            Integer daysCounter = i * 30

            String monthLabel = (i == 0) ? "today" : "within " + daysCounter + " days"
            listLabels.push(monthLabel)

            // Expired items
            if (i == 0) {
                linksExpirationSummary[0] = "/openboxes/inventory/listExpiredStock?status=expired"
            }

            // 1, 3 and 6 months
            if (i == 1 || i == 3 || i == 6) {
                linksExpirationSummary[i] = "/openboxes/inventory/listExpiringStock?status=within" + daysCounter + "Days"
            }
            // 12 month will be 360 days but will link to 365 in the report
            if (i == 12) {
                linksExpirationSummary[i] = "/openboxes/inventory/listExpiringStock?status=within365Days"
            }
        }

        List expirationAlerts = dashboardService.getExpirationAlerts(location)

        expirationAlerts.each {
            // Count only items that expire
            if (it.inventoryItem.expires != "never") {
                // The first element of the dataset represents expired items
                if (it.daysToExpiry <= 0) {
                    expirationSummary[0] += 1
                } else {
                    // Verifies if item expires within querySize * 30 days
                    for (int i = 1; i < querySize; i++) {
                        Integer daysCounter = i * 30

                        // if item expires in daysCounter incoming days, count it
                        if (it.daysToExpiry <= daysCounter) {
                            expirationSummary[i] += 1
                        }
                    }
                }
            }
        }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Expiration(s)', expirationSummary, linksExpirationSummary)
        ]

        IndicatorData indicatorData = new IndicatorData(datasets, listLabels)

        GraphData graphData = new GraphData(indicatorData, "Expiration summary", "line", "/openboxes/inventory/listExpiringStock")

        return graphData
    }

    GraphData getFillRate() {
        List listData = []
        List bar2Data = []
        List listLabel = []
        Date today = new Date()
        today.clearTime()
        for (int i = 5; i >= 0; i--) {
            def monthBegin = today.clone()
            def monthEnd = today.clone()
            monthBegin.set(month: today.month - i, date: 1)
            monthEnd.set(month: today.month - i + 1, date: 1)

            def query1 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ?""", [monthBegin, monthEnd]);

            def query2 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ? and quantityCanceled > 0 and (cancelReasonCode = 'STOCKOUT' or cancelReasonCode = 'LOW_STOCK' or cancelReasonCode = 'COULD_NOT_LOCATE')""", [monthBegin, monthEnd]);
            String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

            listLabel.push(monthLabel)
            listData.push(query1[0])
            bar2Data.push(query2[0])
        }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Line1 Dataset', listData, null, 'line'),
                new IndicatorDatasets('Line2 Dataset', [15, 15, 15, 15, 15, 15], null, 'line'),
                new IndicatorDatasets('Bar1 Dataset', listData),
                new IndicatorDatasets('Bar2 Dataset', bar2Data),
        ];

        IndicatorData indicatorData = new IndicatorData(datasets, listLabel);

        GraphData graphData = new GraphData(indicatorData, "Fill rate", "bar");

        return graphData;
    }

    GraphData getInventorySummaryData(def location) {
        def inventorySummary = dashboardService.getDashboardAlerts(location);

        def inventoryData = [
                inStockCount    : inventorySummary.inStock,
                overStockCount  : inventorySummary.overStock,
                reoderStockCount: inventorySummary.reorderStock,
                lowStockCount   : inventorySummary.lowStock,
                stockOutCount   : inventorySummary.onHandQuantityZero,
        ];

        List listData = []
        for (item in inventoryData) {
            listData.push(item.value ? item.value : 0)
        }

        List<String> links = [
                "/openboxes/inventory/listInStock",
                "/openboxes/inventory/listOverStock",
                "/openboxes/inventory/listReorderStock",
                "/openboxes/inventory/listLowStock",
                "/openboxes/inventory/listQuantityOnHandZero"]

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Inventory Summary', listData, links)
        ];

        IndicatorData indicatorData = new IndicatorData(datasets, ['In stock', 'Above maximum', 'Below reorder', 'Below minimum', 'No longer in stock']);

        GraphData graphData = new GraphData(indicatorData, "Inventory Summary", "horizontalBar");

        return graphData;
    }

    GraphData getSentStockMovements(Location location, def params) {
        Integer querySize = params.querySize ? params.querySize.toInteger() - 1 : 5
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
                listRes.put(itemLocation.name, new IndicatorDatasets(itemLocation.name, [0] * (querySize + 1)))
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
                    String monthLabel = new java.text.DateFormatSymbols().months[tmpDate.month].substring(0, 3)
                    listLabel.push(monthLabel)
                }
            }
        }
        List<IndicatorDatasets> datasets = (List<IndicatorDatasets>) listRes.values().toList();

        IndicatorData indicatorData = new IndicatorData(datasets, listLabel);

        GraphData graphData = new GraphData(indicatorData, "Stock Movements Sent by Month", "bar");

        return graphData;
    }

    GraphData getReceivedStockData(Location location, def params) {
        Integer querySize = params.querySize ? params.querySize.toInteger() - 1 : 5
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
                listRes.put(itemLocation.name, new IndicatorDatasets(itemLocation.name, [0] * (querySize + 1)))
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
                    String monthLabel = new java.text.DateFormatSymbols().months[tmpDate.month].substring(0, 3)
                    listLabel.push(monthLabel)
                }
            }
        }
        List<IndicatorDatasets> datasets = (List<IndicatorDatasets>) listRes.values().toList();

        IndicatorData indicatorData = new IndicatorData(datasets, listLabel);

        GraphData graphData = new GraphData(indicatorData, "Stock Movements Received by Month", "bar");

        return graphData;
    }

    GraphData getOutgoingStock(Location location) {
        Date today = new Date()
        today.clearTime();
        def m4 = today - 4;
        def m7 = today - 7;

        def greenData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated > :day and r.origin = :location and r.status <> 'ISSUED'""",
                ['day': m4, 'location': location]);

        def yellowData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :dayOne and r.dateCreated <= :dayTwo and r.origin = :location and r.status <> 'ISSUED'""",
                ['dayOne': m7, 'dayTwo': m4, 'location': location]);

        def redData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated < :day and r.origin = :location and r.status <> 'ISSUED'""",
                ['day': m7, 'location': location]);

        ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago');
        ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago');
        ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago');

        NumberIndicator numberIndicator = new NumberIndicator(green, yellow, red)

        GraphData graphData = new GraphData(numberIndicator, "Outgoing Stock Movements in Progress", "numbers", "/openboxes/stockMovement/list?receiptStatusCode=PENDING");

        return graphData;
    }

    GraphData getIncomingStock(Location location) {

        def query = Shipment.executeQuery("""select s.currentStatus, count(s) from Shipment s where s.destination = :location and s.currentStatus <> 'RECEIVED' group by s.currentStatus""",
                ['location': location]);

        // Initial state
        ColorNumber pending = new ColorNumber(0, 'Pending', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=PENDING');
        ColorNumber shipped = new ColorNumber(0, 'Shipped', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=SHIPPED');
        ColorNumber partiallyReceived = new ColorNumber(0, 'Partially Received', '/openboxes/stockMovement/list?direction=INBOUND&receiptStatusCode=PARTIALLY_RECEIVED');

        // Changes each ColorNumber if found in query
        query.each {
            if (it[0].name == 'PENDING') {
                pending.value = it[1]
            } else if (it[0].name == 'SHIPPED') {
                shipped.value = it[1]
            } else if (it[0].name == 'PARTIALLY_RECEIVED') {
                partiallyReceived.value = it[1]
            }
        }

        NumberIndicator numberIndicator = new NumberIndicator(pending, shipped, partiallyReceived)

        GraphData graphData = new GraphData(numberIndicator, "Incoming Stock Movements By Status", "numbers", "/openboxes/stockMovement/list?direction=INBOUND");

        return graphData;
    }

    GraphData getDiscrepancy(Location location, def params) {
        Integer querySize = params.querySize ? params.querySize.toInteger() - 1 : 5

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
        results = results.collect {
            [
                    shipmentId    : it[0],
                    shipmentNumber: it[1],
                    shipmentName  : it[2]
            ]
        }

        // Find discrepancies by shipment
        Map discrepenciesByShipmentId =
                results.inject([:]) { map, row ->
                    // Initialize map entry for shipment id
                    if (!map[row.shipmentId])
                        map[row.shipmentId] = row << [count: 0];

                    // Each new shipment row in teh results should increment count
                    map[row.shipmentId].count += 1;
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

        Table tableData = new Table("Shipment", "Name", "Discrepancy", tableBody)

        GraphData graphData = new GraphData(tableData, "Items received with a discrepancy", "table");

        return graphData;
    }

    GraphData getDelayedShipments(Location location) {
        Date oneWeekAgo = LocalDate.now().minusWeeks(1).toDate()
        Date oneMonthAgo = LocalDate.now().minusMonths(1).toDate()
        Date twoMonthsAgo = LocalDate.now().minusMonths(2).toDate()

        def results = Shipment.executeQuery("""
            select s.shipmentType.id, s.shipmentNumber, s.name, s.id
            from Shipment as s
            inner join s.currentEvent as e
            where s.destination = :location
            and s.currentStatus in ('SHIPPED', 'PARTIALLY_RECEIVED')
            and (
                (s.shipmentType.id = 1 and e.eventDate < :oneMonthAgo)
                or (s.shipmentType.id = 2 and e.eventDate < :twoMonthsAgo)
                or (s.shipmentType.id in (3, 4) and e.eventDate < :oneWeekAgo)
            )
        """, [
                'location'    : location,
                'oneWeekAgo'  : oneWeekAgo,
                'oneMonthAgo' : oneMonthAgo,
                'twoMonthsAgo': twoMonthsAgo
        ])

        def numberDelayed = [
                air            : 0,
                sea            : 0,
                landAndSuitcase: 0,
        ]

        results = results.collect {
            if (it[0] == '1') numberDelayed['air'] += 1
            else if (it[0] == '2') numberDelayed['sea'] += 1
            else numberDelayed['landAndSuitcase'] += 1

            TableData tableData = new TableData(it[1], it[2], null, '/openboxes/stockMovement/show/' + it[3])
            return tableData
        }

        Table table = new Table("Shipment", "Name", null, results)

        ColorNumber delayedShipmentByAir = new ColorNumber(numberDelayed['air'], 'By air')
        ColorNumber delayedShipmentBySea = new ColorNumber(numberDelayed['sea'], 'By sea')
        ColorNumber delayedShipmentByLand = new ColorNumber(numberDelayed['landAndSuitcase'], 'By land')

        NumberIndicator numberIndicator = new NumberIndicator(delayedShipmentByAir, delayedShipmentBySea, delayedShipmentByLand)

        NumberTableData numberTableData = new NumberTableData(table, numberIndicator)

        GraphData graphData = new GraphData(numberTableData, "Delayed Shipments", "numberTable");

        return graphData;
    }
}
