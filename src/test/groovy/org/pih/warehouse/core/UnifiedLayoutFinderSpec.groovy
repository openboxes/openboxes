package org.pih.warehouse.core

import spock.lang.Specification
import spock.lang.Unroll

/**
 * The substitution decision: serve layouts/default.gsp in place of
 * layouts/custom.gsp, but only for pages that asked for "custom", only when
 * the instance has opted in, and never when the request named a layout itself.
 *
 * The cases that matter are the ones that must NOT substitute. Forcing the
 * layout on every request — which is what an interceptor doing this in
 * before() is limited to — wrapped React and mobile pages in the legacy
 * chrome: /product/list came back carrying both a React root and the legacy
 * navbar, and /stockMovement/list rendered essentially empty.
 */
@Unroll
class UnifiedLayoutFinderSpec extends Specification {

    void "layout '#resolved' with enabled=#enabled override=#override => substitute=#expected"() {
        expect:
        UnifiedLayoutFinder.shouldSubstitute(resolved, enabled, override) == expected

        where:
        resolved    | enabled | override || expected
        // the one case that substitutes
        'custom'    | true    | false    || true

        // opted out: the shipped default, where nothing may change
        'custom'    | false   | false    || false

        // every other layout is left alone, opted in or not. These are the
        // regressions that would wrap React/print/mobile in legacy chrome.
        'react'     | true    | false    || false
        'react'     | false   | false    || false
        'mobile'    | true    | false    || false
        'print'     | true    | false    || false
        'email'     | true    | false    || false
        'analytics' | true    | false    || false
        'main'      | true    | false    || false
        'bootstrap' | true    | false    || false
        'default'   | true    | false    || false

        // ?layout= named it explicitly, so honour that in both directions.
        // Without this, ?layout=custom on an opted-in instance would be
        // resolved as "custom" and substituted straight back to the unified
        // layout — the override would do nothing in the direction a reviewer
        // most needs it.
        'custom'    | true    | true     || false
        'custom'    | false   | true     || false
        'default'   | true    | true     || false

        // nothing resolved
        null        | true    | false    || false
        ''          | true    | false    || false
    }

    void "the two layout names are the ones the config and interceptor agree on"() {
        expect:
        // Guards against a rename on one side only: the interceptor allowlist,
        // the config documentation and this class all name the same layouts.
        UnifiedLayoutFinder.DECLARED_LAYOUT == 'custom'
        UnifiedLayoutFinder.UNIFIED_LAYOUT == 'default'
    }

    void "the override attribute matches the one the interceptor sets"() {
        expect:
        // These are separate constants in separate source trees — the finder
        // cannot import the controller — so a typo in either would silently
        // disable the override rather than fail.
        UnifiedLayoutFinder.OVERRIDE_ATTRIBUTE == 'org.pih.warehouse.layout.override'
    }
}
