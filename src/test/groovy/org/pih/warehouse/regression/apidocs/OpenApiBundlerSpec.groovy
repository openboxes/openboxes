package org.pih.warehouse.regression.apidocs

import spock.lang.Specification

class OpenApiBundlerSpec extends Specification {

    static final Map<String, String> FILES = [
            'openapi.yaml'           : '''
openapi: 3.0.3
info: {title: T, version: v}
paths:
  /api/things:
    $ref: 'paths/things.yaml#/~1api~1things'
  /api/inline:
    get:
      responses:
        '200': {$ref: '#/components/responses/Ok'}
components:
  schemas:
    Thing: {$ref: 'components/schemas.yaml#/Thing'}
    Part: {$ref: 'components/schemas.yaml#/Part'}
  responses:
    Ok: {$ref: 'components/responses.yaml#/Ok'}
''',
            'paths/things.yaml'      : '''
/api/things:
  get:
    responses:
      '200': {$ref: '../components/responses.yaml#/Ok'}
      '404': {$ref: '../components/responses.yaml#/Missing'}
  post:
    requestBody:
      content:
        application/json:
          schema: {$ref: '../components/schemas.yaml#/Thing'}
''',
            'components/schemas.yaml': '''
Thing:
  type: object
  properties:
    part: {$ref: '#/Part'}
    unshared: {$ref: '#/Unshared'}
Part: {type: string}
Unshared: {type: integer}
''',
            'components/responses.yaml': '''
Ok: {description: OK}
Missing: {description: Not found}
''',
    ]

    OpenApiBundler bundler = new OpenApiBundler({ String path -> FILES[path] })

    void 'path items are inlined from their files'() {
        when:
        Map spec = bundler.bundle()

        then:
        spec.paths['/api/things'].keySet() == ['get', 'post'] as Set
        spec.paths['/api/inline'].get.responses['200'] == ['$ref': '#/components/responses/Ok']
        bundler.problems.empty
    }

    void 'references to shared components become local references'() {
        when:
        Map spec = bundler.bundle()

        then:
        spec.paths['/api/things'].get.responses['200'] == ['$ref': '#/components/responses/Ok']
        spec.paths['/api/things'].post.requestBody.content['application/json'].schema == ['$ref': '#/components/schemas/Thing']
    }

    void 'shared components hold their content, with their own references resolved'() {
        when:
        Map spec = bundler.bundle()

        then:
        spec.components.responses.Ok == [description: 'OK']
        spec.components.schemas.Part == [type: 'string']
        spec.components.schemas.Thing.properties.part == ['$ref': '#/components/schemas/Part']
        // Not a shared component: inlined where it is used.
        spec.components.schemas.Thing.properties.unshared == [type: 'integer']
    }

    void 'references to anything else are inlined'() {
        when:
        Map spec = bundler.bundle()

        then:
        spec.paths['/api/things'].get.responses['404'] == [description: 'Not found']
    }

    void 'a reference that does not resolve is reported'() {
        given:
        Map<String, String> files = new HashMap(FILES)
        files['paths/things.yaml'] = files['paths/things.yaml'].replace('#/Missing', '#/Gone')
        OpenApiBundler broken = new OpenApiBundler({ String path -> files[path] })

        when:
        broken.bundle()

        then:
        broken.problems == ['components/responses.yaml#/Gone does not resolve']
    }

    void 'components defined inline in openapi.yaml stay shared'() {
        given:
        OpenApiBundler inline = new OpenApiBundler({ String path ->
            [
                    'openapi.yaml'    : '''
paths:
  /a: {$ref: 'paths/a.yaml#/~1a'}
components:
  schemas:
    Inline: {type: object}
''',
                    'paths/a.yaml': '''
/a:
  get:
    responses:
      '200':
        content:
          application/json:
            schema: {$ref: '../openapi.yaml#/components/schemas/Inline'}
''',
            ][path]
        })

        when:
        Map spec = inline.bundle()

        then:
        inline.problems.empty
        spec.paths['/a'].get.responses['200'].content['application/json'].schema == ['$ref': '#/components/schemas/Inline']
        spec.components.schemas.Inline == [type: 'object']
    }

    void 'recursive definitions become components instead of being cut off'() {
        given:
        OpenApiBundler recursive = new OpenApiBundler({ String path ->
            [
                    'openapi.yaml'           : '''
paths:
  /tree: {$ref: 'paths/tree.yaml#/~1tree'}
components:
  schemas:
    Shared: {$ref: 'components/schemas.yaml#/Shared'}
''',
                    'paths/tree.yaml'        : '''
/tree:
  get:
    responses:
      '200':
        content:
          application/json:
            schema: {$ref: '../components/schemas.yaml#/Node'}
      '201':
        content:
          application/json:
            schema: {$ref: '../components/schemas.yaml#/Ping'}
''',
                    'components/schemas.yaml': '''
Shared:
  properties:
    self: {$ref: '#/Shared'}
Node:
  properties:
    children: {type: array, items: {$ref: '#/Node'}}
Ping:
  properties:
    pong: {$ref: '#/Pong'}
Pong:
  properties:
    ping: {$ref: '#/Ping'}
''',
            ][path]
        })

        when:
        Map spec = recursive.bundle()

        then:
        recursive.problems.empty
        // a shared component refers to itself locally
        spec.components.schemas.Shared.properties.self == ['$ref': '#/components/schemas/Shared']
        // self-recursive: the first level is inlined, the recursion points at a new component
        spec.paths['/tree'].get.responses['200'].content['application/json'].schema.properties.children.items ==
                ['$ref': '#/components/schemas/Node']
        spec.components.schemas.Node.properties.children.items == ['$ref': '#/components/schemas/Node']
        // mutually recursive
        spec.components.schemas.Ping.properties.pong.properties.ping == ['$ref': '#/components/schemas/Ping']
    }

    void 'a hoisted recursive schema never takes the name of an existing component'() {
        given:
        OpenApiBundler colliding = new OpenApiBundler({ String path ->
            [
                    'openapi.yaml'           : '''
paths:
  /n:
    get:
      responses:
        '200':
          content:
            application/json:
              schema: {$ref: 'other.yaml#/Node'}
components:
  schemas:
    Node: {type: string}
''',
                    'other.yaml': '''
Node:
  properties:
    next: {$ref: '#/Node'}
''',
            ][path]
        })

        when:
        Map spec = colliding.bundle()

        then:
        colliding.problems.empty
        spec.components.schemas.Node == [type: 'string']
        spec.components.schemas.Node_2.properties.next == ['$ref': '#/components/schemas/Node_2']
    }

    void 'recursion outside a schema is reported, not written as a schema'() {
        given:
        OpenApiBundler looping = new OpenApiBundler({ String path ->
            path == 'openapi.yaml' ? '''
paths:
  /loop: {$ref: '#/x-items/loop'}
x-items:
  loop:
    get:
      callbacks:
        again: {$ref: '#/x-items/loop'}
''' : null
        })

        when:
        looping.bundle()

        then:
        looping.problems == ['openapi.yaml#/x-items/loop refers to itself outside a schema, which one document cannot express']
    }

    void 'a recursive schema used outside a schema as well is reported'() {
        given:
        OpenApiBundler mixed = new OpenApiBundler({ String path ->
            [
                    'openapi.yaml': '''
paths:
  /n:
    get:
      responses:
        '200':
          content:
            application/json:
              schema: {$ref: 'other.yaml#/Node'}
        '201': {$ref: 'other.yaml#/Node'}
''',
                    'other.yaml'  : '''
Node:
  properties:
    next: {$ref: '#/Node'}
''',
            ][path]
        })

        when:
        mixed.bundle()

        then:
        mixed.problems == ['other.yaml#/Node is a recursive schema but is also used outside a schema']
    }

    void 'percent-encoded and plain spellings of a reference are the same target'() {
        expect:
        OpenApiBundler.canonical('a.yaml', 'b.yaml#/with%20space') == OpenApiBundler.canonical('a.yaml', 'b.yaml#/with space')
        OpenApiBundler.percentDecode('a%2Bb+c%C3%A9') == 'a+b+c\u00e9'
        OpenApiBundler.percentDecode('100%') == '100%'
    }

    void 'references follow JSON pointers into lists and through escaped and percent-encoded keys'() {
        given:
        OpenApiBundler pointers = new OpenApiBundler({ String path ->
            [
                    'openapi.yaml': '''
paths:
  /p:
    get:
      parameters:
      - {$ref: 'shared.yaml#/lists/params/1'}
      - {$ref: 'shared.yaml#/odd~1key~0name'}
      - {$ref: 'shared.yaml#/with%20space'}
''',
                    'shared.yaml' : '''
lists:
  params:
  - {name: first, in: query}
  - {name: second, in: query}
odd/key~name: {name: escaped, in: query}
with space: {name: spaced, in: query}
''',
            ][path]
        })

        when:
        Map spec = pointers.bundle()

        then:
        pointers.problems.empty
        spec.paths['/p'].get.parameters*.name == ['second', 'escaped', 'spaced']
    }

    void 'dates in the spec keep the text they are written as'() {
        given:
        OpenApiBundler dates = new OpenApiBundler({ String path ->
            path == 'openapi.yaml' ? 'paths:\n  /d:\n    get:\n      x-example: 2024-01-31\n      x-when: 2024-01-31T10:15:00Z\n' : null
        })

        when:
        Map spec = dates.bundle()

        then:
        spec.paths['/d'].get['x-example'] == '2024-01-31'
        spec.paths['/d'].get['x-when'] == '2024-01-31T10:15:00Z'
    }

    void 'canonical resolves references relative to the referring file'() {
        expect:
        OpenApiBundler.canonical(from, ref) == expected

        where:
        from                      | ref                                  || expected
        'paths/things.yaml'       | '../components/responses.yaml#/Ok'   || 'components/responses.yaml#/Ok'
        'openapi.yaml'            | 'components/schemas.yaml#/Thing'     || 'components/schemas.yaml#/Thing'
        'components/schemas.yaml' | '#/Part'                             || 'components/schemas.yaml#/Part'
        'paths/a/b.yaml'          | '../../x.yaml'                       || 'x.yaml'
    }

    void 'the application spec bundles into one document with only local references'() {
        given:
        OpenApiBundler application = new OpenApiBundler({ String path ->
            File f = new File(OpenApiDocument.DEFAULT_DIR, path)
            f.exists() ? f.getText('UTF-8') : null
        })
        OpenApiDocument source = new OpenApiDocument().load()

        when:
        Map spec = application.bundle()
        List<String> refs = []
        collectRefs(spec, refs)

        then:
        application.problems.empty
        spec.paths.size() == source.root.paths.size()
        spec.paths.collect { path, item -> item.keySet().findAll { it in OpenApiDocument.HTTP_METHODS }.size() }.sum() ==
                source.operations.size()
        refs.every { it.startsWith('#/components/') }
    }

    private static void collectRefs(Object node, List<String> refs) {
        if (node instanceof Map) {
            if (node.'$ref') {
                refs << node.'$ref'
            }
            node.values().each { collectRefs(it, refs) }
        } else if (node instanceof List) {
            node.each { collectRefs(it, refs) }
        }
    }
}
