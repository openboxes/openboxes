package org.pih.warehouse.core

import org.pih.warehouse.api.PaginationCommand

class BudgetCodeFilterCommand extends PaginationCommand {
    String q
    Boolean active
    List<String> includeIds = []
    String sort
    String order
    Boolean disableMaxLimit

    @Override
    Integer getMax() {
        if (disableMaxLimit) {
            return Integer.MAX_VALUE
        }
        return super.max
    }
}
