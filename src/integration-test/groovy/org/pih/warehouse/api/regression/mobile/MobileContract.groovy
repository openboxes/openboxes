package org.pih.warehouse.api.regression.mobile

import groovy.json.JsonSlurper

/**
 * The mobile app's API contract (resources: mobile-contract/mobile-contract.json), read once per JVM.
 */
class MobileContract {

    private static Map cached

    static Map load() {
        if (cached == null) {
            InputStream json = MobileContract.getResourceAsStream('/mobile-contract/mobile-contract.json')
            if (json == null) {
                throw new IllegalStateException('mobile-contract/mobile-contract.json is not on the test classpath')
            }
            cached = json.withCloseable { new JsonSlurper().parse(it) } as Map
        }
        return cached
    }

    static List<Map> entries() {
        return load().entries as List<Map>
    }

    static Map entry(String id) {
        Map e = entries().find { it.id == id }
        if (!e) {
            throw new IllegalArgumentException("no contract entry ${id}")
        }
        return e
    }
}
