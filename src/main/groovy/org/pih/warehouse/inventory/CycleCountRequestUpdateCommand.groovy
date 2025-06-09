package org.pih.warehouse.inventory

import grails.validation.Validateable
import java.time.LocalDate

import org.pih.warehouse.core.Person

class CycleCountRequestUpdateCommand implements Validateable {

    /**
     * The request to update.
     */
    CycleCountRequest cycleCountRequest

    // The fields of the request that the user is allowed to update
    Person countAssignee
    LocalDate countDeadline
    Person recountAssignee
    LocalDate recountDeadline

    static constraints = {
        countAssignee(nullable: true)
        countDeadline(nullable: true)
        recountAssignee(nullable: true)
        recountDeadline(nullable: true)
    }
}
