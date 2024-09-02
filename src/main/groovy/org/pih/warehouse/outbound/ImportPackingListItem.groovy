package org.pih.warehouse.outbound

import grails.databinding.BindUsing
import grails.util.Holders
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
        // set provided lot number
        if (source['lotNumber']) {
            return source['lotNumber']
        }

        // otherwise infer inventory item based on provided bin-location
        Location internalLocation = Location.findByNameAndParentLocation(source['binLocation'], obj.origin)
        Product product = Product.findByProductCode(source['product'])
        List<ProductAvailability> items = ProductAvailability.findAllByProductAndBinLocation(product, internalLocation)

        // infer lot-number only if there is a single possible inventory
        if (items.size() == 1) {
            return items.first().lotNumber
        }

        return null
    })
    String lotNumber

    Location origin

    @BindUsing({ obj, source ->
        Location internalLocation = Location.findByNameAndParentLocation(source['binLocation'], obj.origin)
        // If location is not found, but we provided bin location name, it means, the location was not found,
        // and we want to indicate that, instead of falling back to default (null)
        // without this, we would then search e.g. for quantity available to promise for a product in the default bin location
        if (!internalLocation && source['binLocation'] && !source['binLocation'].equalsIgnoreCase(Constants.DEFAULT_BIN_LOCATION_NAME)) {
            // We want to indicate, that a bin location for given name has not been found.
            obj.binLocationFound = false
            // returns a dummy location object to add more context in a response of what location was not found
            return new Location(name: source['binLocation'])
        }
        return internalLocation
     })
    Location binLocation

    Integer quantityPicked

    @BindUsing({ obj, source ->
        Person person = Person.findPersonByNameOrEmail(source['recipient'])
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
                return ['binLocationNotFound', binLocation.name]
            }
        })
        quantityPicked(min: 1, validator: { Integer quantityPicked, ImportPackingListItem item ->
            if (!item.binLocationFound) {
                return ['binLocationNotFound']
            }
            ProductAvailabilityService productAvailabilityService = Holders.grailsApplication.mainContext.getBean(ProductAvailabilityService)
            InventoryItem inventoryItem = item.product?.getInventoryItem(item.lotNumber)
            if (!inventoryItem) {
                return ['inventoryItemNotFound']
            }
            // Associate inventory item's expiration date with expirationDateToDisplay, to display the date in the table
            item.expirationDateToDisplay = inventoryItem.expirationDate
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
                return ['recipientNotFound']
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
