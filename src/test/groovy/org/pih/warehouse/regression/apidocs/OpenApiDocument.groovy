package org.pih.warehouse.regression.apidocs

import org.yaml.snakeyaml.Yaml

/**
 * Reads the multi-file OpenAPI spec under {@link #DEFAULT_DIR}: openapi.yaml whose paths each
 * $ref a path item in paths/<tag>.yaml. Follows those path-level refs only (operation content is
 * resolved by the OpenAPI parser in the lint test).
 */
class OpenApiDocument {

    /**
     * The spec lives in the integration-test resources (the validator loads it from the classpath there); the unit
     * specs read the files. apiDocsGenerate points regression.openapi.dir at that source directory so generated stubs
     * land in the source tree, not in the build output.
     */
    static final File DEFAULT_DIR = new File(System.getProperty('regression.openapi.dir') ?: 'src/integration-test/resources/openapi')

    static final Set<String> HTTP_METHODS = ['get', 'post', 'put', 'patch', 'delete'] as Set

    static final String STATUS = 'x-doc-status'
    static final String STATUS_STUB = 'stub'
    static final String STATUS_CURATED = 'curated'
    static final String STATUS_COMPLETE = 'complete'

    final File dir
    Map root
    /** "METHOD /path" -> operation map. */
    Map<String, Map> operations = [:]
    /** "METHOD /path" -> tag file name the operation lives in. */
    Map<String, String> files = [:]
    List<String> problems = []

    OpenApiDocument(File dir = DEFAULT_DIR) {
        this.dir = dir
    }

    OpenApiDocument load() {
        File rootFile = new File(dir, 'openapi.yaml')
        root = rootFile.exists() ? (Map) new Yaml().load(rootFile.text) : [:]
        Map<String, Map> cache = [:]
        ((Map<String, Map>) root.paths ?: [:]).each { String path, Map pathItem ->
            Map resolved = pathItem
            String file = 'openapi.yaml'
            if (pathItem?.'$ref') {
                String ref = pathItem.'$ref'
                file = ref.substring(0, ref.indexOf('#'))
                String pointer = ref.substring(ref.indexOf('#') + 1)
                Map document = cache.computeIfAbsent(file) { String f ->
                    File tagFile = new File(dir, f)
                    tagFile.exists() ? (Map) new Yaml().load(tagFile.text) : [:]
                }
                resolved = (Map) document[unescapePointer(pointer)]
                if (resolved == null) {
                    problems << "openapi.yaml: ${path} refers to ${ref}, which does not exist"
                    return
                }
            }
            resolved.each { String method, Object operation ->
                if (method in HTTP_METHODS) {
                    String key = "${method.toUpperCase()} ${path}"
                    operations[key] = (Map) operation
                    files[key] = file
                }
            }
        }
        this
    }

    int count(String status) {
        operations.values().count { it[STATUS] == status } as int
    }

    /** JSON pointer for a path item key: "/api/x/{id}" -> "/~1api~1x~1{id}". */
    static String pointerFor(String path) {
        '/' + path.replace('~', '~0').replace('/', '~1')
    }

    static String unescapePointer(String pointer) {
        pointer.substring(1).replace('~1', '/').replace('~0', '~')
    }
}
