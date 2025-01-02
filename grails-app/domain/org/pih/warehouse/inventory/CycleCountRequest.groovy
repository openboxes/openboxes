package org.pih.warehouse.inventory

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountRequest {

    String id

    Location facility

    Product product

    CycleCountStatus status

    // To be included when cycle count table is created
    // CycleCount cycleCount

    CycleCountRequestType requestType

    Boolean blindCount

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
    }

    static constraints = {
        id generator: "uuid"
        product(nullable: true, unique: "facility")
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    Map toJson() {
        return [
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                status: status.toString(),
                requestType: requestType.toString(),
                blindCount: blindCount,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
        ]
    }
}
