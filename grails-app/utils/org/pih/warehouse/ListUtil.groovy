package org.pih.warehouse

class ListUtil {

    static boolean isTypeOfString(List list) {
        if (list?.get(0) instanceof String) {
            return true
        }
        return false
    }
}
