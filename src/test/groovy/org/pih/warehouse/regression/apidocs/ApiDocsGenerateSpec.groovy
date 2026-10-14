package org.pih.warehouse.regression.apidocs

import spock.lang.IgnoreIf

/**
 * Not a test: writes stub operations into the OpenAPI spec (src/integration-test/resources/openapi)
 * for undocumented routes. Run with ./gradlew apiDocsGenerate.
 */
@IgnoreIf({ !Boolean.getBoolean('apiDocs.generate') })
class ApiDocsGenerateSpec extends ApiInventorySpecification {

    void "generate OpenAPI stubs from the route inventory"() {
        given:
        ApiRouteInventory inventory = buildInventory()
        ApiDocsStubGenerator generator = new ApiDocsStubGenerator(OpenApiDocument.DEFAULT_DIR, inventory.routes)
        generator.prune = Boolean.getBoolean('apiDocs.prune')
        println "apiDocsGenerate: ${inventory.routes.size()} operations ${inventory.counts}"
        if (inventory.problems) {
            throw new AssertionError('Fix the route inventory first, nothing was written:\n  ' + inventory.problems.join('\n  '))
        }

        when:
        generator.generate()
        generator.messages.each { println "apiDocsGenerate: ${it}" }
        generator.pruned.each { println "apiDocsGenerate (prune): ${it}" }

        then: 'nothing left to do by hand'
        generator.manualSteps.isEmpty() ?: { throw new AssertionError('Finish by hand:\n  ' + generator.manualSteps.join('\n  ')) }()
    }
}
