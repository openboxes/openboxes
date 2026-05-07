package org.pih.warehouse.picklist

import grails.validation.Validateable

class PackImportItemCommand implements Validateable {

    String id
    String palletName
    String boxName
    String recipient

    static constraints = {
        id(nullable: false, blank: false)
        palletName(nullable: true)
        boxName(nullable: true, validator: { String boxName, PackImportItemCommand packImportItemCommand ->
            return (boxName && !packImportItemCommand.palletName) ? ['boxName.packLevel1.required'] : true
        })
        recipient(nullable: true)
    }
}
