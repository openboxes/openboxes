package org.pih.warehouse.regression.apidocs

import spock.lang.Shared
import spock.lang.Specification

/**
 * What "x-doc-status: complete" promises about an operation, checked on the bundled spec (what Swagger UI and API
 * clients get):
 *
 *  - a summary and a description written for people (not the generated controller.action placeholder);
 *  - every parameter described, every path parameter declared;
 *  - request and success response bodies with real schemas (no "not documented yet" stub) and an example;
 *  - error responses in the API's error shape, including a specific 4xx wherever the call can fail that way.
 *
 * The remaining promise - at least one integration test calls the operation successfully through the spec
 * validator - is checked by the build's apiOperationCoverage task, which sees the test run.
 */
class OpenApiCompleteOperationsSpec extends Specification {

    /** Operations that accept a session but answer anonymous callers too. */
    static final List<String> LOGIN_OPTIONAL = ['api_logout', 'api_status']

    @Shared
    Map spec

    @Shared
    Map<String, Map> complete

    void setupSpec() {
        OpenApiBundler bundler = new OpenApiBundler({ String file ->
            File f = new File(OpenApiDocument.DEFAULT_DIR, file)
            f.exists() ? f.getText('UTF-8') : null
        })
        spec = bundler.bundle()
        assert bundler.problems.isEmpty()
        complete = [:]
        ((Map<String, Map>) spec.paths).each { String path, Map item ->
            item.each { String method, Object op ->
                boolean isComplete = ((Map) op)[OpenApiDocument.STATUS] == OpenApiDocument.STATUS_COMPLETE
                if (method in OpenApiDocument.HTTP_METHODS && isComplete) {
                    complete["${method.toUpperCase()} ${path}".toString()] = (Map) op
                }
            }
        }
    }

    void 'the demo slice is documented completely'() {
        expect: 'guards against the check silently covering nothing'
        complete.size() >= 45
    }

    void 'every documented error response shows an example with its own status'() {
        given: 'every error response body that uses the Error schema itself (extensions document their own body)'
        Map error = resolve(['$ref': '#/components/schemas/Error']) as Map
        List<String> problems = []
        Map<String, Integer> checked = [specific: 0, 'default': 0]
        ((Map<String, Map>) spec.paths).each { String path, Map item ->
            item.findAll { it.key in OpenApiDocument.HTTP_METHODS }.each { String method, Object op ->
                ((Map<String, Object>) ((Map) op).responses ?: [:]).each { String status, Object ref ->
                    if (!(status ==~ /[45]\d\d/ || status == 'default')) {
                        return
                    }
                    ((Map<String, Map>) (resolve(ref) as Map)?.content ?: [:]).each { String mediaType, Map media ->
                        if (resolve(media?.schema) != error) {
                            return
                        }
                        checked[status == 'default' ? 'default' : 'specific']++
                        String key = "${method.toUpperCase()} ${path} ${status} ${mediaType}"
                        problems.addAll(exampleProblems(key, status, media))
                    }
                }
            }
        }

        expect: 'both kinds were found (guards against the check silently covering nothing)'
        checked.specific > 500
        checked.'default' > 300
        problems.isEmpty() ?: fail(problems)
    }

    void 'the error example check refuses a missing example, a wrong status and a non-integer code'() {
        expect:
        exampleProblems('k', '403', [:]) == ['k: no example']
        exampleProblems('k', '403', [example: [errorCode: 404, errorMessage: 'x']]).size() == 1
        exampleProblems('k', 'default', [example: [errorCode: '500', errorMessage: 'x']]).size() == 1
        exampleProblems('k', 'default', [example: [errorCode: 600, errorMessage: 'x']]).size() == 1
        exampleProblems('k', '400', [examples: [a: [value: [errorCode: 400, errorMessage: 'x']], b: [value: [errorCode: 409, errorMessage: 'y']]]]).size() == 1
        exampleProblems('k', '403', [example: [errorCode: 403, errorMessage: 'Access denied']]) == []
        exampleProblems('k', 'default', [example: [errorCode: 500, errorMessage: 'x']]) == []
    }

    /** Problems with an error response's example(s): one must exist, with errorCode = the status (4xx/5xx for default). */
    List<String> exampleProblems(String key, String status, Map media) {
        List<Map> values = media.example != null ? [media.example as Map]
                : ((Map<String, Object>) media.examples ?: [:]).values().collect { (resolve(it) as Map)?.value as Map }
        if (!values) {
            return ["${key}: no example".toString()]
        }
        return values.findAll { Map value ->
            Object code = value?.errorCode
            boolean codeMatches = code instanceof Integer &&
                    (status == 'default' ? (code as int) in 400..599 : code == (status as Integer))
            return !codeMatches || !(value?.errorMessage instanceof String)
        }.collect { "${key}: example ${it} does not match the status".toString() }
    }

    void 'every complete operation meets the definition of complete'() {
        when:
        List<String> problems = complete.collectMany { String key, Map op ->
            problemsOf(key, op).collect { "${key}: ${it}".toString() }
        }

        then:
        problems.isEmpty() ?: fail(problems)
    }

    void 'each create schema has exactly the properties of its input schema'() {
        given: 'create schemas are copies with required fields (the validator mishandles allOf with open parts)'
        Map schemas = (Map) spec.components.schemas

        expect:
        ['ProductCreate': 'ProductInput', 'CategoryCreate': 'CategoryInput', 'UnitOfMeasureCreate': 'UnitOfMeasureInput',
         'StockMovementCreate': 'StockMovementInput', 'StocklistCreate': 'StocklistInput']
                .every { String create, String input -> schemas[create].properties == schemas[input].properties }
    }

    void 'the check finds a stub response in an operation marked complete'() {
        given:
        Map op = [
                summary          : 'List things',
                description      : 'Lists the things.',
                'x-openboxes-controller': 'thingApi',
                'x-openboxes-action': 'list',
                responses        : [
                        '200': ['$ref': '#/components/responses/StubSuccess'],
                        '404': ['$ref': '#/components/responses/NotFound'],
                ],
        ]

        expect:
        problemsOf('GET /api/things', op).any { it.contains('not documented yet') }
    }

    List<String> problemsOf(String key, Map op) {
        List<String> problems = []
        String path = key.split(' ', 2)[1]
        String placeholder = "${op['x-openboxes-controller']}.${op['x-openboxes-action']}"
        if (!op.summary || op.summary == placeholder) {
            problems << 'needs a summary'
        }
        if (!op.description) {
            problems << 'needs a description'
        }
        List<Map> parameters = ((List) op.parameters ?: []).collect { resolve(it) as Map }
        parameters.findAll { !it.description }.each { problems << "parameter ${it.name} needs a description" }
        Set<String> declaredPathParams = parameters.findAll { it.in == 'path' }*.name as Set
        Set<String> templateParams = (path =~ /\{([^}]+)\}/).collect { it[1] } as Set
        if (declaredPathParams != templateParams) {
            problems << "path parameters ${declaredPathParams} do not match the path ${templateParams}"
        }
        if (op.requestBody) {
            Map body = resolve(op.requestBody) as Map
            if (isStub(op.requestBody) || !body.content) {
                problems << 'request body is not documented yet'
            } else {
                body.content.each { String mediaType, Map media ->
                    if (!media.schema || isOpenSchema(media.schema)) {
                        problems << "request body ${mediaType} needs a schema"
                    }
                }
            }
        }
        Map<String, Object> responses = (Map) op.responses ?: [:]
        Map<String, Object> success = responses.findAll { it.key ==~ /2\d\d/ }
        if (!success) {
            problems << 'needs a success response'
        }
        success.each { String status, Object ref ->
            Map response = resolve(ref) as Map
            if (isStub(ref)) {
                problems << "response ${status} is not documented yet"
                return
            }
            (response.content ?: [:]).each { String mediaType, Map media ->
                if (!media.schema || isOpenSchema(media.schema)) {
                    problems << "response ${status} ${mediaType} needs a schema"
                }
                if (media.example == null && !media.examples && resolve(media.schema)?.example == null) {
                    problems << "response ${status} ${mediaType} needs an example"
                }
            }
        }
        Map<String, Object> errors = responses.findAll { it.key ==~ /[45]\d\d/ || it.key == 'default' }
        // An operation that needs a login, or takes input, can fail in a specific way (401, 404, 400, ...). Login
        // counts as required unless the operation declares no security (security: [], the public session calls)
        // or answers anonymous callers too (logout and status accept a session but do not need one).
        boolean loginRequired = op.security != [] && !(op.operationId in LOGIN_OPTIONAL)
        boolean canFailSpecifically = loginRequired || parameters || op.requestBody
        if (canFailSpecifically && !responses.keySet().any { it ==~ /4\d\d/ }) {
            problems << 'needs at least one specific 4xx response'
        }
        errors.each { String status, Object ref ->
            Map schema = resolve(resolve(ref)?.content?.'application/json'?.schema) as Map
            if (!isErrorShape(schema)) {
                problems << "response ${status} must use the Error schema (or one that extends it)"
            }
        }
        if (mentionsStub(op)) {
            problems << 'refers to a "not documented yet" stub component'
        }
        return problems
    }

    /**
     * The API's error shape: the Error schema itself, or a schema that extends it (errorCode and errorMessage
     * required, e.g. a validation error that also carries the rows it checked).
     */
    boolean isErrorShape(Map schema) {
        Map error = resolve(['$ref': '#/components/schemas/Error']) as Map
        return schema == error || (schema?.required as List)?.containsAll(['errorCode', 'errorMessage']) &&
                schema.properties?.errorCode?.type == 'integer' && schema.properties?.errorMessage?.type == 'string'
    }

    /** Follows local "#/components/..." references. */
    Object resolve(Object node) {
        int hops = 0
        while (node instanceof Map && ((Map) node).'$ref' instanceof String && hops++ < 10) {
            String ref = ((Map) node).'$ref'
            if (!ref.startsWith('#/')) {
                return node
            }
            node = ref.substring(2).split('/').inject(spec) { Object current, String token ->
                current instanceof Map ? ((Map) current)[OpenApiDocument.unescapePointer('/' + token)] : null
            }
        }
        return node
    }

    static boolean isStub(Object node) {
        Object ref = node instanceof Map ? ((Map) node).'$ref' : null
        return ref instanceof String && ((String) ref).contains('/Stub')
    }

    /** A schema that says nothing: no type, no properties, no reference - just "any object". */
    boolean isOpenSchema(Object schema) {
        Map resolved = resolve(schema) as Map
        return resolved == null || !(resolved.type || resolved.properties || resolved.items || resolved.allOf ||
                resolved.oneOf || resolved.anyOf)
    }

    static boolean mentionsStub(Object node) {
        if (node instanceof Map) {
            return isStub(node) || ((Map) node).values().any { mentionsStub(it) }
        }
        if (node instanceof List) {
            return ((List) node).any { mentionsStub(it) }
        }
        return false
    }

    private static boolean fail(List messages) {
        throw new AssertionError("${messages.size()} problem(s):\n  " + messages.join('\n  '))
    }
}
