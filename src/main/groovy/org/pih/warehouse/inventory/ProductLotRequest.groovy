package org.pih.warehouse.inventory

import grails.validation.Validateable

import org.pih.warehouse.product.Product

class ProductLotRequest implements Validateable {

    Product product

    String lotNumber

    static constraints = {
        lotNumber(nullable: true)
    }
}
