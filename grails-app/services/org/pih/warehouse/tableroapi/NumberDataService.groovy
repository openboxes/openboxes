package org.pih.warehouse.tableroapi

import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.tablero.NumberData

class NumberDataService {
    def messageService

    NumberData getInventoryByLotAndBin(def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

        def binLocations = InventorySnapshot.executeQuery("select count(*) from InventorySnapshot i where i.location = :location and i.date = :tomorrow and i.quantityOnHand > 0",
                ['location': location, 'tomorrow': tomorrow]);
        
        def title = [
            code : "react.default.dashboard.numberData.inventoryByLotAndBin",
            message : messageService.getMessage("react.default.dashboard.numberData.inventoryByLotAndBin")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.inStock",
            message : messageService.getMessage("react.default.dashboard.subtitle.inStock")
        ]

        return new NumberData(
            title,
            binLocations[0],
            subTitle, "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock"
            )
    }

    NumberData getInProgressShipments(def user, def location) {
        def shipments = Requisition.executeQuery("select count(*) from Requisition r join r.shipments s where r.origin = :location and s.currentStatus = 'PENDING' and r.createdBy = :user",
                ['location': location, 'user': user]);
        
        def title = [
            code : "react.default.dashboard.numberData.inProgressShipments",
            message : messageService.getMessage("react.default.dashboard.numberData.inProgressShipments")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.shipments",
            message : messageService.getMessage("react.default.dashboard.subtitle.shipments")
        ]

        return new NumberData(
            title,
            shipments[0],
            subTitle, "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id
            )
    }

    NumberData getInProgressPutaways(def user, def location) {
        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderTypeCode = 'TRANSFER_ORDER' AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location",
                ['user': user, 'location': location]);

        def title = [
            code : "react.default.dashboard.numberData.inProgressPutaways",
            message : messageService.getMessage("react.default.dashboard.numberData.inProgressPutaways")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.putaways",
            message : messageService.getMessage("react.default.dashboard.subtitle.putaways")
        ]
        
        return new NumberData(
            title,
            incompletePutaways[0],
            subTitle, "/openboxes/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING&orderedBy=" + user.id)
    }

    NumberData getReceivingBin(def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

        def receivingBin = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.product.id) from InventorySnapshot i 
            LEFT JOIN i.location l 
            LEFT JOIN i.binLocation bl
            WHERE l = :location AND i.quantityOnHand > 0 
            AND i.date = :tomorrow AND bl.locationType.id = :locationType""",
                [
                        'location'    : location,
                        'tomorrow'    : tomorrow,
                        'locationType': Constants.RECEIVING_LOCATION_TYPE_ID,
                ]);

        def title = [
            code : "react.default.dashboard.numberData.receivingBin",
            message : messageService.getMessage("react.default.dashboard.numberData.receivingBin")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.products",
            message : messageService.getMessage("react.default.dashboard.subtitle.products")
        ]
        
        return new NumberData(
            title,
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
            code : "react.default.dashboard.numberData.itemsInventoried",
            message : messageService.getMessage("react.default.dashboard.numberData.itemsInventoried")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.items",
            message : messageService.getMessage("react.default.dashboard.subtitle.items")
        ]

        return new NumberData(
            title,
            itemsInventoried[0],
            subTitle
            )
    }

    NumberData getDefaultBin(def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

        def productsInDefaultBin = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.product.id) FROM InventorySnapshot i
            WHERE i.location = :location
            AND i.quantityOnHand > 0
            AND i.binLocationName = 'DEFAULT'
            AND i.date = :tomorrow""",
                [
                        'location': location,
                        'tomorrow': tomorrow
                ]);
        
        def title = [
            code : "react.default.dashboard.numberData.defaultBin",
            message : messageService.getMessage("react.default.dashboard.numberData.defaultBin")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.products",
            message : messageService.getMessage("react.default.dashboard.subtitle.products")
        ]

        return new NumberData(
            title,
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
            code : "react.default.dashboard.numberData.productWithNegativeInventory",
            message : messageService.getMessage("react.default.dashboard.numberData.productWithNegativeInventory")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.products",
            message : messageService.getMessage("react.default.dashboard.subtitle.products")
        ]

        return new NumberData(
            title,
            numberOfProducts,
            subTitle,
            "/openboxes/report/showBinLocationReport?location.id=" + location.id, tooltipData
            )
    }

    NumberData getExpiredProductsInStock(def location) {
        Date today = LocalDate.now().toDate();
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

          def expiredProductsInStock = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.id) FROM InventorySnapshot i
            WHERE i.location = :location
            AND i.quantityOnHand > 0
            AND i.date = :tomorrow
            AND i.inventoryItem.expirationDate < :today
            """,
                [
                        'location': location,
                        'tomorrow': tomorrow,
                        'today' : today,
                ]);

        def title = [
            code : "react.default.dashboard.numberData.expiredProductsInStock",
            message : messageService.getMessage("react.default.dashboard.numberData.expiredProductsInStock")
        ]

        def subTitle = [
            code : "react.default.dashboard.subtitle.products",
            message : messageService.getMessage("react.default.dashboard.subtitle.products")
        ]

        return new NumberData(
            title,
            expiredProductsInStock[0],
            subTitle, "/openboxes/inventory/listExpiredStock?status=expired"
            )
    }
}
