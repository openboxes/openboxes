package org.pih.warehouse.regression.apidocs

/**
 * Writes build/reports/api-docs/inventory.csv (one row per operation) and summary.md (counters and
 * how the operation count reconciles with the mapping blocks in UrlMappings.groovy), and prints the
 * listed / complete / exercised counters.
 */
class ApiInventoryReport {

    final ApiRouteInventory inventory
    final OpenApiDocument document

    ApiInventoryReport(ApiRouteInventory inventory, OpenApiDocument document) {
        this.inventory = inventory
        this.document = document
    }

    void write(File dir) {
        dir.mkdirs()
        new File(dir, 'inventory.csv').withWriter { Writer w ->
            w << 'method,path,controller,action,source,status\n'
            inventory.routes.each { ApiRoute r ->
                String status = document.operations[r.key]?.get(OpenApiDocument.STATUS) ?: 'undocumented'
                w << [r.method, r.path, r.controller, r.action, r.source.name().toLowerCase(), status].join(',') << '\n'
            }
        }
        new File(dir, 'summary.md').text = summary()
        File unaccounted = new File(dir, 'unaccounted-routes.yaml')
        unaccounted.text = inventory.unaccounted.collect {
            "- {kind: ${it.kind}, path: '${it.path}', method: '${it.method}', target: ${it.target}, reason: TODO}"
        }.join('\n') + '\n'

        println counters()
    }

    String counters() {
        List<ApiRoute> listed = inventory.routes.findAll { it.source != ApiRoute.Source.GENERIC }
        int complete = listed.count { document.operations[it.key]?.get(OpenApiDocument.STATUS) == OpenApiDocument.STATUS_COMPLETE } as int
        "API docs: ${listed.size()} operations listed, ${complete} complete, exercised not recorded yet" +
                " (+ ${inventory.counts.generic} Generic API operations, free-form)"
    }

    String summary() {
        Map<String, Integer> c = inventory.counts
        Map<ApiRoute.Source, List<ApiRoute>> bySource = inventory.routes.groupBy { it.source }
        StringBuilder sb = new StringBuilder('# API route inventory\n\n')
        sb << counters() << '\n\n'
        sb << '## Reconciliation with UrlMappings.groovy\n\n'
        sb << "| Step | Operations |\n|---|---|\n"
        sb << "| /api mapping blocks | ${c.apiMappingBlocks} blocks |\n"
        sb << "| (path, method) pairs in all /api method maps (a static count of the file gives the same) | ${c.explicitPairs + c.dynamicTemplatePairs + c.genericTemplatePairs} |\n"
        sb << "| - pairs in the dynamic `\${resource}` templates (expanded below) | -${c.dynamicTemplatePairs} |\n"
        sb << "| - pairs in the Generic API templates (reported separately) | -${c.genericTemplatePairs} |\n"
        sb << "| = (path, method) pairs in explicit method maps | ${c.explicitPairs} |\n"
        sb << "| - of those, target controller action missing (missing-action) | -${c.unresolvedMappings} |\n"
        sb << "| - of those, answered by another mapping (shadowed) | -${c.shadowedMappings} |\n"
        sb << "| + dynamic `\${resource}` routes kept | +${c.dynamicKept} |\n"
        sb << "| (dynamic shadow paths of explicitly mapped actions, dynamic-shadow) | ${c.dynamicPruned} |\n"
        sb << "| (dynamic candidates that do not resolve) | ${c.dynamicUnreachable} |\n"
        sb << "| + method-unbounded routes declared in route-methods.yaml | +${c.unboundedDeclared} |\n"
        sb << "| (method-unbounded aliases of documented actions, wildcard-alias) | ${c.unboundedAliases} |\n"
        sb << "| - routes listed twice (an explicit and a dynamic mapping reach the same action) | -${c.duplicates} |\n"
        sb << "| - routes left out of the docs (excluded) | -${c.excluded} |\n"
        sb << "| **= operations listed** | **${c.listed}** |\n"
        sb << "| Generic API (free-form, separate) | ${c.generic} |\n\n"
        sb << "Method fallthrough: a method missing from a mapping's method map is dispatched to the controller's default action " +
                "instead of returning 405; ${c.methodFallthrough} (path, method) pairs of listed paths reach an action that way. " +
                "They are not documented; each is accounted for in ignored-routes.yaml (kind method-fallthrough).\n\n"
        sb << "By source: " + ApiRoute.Source.values().collect { "${it.name().toLowerCase()} ${bySource[it]?.size() ?: 0}" }.join(', ') << '\n\n'
        sb << "By method: " + inventory.routes.countBy { it.method }.sort().collect { k, v -> "${k} ${v}" }.join(', ') << '\n\n'
        sb << '## Dynamic shadow paths (not listed)\n\n| Method | Shadow path | Explicit path | Target |\n|---|---|---|---|\n'
        inventory.pruned.each { sb << "| ${it.route.method} | `${it.route.path}` | `${it.explicit.path}` | ${it.route.target} |\n" }
        sb << '\n'
        sb << '## Accounted for in ignored-routes.yaml\n\n| Kind | Method | Path | Target | Reason |\n|---|---|---|---|---|\n'
        inventory.ignored.sort { [it.kind, it.path, it.method].join(' ') }.each {
            sb << "| ${it.kind} | ${it.method} | `${it.path}` | ${it.target} | ${it.reason} |\n"
        }
        sb << '\n## Problems\n\n'
        sb << (inventory.problems ? inventory.problems.collect { "- ${it}" }.join('\n') : 'none') << '\n'
        sb.toString()
    }
}
