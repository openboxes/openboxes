package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductCatalog

class CycleCountCandidateFilterCommand extends PaginationCommand implements Validateable {

    List<CycleCountCandidateStatus> statuses

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
    }
}
