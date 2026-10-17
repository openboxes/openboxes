package org.pih.warehouse.api.regression.platform.base

import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification

import org.pih.warehouse.product.Category

/**
 * Waits after a successful POST /api/categories until the new category is committed.
 *
 * CategoryApiController.save() saves without flushing and renders the category as JSON; the JSON converter flushes
 * and closes the response writer, so the client has the response (with the new id) before the open-session-in-view
 * flush writes the row. ApiSpec.setup() creates the root category and immediately POSTs the main product that
 * references it; when the server is slow to flush, the product insert fails on the product -> category foreign key
 * (HTTP 500, "Cannot add or update a child row"). The filter closes that window: it returns the response only once
 * the id is visible to a new transaction (or after a bounded wait, leaving any failure to the caller's assertions).
 *
 * Enabled per spec by {@link RegressionApiTrackingExtension}.
 */
class CategoryCommitFilter implements Filter {

    static final CategoryCommitFilter INSTANCE = new CategoryCommitFilter()

    static volatile boolean enabled = false

    static final long TIMEOUT_MS = 10000

    static final long POLL_MS = 25

    @Override
    Response filter(FilterableRequestSpecification requestSpec,
                    FilterableResponseSpecification responseSpec,
                    FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec)
        if (enabled && requestSpec.method == 'POST' && isCategoryCollection(requestSpec.URI)
                && response.statusCode == 200) {
            String id = createdId(response)
            if (id) {
                awaitCommitted(id)
            }
        }
        return response
    }

    private static boolean isCategoryCollection(String uri) {
        String path = new URI(uri).path
        return path?.endsWith('/api/categories')
    }

    private static String createdId(Response response) {
        try {
            Object id = response.jsonPath().get('id')
            return id instanceof String ? (String) id : null
        } catch (Exception ignored) {
            return null
        }
    }

    private static void awaitCommitted(String id) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            // A new transaction each time: a fresh snapshot on its own connection, never the caller's transaction.
            boolean committed = Category.withNewTransaction { Category.countById(id) > 0 }
            if (committed) {
                return
            }
            Thread.sleep(POLL_MS)
        }
    }
}
