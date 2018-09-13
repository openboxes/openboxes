package org.pih.warehouse.api

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container


@Validateable
class PutawayItem {

    String id
    Product product
    InventoryItem inventoryItem
    Container container
    Location currentFacility
    Location currentLocation
    Location putawayFacility
    Location putawayLocation
    Person recipient
    BigDecimal quantity
    List<AvailableItem> availableItems
    PutawayStatus putawayStatus
    Transaction transaction

    List<PutawayItem> splitItems = []

    String getCurrentBins() {
        String currentBins = ""
        if (availableItems) {
            currentBins = availableItems?.collect { it?.binLocation?.name }.sort().join(",")
        }
        return currentBins
    }

    String getCurrentBinsAbbreviated() {
        String currentBins = getCurrentBins()
        return StringUtils.abbreviate(currentBins, 25)
    }


    Map toJson() {
        return [
                id: id,
                "stockMovement.id": currentLocation?.name,
                "stockMovement.name": currentLocation?.name,
                putawayStatus: transaction ? PutawayStatus.COMPLETE.name() : putawayStatus?.name(),
                transactionNumber: transaction?.transactionNumber,
                "currentFacility.id": currentFacility?.id,
                "currentFacility.name": currentFacility?.name,
                "currentLocation.id": currentLocation?.id,
                "currentLocation.name": currentLocation?.name,
                container: container,
                "product.id": product?.id,
                "product.productCode": product?.productCode,
                "product.name": product?.name,
                "inventoryItem.id": inventoryItem?.id,
                "inventoryItem.lotNumber": inventoryItem?.lotNumber,
                "inventoryItem.expirationDate": inventoryItem?.expirationDate,
                "recipient.id": recipient?.id,
                "recipient.name": recipient?.name,
                currentBins: currentBins,
                currentBinsAbbreviated: currentBinsAbbreviated,
                "putawayFacility.id": putawayFacility?.id,
                "putawayFacility.name": putawayFacility?.name,
                "putawayLocation.id": putawayLocation?.id,
                "putawayLocation.name": putawayLocation?.name,
                quantity: quantity,
                splitItems: splitItems.collect { it?.toJson() }
        ]
    }
}
