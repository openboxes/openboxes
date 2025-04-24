package org.pih.warehouse.product

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand

class ProductSupplierFilterCommand extends PaginationCommand implements Validateable {

    String product

    String supplier

    List<String> defaultPreferenceTypes

    Date createdFrom

    Date createdTo

    Boolean includeInactive

    String searchTerm

    String sort

    String order

    String format

    Boolean disableMaxLimit

    @Override
    Integer getMax() {
        if (disableMaxLimit) {
            return Integer.MAX_VALUE
        }
        return super.getMax()
    }


    static constraints = {
        product nullable: true
        supplier nullable: true
        defaultPreferenceTypes nullable: true
        createdFrom nullable: true
        createdTo nullable: true
        includeInactive nullable: true
        searchTerm nullable: true
        sort nullable: true
        order nullable: true
        format nullable: true
        disableMaxLimit nullable: true
    }
}
