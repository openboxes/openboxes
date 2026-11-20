package org.pih.warehouse.inventory

import org.apache.commons.lang.StringUtils
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidatable

class ListAvailableItemsCommand extends PaginationCommand implements ObjectValidatable {

    Location facility

    def beforeValidate() {
        String facilityId = RequestContextHolder.getRequestAttributes().params?.facilityId
        facility = StringUtils.isBlank(facilityId) ? null : Location.read(facilityId)
    }

    static constraints = {
        facility nullable: false
    }
}
