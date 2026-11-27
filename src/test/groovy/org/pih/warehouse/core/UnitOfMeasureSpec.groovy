/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.PendingFeature
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

    void 'units with different ids are not equal'() {
        given:
        UnitOfMeasure a = new UnitOfMeasure()
        a.id = 'EA'
        UnitOfMeasure b = new UnitOfMeasure()
        b.id = 'BX'

        expect:
        a != b
        new HashSet([a, b]).size() == 2
    }

    @PendingFeature(reason = 'equals treats two unsaved units (null ids) as equal while hashCode differs')
    void 'units without an id are only equal to themselves'() {
        given:
        UnitOfMeasure a = new UnitOfMeasure()
        UnitOfMeasure b = new UnitOfMeasure()

        expect:
        a != b
        new HashSet([a, b]).size() == 2
    }
}
