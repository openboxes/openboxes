package org.pih.warehouse.regression.platform.product

import org.pih.warehouse.product.*
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

class ProductSearchDtoSpec extends Specification implements TagLibUnitTest<ApplicationTagLib> {

    void setupSpec() {
        mockTagLib(ApplicationTagLib)
    }

    void 'the search result json carries the product basics and its color'() {
        given:
        ProductSearchDto dto = new ProductSearchDto(id: 'p1', productCode: 'G1', active: true, name: 'Gloves',
                displayName: 'Nitrile gloves', productColor: '#ff0000', lotAndExpiryControl: true, exactMatch: true,
                unitOfMeasure: 'EA')

        expect:
        dto.handlingIcons == []
        dto.toJson() == [id: 'p1', productCode: 'G1', active: true, name: 'Gloves', displayName: 'Nitrile gloves',
                         color: '#ff0000', handlingIcons: [], lotAndExpiryControl: true, exactMatch: true,
                         unitOfMeasure: 'EA']
    }

    void 'flags default to false'() {
        expect:
        with(new ProductSearchDto()) {
            !coldChain && !controlledSubstance && !hazardousMaterial && !reconditioned && !lotAndExpiryControl &&
                    !exactMatch
        }
    }
}
