package org.pih.warehouse.common.util

class RandomUtil {

    Random random = new Random()

    /**
     * Generate a random String value for a given field name.
     *
     * Needed so that we can generate primary keys that don't clash with existing data but also provides
     * an easy way to identify what data is created by our tests.
     */
    String randomStringFieldValue(String fieldName) {
        return "${fieldName}-TEST-${Math.abs(random.nextLong())}"
    }
}
