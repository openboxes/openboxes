package org.pih.warehouse.regression.apidocs

import grails.core.GrailsApplication
import grails.core.GrailsControllerClass
import grails.web.mapping.UrlMapping
import grails.web.mapping.UrlMappings
import org.yaml.snakeyaml.Yaml

/**
 * Builds the list of API operations from the runtime URL mapping table.
 *
 * Every candidate operation is derived from an /api mapping in UrlMappings.groovy and then checked
 * against the real routing: its sample URI is resolved with the application's own
 * {@link UrlMappings} holder (which only returns matches whose controller and action exist, in
 * precedence order), and the candidate is kept only when that resolution reaches the controller
 * action it claims. See route-methods.yaml and ignored-routes.yaml for the two reviewed inputs.
 */
class ApiRouteInventory {

    static final String ROUTE_METHODS = '/regression-apidocs/route-methods.yaml'
    static final String IGNORED_ROUTES = '/regression-apidocs/ignored-routes.yaml'

    /** Sample value substituted for path variables when resolving a candidate. */
    static final String SAMPLE_ID = 'x1'

    final UrlMappings holder
    final GrailsApplication grailsApplication
    /** (method, uri) -> "controller/action" of the winning match, or null when nothing resolves. */
    final Closure<String> resolver

    List<ApiRoute> routes = []
    List<Map> ignored = []
    /** Findings no ignored-routes.yaml rule accounts for. */
    List<Map> unaccounted = []
    List<String> problems = []
    Map<String, Integer> counts = [:].withDefault { 0 }

    ApiRouteInventory(UrlMappings holder, GrailsApplication grailsApplication, Closure<String> resolver) {
        this.holder = holder
        this.grailsApplication = grailsApplication
        this.resolver = resolver
    }

    ApiRouteInventory build() {
        List<Map> ignoreRules = loadYaml(IGNORED_ROUTES)
        List<Map> routeMethods = loadYaml(ROUTE_METHODS)

        // "/api" and "/api/..." only: "/apidocs" (the documentation page) is not part of the API
        List<UrlMapping> apiMappings = holder.urlMappings.findAll {
            String pattern = it.urlData.urlPattern
            pattern == '/api' || pattern.startsWith('/api/')
        } as List<UrlMapping>
        counts.apiMappingBlocks = apiMappings.size()

        List<ApiRoute> explicit = []
        List<ApiRoute> dynamic = []
        List<ApiRoute> generic = []
        List<Map> unbounded = []
        List<ApiRoute> unresolved = []
        List<ApiRoute> shadowed = []

        apiMappings.each { UrlMapping mapping ->
            Object actionName = mapping.actionName
            String pattern = mapping.urlData.urlPattern
            if (pattern.startsWith('/api/generic/')) {
                counts.genericTemplatePairs += ((Map) actionName).size()
                ((Map) actionName).keySet().each { String method ->
                    generic << route(mapping, method, 'genericApi', (String) ((Map) actionName)[method], ApiRoute.Source.GENERIC, [resource: 'product'])
                }
            } else if (isDynamic(mapping)) {
                if (actionName instanceof Map) {
                    counts.dynamicTemplatePairs += ((Map) actionName).size()
                    apiControllers().each { GrailsControllerClass controllerClass ->
                        String resource = controllerClass.logicalPropertyName - ~/Api$/
                        ((Map) actionName).each { String method, String action ->
                            dynamic << route(mapping, method, controllerClass.logicalPropertyName, action, ApiRoute.Source.DYNAMIC, [resource: resource])
                        }
                    }
                }
            } else if (actionName instanceof Map) {
                String controller = declaredController(mapping)
                ((Map) actionName).each { String method, String action ->
                    counts.explicitPairs++
                    explicit << route(mapping, method, controller, action, ApiRoute.Source.EXPLICIT, [:])
                }
            } else {
                unbounded << [mapping: mapping, controller: declaredController(mapping), action: actionName instanceof String ? actionName : null]
            }
        }

        // Explicit mappings: must resolve to the action they declare.
        explicit.each { ApiRoute r ->
            String winner = resolve(r)
            if (winner == null) {
                unresolved << r
                finding('missing-action', r, "${r.key} is mapped to ${r.target}, which does not exist (pattern ${r.mappingPattern})")
            } else if (winner != r.target) {
                shadowed << r
                finding('shadowed', r, "${r.key} is mapped to ${r.target} but another mapping answers it (${winner})")
            } else {
                routes << r
            }
        }
        this.explicitRoutes = new ArrayList<ApiRoute>(routes)

        // Dynamic ${resource} mappings: keep resolvable candidates. Shadow paths such as /api/categorys
        // next to the explicit /api/categories (same action, same method, same path apart from another
        // spelling of the same resource) are not listed; each is a finding for ignored-routes.yaml.
        dynamic.each { ApiRoute r ->
            if (resolve(r) != r.target) {
                counts.dynamicUnreachable++
            } else if (explicitShadowOf(r)) {
                counts.dynamicPruned++
                pruned << [route: r, explicit: explicitShadowOf(r)]
                finding('dynamic-shadow', r, "${r.key} reaches ${r.target}, which is documented as ${explicitShadowOf(r).key}")
            } else {
                counts.dynamicKept++
                routes << r
            }
        }
        Set<String> reachable = routes*.target as Set

        // Method-unbounded mappings: every action they can reach must be declared in
        // route-methods.yaml or accounted for in ignored-routes.yaml.
        Set<String> declared = routeMethods.collect { "${it.controller}/${it.action}".toString() } as Set
        Set<String> usedDeclarations = [] as Set
        unbounded.each { Map u ->
            UrlMapping mapping = (UrlMapping) u.mapping
            List<String> actions = u.action ? [(String) u.action] : controllerActions((String) u.controller)
            actions.each { String action ->
                String target = "${u.controller}/${action}"
                if (target in declared) {
                    usedDeclarations << target
                    return
                }
                ApiRoute candidate = route(mapping, 'GET', (String) u.controller, action, ApiRoute.Source.UNBOUNDED, [action: action])
                if (resolve(candidate) != target) {
                    counts.unboundedUnreachable++
                    return
                }
                candidate.method = null
                if (target in reachable) {
                    counts.unboundedAliases++
                    finding('wildcard-alias', candidate, "${candidate.path} reaches ${target} with any HTTP method; that action is documented under another path")
                } else {
                    finding('wildcard-only', candidate, "${candidate.path} reaches ${target} with any HTTP method and is not declared in route-methods.yaml")
                }
            }
        }
        Set<String> declaredPaths = [] as Set
        routeMethods.each { Map declaration ->
            String target = "${declaration.controller}/${declaration.action}"
            if (!(target in usedDeclarations)) {
                problems << "route-methods.yaml: ${target} is not reachable through any method-unbounded /api mapping (stale entry?)"
            }
            declaredPaths << (String) declaration.path
            ((List<String>) declaration.methods).each { String method ->
                ApiRoute r = new ApiRoute(path: declaration.path, method: method, controller: declaration.controller, action: declaration.action,
                        source: ApiRoute.Source.UNBOUNDED, pathParams: pathParams((String) declaration.path), mappingPattern: 'route-methods.yaml')
                String winner = resolve(r)
                if (winner != target) {
                    problems << "route-methods.yaml: ${r.key} resolves to ${winner ?: 'nothing'}, expected ${target}"
                } else {
                    counts.unboundedDeclared++
                    routes << r
                }
            }
        }

        // Generic API: one free-form family.
        generic.each { ApiRoute r ->
            if (resolve(r) == r.target) {
                routes << r
            } else {
                problems << "Generic API route ${r.key} does not resolve to ${r.target}"
            }
        }

        // Two mappings producing the same (path, method) must agree on the target.
        routes.groupBy { it.key }.each { String key, List<ApiRoute> same ->
            if (same*.target.unique().size() > 1) {
                problems << "Duplicate operation ${key} with different targets: ${same*.target}"
            }
        }
        int before = routes.size()
        routes = routes.unique { it.key }.sort()
        counts.duplicates = before - routes.size()

        // A method missing from a mapping's method map is not a 405: Grails dispatches it to the
        // controller's default action. Every such (path, method) that reaches an action is a finding.
        // (Paths from route-methods.yaml accept any method by design and are not checked.)
        List<ApiRoute> shadows = pruned.collect { (ApiRoute) it.route }
        Set<String> routedKeys = (routes*.key + shadows*.key) as Set
        (routes + shadows).groupBy { it.path }.each { String path, List<ApiRoute> pathRoutes ->
            if (path in declaredPaths) {
                return
            }
            (ApiRoute.METHODS - pathRoutes*.method).each { String method ->
                String winner = resolver.call(method, sampleUri(path))
                if (winner && !routedKeys.contains("${method} ${path}".toString())) {
                    ApiRoute r = new ApiRoute(path: path, method: method, controller: winner.split('/')[0], action: winner.split('/')[1])
                    if (r.action == defaultAction(r.controller)) {
                        counts.methodFallthrough++
                        finding('method-fallthrough', r, "${r.key} is not in the mapping's method map but reaches ${winner} (default action)")
                    } else {
                        finding('unlisted', r, "${r.key} reaches ${winner}, but no /api mapping lists it")
                    }
                }
            }
        }

        // Routes deliberately left out of the docs (kind "excluded").
        List<ApiRoute> excluded = []
        routes.each { ApiRoute r ->
            Map rule = matchRule(ignoreRules, 'excluded', r.path, r.method, r.target)
            if (rule) {
                excluded << r
                ignored << [kind: 'excluded', path: r.path, method: r.method, target: r.target, message: 'routed, left out of the docs', reason: rule.reason]
            }
        }
        counts.excluded = excluded.size()
        routes.removeAll(excluded)

        findings.each { Map f ->
            Map rule = matchRule(ignoreRules, (String) f.kind, (String) f.path, (String) f.method, (String) f.target)
            if (rule) {
                ignored << (f + [reason: rule.reason])
            } else {
                unaccounted << f
                problems << "Unaccounted route (${f.kind}): ${f.message}. Declare it in route-methods.yaml, or add it to ignored-routes.yaml " +
                        "(build/reports/api-docs/unaccounted-routes.yaml has the entries to review)"
            }
        }
        ignoreRules.each { Map rule ->
            (ruleMethods(rule) - (rule.usedMethods ?: [])).each { String method ->
                problems << "ignored-routes.yaml: no ${rule.kind} route matches ${method} ${rule.path} -> ${rule.target} (stale entry?)"
            }
        }

        counts.unresolvedMappings = unresolved.size()
        counts.shadowedMappings = shadowed.size()
        counts.listed = routes.count { it.source != ApiRoute.Source.GENERIC }
        counts.generic = routes.count { it.source == ApiRoute.Source.GENERIC }
        counts.ignored = ignored.size()
        counts.findings = findings.size()
        this
    }

    private List<Map> findings = []
    private List<ApiRoute> explicitRoutes = []
    /** Dynamic candidates not listed as shadow paths, with the explicit route they shadow. */
    List<Map> pruned = []

    /**
     * The explicit route a dynamic candidate shadows: same method and target, same path except the
     * resource segment, and both resource segments start with the resource's stem
     * ("categor" for category: categorys vs categories).
     */
    private ApiRoute explicitShadowOf(ApiRoute r) {
        List<String> segments = r.path.tokenize('/')
        String resource = r.controller - ~/Api$/
        String stem = resource.endsWith('y') ? resource[0..-2] : resource
        explicitRoutes.find { ApiRoute e ->
            List<String> other = e.path.tokenize('/')
            e.method == r.method && e.target == r.target && other.size() == segments.size() &&
                    other[1] != segments[1] && other[1].startsWith(stem) && segments[1].startsWith(stem) &&
                    (2..<segments.size()).every { int i -> other[i] == segments[i] }
        }
    }

    private void finding(String kind, ApiRoute r, String message) {
        findings << [kind: kind, path: r.path, method: r.method ?: '*', target: r.target, message: message]
    }

    /** Rules match on kind, path, target and method exactly; "methods: [..]" lists several, "*" means any method. */
    private static Map matchRule(List<Map> rules, String kind, String path, String method, String target) {
        String m = method ?: '*'
        Map rule = rules.find { it.kind == kind && it.path == path && it.target == target && m in ruleMethods(it) }
        if (rule) {
            rule.usedMethods = ((rule.usedMethods ?: []) + m).unique()
        }
        rule
    }

    private static List<String> ruleMethods(Map rule) {
        (rule.methods ?: [rule.method ?: '*']) as List<String>
    }

    private String resolve(ApiRoute r) {
        resolver.call(r.method ?: 'GET', sampleUri(r.path))
    }

    static String sampleUri(String pathTemplate) {
        pathTemplate.replaceAll(/\{resource\}/, 'product').replaceAll(/\{[^}]+\}/, SAMPLE_ID)
    }

    private static boolean isDynamic(UrlMapping mapping) {
        mapping.urlData.tokens.any { it == '(*)s' }
    }

    private List<GrailsControllerClass> apiControllers() {
        (grailsApplication.getArtefacts('Controller') as List<GrailsControllerClass>)
                .findAll { it.logicalPropertyName.endsWith('Api') && it.logicalPropertyName != 'genericApi' }
                .sort { it.logicalPropertyName }
    }

    private String defaultAction(String controller) {
        ((GrailsControllerClass) grailsApplication.getArtefactByLogicalPropertyName('Controller', controller))?.defaultAction
    }

    private List<String> controllerActions(String controller) {
        GrailsControllerClass controllerClass = (GrailsControllerClass) grailsApplication.getArtefactByLogicalPropertyName('Controller', controller)
        (controllerClass?.actions ?: []).sort() as List<String>
    }

    private static String declaredController(UrlMapping mapping) {
        Object controller = mapping.controllerName
        if (controller instanceof Closure) {
            Closure c = (Closure) controller.clone()
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c.delegate = [params: [:]]
            controller = c.call()
        }
        controller?.toString()
    }

    /**
     * Build a route from a mapping. Path variables become {name}; "(*)s" becomes the resource name
     * plus "s"; a variable in {@code literals} (resource, action) is replaced by its literal value.
     * Optional variables are left out.
     */
    private static ApiRoute route(UrlMapping mapping, String method, String controller, String action, ApiRoute.Source source, Map<String, String> literals) {
        String pattern = mapping.urlData.urlPattern
        List<String> names = mapping.constraints*.propertyName.findAll { it != 'format' } as List<String>
        Iterator<String> nameIterator = names.iterator()
        List<String> segments = []
        mapping.urlData.tokens.each { String token ->
            // OBAF also maps "$name**" (a variable that may contain slashes, token "(**)").
            if (!token.contains('(*')) {
                segments << token
                return
            }
            String name = nameIterator.next()
            boolean optional = token.endsWith('?')
            String value = literals[name] != null && !(source == ApiRoute.Source.GENERIC) ? literals[name] : "{${name}}".toString()
            if (optional) {
                return
            }
            segments << token.replace('(**)', value).replace('(*)', value).replace('?', '')
        }
        String path = '/' + segments.join('/')
        if (pattern.endsWith('/') && !path.endsWith('/')) {
            path += '/'
        }
        new ApiRoute(path: path, method: method, controller: controller, action: action, source: source,
                pathParams: pathParams(path), formatSuffix: pattern.contains('(.(*))?'), mappingPattern: pattern)
    }

    static List<String> pathParams(String path) {
        (path =~ /\{([^}]+)\}/).collect { ((List) it)[1] } as List<String>
    }

    private static List<Map> loadYaml(String resource) {
        InputStream stream = ApiRouteInventory.getResourceAsStream(resource)
        if (!stream) {
            return []
        }
        stream.withCloseable { (new Yaml().load(it) ?: []) as List<Map> }
    }
}
