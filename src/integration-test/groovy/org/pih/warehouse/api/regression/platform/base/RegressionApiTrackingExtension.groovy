package org.pih.warehouse.api.regression.platform.base

import io.restassured.RestAssured
import io.restassured.filter.Filter
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

/**
 * Hooks the regression suite's API tooling into the integration specs:
 *
 *  - installs the recorder, the OpenAPI validation filter and the response capture as RestAssured GLOBAL filters,
 *    so every call any integration spec makes goes through them without editing upstream code;
 *  - tells {@link ApiOperationRecorder} when a Spock feature method (as opposed to a fixture) is running;
 *  - switches {@link ObafAjaxHeaderFilter} on for {@link RegressionApiSpec} subclasses;
 *  - hands a spec's {@link EnforceSpecValidation} to {@link SpecValidationFilter}.
 *
 * Registered via META-INF/services in the integration-test resources. Inert unless the test JVM has
 * -Dregression.suite=true (set by the regression test tasks), and then only for the regression suite's specs, so
 * the other integration specs never see these filters.
 * The filters are re-installed before each spec in case a spec called RestAssured.reset().
 */
class RegressionApiTrackingExtension implements IGlobalExtension {

    static final List<Filter> GLOBAL_FILTERS = [
            ApiOperationRecorder.INSTANCE,   // records which API operations the tests exercise
            ObafAjaxHeaderFilter.INSTANCE,   // OBAF answers JSON errors to AJAX requests only (RegressionApiSpec only)
            SpecValidationFilter.INSTANCE,   // checks the call against the OpenAPI spec (report-only by default)
            ApiResponseCapture.INSTANCE,     // optionally keeps response samples for the spec
    ].asImmutable()

    static boolean enabled() {
        return Boolean.getBoolean('regression.suite')
    }

    @Override
    void start() {
        if (enabled()) {
            installFilters()
        }
    }

    static synchronized void installFilters() {
        List<Filter> current = RestAssured.filters()
        if (!GLOBAL_FILTERS.every { current.contains(it) }) {
            RestAssured.replaceFiltersWith(current.findAll { !(it in GLOBAL_FILTERS) } + GLOBAL_FILTERS)
        }
    }

    @Override
    void visitSpec(SpecInfo spec) {
        String specName = spec.reflection.name
        if (!enabled() || !specName.startsWith('org.pih.warehouse.api.regression.')) {
            return
        }
        boolean ajax = RegressionApiSpec.isAssignableFrom(spec.reflection)
        EnforceSpecValidation enforcement = spec.reflection.getAnnotation(EnforceSpecValidation)
        spec.addInterceptor { IMethodInvocation invocation ->
            installFilters()
            ApiOperationRecorder.currentSpec = specName
            ObafAjaxHeaderFilter.enabled = ajax
            SpecValidationFilter.specEnforcement = enforcement
            try {
                invocation.proceed()
            } finally {
                ApiOperationRecorder.currentSpec = null
                ObafAjaxHeaderFilter.enabled = false
                SpecValidationFilter.specEnforcement = null
            }
        }
        spec.allFeatures.each { feature ->
            feature.featureMethod.addInterceptor { IMethodInvocation invocation ->
                ApiOperationRecorder.currentFeature = "${specName}#${invocation.iteration?.name ?: feature.name}"
                try {
                    invocation.proceed()
                } finally {
                    ApiOperationRecorder.currentFeature = null
                }
            }
        }
    }

    @Override
    void stop() {}
}
