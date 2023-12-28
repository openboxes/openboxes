package org.pih.warehouse.product

import org.pih.warehouse.api.PaginationParams

class ProductSupplierListParams extends PaginationParams {

    String product

    String supplier

    String preferenceType

    Date createdFrom

    Date createdTo

    Boolean active

    String searchTerm

    String sort

    String order
}
