package org.pih.warehouse.outbound

import grails.databinding.BindUsing
import grails.util.Holders
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import grails.validation.Validateable
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability


class ImportPackingListItem implements Validateable {

    String rowId

    String palletName

    String boxName

    @BindUsing({ obj, source -> Product.findByProductCode(source['product']) })
    Product product

    @BindUsing({ obj, source ->
        String lotNumber = source['lotNumber']
        String productCode = source['product']
        String binLocationName = source['binLocation']

        // If lot number is provided then just return this value and bind it
        // Otherwise try to infer the lot number
        if (lotNumber) {
            return lotNumber
        }

        ProductAvailabilityService productAvailabilityService = Holders.grailsApplication.mainContext.getBean(ProductAvailabilityService)

        Product product = Product.findByProductCode(productCode)
        List<AvailableItem> availableItems = productAvailabilityService
                .getAvailableBinLocations(obj.origin as Location, product?.id)

        // If bin location is not provided then first check if inventory with default lot number is available
        if (!binLocationName) {
            List<AvailableItem> itemsWithDefaultBin = availableItems.findAll {
                !it?.inventoryItem?.lotNumber // Null or empty lot number
            }
            // only infer if there is one possible value
            if (itemsWithDefaultBin.size() == 1) {
                return itemsWithDefaultBin.first()?.inventoryItem?.lotNumber
            }
        }

        // If failed to find exact or default lotNumber try to infer based on provided binLocation
        // this also includes looking for default binLocation which is represented as a null value
        List<AvailableItem> availableItemsByBinLocation = availableItems.findAll {
            it?.binLocation?.name == binLocationName
        }

        // only infer if there is one possible value
        if (availableItemsByBinLocation.size() == 1) {
            return availableItemsByBinLocation.first()?.inventoryItem?.lotNumber
        }

        return null
    })
    String lotNumber

    Location origin

    @BindUsing({ obj, source ->
        String lotNumber = source['lotNumber']
        String productCode = source['product']
        String binLocationName = source['binLocation']

        ProductAvailabilityService productAvailabilityService = Holders.grailsApplication.mainContext.getBean(ProductAvailabilityService)

        Product product = Product.findByProductCode(productCode)
        List<AvailableItem> availableItems = productAvailabilityService
                .getAvailableBinLocations(obj.origin as Location, product?.id)

        // if binLocation is provided then look for one available in stock
        if (binLocationName) {
            AvailableItem availableItemByBinLocation = availableItems.find {
                it?.binLocation?.name == binLocationName
            }

            if (availableItemByBinLocation) {
                return availableItemByBinLocation?.binLocation
            }

            obj.binLocationFound = false
            // return a dummy location object to add more context in a response of which location was not found
            return new Location(name: binLocationName)
        }

        if (lotNumber) {
            // if bin location is not provided and lot is provided
            // then look for it in available stock
            List<AvailableItem> availableItemsWithProvidedLotNumber = availableItems.findAll {
                it?.inventoryItem?.lotNumber == lotNumber
            }
            if(availableItemsWithProvidedLotNumber.size() == 1) {
                return availableItemsWithProvidedLotNumber.first()?.binLocation
            }
        } else {
            // if bin location is not provided and lot number is not provided
            // then check if we have default lot
            List<AvailableItem> availableItemsWithDefaultLot = availableItems.findAll {
                !it?.inventoryItem?.lotNumber // Null or empty lot number
            }
            if(availableItemsWithDefaultLot.size() == 1) {
                return availableItemsWithDefaultLot.first()?.binLocation
            }
        }

        return null
     })
    Location binLocation

    Integer quantityPicked

    @BindUsing({ obj, source ->
        Person person = Person.findByNameOrEmail(source['recipient'])
        if (!person && source['recipient']) {
            // We want to indicate if a recipient was not found, but the search term was given
            obj.recipientFound = false
            // returns a dummy person object to add more context in a response of what person was not found
            return new Person(firstName: source['recipient'], lastName: "")
        }
        return person
    })
    Person recipient

    /**
     * Flag to indicate whether binLocation has been found and bound.
     * We can't rely on binLocation = null, because we accept the null as DEFAULT,
     * so we want to know if the binLocation is null, because we have not provided (or provided DEFAULT) the bin location name
     * or the binLocation for such name does not exist.
     * This can't be done in the BindUsing and rejectValue, because the errors would be cleared before running the validator logic
     * The flag is set to false if a bin location is not found for given name in the BindUsing of binLocation
     */
    boolean binLocationFound = true

    /**
     * Flag to indicate a reason why recipient is potentially null - if it has not been provided or
     * it has been provided, but no person with given search term was found
     */
    boolean recipientFound = true

    // Helper field to return expiration date for inventory item found in the quantityPicked validation, to display it in the table
    Date expirationDateToDisplay


    static constraints = {
        rowId(blank: false)
        palletName(nullable: true)
        boxName(nullable: true, validator: { String boxName, ImportPackingListItem item ->
            if (boxName && !item.palletName) {
                return ['packLevel1.required']
            }
            return true
        })
        lotNumber(nullable: true, blank: true, validator: { String lotNumber, ImportPackingListItem item ->
            InventoryItem inventoryItem = item.product?.getInventoryItem(lotNumber)
            if (inventoryItem) {
                return true
            }
            return ['inventoryItemNotFound', lotNumber, item.product?.productCode]
        })
        binLocation(nullable: true, validator: { Location binLocation, ImportPackingListItem item ->
            if (!item.binLocationFound) {
                return ['binLocationNotFound', binLocation?.name]
            }
        })
        quantityPicked(min: 1, validator: { Integer quantityPicked, ImportPackingListItem item ->
            InventoryItem inventoryItem = item.product?.getInventoryItem(item.lotNumber)
            if (!inventoryItem) {
                return ['inventoryItemNotFound']
            }
            // Associate inventory item's expiration date with expirationDateToDisplay, to display the date in the table
            item.expirationDateToDisplay = inventoryItem.expirationDate
            if (!item.binLocationFound) {
                return ['binLocationNotFound', item.binLocation.name]
            }
            ProductAvailabilityService productAvailabilityService = Holders.grailsApplication.mainContext.getBean(ProductAvailabilityService)
            Integer quantity = productAvailabilityService.getQuantityAvailableToPromiseForProductInBin(item.origin, item.binLocation, inventoryItem)
            if (quantity <= 0) {
                return ['stockout']
            }
            if (quantityPicked > quantity) {
                return ['overpick', quantityPicked]
            }
            return true
        })
        recipient(nullable: true, validator: { Person recipient, ImportPackingListItem item ->
            if (!item.recipientFound) {
                return ['recipientNotFound', item.recipient?.name]
            }
        })
        binLocationFound(bindable: false)
        recipientFound(bindable: false)
        expirationDateToDisplay(nullable: true, bindable: false)
    }

    Map toTableJson() {
        [
            rowId: rowId,
            product: [
                id: product?.id,
                name: product?.name,
                productCode: product?.productCode,
                displayNames: product?.displayNames,
                color: product?.color,
                handlingIcons: product?.handlingIcons,
            ],
            binLocation: binLocation?.name,
            palletName: palletName,
            boxName: boxName,
            lotNumber: lotNumber,
            quantityPicked: quantityPicked,
            recipient: recipient?.name,
            expirationDate: expirationDateToDisplay,
            origin: [
                id: origin?.id,
            ],
        ]
    }
}
