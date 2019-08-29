/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Person


class Container implements Comparable, java.io.Serializable {

    String id
    String name
    String containerNumber
    // An official container number (if it exists)
    String description                                        // Description of contents
    Person recipient
    // Person who is assigned to receive the container
    Integer sortOrder                                        //
    Float height                                            // height of container
    Float width                                            // width of container
    Float length                                            // length of container
    String volumeUnits = Constants.DEFAULT_VOLUME_UNITS
    // standard dimensional unit: cm, in, ft,
    Float weight                                            // weight of container
    String weightUnits = Constants.DEFAULT_WEIGHT_UNITS    // standard weight unit: kg, lb
    Date dateCreated
    Date lastUpdated

    ContainerType containerType            // Type of container
    ContainerStatus containerStatus        // Status of the container (open, closed)
    Container parentContainer

    // Added parentContainer to belongsTo in order to allow automatic cascade-delete of children when deleting a container
    static belongsTo = [shipment: Shipment]
    static hasMany = [containers: Container]


    static transients = ["optionValue", "shipmentItems"]
    static mapping = {
        id generator: 'uuid'
    }

    // Constraints
    static constraints = {
        name(empty: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        containerNumber(nullable: true, maxSize: 255)
        parentContainer(nullable: true)
        recipient(nullable: true)
        height(nullable: true, max: 99999999F)
        width(nullable: true, max: 99999999F)
        length(nullable: true, max: 99999999F)
        volumeUnits(nullable: true, maxSize: 255)
        weight(nullable: true, max: 99999999F)
        weightUnits(nullable: true)
        containerType(nullable: false)
        containerStatus(nullable: true)
        sortOrder(nullable: true)
    }

    String toString() { name }

    int compareTo(obj) {
        if (!sortOrder && obj?.sortOrder) {
            return -1
        } else if (!obj?.sortOrder && sortOrder) {
            return 1
        } else if (sortOrder <=> obj?.sortOrder != 0) {
            return sortOrder <=> obj?.sortOrder
        } else {
            return id <=> obj?.id
        }
    }

    /**
     * Makes a copy of this container
     * But does not copy references to associated shipments or child containers
     * Also doesn't copy id, date created and last updated
     */
    Container copyContainer() {

        // TODO: figure out sort order!

        Container newContainer = new Container(
                name: this.name,
                containerNumber: this.containerNumber,
                description: this.description,
                recipient: this.recipient,
                sortOrder: this.sortOrder,
                height: this.height,
                width: this.width,
                length: this.length,
                volumeUnits: this.volumeUnits,
                weight: this.weight,
                weightUnits: this.weightUnits,
                containerType: this.containerType,
                containerStatus: this.containerStatus
        )

    }

    List<ShipmentItem> getShipmentItems() {
        return ShipmentItem.findAllByContainer(this)
    }

    /**
     * Avoid collection [org.pih.warehouse.core.User.locationRoles] was not processed by flush().
     *
     * FIXME Refactor method above once this works, but I didn't want to break the code above since it's used all over the place.
     *
     * @return
     */
    def getShipmentItemsFromSession() {
        return shipment?.shipmentItems?.findAll { it.container == this }
    }

    String getOptionValue() {
        return containerType.name + "-" + name
    }

    /**
     * Adds a new container to this container of the specified type
     */
    Container addNewContainer(ContainerType containerType) {
        def sortOrder = (containers) ? containers.size() : 0

        def container = new Container(
                parentContainer: this,
                containerType: containerType,
                shipment: shipment,
                sortOrder: sortOrder
        )

        addToContainers(container)
        shipment.addToContainers(container)

        return container
    }


    Container findContainerByName(String name) {
        return containers.find { it.name.equalsIgnoreCase(name) }
    }

    Container findOrCreateBox(String boxName) {
        Container box = findContainerByName(boxName)
        if (!box) {
            box = addNewBox(this, boxName)
        }
        return box
    }

    Container addNewBox(Container parentContainer, String boxName) {
        Container box = addNewContainer(ContainerType.findById(Constants.BOX_CONTAINER_TYPE_ID))
        box.name = boxName
        return box

    }


    /**
     * Adds a new item to the container
     */
    ShipmentItem addNewItem() {
        def item = new ShipmentItem(container: this, recipient: this.recipient, shipment: this.shipment)
        this.shipment.addToShipmentItems(item)
        return item
    }

    Float totalWeightInKilograms() {
        log.info("Container " + this.name + " weighs " + weight + " " + weightUnits)
        if (weight) {
            return ("kg".equals(weightUnits)) ? weight : weight * Constants.KILOGRAMS_PER_POUND
        } else if (containers) {
            return containers.collect { it.totalWeightInKilograms() }.sum()
        } else {
            return 0.0
        }
    }

    Float totalWeightInPounds() {
        if (weight) {
            return ("lbs".equals(weightUnits)) ? weight : weight * Constants.POUNDS_PER_KILOGRAM
        } else if (containers) {
            return containers.collect { it.totalWeightInPounds() }.sum()
        } else {
            return 0.0
        }
    }

}
