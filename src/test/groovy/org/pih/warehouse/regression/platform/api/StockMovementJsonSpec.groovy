package org.pih.warehouse.regression.platform.api

import spock.lang.PendingFeature

import org.pih.warehouse.api.*
import grails.testing.gorm.DataTest
import grails.util.Holders
import spock.lang.Specification

import org.pih.warehouse.requisition.RequisitionSourceType

class StockMovementJsonSpec extends Specification implements DataTest {

    // toJson labels the status through the application tag lib
    static class LabelTagLib {
        String message(Map args) {
            return "label for ${args.code}"
        }
    }

    void setup() {
        Holders.grailsApplication = grailsApplication
        defineBeans {
            "org.grails.plugins.web.taglib.ApplicationTagLib"(LabelTagLib)
        }
    }

    @PendingFeature(reason = 'StockMovement.toJson reads sourceType?.name, a property the enum lacks')
    void 'toJson should name the source type: #sourceType'() {
        given:
        StockMovement stockMovement = new StockMovement(id: 'sm1', sourceType: sourceType)

        expect:
        stockMovement.toJson().sourceType == expected

        where:
        sourceType                       || expected
        RequisitionSourceType.ELECTRONIC || 'ELECTRONIC'
        RequisitionSourceType.PAPER      || 'PAPER'
        null                             || null
    }
}
