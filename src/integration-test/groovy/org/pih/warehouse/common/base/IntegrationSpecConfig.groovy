package org.pih.warehouse.common.base

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.Ordered

import org.pih.warehouse.common.web.DeferResponseCloseFilter

/**
 * Manages any integration test specific application configuration such as wiring up test components.
 *
 * As an extra layer of security, our tests components should only ever use the @TestComponent annotation
 * (and not @Component). That way we can be extra certain that our test components wont ever leak into our
 * real app context when running the app for production use.
 *
 * Further reading:
 * - https://docs.spring.io/spring-framework/reference/testing/testcontext-framework.html
 * - https://docs.spring.io/spring-boot/docs/1.5.9.RELEASE/reference/htmlsingle/#boot-features-testing-spring-boot-applications-detecting-config
 *
 * TODO: https://pihemr.atlassian.net/browse/OBPIH-6486
 *       We currently have all integration tests importing this single config, which isn't great for test flexibility.
 *       We want our tests to be able to set up their own configs as needed. For example, we want api tests to import
 *       an ApiTestConfig class that does "@ComponentScan("org.pih.warehouse.api.*")" to wire in api clients without
 *       forcing slice tests to bring them in as well. Unfortunately the @ComponentScan breaks tests if we have it
 *       for some tests but not all of them. We need to figure out why this is the case.
 */
@TestConfiguration
@ComponentScan(["org.pih.warehouse.api.*", "org.pih.warehouse.slice.*", "org.pih.warehouse.smoke.*"])
class IntegrationSpecConfig {

    /**
     * Keeps a response that the controller finished by closing the writer or stream from completing before the
     * request has unwound, so that a spec's next request cannot overtake the previous request's transaction.
     * See DeferResponseCloseFilter.
     */
    @Bean
    FilterRegistrationBean deferResponseCloseFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean(new DeferResponseCloseFilter())
        // Right after the Sentry tracing filter (HIGHEST_PRECEDENCE + 1 in resources.groovy), so that Sentry's request
        // span still wraps everything and every other filter and the Grails dispatcher see the wrapped response.
        registration.order = Ordered.HIGHEST_PRECEDENCE + 2
        return registration
    }
}
