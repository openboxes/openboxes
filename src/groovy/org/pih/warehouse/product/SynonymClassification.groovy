package org.pih.warehouse.product

enum SynonymClassification {
    DISPLAY_NAME,
    BRAND_NAME,
    ALTERNATE_NAME


    String toString() {
        return name()
    }

    static list() {
        [DISPLAY_NAME, BRAND_NAME, ALTERNATE_NAME]
    }

}
