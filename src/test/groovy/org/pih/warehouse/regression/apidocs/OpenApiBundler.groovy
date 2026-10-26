package org.pih.warehouse.regression.apidocs

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.nodes.Tag

/**
 * Turns the multi-file OpenAPI spec (openapi.yaml whose paths and components $ref other files) into one document:
 * path items are inlined, and references to shared components become local "#/components/..." references.
 *
 * Swagger UI and API clients handle one self-contained document far better than dozens of cross-file references,
 * while the multi-file layout keeps the source reviewable.
 *
 * Anything that cannot be bundled faithfully (a reference that does not resolve) is recorded in {@link #problems};
 * callers must not serve a document that has problems.
 */
class OpenApiBundler {

    /** Reads a spec file by its path relative to the spec root, or returns null when it does not exist. */
    private final Closure<String> readFile

    private final Map<String, Object> documents = [:]

    /** "file#/pointer" of each shared component -> its local reference, e.g. "#/components/responses/StubSuccess". */
    private final Map<String, String> localRefs = [:]

    /** Recursive definitions found while inlining: "file#/pointer" -> name under components/schemas. */
    private final Map<String, String> hoisted = [:]

    /** Names already used under components/schemas in openapi.yaml (hoisted names must not collide with them). */
    private final Set<String> reservedSchemaNames = [] as Set

    final List<String> problems = []

    OpenApiBundler(Closure<String> readFile) {
        this.readFile = readFile
    }

    Map bundle(String rootFile = 'openapi.yaml') {
        Map root = (Map) document(rootFile)
        if (root == null) {
            throw new IllegalArgumentException("${rootFile} not found")
        }
        Map result = new LinkedHashMap(root)

        // Every shared component is a target for local references: under its own name in openapi.yaml, and under
        // the file location it is defined in when openapi.yaml only refers to it.
        Map components = (Map) root.components ?: [:]
        components.each { String section, Object entries ->
            if (entries instanceof Map) {
                ((Map) entries).each { String name, Object value ->
                    String local = "#/components/${section}/${pointerToken(name)}".toString()
                    localRefs[canonical(rootFile, local)] = local
                    if (value instanceof Map && ((Map) value).'$ref' instanceof String) {
                        localRefs[canonical(rootFile, (String) ((Map) value).'$ref')] = local
                    }
                }
            }
        }
        reservedSchemaNames.addAll(((Map) components.schemas ?: [:]).keySet().collect { it as String })
        Map bundledComponents = components.collectEntries { String section, Object entries ->
            if (!(entries instanceof Map)) {
                return [(section): entries]
            }
            [(section): ((Map) entries).collectEntries { String name, Object value ->
                [(name): inlineDefinition(rootFile, value, section == 'schemas')]
            }]
        }

        // A path item may live in another file: its own references are relative to that file.
        result.paths = ((Map) root.paths ?: [:]).collectEntries { String path, Object item ->
            [(path): inlineDefinition(rootFile, item)]
        }

        // Recursive definitions become components of their own (a recursive structure cannot be inlined).
        if (hoisted) {
            Map schemas = new LinkedHashMap((Map) bundledComponents.schemas ?: [:])
            List<String> done = []
            while (hoisted.keySet().any { !(it in done) }) {
                String target = hoisted.keySet().find { !(it in done) }
                done << target
                schemas[hoisted[target]] = resolveRefs(fileOf(target), follow(target), [target] as Set, true)
            }
            bundledComponents.schemas = schemas
        }
        if (bundledComponents) {
            result.components = bundledComponents
        }
        return result
    }

    /** A node defined by reference (a component or a path item) is replaced by its content, references resolved. */
    private Object inlineDefinition(String file, Object value, boolean schema = false) {
        if (value instanceof Map && ((Map) value).'$ref' instanceof String) {
            String target = canonical(file, (String) ((Map) value).'$ref')
            return resolveRefs(fileOf(target), follow(target), [target] as Set, schema)
        }
        return resolveRefs(file, value, [] as Set, schema)
    }

    // Keys under which a value (or, for the list/map ones, each member) is a Schema Object.
    private static final Set<String> SCHEMA_KEYS = ['schema', 'items', 'additionalProperties', 'not'] as Set
    private static final Set<String> SCHEMA_MEMBER_KEYS = ['properties', 'allOf', 'oneOf', 'anyOf'] as Set

    /**
     * Copies a node, turning every $ref into a local reference (shared component) or the referenced content.
     * `schema` tells whether the node is a Schema Object (or inside one): only schemas may recurse, through a new
     * component under components/schemas; any other recursion cannot be written as one document and is a problem.
     */
    private Object resolveRefs(String file, Object node, Set<String> inlining = [] as Set, boolean schema = false) {
        if (node instanceof Map) {
            Map map = (Map) node
            if (map.'$ref' instanceof String) {
                String target = canonical(file, (String) map.'$ref')
                if (localRefs[target]) {
                    return ['$ref': localRefs[target]]
                }
                if (hoisted[target]) {
                    if (!schema) {
                        problems << "${target} is a recursive schema but is also used outside a schema".toString()
                        return [:]
                    }
                    return ['$ref': "#/components/schemas/${pointerToken(hoisted[target])}".toString()]
                }
                if (inlining.contains(target)) {
                    if (!schema) {
                        problems << "${target} refers to itself outside a schema, which one document cannot express".toString()
                        return [:]
                    }
                    hoisted[target] = hoistedName(target)
                    return ['$ref': "#/components/schemas/${pointerToken(hoisted[target])}".toString()]
                }
                Object content = follow(target)
                return content == null ? [:] : resolveRefs(fileOf(target), content, inlining + target, schema)
            }
            return map.collectEntries { key, value ->
                boolean childSchema = schema || key in SCHEMA_KEYS || key in SCHEMA_MEMBER_KEYS
                [(key): resolveRefs(file, value, inlining, childSchema)]
            }
        }
        if (node instanceof List) {
            return ((List) node).collect { resolveRefs(file, it, inlining, schema) }
        }
        return node
    }

    /** A components/schemas name for a hoisted recursive definition: its last pointer token, made unique. */
    private String hoistedName(String target) {
        String pointer = target.contains('#') ? target.substring(target.indexOf('#') + 1) : ''
        String base = pointer ? decodeToken(pointer.substring(pointer.lastIndexOf('/') + 1)) : fileOf(target)
        base = base.replaceAll(/[^A-Za-z0-9._-]/, '_') ?: 'Recursive'
        String name = base
        int suffix = 2
        while (hoisted.values().contains(name) || reservedSchemaNames.contains(name)) {
            name = "${base}_${suffix++}".toString()
        }
        return name
    }

    /** The node a canonical reference points to (RFC 6901 JSON pointer), or null (recorded as a problem). */
    private Object follow(String target) {
        Object node = document(fileOf(target))
        String pointer = target.contains('#') ? target.substring(target.indexOf('#') + 1) : ''
        if (node != null && pointer) {
            if (!pointer.startsWith('/')) {
                node = null
            } else {
                for (String token : pointer.substring(1).split('/', -1)) {
                    String key = decodeToken(token)
                    if (node instanceof Map) {
                        node = ((Map) node)[key]
                    } else if (node instanceof List && key ==~ /0|[1-9][0-9]*/ && (key as int) < ((List) node).size()) {
                        node = ((List) node)[key as int]
                    } else {
                        node = null
                    }
                }
            }
        }
        if (node == null) {
            problems << "${target} does not resolve"
        }
        return node
    }

    /** One JSON pointer token (already percent-decoded, see canonical): ~1 -> / and ~0 -> ~. */
    static String decodeToken(String token) {
        return token.replace('~1', '/').replace('~0', '~')
    }

    /** Percent-decodes a URI fragment (RFC 3986: %XX octets as UTF-8; '+' is a plain character). */
    static String percentDecode(String text) {
        if (!text.contains('%')) {
            return text
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        int i = 0
        while (i < text.length()) {
            char c = text.charAt(i)
            if (c == ((char) "%") && i + 2 < text.length() && text.substring(i + 1, i + 3) ==~ /[0-9A-Fa-f]{2}/) {
                bytes.write(Integer.parseInt(text.substring(i + 1, i + 3), 16))
                i += 3
            } else {
                int codePoint = text.codePointAt(i)
                bytes.write(new String(Character.toChars(codePoint)).getBytes('UTF-8'))
                i += Character.charCount(codePoint)
            }
        }
        return new String(bytes.toByteArray(), 'UTF-8')
    }

    private static String pointerToken(String name) {
        return name.replace('~', '~0').replace('/', '~1')
    }

    /**
     * "file#/pointer" with the file path relative to the spec root ("#/x" refers to the file itself) and the
     * pointer in one spelling (percent-decoded), so equal targets always compare equal.
     */
    static String canonical(String fromFile, String ref) {
        String filePart = ref.contains('#') ? ref.substring(0, ref.indexOf('#')) : ref
        String pointer = ref.contains('#') ? percentDecode(ref.substring(ref.indexOf('#'))) : ''
        String file = filePart ? normalize(parentOf(fromFile) + filePart) : fromFile
        return file + pointer
    }

    private static String fileOf(String canonicalRef) {
        return canonicalRef.contains('#') ? canonicalRef.substring(0, canonicalRef.indexOf('#')) : canonicalRef
    }

    private static String parentOf(String file) {
        return file.contains('/') ? file.substring(0, file.lastIndexOf('/') + 1) : ''
    }

    private static String normalize(String path) {
        List<String> parts = []
        path.split('/').each { String part ->
            if (part == '..') {
                if (parts) {
                    parts.remove(parts.size() - 1)
                }
            } else if (part && part != '.') {
                parts << part
            }
        }
        return parts.join('/')
    }

    private Object document(String file) {
        if (!documents.containsKey(file)) {
            String text = readFile(file)
            documents[file] = text == null ? null : new Yaml(new TextTimestamps()).load(text)
        }
        return documents[file]
    }

    /**
     * Loads YAML timestamps (e.g. an unquoted "example: 2024-01-31") as the text they are written as, so they reach
     * the JSON document unchanged instead of as a converted date-time.
     */
    static class TextTimestamps extends SafeConstructor {
        TextTimestamps() {
            super(new LoaderOptions())
            yamlConstructors.put(Tag.TIMESTAMP, yamlConstructors.get(Tag.STR))
        }
    }
}
