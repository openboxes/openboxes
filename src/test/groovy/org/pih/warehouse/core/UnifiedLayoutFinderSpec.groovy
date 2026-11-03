package org.pih.warehouse.core

import spock.lang.Specification
import spock.lang.Unroll

/**
 * The substitution decision: serve layouts/default.gsp in place of
 * layouts/custom.gsp, but only for pages that asked for "custom" and only when
 * the instance has opted in.
 *
 * The cases that matter are the ones that must NOT substitute. Forcing the
 * layout on every request - which is what an interceptor doing this in
 * before() is limited to - wrapped React and mobile pages in the legacy
 * chrome: /product/list came back carrying both a React root and the legacy
 * navbar, and /stockMovement/list rendered essentially empty.
 */
@Unroll
class UnifiedLayoutFinderSpec extends Specification {

    void "layout '#resolved' with enabled=#enabled => substitute=#expected"() {
        expect:
        UnifiedLayoutFinder.shouldSubstitute(resolved, enabled) == expected

        where:
        resolved    | enabled || expected
        // the one case that substitutes
        'custom'    | true    || true

        // opted out: the shipped default, where nothing may change
        'custom'    | false   || false

        // every other layout is left alone, opted in or not. These are the
        // regressions that would wrap React/print/mobile in legacy chrome.
        'react'     | true    || false
        'react'     | false   || false
        'mobile'    | true    || false
        'print'     | true    || false
        'email'     | true    || false
        'analytics' | true    || false
        'main'      | true    || false
        'bootstrap' | true    || false
        'default'   | true    || false

        // nothing resolved
        null        | true    || false
        ''          | true    || false
    }

    void "the two layout names are the ones the config documents"() {
        expect:
        // Guards against a rename on one side only: the config documentation
        // and this class name the same layouts.
        UnifiedLayoutFinder.DECLARED_LAYOUT == 'custom'
        UnifiedLayoutFinder.UNIFIED_LAYOUT == 'default'
    }
}
