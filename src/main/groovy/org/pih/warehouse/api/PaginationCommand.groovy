package org.pih.warehouse.api

import grails.validation.Validateable
import org.pih.warehouse.core.PaginationParams

class PaginationCommand extends PaginationParams implements Validateable {

    private static final int ABSOLUTE_MAX = 100
    private static final int DEFAULT_MAX = 10

    Boolean paginationEnabled = true

    @Override
    Integer getMax() {
        if (!paginationEnabled) {
            return Integer.MAX_VALUE
        }
        return Math.min(super.max ?: DEFAULT_MAX, ABSOLUTE_MAX)
    }

    @Override
    Integer getOffset() {
        return Math.max(super.offset ?: 0, 0)
    }

    Map<String, Integer> getPaginationParams() {
        return [
            max: getMax(),
            offset: getOffset(),
        ]
    }
}
