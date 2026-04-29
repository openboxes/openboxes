package org.pih.warehouse.api

class OptionalPaginationCommand extends PaginationCommand {

    Boolean disablePagination = false

    @Override
    boolean paginationDisabled() {
        return disablePagination
    }
}
