package org.pih.warehouse.component.base

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * Configures the application context for tests, wiring in our test components.
 *
 * As an extra layer of security, our tests components should only ever use the @TestComponent annotation
 * (and not @Component). That way we can be extra certain that our test components wont ever leak into our
 * real app context when running the app for production use.
 *
 * https://docs.spring.io/spring-framework/reference/testing/testcontext-framework.html
 */
@TestConfiguration
@ComponentScan("org.pih.warehouse.component.*")
class ComponentTestConfig {

}
