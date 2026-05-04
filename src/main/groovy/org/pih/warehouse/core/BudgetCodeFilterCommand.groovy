package org.pih.warehouse.core

import org.pih.warehouse.api.OptionalPaginationCommand

class BudgetCodeFilterCommand extends OptionalPaginationCommand {
    String q
    Boolean active
    List<String> budgetCodeIds = []
    String sort
    String order
}
