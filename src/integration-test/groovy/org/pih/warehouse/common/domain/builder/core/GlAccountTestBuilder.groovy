package org.pih.warehouse.common.domain.builder.core

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.GlAccountType

@InheritConstructors
class GlAccountTestBuilder extends TestBuilder<GlAccount> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                code: randomUtil.randomStringFieldValue("code"),
                name: randomUtil.randomStringFieldValue("name"),
        ] as Map<String, Object>
    }

    GlAccountTestBuilder glAccountType(GlAccountType glAccountType) {
        args.glAccountType = glAccountType
        return this
    }

    GlAccountTestBuilder active(Boolean active) {
        args.active = active
        return this
    }
}
