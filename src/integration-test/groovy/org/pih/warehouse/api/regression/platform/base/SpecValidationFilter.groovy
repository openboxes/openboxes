package org.pih.warehouse.api.regression.platform.base

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.report.LevelResolver
import com.atlassian.oai.validator.report.ValidationReport
import com.atlassian.oai.validator.restassured.RestAssuredRequest
import com.atlassian.oai.validator.restassured.RestAssuredResponse
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist
import com.atlassian.oai.validator.whitelist.rule.WhitelistRule
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification

import org.pih.warehouse.regression.apidocs.OpenApiBundler

/**
 * Checks every API call against the regression suite's OpenAPI spec (src/integration-test/resources/openapi).
 *
 * REPORT-ONLY by default (-Pregression.specValidation=enforce makes a violation fail the call; per spec,
 * {@link EnforceSpecValidation} enforces during its features; -Pregression.specValidation=report-only ignores those
 * annotations too, e.g. to triage a family's assertion failures). In report mode nothing ever throws: each violation is one JSON line in
 * ${regression.reports.dir}/spec-violations/${regression.testTask}.jsonl, and the regressionReports task copies them all to
 * build/reports/regression/spec-violations.jsonl with a per-kind summary. Why report-only: OBAF's error layer does not
 * follow the documented error contract (errors are {errorCode, errorMessage} only for AJAX requests, an anonymous API
 * call is a 302 to errors/handleUnauthorized, role refusals redirect to handleForbidden, many client errors are 500s),
 * and the OBAF-only operations are stubs.
 *
 * Violation kinds:
 *   not-in-spec            the operation is not in the spec (or not with this method)
 *   request / response     the request or response does not match the documented schema/status/content type
 *   obaf-auth-redirect     302 to errors/handleUnauthorized or handleForbidden (OBAF behaviour; the documented contract is 401/403)
 *   redirect               any other 3xx from an /api call
 *   html                   an HTML page from an /api call
 *   error-shape            a 4xx/5xx that is not JSON {errorCode, errorMessage}
 *   error-code             errorCode differs from the HTTP status
 *   validator-unavailable  the spec could not be bundled/loaded (logged once)
 * Only /api calls are checked.
 */
class SpecValidationFilter implements Filter {

    static final SpecValidationFilter INSTANCE = new SpecValidationFilter()

    static boolean enforcing() {
        return System.getProperty('regression.specValidation', 'report') == 'enforce'
    }

    /** The running spec's {@link EnforceSpecValidation}, or null (set by RegressionApiTrackingExtension). */
    static volatile EnforceSpecValidation specEnforcement

    /** Enforce this call: globally, or because a feature of an {@link EnforceSpecValidation} spec is running. */
    static boolean enforcingNow() {
        return enforcing() || (specEnforcement != null && ApiOperationRecorder.currentFeature != null &&
                System.getProperty('regression.specValidation', 'report') != 'report-only')
    }

    /** Whether the running spec's {@link EnforceSpecValidation#tolerate} rules cover this violation. */
    static boolean tolerated(Map violation, String method, String path) {
        String apiPath = path.replaceFirst(/^\/openboxes\/api/, '')
        return specEnforcement?.tolerate()?.any { String rule ->
            List<String> parts = rule.trim().split(/\s+/, 4) as List<String>
            parts.size() == 4 && parts[1] == violation.kind && parts[2].equalsIgnoreCase(method) && apiPath ==~ parts[3]
        } ?: false
    }

    /** An operation whose docs are still a stub (x-doc-status: stub) may be called with undocumented query parameters. */
    private static final WhitelistRule STUB_QUERY_PARAMETERS = { message, operation, request, response ->
        message.key == 'validation.request.parameter.query.unexpected' &&
                operation?.operation?.extensions?.get('x-doc-status') == 'stub'
    } as WhitelistRule

    /** The RestAssured adapter hands the validator no body for multipart requests. */
    private static final WhitelistRule MULTIPART_BODY_NOT_VISIBLE = { message, operation, request, response ->
        message.key == 'validation.request.body.missing' &&
                request?.contentType?.orElse('')?.toLowerCase()?.startsWith('multipart/')
    } as WhitelistRule

    /** An object body of the generic create (documented as object-or-array). */
    private static final Set<String> OBJECT_OR_ARRAY_BODY_OPERATIONS = ['genericApi_create'] as Set

    private static final WhitelistRule OBJECT_OR_ARRAY_BODY = { message, operation, request, response ->
        message.key.startsWith('validation.request.body.schema') && message.key.endsWith('additionalProperties') &&
                operation?.operation?.operationId in OBJECT_OR_ARRAY_BODY_OPERATIONS &&
                request?.body?.orElse('')?.trim()?.startsWith('{')
    } as WhitelistRule

    private static final ThreadLocal<Boolean> LAST_CALL_VALIDATED = ThreadLocal.withInitial { false }

    private static volatile boolean unavailableReported = false

    @Lazy
    static OpenApiInteractionValidator validator = {
        OpenApiBundler bundler = new OpenApiBundler({ String file ->
            SpecValidationFilter.classLoader.getResource("openapi/${file}")?.getText('UTF-8')
        })
        Map spec = bundler.bundle()
        if (bundler.problems) {
            throw new IllegalStateException("The OpenAPI spec does not bundle: ${bundler.problems}")
        }
        OpenApiInteractionValidator
                .createForInlineApiSpecification(JsonOutput.toJson(spec))
                .withBasePathOverride('/openboxes')
                .withLevelResolver(LevelResolver.create()
                        .withLevel('validation.request.security', ValidationReport.Level.IGNORE)
                        .build())
                .withWhitelist(ValidationErrorsWhitelist.create()
                        .withRule('stub operations do not list their query parameters yet', STUB_QUERY_PARAMETERS)
                        .withRule('the validator cannot read multipart request bodies', MULTIPART_BODY_NOT_VISIBLE)
                        .withRule('an object body of an object-or-array operation', OBJECT_OR_ARRAY_BODY))
                .build()
    }()

    /** Whether the last call on this thread passed validation (read and reset by ApiOperationRecorder). */
    static boolean consumeValidated() {
        boolean validated = LAST_CALL_VALIDATED.get()
        LAST_CALL_VALIDATED.set(false)
        return validated
    }

    @Override
    Response filter(FilterableRequestSpecification requestSpec,
                    FilterableResponseSpecification responseSpec,
                    FilterContext ctx) {
        LAST_CALL_VALIDATED.set(false)
        Response response = ctx.next(requestSpec, responseSpec)
        Request request = RestAssuredRequest.of(requestSpec)
        if (!(request.path ==~ /^\/openboxes\/api(\/.*)?$/)) {
            return response
        }
        List<Map> violations = []
        String body = safeBody(response)
        Map contract = contractProblem(response.statusCode, response.contentType, body, response.header('Location'))
        if (contract) {
            violations << contract
        }
        if (!contract || !(contract.kind in ['obaf-auth-redirect', 'redirect', 'html'])) {
            ValidationReport report = validate(request, response)
            if (report == null) {
                violations << [kind: 'validator-unavailable', messages: []]
            } else if (report.hasErrors()) {
                List<ValidationReport.Message> errors = report.messages.findAll { it.level == ValidationReport.Level.ERROR }
                String kind = errors.any { it.key in ['validation.request.path.missing', 'validation.request.operation.notAllowed'] } ?
                        'not-in-spec' : errors.any { it.key.startsWith('validation.request') } ? 'request' : 'response'
                violations << [kind: kind, messages: errors.collect { "${it.key}: ${it.message}".toString().take(500) }]
            }
        }
        if (violations) {
            violations.each { record(it, request, response.statusCode) }
            List<Map> enforced = enforcing() ? violations : enforcingNow() ?
                    violations.findAll { !tolerated(it, request.method.name(), request.path) } : []
            if (enforced) {
                throw new SpecValidationException("${request.method} ${request.path} -> ${response.statusCode}: " +
                        enforced.collect { "${it.kind} ${it.messages ?: it.detail ?: ''}" }.join('; '))
            }
        } else {
            LAST_CALL_VALIDATED.set(true)
        }
        return response
    }

    private static ValidationReport validate(Request request, Response response) {
        try {
            return response.statusCode < 400 ?
                    validator.validate(request, RestAssuredResponse.of(response)) :
                    validator.validateResponse(request.path, request.method, RestAssuredResponse.of(response))
        } catch (Throwable t) {
            if (!unavailableReported) {
                unavailableReported = true
                System.err.println("[SpecValidationFilter] validator unavailable: ${t}")
            }
            return null
        }
    }

    private static String safeBody(Response response) {
        try {
            return response.body?.asString()
        } catch (Exception ignored) {
            return null
        }
    }

    /**
     * What the API promises beyond the spec (no redirects or pages, errors in the error shape), classified for
     * OBAF so the report can tell OBAF's known behaviour (obaf-auth-redirect) from everything else.
     * Returns null when the response keeps the promise.
     */
    static Map contractProblem(int status, String contentType, String body, String location = null) {
        if (status >= 300 && status < 400) {
            if (location?.contains('handleUnauthorized') || location?.contains('handleForbidden')) {
                return [kind: 'obaf-auth-redirect', detail: location.replaceFirst(/^https?:\/\/[^\/]+/, '')]
            }
            return [kind: 'redirect', detail: location?.replaceFirst(/^https?:\/\/[^\/]+/, '')]
        }
        if (contentType?.startsWith('text/html')) {
            return [kind: 'html', detail: contentType]
        }
        if (status >= 400) {
            if (!contentType?.startsWith('application/json')) {
                return [kind: 'error-shape', detail: "content type '${contentType}'".toString()]
            }
            Object error
            try {
                error = new JsonSlurper().parseText(body ?: '')
            } catch (Exception ignored) {
                return [kind: 'error-shape', detail: body?.take(200)]
            }
            if (!(error instanceof Map) || !(error.errorMessage instanceof String)) {
                return [kind: 'error-shape', detail: body?.take(200)]
            }
            if (error.errorCode != status) {
                return [kind: 'error-code', detail: "errorCode ${error.errorCode} on HTTP ${status}".toString()]
            }
        }
        return null
    }

    private static synchronized void record(Map violation, Request request, int status) {
        String dir = System.getProperty('regression.reports.dir')
        if (!dir) {
            return
        }
        File out = new File(dir, "spec-violations/${System.getProperty('regression.testTask', 'integrationTest')}.jsonl")
        out.parentFile.mkdirs()
        out << JsonOutput.toJson([
                kind    : violation.kind,
                method  : request.method.name(),
                path    : request.path,
                status  : status,
                spec    : ApiOperationRecorder.currentSpec,
                test    : ApiOperationRecorder.currentFeature,
                detail  : violation.detail,
                messages: violation.messages,
        ]) + '\n'
    }

    static class SpecValidationException extends RuntimeException {
        SpecValidationException(String message) {
            super(message)
        }
    }
}
