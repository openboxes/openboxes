package org.pih.warehouse.api.regression.platform.base

import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification

/**
 * Adapter for OBAF's error layer: OBAF answers errors as {errorCode, errorMessage}
 * JSON only to requests it considers AJAX (RequestUtil.isAjax: X-Requested-With, or JSON in Accept/Content-Type);
 * anything else gets a redirect or an HTML page. The regression specs assume JSON errors everywhere, so while a
 * {@link RegressionApiSpec} runs every request carries "X-Requested-With: XMLHttpRequest" unless it sets its own.
 *
 * Enabled per spec by {@link RegressionApiTrackingExtension}.
 */
class ObafAjaxHeaderFilter implements Filter {

    static final ObafAjaxHeaderFilter INSTANCE = new ObafAjaxHeaderFilter()

    static volatile boolean enabled = false

    @Override
    Response filter(FilterableRequestSpecification requestSpec,
                    FilterableResponseSpecification responseSpec,
                    FilterContext ctx) {
        if (enabled && !requestSpec.headers.hasHeaderWithName('X-Requested-With')) {
            requestSpec.header('X-Requested-With', 'XMLHttpRequest')
        }
        return ctx.next(requestSpec, responseSpec)
    }
}
