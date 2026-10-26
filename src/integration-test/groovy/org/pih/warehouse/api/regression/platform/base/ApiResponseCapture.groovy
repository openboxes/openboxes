package org.pih.warehouse.api.regression.platform.base

import groovy.json.JsonOutput
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification

/**
 * Keeps one real request/response sample per API operation, status and query parameter set, for drafting the
 * OpenAPI stubs of OBAF's routes. Enabled with -PapiDocs.captureDir=<dir> (the regression test tasks pass it to the
 * test JVM); a no-op otherwise, and never part of a gate.
 * Only calls made by feature methods are kept (not setup fixtures).
 */
class ApiResponseCapture implements Filter {

    static final ApiResponseCapture INSTANCE = new ApiResponseCapture()

    private static final String CAPTURE_DIR = System.getProperty('apiDocs.captureDir')

    @Override
    Response filter(FilterableRequestSpecification requestSpec,
                    FilterableResponseSpecification responseSpec,
                    FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec)
        if (CAPTURE_DIR && ApiOperationRecorder.currentFeature != null) {
            capture(requestSpec, response)
        }
        return response
    }

    private static synchronized void capture(FilterableRequestSpecification requestSpec, Response response) {
        String template = (requestSpec.basePath ?: '') + (requestSpec.userDefinedPath ?: requestSpec.derivedPath)
        String variant = requestSpec.queryParams.collect { k, v -> k == 'format' ? "${k}-${v}" : k }.sort().join('_')
        String name = "${requestSpec.method} ${template} ${response.statusCode} ${variant}"
                .replaceAll(/[^A-Za-z0-9_.-]+/, '_') + '.json'
        File dir = new File(CAPTURE_DIR)
        dir.mkdirs()
        File file = new File(dir, name)
        if (file.exists()) {
            return
        }
        file.text = JsonOutput.prettyPrint(JsonOutput.toJson([
                method     : requestSpec.method,
                template   : template,
                query      : requestSpec.queryParams,
                status     : response.statusCode,
                contentType: response.contentType,
                test       : ApiOperationRecorder.currentFeature,
                request    : requestSpec.body instanceof String ? redacted((String) requestSpec.body) : null,
                response   : response.asString(),
        ]))
    }

    /** Samples must not keep credentials: password values are replaced. */
    static String redacted(String body) {
        return body.replaceAll(/("[^"]*password[^"]*"\s*:\s*)"(?:[^"\\]|\\.)*"/, '$1"***"')
    }
}
