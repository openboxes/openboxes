package org.pih.warehouse.common.service

/**
 * Test services contain common helper/convenience methods that tests can use to simplify their flows.
 *
 * A common use case for a test service is test data population that would be difficult to achieve via API calls alone,
 * or when we need to chain multiple API calls together to achieve some flow that tests will re-use a lot.
 *
 * Make sure to annotate child class implementations with @Transactional as needed otherwise any database operations
 * in the service won't actually be applied.
 *
 * We should avoid putting overly complex logic inside of test services. The more test-specific logic that we add,
 * the less we end up testing actual application code and the more risk for introducing test-specific bugs. Ideally
 * tests are simply a series of API calls followed by assertions.
 */
interface BaseTestService {
}
