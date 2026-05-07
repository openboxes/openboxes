package org.pih.warehouse.packList

import grails.validation.Validateable

class PackImportItemCommand implements Validateable {

    String id
    String packLevel1
    String packLevel2
    String recipient

    static constraints = {
        id(nullable: false, blank: false)
        packLevel1(nullable: true)
        packLevel2(nullable: true, validator: { String palletLevel2, PackImportItemCommand packImportItemCommand ->
            return (palletLevel2 && !packImportItemCommand.packLevel1) ? ['boxName.packLevel1.required'] : true
        })
        recipient(nullable: true)
    }
}
