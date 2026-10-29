package org.pih.warehouse.common.domain.builder.core

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.GlAccountType
import org.pih.warehouse.core.GlAccountTypeCode

@InheritConstructors
class GlAccountTypeTestBuilder extends TestBuilder<GlAccountType> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                code: randomUtil.randomStringFieldValue("code"),
                glAccountTypeCode: GlAccountTypeCode.EXPENSE,
        ] as Map<String, Object>
    }
}
