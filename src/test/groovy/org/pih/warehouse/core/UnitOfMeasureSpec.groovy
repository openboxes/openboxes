package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class UnitOfMeasureSpec extends Specification implements DomainUnitTest<UnitOfMeasure> {

    void 'units with the same id have the same hash code'() {
        given:
        UnitOfMeasure a = new UnitOfMeasure()
        a.id = 'EA'
        UnitOfMeasure b = new UnitOfMeasure()
        b.id = 'EA'

        expect:
        a == b
        a.hashCode() == b.hashCode()
        new HashSet([a, b]).size() == 1
    }
}
