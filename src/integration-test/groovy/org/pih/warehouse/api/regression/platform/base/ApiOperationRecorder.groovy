package org.pih.warehouse.api.regression.platform.base

import groovy.json.JsonOutput
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification

/**
 * Records every API call so the build can report which API operations the tests exercise.
 * Installed as a RestAssured GLOBAL filter
 * by {@link RegressionApiTrackingExtension} instead of being added in upstream's Api.request() (not edited).
 *
 * One JSON line per call is appended to "hits.jsonl" in the directory given by the "apiCoverage.dir" system property
 * (set by the regression test tasks in gradle/regression-tests.gradle). A no-op when the property is absent.
 *
 * Only calls made while a Spock feature method is running count towards coverage; calls from setup/cleanup fixtures
 * are recorded with "feature: false".
 */
class ApiOperationRecorder implements Filter {

    static final ApiOperationRecorder INSTANCE = new ApiOperationRecorder()

    private static final String OUTPUT_DIR = System.getProperty('apiCoverage.dir')

    /** The spec and feature currently running, or null outside of a feature method. Set by the Spock extension. */
    static volatile String currentFeature

    /** The spec currently running (set for setup/cleanup too). Set by the Spock extension. */
    static volatile String currentSpec

    @Override
    Response filter(FilterableRequestSpecification requestSpec,
                    FilterableResponseSpecification responseSpec,
                    FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec)
        if (OUTPUT_DIR) {
            record([
                    method    : requestSpec.method,
                    basePath  : requestSpec.basePath,
                    template  : requestSpec.userDefinedPath,
                    path      : requestSpec.derivedPath,
                    pathParams: requestSpec.namedPathParams.collectEntries { k, v -> [(k): String.valueOf(v)] },
                    status    : response.statusCode,
                    feature   : currentFeature != null,
                    spec      : currentSpec,
                    test      : currentFeature,
                    // Passed the OpenAPI validation filter (report-only mode: no violation was recorded for it).
                    validated : SpecValidationFilter.consumeValidated(),
            ])
        } else {
            SpecValidationFilter.consumeValidated()
        }
        return response
    }

    private static synchronized void record(Map hit) {
        File dir = new File(OUTPUT_DIR)
        dir.mkdirs()
        new File(dir, 'hits.jsonl') << JsonOutput.toJson(hit) + '\n'
    }
}
