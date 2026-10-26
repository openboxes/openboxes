package org.pih.warehouse.api.regression.platform.builder

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Organization

/**
 * Upstream's LocationTestBuilder plus an organization() method. Port: `new LocationTestBuilder()` ->
 * `new RegressionLocationTestBuilder()` where a spec calls organization(); the inherited builder methods return the
 * runtime instance, so chains keep working (dynamic Groovy).
 */
@InheritConstructors
class RegressionLocationTestBuilder extends LocationTestBuilder {

    /** The organization the location belongs to (after asFacility, which sets the main one). */
    RegressionLocationTestBuilder organization(Organization organization) {
        args.organization = organization
        return this
    }
}
