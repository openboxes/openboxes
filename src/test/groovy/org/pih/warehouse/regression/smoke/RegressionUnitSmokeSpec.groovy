package org.pih.warehouse.regression.smoke

import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.StringAbbreviator

/**
 * Smoke check for the regression unit suite: compiled against OBAF's main
 * classes and run by the regressionTest task.
 */
class RegressionUnitSmokeSpec extends Specification {

    @Unroll
    void 'StringAbbreviator turns "#name" into #expected'() {
        expect:
        new StringAbbreviator().abbreviate(name, 2, 6, ',') == expected

        where:
        name           || expected
        'Big Bad Guys' || 'BBG'
        'Biggerer'     || 'BIGGER'
        ''             || null
    }
}
