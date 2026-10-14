package org.pih.warehouse.regression.apidocs

import spock.lang.Shared

/**
 * Keeps the OpenAPI spec and the routing in sync: every /api operation the application routes must
 * be in the spec, every operation in the spec must be routed, and each must point at the same
 * controller action. Writes the inventory report to build/reports/api-docs/.
 */
class OpenApiCompletenessSpec extends ApiInventorySpecification {

    @Shared ApiRouteInventory inventory
    @Shared OpenApiDocument document

    void setup() {
        if (!inventory) {
            inventory = buildInventory()
            document = new OpenApiDocument().load()
            new ApiInventoryReport(inventory, document).write(new File('build/reports/api-docs'))
        }
    }

    void "the route inventory is consistent with UrlMappings, route-methods.yaml and ignored-routes.yaml"() {
        expect:
        inventory.problems.isEmpty() ?: fail(inventory.problems)
    }

    void "every routed API operation is in the OpenAPI spec"() {
        when:
        List<ApiRoute> missing = inventory.routes.findAll { !document.operations.containsKey(it.key) }

        then:
        missing.isEmpty() ?: fail(missing.collect { "Undocumented route: ${it.key} -> ${it.target}" },
                'Run ./gradlew apiDocsGenerate to add stubs for them, then document them.')
    }

    void "every operation in the OpenAPI spec is routed"() {
        when:
        Set<String> routed = inventory.routes*.key as Set
        List<String> ghosts = document.operations.keySet().findAll { !(it in routed) }.sort()

        then:
        document.problems.isEmpty() ?: fail(document.problems)
        ghosts.isEmpty() ?: fail(ghosts.collect { "Spec operation ${it} (${document.files[it]}) does not route anywhere" })
    }

    void "every spec operation names the controller action it is routed to"() {
        when:
        List<String> mismatches = inventory.routes.findResults { ApiRoute r ->
            Map op = document.operations[r.key]
            String documented = op ? "${op['x-openboxes-controller']}/${op['x-openboxes-action']}" : null
            op && documented != r.target ? "${r.key}: spec says ${documented}, routing says ${r.target}".toString() : null
        }

        then:
        mismatches.isEmpty() ?: fail(mismatches)
    }

    void "operation ids are unique"() {
        when:
        Map<Object, List<String>> byId = document.operations.keySet().groupBy { document.operations[it].operationId }

        then:
        byId.findAll { it.key == null || it.value.size() > 1 }.isEmpty() ?:
                fail(byId.findAll { it.key == null || it.value.size() > 1 }.collect { "operationId ${it.key}: ${it.value}" })
    }

    private static boolean fail(List messages, String hint = null) {
        throw new AssertionError("${messages.size()} problem(s):\n  " + messages.join('\n  ') + (hint ? "\n${hint}" : ''))
    }
}
