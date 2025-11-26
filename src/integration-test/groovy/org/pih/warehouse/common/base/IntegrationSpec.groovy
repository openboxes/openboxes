package org.pih.warehouse.common.base

import grails.buildtestdata.TestDataBuilder
import grails.test.mixin.integration.Integration
import org.springframework.context.annotation.Import
import spock.lang.Specification

/**
 * Base class for all integration tests.
 */
@Integration
@Import(IntegrationSpecConfig.class)
abstract class IntegrationSpec extends Specification implements TestDataBuilder {
}
