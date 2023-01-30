package org.pih.warehouse.core

enum SynonymTypeCode {
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
