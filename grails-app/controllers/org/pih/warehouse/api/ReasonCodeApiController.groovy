/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.ReasonCode

class ReasonCodeApiController {

    def locationService
    def messageSource

    def list = {

        List<ReasonCodeCommand> reasonCodes = []
        ActivityCode[] activityCodes = params.list("activityCode") as ActivityCode[]

        if (ActivityCode.SUBSTITUTE_REQUISITION_ITEM in activityCodes) {
            reasonCodes.addAll(getReasonCodes(ReasonCode.listRequisitionSubstitutionReasonCodes()))
        } else if (ActivityCode.MODIFY_REQUISITION_ITEM in activityCodes) {
            reasonCodes.addAll(getReasonCodes(ReasonCode.listRequisitionQuantityChangeReasonCodes()))
        } else if (ActivityCode.ADJUST_INVENTORY in activityCodes) {
            reasonCodes.addAll(getReasonCodes(ReasonCode.listInventoryAdjustmentReasonCodes()))
        } else {
            reasonCodes.addAll(getReasonCodes(ReasonCode.listDefault()))
        }
        render([data: reasonCodes.collect { it.toJson() }] as JSON)
    }

    def read = {
        ReasonCode reasonCodeEnum = params.id as ReasonCode
        ReasonCodeCommand reasonCode = getReasonCode(reasonCodeEnum)
        render([data: reasonCode?.toJson()] as JSON)
    }

    List<ReasonCodeCommand> getReasonCodes(List<ReasonCode> reasonCodeEnums) {
        List<ReasonCodeCommand> reasonCodes = []
        reasonCodeEnums.eachWithIndex { ReasonCode reasonCodeEnum, index ->
            reasonCodes << getReasonCode(reasonCodeEnum)
        }
        return reasonCodes
    }

    ReasonCodeCommand getReasonCode(ReasonCode reasonCodeEnum) {
        Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale ?: "en")
        Locale locale = session?.user?.locale ?: defaultLocale
        ReasonCodeCommand reasonCode = new ReasonCodeCommand()
        reasonCode.id = reasonCodeEnum.name()
        reasonCode.name = messageSource.getMessage("enum.ReasonCode.${reasonCodeEnum.name()}", null, null, locale)
        reasonCode.description = reasonCodeEnum.name()
        reasonCode.sortOrder = reasonCodeEnum.sortOrder
        return reasonCode
    }

}


class ReasonCodeCommand {

    String id
    String name
    String description
    Integer sortOrder

    Map toJson() {
        return [
                id       : id,
                name     : name,
                sortOrder: sortOrder
        ]
    }

}
