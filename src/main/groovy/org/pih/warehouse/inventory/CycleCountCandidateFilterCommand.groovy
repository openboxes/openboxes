package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductCatalog

import java.time.LocalDate

class CycleCountCandidateFilterCommand extends PaginationCommand implements Validateable {

    List<CycleCountCandidateStatus> statuses

    // Filter param for populating data related to exact ids
    List<String> requestIds

    String searchTerm

    List<Category> categories

    List<String> internalLocations

    Date dateLastCount

    List<Tag> tags

    List<ProductCatalog> catalogs

    List<String> abcClasses

    String sort

    String order

    String format

    // FIXME This should be called includeNegativeQuantitiesOnly (or something similar) to be absolutely
    //  clear about our intention.
    Boolean negativeQuantity = Boolean.FALSE

    Boolean includeStockOnHandOrNegativeStock = Boolean.TRUE

    Boolean showCycleCountsInProgress = Boolean.FALSE

    List<Person> countAssignees

    List<Person> recountAssignees

    LocalDate countDeadline

    LocalDate recountDeadline

    static constraints = {
        statuses(nullable: true)
        searchTerm(nullable: true)
        categories(nullable: true)
        internalLocations(nullable: true)
        dateLastCount(nullable: true)
        tags(nullable: true)
        catalogs(nullable: true)
        abcClasses(nullable: true)
        sort(nullable: true)
        order(nullable: true)
        format(nullable: true)
        negativeQuantity(nullable: true)
        showCycleCountsInProgress(nullable: true)
        countAssignees(nullable: true)
        recountAssignees(nullable: true)
        countDeadline(nullable: true)
        recountDeadline(nullable: true)
    }
}
