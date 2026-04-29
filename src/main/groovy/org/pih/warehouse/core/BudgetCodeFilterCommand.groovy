package org.pih.warehouse.core

import org.pih.warehouse.api.PaginationCommand

class BudgetCodeFilterCommand extends PaginationCommand {
    String q
    Boolean active
    List<String> budgetCodeIds = []
    String sort
    String order
}
