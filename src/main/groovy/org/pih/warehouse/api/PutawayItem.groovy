package org.pih.warehouse.api

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
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
    Integer quantityAvailable
    List<AvailableItem> availableItems
    PutawayStatus putawayStatus
    Transaction transaction
    InventoryLevel inventoryLevel


    Boolean delete = Boolean.FALSE
    List<PutawayItem> splitItems = []

    static constrants = {
        quantityAvailable(nullable: true)
    }

    String getCurrentBins() {
        String currentBins = ""
        if (availableItems) {
            currentBins = availableItems?.findAll {
                !it?.binLocation?.supports(ActivityCode.RECEIVE_STOCK)
            }?.collect { it?.binLocation?.name }?.unique()?.sort()?.join(", ")
        }
        return currentBins
    }

    String getPreferredBin() {
        return inventoryLevel?.binLocation ?: ""
    }

    String getCurrentBinsAbbreviated() {
        String currentBins = getCurrentBins()
        return StringUtils.abbreviate(currentBins, 25)
    }

    static PutawayItem createFromOrderItem(OrderItem orderItem) {
        PutawayItem putawayItem = new PutawayItem()
        putawayItem.id = orderItem.id
        putawayItem.product = orderItem.product
        putawayItem.inventoryItem = orderItem.inventoryItem
        putawayItem.quantity = orderItem.quantity
        putawayItem.putawayStatus = PutawayItem.getPutawayItemStatus(orderItem.orderItemStatusCode)
        putawayItem.currentFacility = orderItem.order.origin
        putawayItem.currentLocation = orderItem.originBinLocation
        putawayItem.putawayFacility = orderItem.order.destination
        putawayItem.putawayLocation = orderItem.destinationBinLocation
        putawayItem.recipient = orderItem.recipient ?: orderItem.order.recipient

        orderItem.orderItems?.each { item ->
            putawayItem.splitItems.add(PutawayItem.createFromOrderItem(item))
        }

        return putawayItem
    }

    static PutawayStatus getPutawayItemStatus(OrderItemStatusCode orderItemStatusCode) {
        switch (orderItemStatusCode) {
            case OrderItemStatusCode.PENDING:
                return PutawayStatus.PENDING
            case OrderItemStatusCode.COMPLETED:
                return PutawayStatus.COMPLETED
            case OrderItemStatusCode.CANCELED:
                return PutawayStatus.CANCELED
            default:
                return null
        }
    }


    Map toJson() {
        return [
                id                            : id,
                "stockMovement.id"            : currentLocation?.name,
                "stockMovement.name"          : currentLocation?.name,
                putawayStatus                 : putawayStatus?.name(),
                transactionNumber             : transaction?.transactionNumber,
                "currentFacility.id"          : currentFacility?.id,
                "currentFacility.name"        : currentFacility?.name,
                "currentLocation.id"          : currentLocation?.id,
                "currentLocation.name"        : currentLocation?.name,
                container                     : container,
                "product.id"                  : product?.id,
                "product.productCode"         : product?.productCode,
                "product.name"                : product?.name,
                "inventoryItem.id"            : inventoryItem?.id,
                "inventoryItem.lotNumber"     : inventoryItem?.lotNumber,
                "inventoryItem.expirationDate": inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                "recipient.id"                : recipient?.id,
                "recipient.name"              : recipient?.name,
                currentBins                   : currentBins,
                preferredBin                  : preferredBin,
                currentBinsAbbreviated        : currentBinsAbbreviated,
                "putawayFacility.id"          : putawayFacility?.id,
                "putawayFacility.name"        : putawayFacility?.name,
                "putawayLocation.id"          : putawayLocation?.id,
                "putawayLocation.name"        : putawayLocation?.name,
                quantity                      : quantity,
                quantityAvailable             : quantityAvailable,
                splitItems                    : splitItems.collect { it?.toJson() }
        ]
    }
}
