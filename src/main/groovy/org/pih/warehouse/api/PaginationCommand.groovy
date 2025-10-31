package org.pih.warehouse.api

import grails.validation.Validateable
import org.pih.warehouse.core.PaginationParams

class PaginationCommand extends PaginationParams implements Validateable {

    @Override
    Integer getMax() {
        return Math.min(super.max ?: 10, 100)
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
