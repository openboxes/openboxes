package org.pih.warehouse.tableroapi

import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.tablero.NumberData

class NumberDataService {
    def messageService

    NumberData getInventoryByLotAndBin(def location) {
        def binLocations = ProductAvailability.executeQuery("select count(*) from ProductAvailability pa where pa.location = :location and pa.quantityOnHand > 0",
                ['location': location])

        def title = [
            code : "react.dashboard.inventoryByLotAndBin.title.label",
            message : messageService.getMessage("react.dashboard.inventoryByLotAndBin.title.label")
        ]

        def info = [
            code: "react.dashboard.inventoryByLotAndBin.info.label",
            message: messageService.getMessage("react.dashboard.inventoryByLotAndBin.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.inStock.label",
            message : messageService.getMessage("react.dashboard.subtitle.inStock.label")
        ]

        return new NumberData(
            title,
            info,
            binLocations[0],
            subTitle, "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock"
            )
    }

    NumberData getInProgressShipments(def user, def location) {
        def shipments = Requisition.executeQuery("select count(*) from Requisition r join r.shipments s where r.origin = :location and s.currentStatus = 'PENDING' and r.createdBy = :user",
                ['location': location, 'user': user]);

        def title = [
            code : "react.dashboard.inProgressShipments.title.label",
            message : messageService.getMessage("react.dashboard.inProgressShipments.title.label")
        ]

        def info = [
            code: "react.dashboard.inProgressShipments.info.label",
            message: messageService.getMessage("react.dashboard.inProgressShipments.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.shipments.label",
            message : messageService.getMessage("react.dashboard.subtitle.shipments.label")
        ]

        return new NumberData(
            title,
            info,
            shipments[0],
            subTitle, "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id
            )
    }

    NumberData getInProgressPutaways(def user, def location) {
        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderType = :orderType AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location",
                ['user': user, 'location': location, 'orderType': OrderType.findByCode(Constants.PUTAWAY_ORDER)])

        def title = [
            code : "react.dashboard.inProgressPutaways.title.label",
            message : messageService.getMessage("react.dashboard.inProgressPutaways.title.label")
        ]

        def info = [
            code: "react.dashboard.inProgressPutaways.info.label",
            message: messageService.getMessage("react.dashboard.inProgressPutaways.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.putaways.label",
            message : messageService.getMessage("react.dashboard.subtitle.putaways.label")
        ]

        return new NumberData(
            title,
            info,
            incompletePutaways[0],
            subTitle, "/openboxes/order/list?orderType=PUTAWAY_ORDER&status=PENDING&orderedBy=" + user.id)
    }

    NumberData getReceivingBin(def location) {
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

        def title = [
            code : "react.dashboard.receivingBin.title.label",
            message : messageService.getMessage("react.dashboard.receivingBin.title.label")
        ]

        def info = [
            code: "react.dashboard.receivingBin.info.label",
            message: messageService.getMessage("react.dashboard.receivingBin.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.products.label",
            message : messageService.getMessage("react.dashboard.subtitle.products.label")
        ]

        return new NumberData(
            title,
            info,
            receivingBin[0],
            subTitle, "/openboxes/report/showBinLocationReport?status=inStock"
            )
    }

    NumberData getItemsInventoried(def location) {
        Date firstOfMonth = LocalDate.now().withDayOfMonth(1).toDate();

        def itemsInventoried = TransactionEntry.executeQuery("""
            SELECT COUNT(distinct ii.product.id) from TransactionEntry te
            INNER JOIN te.inventoryItem ii
            INNER JOIN te.transaction t
            WHERE t.inventory = :inventory
            AND t.transactionType.transactionCode = :transactionCode 
            AND t.transactionDate >= :firstOfMonth""",
                [
                        inventory      : location?.inventory,
                        transactionCode: TransactionCode.PRODUCT_INVENTORY,
                        firstOfMonth   : firstOfMonth,
                ]);

        def title = [
            code : "react.dashboard.itemsInventoried.title.label",
            message : messageService.getMessage("react.dashboard.itemsInventoried.title.label")
        ]

        def info = [
            code: "react.dashboard.itemsInventoried.info.label",
            message: messageService.getMessage("react.dashboard.itemsInventoried.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.items.label",
            message : messageService.getMessage("react.dashboard.subtitle.items.label")
        ]

        return new NumberData(
            title,
            info,
            itemsInventoried[0],
            subTitle
            )
    }

    NumberData getDefaultBin(def location) {
        def productsInDefaultBin = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.product.id) FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand > 0
            AND pa.binLocation is null""",
                [
                    'location': location
                ])

        def title = [
            code : "react.dashboard.defaultBin.title.label",
            message : messageService.getMessage("react.dashboard.defaultBin.title.label")
        ]

        def info = [
            code: "react.dashboard.defaultBin.info.label",
            message: messageService.getMessage("react.dashboard.defaultBin.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.products.label",
            message : messageService.getMessage("react.dashboard.subtitle.products.label")
        ]

        return new NumberData(
            title,
            info,
            productsInDefaultBin[0],
            subTitle, "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock"
            )
    }

    NumberData getProductWithNegativeInventory(def location) {

        Date tomorrow = LocalDate.now().plusDays(1).toDate();
        Integer numberOfProducts = 0;

        def productsWithNegativeInventory = InventorySnapshot.executeQuery("""
            SELECT i.productCode, i.product.name, i.lotNumber, i.binLocationName, i.quantityOnHand FROM InventorySnapshot i
            WHERE i.location = :location
            AND i.quantityOnHand < 0
            AND i.date = :tomorrow
            ORDER BY i.quantityOnHand ASC
            """,
                [
                        'location': location,
                        'tomorrow': tomorrow
                ]);

        numberOfProducts = productsWithNegativeInventory.size()

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

        def title = [
            code : "react.dashboard.productWithNegativeInventory.title.label",
            message : messageService.getMessage("react.dashboard.productWithNegativeInventory.title.label")
        ]

        def info = [
            code: "react.dashboard.productWithNegativeInventory.info.label",
            message: messageService.getMessage("react.dashboard.productWithNegativeInventory.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.products.label",
            message : messageService.getMessage("react.dashboard.subtitle.products.label")
        ]

        return new NumberData(
            title,
            info,
            numberOfProducts,
            subTitle,
            "/openboxes/report/showBinLocationReport?location.id=" + location.id, tooltipData
            )
    }

    NumberData getExpiredProductsInStock(def location) {
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

        def title = [
            code : "react.dashboard.expiredProductsInStock.title.label",
            message : messageService.getMessage("react.dashboard.expiredProductsInStock.title.label")
        ]

        def info = [
            code : "react.dashboard.expiredProductsInStock.info.label",
            message : messageService.getMessage("react.dashboard.expiredProductsInStock.info.label")
        ]

        def subTitle = [
            code : "react.dashboard.subtitle.products.label",
            message : messageService.getMessage("react.dashboard.subtitle.products.label")
        ]

        return new NumberData(
            title,
            info,
            expiredProductsInStock[0],
            subTitle, "/openboxes/inventory/listExpiredStock?status=expired"
            )
    }

    NumberData getOpenStockRequests(def location) {
        def openStockRequests = Requisition.executeQuery("""
            SELECT COUNT(distinct r.id) FROM Requisition r
            WHERE r.origin = :location
            AND r.sourceType = :sourceType
            AND r.status NOT IN (:statuses)
            """,
                [
                        'location': location,
                        'sourceType' : RequisitionSourceType.ELECTRONIC,
                        'statuses' : [RequisitionStatus.CREATED, RequisitionStatus.ISSUED, RequisitionStatus.CANCELED],
                ])

        def title = [
                code : "react.dashboard.openStockRequests.title.label",
                message : messageService.getMessage("react.dashboard.openStockRequests.title.label")
        ]

        def info = [
            code: "react.dashboard.openStockRequests.info.label",
            message: messageService.getMessage("react.dashboard.openStockRequests.info.label")
        ]

        def subTitle = [
                code : "react.dashboard.requests.subtitle.label",
                message : messageService.getMessage("react.dashboard.requests.subtitle.label")
        ]

        return new NumberData(
                title,
                info,
                openStockRequests[0],
                subTitle, "/openboxes/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC"
        )
    }

    NumberData getInventoryValue (def location) {
        def inventoryValue = ProductAvailability.executeQuery("""select sum (pa.quantityOnHand * p.pricePerUnit) 
                from ProductAvailability as pa
                inner join pa.product as p 
                where pa.location = :location""",
                ['location': location])

        def title = [
                code : "react.dashboard.inventoryValue.title.label",
                message : messageService.getMessage("react.dashboard.inventoryValue.title.label")
        ]

        def subTitle = [
                code : "react.dashboard.subtitle.inStock.label",
                message : messageService.getMessage("react.dashboard.subtitle.inStock.label")
        ]

        return new NumberData(
                title,
                null,
                inventoryValue[0],
                subTitle,
                null,
                null,
                'dollars'
        )
    }
}
