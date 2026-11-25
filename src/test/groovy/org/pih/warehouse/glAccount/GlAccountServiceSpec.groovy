package org.pih.warehouse.glAccount

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.GlAccount

@Unroll
class GlAccountServiceSpec extends Specification implements ServiceUnitTest<GlAccountService>, DataTest {

    void setupSpec() {
        mockDomain(GlAccount)
    }

    void setup() {
        new GlAccount(code: 'A1').save(validate: false)
        new GlAccount(code: 'A2').save(validate: false)
        new GlAccount(code: 'I1', active: false).save(validate: false)
    }

    void 'getGlAccounts should return #expectedCodes for params #params'() {
        expect:
        service.getGlAccounts(params)*.code as Set == expectedCodes as Set

        where:
        params            || expectedCodes
        [:]               || ['A1', 'A2', 'I1']
        [active: 'true']  || ['A1', 'A2']
        [active: 'false'] || ['I1']
    }
}
