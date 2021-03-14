package org.pih.warehouse.tableroapi

import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.tablero.GraphData
import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.Table
import org.pih.warehouse.tablero.ColorNumber
import org.pih.warehouse.tablero.MultipleNumbersIndicator
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.NumbersIndicator
import org.pih.warehouse.tablero.IndicatorDatasets
import org.pih.warehouse.tablero.NumberTableData
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.core.Location
import org.joda.time.LocalDate
import org.pih.warehouse.util.LocalizationUtil

class IndicatorDataService {

    def dashboardService
    def dataService
    def messageService

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

            String label = (i == 0) ? "react.dashboard.timeline.today.label" : "react.dashboard.timeline.within${daysCounter}Days.label"
            def monthLabel = [
            code : label,
            message : messageService.getMessage(label)
            ]
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

        def title = [
            code : "react.dashboard.expirationSummaryData.title.label",
            message : messageService.getMessage("react.dashboard.expirationSummaryData.title.label")
        ]

        def info = [
            code : "react.dashboard.expirationSummaryData.info.label",
            message : messageService.getMessage("react.dashboard.expirationSummaryData.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            "line",
            "/openboxes/inventory/listExpiringStock"
            )

        return graphData
    }

    GraphData getFillRate(Location location, def destination, def params) {
        Integer querySize = params.querySize ? params.querySize.toInteger() : 6
        List listFiltersSelected = params.list('listFiltersSelected').toList()
        List listValues = params.list('value').toList()
        String extraCondition = ''
        String conditionStarter = 'where'

        if( listFiltersSelected.contains('category') && listValues.size > 0) {
            extraCondition = """
            join product as p on fr.product_id = p.id 
            join category as c on p.category_id = c.id
            where (
            """
            for(int i = 0; i < listValues.size; i ++) {
                extraCondition = "${extraCondition} c.id = '${listValues[i]}'"
                extraCondition = i<listValues.size - 1 ? "${extraCondition} or" : extraCondition
            }
            conditionStarter = ') and'
        }

        List listLabels = []


        List averageFillRateResult = []
        List requestLinesSubmittedResult = []
        List linesCancelledStockoutResult = []
        List averageTargetFillRate = [0.9]*querySize

        Date today = new Date()
        today.clearTime()
        for (int i = querySize; i > 0; i--) {
            def monthBegin = today.clone()
            def monthEnd = today.clone()
            monthBegin.set(month: today.month - i, date: 1)
            monthEnd.set(month: today.month - i + 1, date: 1)

            String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

            listLabels.push("${monthLabel} ${monthBegin.year + 1900}")

            def averageFillRate = dataService.executeQuery("""
            select avg(fr.fill_rate) FROM fill_rate as fr
            ${extraCondition}
            ${conditionStarter}
            fr.transaction_date <= :monthEnd 
            and fr.transaction_date > :monthBegin 
            and fr.origin_id = :origin
            and (fr.destination_id = :destination OR :destination IS NULL)
            GROUP BY MONTH(fr.transaction_date), YEAR(fr.transaction_date)
            """, [
                'monthEnd'    : monthEnd,
                'monthBegin'  : monthBegin,
                'destination' : destination?.id,
                'origin'      : location.id,
            ]);

            averageFillRate[0] == null ? averageFillRateResult.push(0) : averageFillRateResult.push(averageFillRate[0][0])

            def requestLinesSubmitted = dataService.executeQuery("""
            select count(fr.id) FROM fill_rate as fr
            ${extraCondition}
            ${conditionStarter}
            fr.transaction_date <= :monthEnd 
            and fr.transaction_date > :monthBegin 
            and (fr.destination_id = :destination OR :destination IS NULL) 
            and fr.origin_id = :origin
            GROUP BY MONTH(fr.transaction_date), YEAR(fr.transaction_date)
            """, [
                'monthEnd'    : monthEnd,
                'monthBegin'  : monthBegin,
                'destination' : destination?.id,
                'origin'      : location.id,
            ]);

            requestLinesSubmitted[0] == null ? requestLinesSubmittedResult.push(0) : requestLinesSubmittedResult.push(requestLinesSubmitted[0][0])

            def linesCancelledStockout = dataService.executeQuery("""
            select count(fr.id) FROM fill_rate as fr
            ${extraCondition}
            ${conditionStarter}
            fr.transaction_date <= :monthEnd and fr.transaction_date > :monthBegin 
            and (fr.destination_id = :destination OR :destination IS NULL)
            and fr.origin_id = :origin 
            and fr.fill_rate = 0
            GROUP BY MONTH(fr.transaction_date), YEAR(fr.transaction_date)
            """, [
                'monthEnd'    : monthEnd,
                'monthBegin'  : monthBegin,
                'destination' : destination?.id,
                'origin'      : location.id,
            ]);

            linesCancelledStockout[0] == null ? linesCancelledStockoutResult.push(0) : linesCancelledStockoutResult.push(linesCancelledStockout[0][0])
        }

            averageFillRateResult = averageFillRateResult.collect{ it * 100 }
            averageTargetFillRate = averageTargetFillRate.collect{ it * 100 }

        // Loading the config of the legend
        Map legendConfig = [
            'pointStyle' : 'circle',
        ]

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Request lines submitted', requestLinesSubmittedResult, null, 'bar', 'left-y-axis', legendConfig),
                new IndicatorDatasets('Lines cancelled stock out', linesCancelledStockoutResult, null, 'bar', 'left-y-axis', legendConfig),
                new IndicatorDatasets('Average Fill Rate', averageFillRateResult, null, 'line', 'right-y-axis', legendConfig),
                new IndicatorDatasets('Average of target Fill Rate', averageTargetFillRate, null, 'line', 'right-y-axis', legendConfig),
        ]

        IndicatorData indicatorData = new IndicatorData(datasets, listLabels)

        def title = [
            code : "react.dashboard.fillRate.title.label",
            message : messageService.getMessage("react.dashboard.fillRate.title.label")
        ]

        def info = [
            code : "react.dashboard.fillRate.info.label",
            message : messageService.getMessage("react.dashboard.fillRate.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            "bar"
            )

        return graphData
    }

    GraphData getFillRateSnapshot (Location origin, def params) {
        String listFiltersSelected = params.list('listFiltersSelected').toList()
        List listValues = params.list('value').toList()
        List averageFillRateResult = []
        List listLabels = []
        Date today = new Date()
        today.clearTime()
        String extraCondition = ''
        String conditionStarter = 'where'

        if( listFiltersSelected.contains('category') && listValues.size > 0) {
            extraCondition = """
            join product as p on fr.product_id = p.id 
            join category as c on p.category_id = c.id
            where (
            """
            for(int i = 0; i < listValues.size; i ++) {
                extraCondition = "${extraCondition} c.id = '${listValues[i]}'"
                extraCondition = i<listValues.size - 1 ? "${extraCondition} or" : extraCondition
            }
            conditionStarter = ') and'
        }

        for (int i = 12; i > 0; i--) {
            def monthBegin = today.clone()
            def monthEnd = today.clone()
            monthBegin.set(month: today.month - i, date: 1)
            monthEnd.set(month: today.month - i + 1, date: 1)
            String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]
            listLabels.push("${monthLabel} ${monthBegin.year + 1900}")

            def averageFillRate = dataService.executeQuery("""
            select avg(fr.fill_rate) FROM fill_rate as fr
            ${extraCondition}
            ${conditionStarter} 
            fr.transaction_date > :monthBegin
            and fr.transaction_date <= :monthEnd
            and fr.origin_id = :origin 
            GROUP BY MONTH(fr.transaction_date), YEAR(fr.transaction_date)
            """, [

                'monthBegin'  : monthBegin,
                'monthEnd'    : monthEnd,
                'origin'      : origin.id,
                'listValues'  : listValues,
            ]);

            averageFillRate[0] == null ? averageFillRateResult.push(0) : averageFillRateResult.push(averageFillRate[0][0])
        }

        averageFillRateResult = averageFillRateResult.collect{ it * 100 }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Average Fill Rate', averageFillRateResult, null, 'line'),
        ];

        int averageLastMonth = averageFillRateResult[averageFillRateResult.size - 1]

        ColorNumber colorNumber = new ColorNumber(averageLastMonth, 'Fill Rate Last Month', null, null, 90)
        colorNumber.setConditionalColors(87, colorNumber.value2)
        def variation = colorNumber.value - colorNumber.value2
        colorNumber.value2 = variation > 0 ? "+${variation} %" : "${variation} %"
        colorNumber.value = "${colorNumber.value}%"

        IndicatorData indicatorData = new IndicatorData(datasets, listLabels, colorNumber);
        def title = [
            code : "react.dashboard.fillRateSnapshot.title.label",
            message : messageService.getMessage("react.dashboard.fillRateSnapshot.title.label")
        ]

        def info = [
            code : "react.dashboard.fillRateSnapshot.info.label",
            message : messageService.getMessage("react.dashboard.fillRateSnapshot.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            "sparkline",
            null
            );

        return graphData;
    }

    List getFillRateDestinations(Location origin) {
        def destinations = dataService.executeQuery("""
            select 
                distinct(destination_id) as id, 
                case when length(location.name) > 30 
                    then concat(substring(location.name, 1, 30), '...')
                    else location.name 
                end as name
            FROM fill_rate as fr
            JOIN location on location.id = fr.destination_id
            where fr.origin_id = :origin 
            """, [
                'origin': origin.id,
        ]);

        return destinations
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

        IndicatorData indicatorData = new IndicatorData(datasets, ['In stock', 'Above maximum', 'Below reorder', 'Below minimum', 'No longer in stock'])

        def title = [
            code : "react.dashboard.inventorySummaryData.title.label",
            message : messageService.getMessage("react.dashboard.inventorySummaryData.title.label")
        ]

        def info = [
            code : "react.dashboard.inventorySummaryData.info.label",
            message : messageService.getMessage("react.dashboard.inventorySummaryData.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            "horizontalBar"
            )

        return graphData
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
        List listLabel = fillLabels(querySize)

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
            }
        }
        List<IndicatorDatasets> datasets = (List<IndicatorDatasets>) listRes.values().toList()

        IndicatorData indicatorData = new IndicatorData(datasets, listLabel)

        def title = [
            code : "react.dashboard.sentStockMovements.title.label",
            message : messageService.getMessage("react.dashboard.sentStockMovements.title.label")
        ]

        def info = [
            code : "react.dashboard.sentStockMovements.info.label",
            message : messageService.getMessage("react.dashboard.sentStockMovements.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            "bar"
            )

        return graphData
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
        List listLabel = fillLabels(querySize)

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
            }
        }
        List<IndicatorDatasets> datasets = (List<IndicatorDatasets>) listRes.values().toList()

        IndicatorData indicatorData = new IndicatorData(datasets, listLabel)

        def title = [
            code : "react.dashboard.receivedStockData.title.label",
            message : messageService.getMessage("react.dashboard.receivedStockData.title.label")
        ]

        def info = [
            code : "react.dashboard.receivedStockData.info.label",
            message : messageService.getMessage("react.dashboard.receivedStockData.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            "bar"
            )

        return graphData
    }

    GraphData getOutgoingStock(Location location) {
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

        def baseUrl = '/openboxes/stockMovement/list?direction=OUTBOUND'
        def status = '&status=' + RequisitionStatus.listPending().join('&status=')

        ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago', baseUrl + status + "&createdAfter=${m4.format("MM/dd/yyyy")}")
        ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago', baseUrl + status + "&createdAfter=${m7.format("MM/dd/yyyy")}&createdBefore=${m4.format("MM/dd/yyyy")}")
        ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago', baseUrl + status + "&createdBefore=${m7.format("MM/dd/yyyy")}")

        NumbersIndicator numbersIndicator = new NumbersIndicator(green, yellow, red)

        def title = [
            code : "react.dashboard.outgoingStock.title.label",
            message : messageService.getMessage("react.dashboard.outgoingStock.title.label")
        ]

        def info = [
            code : "react.dashboard.outgoingStock.info.label",
            message : messageService.getMessage("react.dashboard.outgoingStock.info.label")
        ]

        GraphData graphData = new GraphData(
            numbersIndicator,
            title,
            info,
            "numbers",
            "/openboxes/stockMovement/list?receiptStatusCode=PENDING"
            )

        return graphData
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

        NumbersIndicator numbersIndicator = new NumbersIndicator(pending, shipped, partiallyReceived)

        def title = [
            code : "react.dashboard.incomingStock.title.label",
            message : messageService.getMessage("react.dashboard.incomingStock.title.label")
        ]

        def info = [
            code : "react.dashboard.incomingStock.info.label",
            message : messageService.getMessage("react.dashboard.incomingStock.info.label")
        ]

        GraphData graphData = new GraphData(
            numbersIndicator,
            title,
            info,
            "numbers",
            "/openboxes/stockMovement/list?direction=INBOUND")

        return graphData
    }

    GraphData getDiscrepancy(Location location, def params) {
        Integer querySize = params.querySize ? params.querySize.toInteger() - 1 : 5

        LocalDate queryLimit = LocalDate.now().minusMonths(querySize).withDayOfMonth(1)

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
                and r.actualDeliveryDate > :limit 
            group by s.shipmentNumber, s.id, si.id, si.quantity
            having si.quantity <> sum(ri.quantityReceived)
        """ , [
                        'location': location,
                        'limit'   : queryLimit.toDate(),
                ])

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

        def title = [
            code : "react.dashboard.discrepancy.title.label",
            message : messageService.getMessage("react.dashboard.discrepancy.title.label")
        ]

        def info = [
            code : "react.dashboard.discrepancy.info.label",
            message : messageService.getMessage("react.dashboard.discrepancy.info.label")
        ]

        GraphData graphData = new GraphData(
            tableData,
            title,
            info,
            "table"
            )

        return graphData;
    }

    GraphData getDelayedShipments(Location location, String contextPath) {
        Date oneWeekAgo = LocalDate.now().minusWeeks(1).toDate()
        Date oneMonthAgo = LocalDate.now().minusMonths(1).toDate()
        Date twoMonthsAgo = LocalDate.now().minusMonths(2).toDate()

        def results = Shipment.executeQuery("""
            select s.shipmentType.id, s.shipmentType.name, s.shipmentNumber, s.name, s.id
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
            def shipmentType = LocalizationUtil.getLocalizedString(it[1], new Locale("en"))

            TableData tableData = new TableData(it[2], it[3], null, '/openboxes/stockMovement/show/' + it[4], "${contextPath}/images/icons/shipmentType/ShipmentType" + shipmentType + '.png')
            return tableData
        }

        Table table = new Table("Shipment", "Name", null, results)

        ColorNumber delayedShipmentByAir = new ColorNumber(numberDelayed['air'], 'By air')
        ColorNumber delayedShipmentBySea = new ColorNumber(numberDelayed['sea'], 'By sea')
        ColorNumber delayedShipmentByLand = new ColorNumber(numberDelayed['landAndSuitcase'], 'By land')

        NumbersIndicator numbersIndicator = new NumbersIndicator(delayedShipmentByAir, delayedShipmentBySea, delayedShipmentByLand)

        NumberTableData numberTableData = new NumberTableData(table, numbersIndicator)

        def title = [
            code : "react.dashboard.delayedShipments.title.label",
            message : messageService.getMessage("react.dashboard.delayedShipments.title.label")
        ]

        def info = [
            code : "react.dashboard.delayedShipments.info.label",
            message : messageService.getMessage("react.dashboard.delayedShipments.info.label")
        ]

        GraphData graphData = new GraphData(
            numberTableData,
            title,
            info,
            "numberTable"
            )

        return graphData;
    }

    GraphData getProductsInventoried(Location location) {
        List monthsCount = [3, 6, 9, 12, 0]
        List listPercentageNumbers = []
        Map listErrorSuccessIntervals = [
                3 : [18, 25],
                6 : [36, 50],
                9 : [54, 75],
                12: [75, 95],
                0 : [75, 95],
        ]

        def productInStock = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.product.id) FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand > 0""",
                [
                        'location': location
                ])

        monthsCount.each {
            def subtitle
            def percentage
            def inventoriedProducts

            if (it != 0) {
                subtitle = "< ${it} months"
                LocalDate period = LocalDate.now().minusMonths(it)

                inventoriedProducts = dataService.executeQuery(
                    """
                        SELECT count(distinct p.id)
                        FROM transaction_entry te
                        INNER JOIN inventory_item ii ON te.inventory_item_id = ii.id
                        INNER JOIN product p ON ii.product_id = p.id
                        INNER JOIN transaction t ON te.transaction_id = t.id
                        INNER JOIN transaction_type tt ON t.transaction_type_id = tt.id
                        LEFT JOIN location l ON t.inventory_id = l.inventory_id
                        WHERE l.id = '${location.id}'
                        AND tt.transaction_code = '${TransactionCode.PRODUCT_INVENTORY}'
                        AND t.transaction_date >= '${period}';
                    """
                )
            } else {
                subtitle = "Ever"

                inventoriedProducts = dataService.executeQuery(
                        """
                        SELECT count(distinct p.id)
                        FROM transaction_entry te
                        INNER JOIN inventory_item ii ON te.inventory_item_id = ii.id
                        INNER JOIN product p ON ii.product_id = p.id
                        INNER JOIN transaction t ON te.transaction_id = t.id
                        INNER JOIN transaction_type tt ON t.transaction_type_id = tt.id
                        LEFT JOIN location l ON t.inventory_id = l.inventory_id
                        WHERE l.id = '${location.id}'
                        AND tt.transaction_code = '${TransactionCode.PRODUCT_INVENTORY}';
                    """
                )
            }

            percentage = productInStock[0] == 0 ? 0 : Math.round(inventoriedProducts[0][0] / productInStock[0] * 100)
            ColorNumber colorNumber = new ColorNumber(percentage, subtitle)
            colorNumber.setConditionalColors(listErrorSuccessIntervals.get(it)[0], listErrorSuccessIntervals.get(it)[1])
            colorNumber.value = "${colorNumber.value}%"
            listPercentageNumbers.push(colorNumber)
        }
        MultipleNumbersIndicator multipleNumbersIndicator = new MultipleNumbersIndicator(listPercentageNumbers)

        def title = [
            code : "react.dashboard.productsInventoried.title.label",
            message : messageService.getMessage("react.dashboard.productsInventoried.title.label")
        ]

        GraphData productsInventoried = new GraphData(
            multipleNumbersIndicator,
            title,
            null,
            'numbersCustomColors'
            )

        return productsInventoried
    }

    GraphData getLossCausedByExpiry(Location location, def params) {

        Integer querySize = params.querySize ? params.querySize.toInteger() - 1 : 5
        LocalDate queryLimit = LocalDate.now().minusMonths(querySize).withDayOfMonth(1)

        def valuesRemovedDueToExpiry = dataService.executeQuery(
            """
                SELECT sum(p.price_per_unit * te.quantity), month(t.transaction_date), year(t.transaction_date)
                FROM transaction_entry te 
                INNER JOIN transaction t ON te.transaction_id = t.id
                INNER JOIN inventory_item ii ON te.inventory_item_id = ii.id
                INNER JOIN product p ON ii.product_id = p.id
                LEFT JOIN location l ON t.inventory_id = l.inventory_id
                WHERE l.id = '${location.id}'
                AND t.transaction_date >= '${queryLimit}'
                AND t.transaction_type_id = 4
                GROUP BY month(t.transaction_date), year(t.transaction_date);
            """
        )

        def valuesNotExpiredLastDayOfMonth = InventorySnapshot.executeQuery("""
            select sum(iis.quantityOnHand * p.pricePerUnit), month(iis.date), year(iis.date)
            from InventorySnapshot as iis
            inner join iis.product as p
            inner join iis.inventoryItem as ii
            where iis.location = :location
            and iis.date >= :limit
            and iis.date = LAST_DAY(iis.date)
            and ii.expirationDate > iis.date
            group by month(iis.date), year(iis.date)
        """,
                [
                        'location': location,
                        'limit'   : queryLimit.toDate()
                ])

        def valuesExpiredLastDayOfMonth = InventorySnapshot.executeQuery("""
            select sum(iis.quantityOnHand * p.pricePerUnit), month(iis.date), year(iis.date)
            from InventorySnapshot as iis
            inner join iis.product as p
            inner join iis.inventoryItem as ii
            where iis.location = :location
            and iis.date >= :limit
            and iis.date = LAST_DAY(iis.date)
            and ii.expirationDate <= iis.date
            group by month(iis.date), year(iis.date)
        """,
                [
                        'location': location,
                        'limit'   : queryLimit.toDate()
                ])

        // Filling the labels
        List listLabels = fillLabels(querySize)

        // Filling the data lists
        // Each data is an array of [value, month, year]
        def filledValuesRemovedDueToExpiry = fillData(valuesRemovedDueToExpiry, querySize, 0, 1, 2)
        def filledValuesNotExpiredLastDayOfMonth = fillData(valuesNotExpiredLastDayOfMonth, querySize, 0, 1, 2)
        def filledValuesExpiredLastDayOfMonth = fillData(valuesExpiredLastDayOfMonth, querySize, 0, 1, 2)

        // Calculating the percentage
        List percentage = [];
        for (int i = 0; i <= querySize; i++) {
            def removedDueToExpiry = filledValuesRemovedDueToExpiry == null ? 0 : filledValuesRemovedDueToExpiry[i]
            def notExpiredLastDayOfMonth = filledValuesNotExpiredLastDayOfMonth == null ? 0 : filledValuesNotExpiredLastDayOfMonth[i]
            def expiredLastDayOfMonth = filledValuesExpiredLastDayOfMonth == null ? 0 : filledValuesExpiredLastDayOfMonth[i]
            def sum = removedDueToExpiry + notExpiredLastDayOfMonth + expiredLastDayOfMonth
            if (sum == 0) {
                percentage.push(0)
            } else {
                percentage.push(Math.round((removedDueToExpiry / sum) * 100) / 100)
            }
        }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Percentage removed due to expiry', percentage, null, 'line'),
                new IndicatorDatasets('Inventory value not expired last day of month', filledValuesNotExpiredLastDayOfMonth, null, 'bar'),
                new IndicatorDatasets('Inventory value expired last day of month', filledValuesExpiredLastDayOfMonth, null, 'bar'),
                new IndicatorDatasets('Inventory value removed due to expiry', filledValuesRemovedDueToExpiry, null, 'bar'),
        ]

        IndicatorData indicatorData = new IndicatorData(datasets, listLabels)

        def title = [
            code : "react.dashboard.lossCausedByExpiry.title.label",
            message : messageService.getMessage("react.dashboard.lossCausedByExpiry.title.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            null,
            "bar"
            )

        return graphData
    }

    GraphData getPercentageAdHoc(Location location) {
        Calendar calendar = Calendar.instance
        // we need to get all requisitions that were created from the first day of the current month
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)
        def firstDayOfMonth = calendar.getTime()

        List<String> listLabels = []
        List<Integer> listData = []

        def percentageAdHoc = Requisition.executeQuery("""
            select count(r.id), r.type
            from Requisition as r
            where r.origin.id = :location
            and r.requestedDeliveryDate >= :firstDayOfMonth
            group by r.type
        """,
                [
                        'location': location.id,
                        'firstDayOfMonth': firstDayOfMonth,
                ])

        percentageAdHoc.each {
            if (RequisitionType.listRequestTypes().contains(it[1])) {
                listLabels.push(it[1].toString())
                listData.push(it[0])
            }
        }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Number of requests', listData, null , 'doughnut')
        ]

        IndicatorData indicatorData = new IndicatorData(datasets, listLabels)

        def title = [
            code : "react.dashboard.percentageAdHoc.title.label",
            message : messageService.getMessage("react.dashboard.percentageAdHoc.title.label")
        ]

        def info = [
            code : "react.dashboard.percentageAdHoc.info.label",
            message : messageService.getMessage("react.dashboard.percentageAdHoc.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            'doughnut',
            '/openboxes/stockMovement/list?direction=OUTBOUND'
            )

        return graphData
    }

    GraphData getStockOutLastMonth(Location location) {

        List<String> listLabels = []
        List<Integer> listData = []

        def stockOutLastMonth = dataService.executeQuery("""
            select count(pss.product_id), pss.stockout_status 
            from product_stockout_status as pss
            where pss.location_id = :location
            group by pss.stockout_status
        """,
                [
                        'location': location.id,
                ]);

        stockOutLastMonth.each {
                listLabels.push(it[1].toString())
                listData.push(it[0])
        }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Number of stockout', listData, null , 'doughnut')
        ]

        IndicatorData indicatorData = new IndicatorData(datasets, listLabels)

        def title = [
            code : "react.dashboard.stockOutLastMonth.title.label",
            message : messageService.getMessage("react.dashboard.stockOutLastMonth.title.label")
        ]

        def info = [
            code : "react.dashboard.stockOutLastMonth.info.label",
            message : messageService.getMessage("react.dashboard.stockOutLastMonth.info.label")
        ]

        GraphData graphData = new GraphData(
            indicatorData,
            title,
            info,
            'doughnut'
            )

        return graphData
    }

    private List fillLabels(int querySize) {
        Date today = new Date()
        today.clearTime()

        List labels = []

        for (int i = querySize; i >= 0; i--) {
            Date tmpDate = today.clone()
            tmpDate.set(month: today.month - i, date: 1)

            String monthLabel = new java.text.DateFormatSymbols().months[tmpDate.month].substring(0, 3)
            String yearLabel = tmpDate.year + 1900
            labels.push("${monthLabel} ${yearLabel}")
        }

        return labels
    }

    private List fillData(List dataList, int querySize, int dataIndex, int monthIndex, int yearIndex) {
        Date today = new Date()
        today.clearTime()

        List filledList = [0] * (querySize + 1)

        dataList.each {
            for (int i = querySize; i >= 0; i--) {
                Date tmpDate = today.clone()
                tmpDate.set(month: today.month - i, date: 1)

                // If there is data, update the dataset in the proper position
                if (tmpDate.month == it[monthIndex] - 1 && tmpDate.year + 1900 == it[yearIndex]) {
                    filledList[querySize - i] = it[dataIndex]
                }
            }
        }

        return filledList;
    }
}
