package org.pih.warehouse.common.base

import grails.buildtestdata.TestDataBuilder
import grails.test.mixin.integration.Integration
import org.pih.warehouse.Application
import org.springframework.context.annotation.Import
import spock.lang.Specification

/**
 * Base class for all integration tests.
 *
 * Explicitly assigning applicationClass here makes integration tests work on a Mac.
 * This workaround may not be needed on other platforms, or at all in Grails 4+.
 * I learned about it from an old SO post detailing a similar issue in IntelliJ.
 * See https://stackoverflow.com/questions/48823524/grails-3-intellij-running-integration-tests-yields-no-gorm-implementations-c
 */
@Integration(applicationClass = Application.class)
@Import(IntegrationSpecConfig.class)
abstract class IntegrationSpec extends Specification implements TestDataBuilder {
}