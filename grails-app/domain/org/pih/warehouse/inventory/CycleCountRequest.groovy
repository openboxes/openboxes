package org.pih.warehouse.inventory

import java.time.LocalDate

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountRequest {

    String id

    Location facility

    Product product

    CycleCountRequestStatus status

    CycleCount cycleCount

    CycleCountRequestType requestType

    Boolean blindCount

    /**
     * The person who is responsible for performing the count. Note that this might be different from
     * the person who *actually* performs the count.
     */
    Person countAssignee

    /**
     * The date that the count should be performed by.
     */
    LocalDate countDeadline

    /**
     * The person who is responsible for performing the recount. Note that this might be different from
     * the person who *actually* performs the recount.
     */
    Person recountAssignee

    /**
     * The date that the recount should be performed by.
     */
    LocalDate recountDeadline

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
        product(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
        cycleCount(unique: true, nullable: true) // Unique: true determines the unidirectional 1:1 association between cycle count request and cycle count
        countAssignee(nullable: true)
        countDeadline(nullable: true)
        recountAssignee(nullable: true)
        recountDeadline(nullable: true)
    }

    Map toJson() {
        return [
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                status: status.toString(),
                requestType: requestType.toString(),
                initialCount: [
                        deadline: countDeadline,
                        assignee: countAssignee
                ],
                verificationCount: [
                        deadline: recountDeadline,
                        assignee: recountAssignee
                ],
                inventoryItemsCount: cycleCount?.cycleCountItems?.size(),
                blindCount: blindCount,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
        ]
    }
}
