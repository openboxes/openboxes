package org.pih.warehouse.importer.spec.base

import org.pih.warehouse.common.base.IntegrationSpec

/**
 * Base class for all importer tests.
 *
 * The importers only need a small sub-set of the application beans and so we could
 * define a custom context configuration here for them to use, but since API tests
 * already need the full context, which across all tests when running them together,
 * it would only save time when running importer tests on their own.
 */
class ImporterSpec extends IntegrationSpec {

}
