package org.pih.warehouse.api.regression.mobile

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Names the contract/mobile-contract.json entries (`file#function` ids) a
 * feature exercises. MobileContractCompletenessSpec fails when an entry has no
 * feature carrying its id, or when an id here is not in the contract.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Covers {
    String[] value()
}
