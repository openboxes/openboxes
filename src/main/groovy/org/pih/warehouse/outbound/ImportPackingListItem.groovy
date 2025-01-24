package org.pih.warehouse.outbound

import grails.databinding.BindUsing
import grails.util.Holders
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import grails.validation.Validateable
import org.pih.warehouse.fulfillment.FulfillmentService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product


class ImportPackingListItem implements Validateable {

    String rowId

    String palletName

    String boxName

    @BindUsing({ obj, source -> Product.findByProductCode(source['product']) })
    Product product

    @BindUsing({ obj, source ->
        FulfillmentService fulfillmentService = Holders.grailsApplication.mainContext.getBean(FulfillmentService)
        return fulfillmentService.bindOrInferLotNumber(obj as ImportPackingListItem, source as Map)
    })
    String lotNumber

    Location origin

    @BindUsing({  obj, source ->
        FulfillmentService fulfillmentService = Holders.grailsApplication.mainContext.getBean(FulfillmentService)
        return fulfillmentService.bindOrInferBinLocation(obj as ImportPackingListItem, source as Map)
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
