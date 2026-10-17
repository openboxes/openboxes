package org.pih.warehouse.regression.apidocs

import com.atlassian.oai.validator.OpenApiInteractionValidator
import groovy.json.JsonOutput
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.core.models.SwaggerParseResult
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.representer.Representer
import spock.lang.Specification

/**
 * The shipped spec must parse cleanly (all $refs resolvable) and load into the request validator
 * that the API integration tests use.
 */
class OpenApiLintSpec extends Specification {

    static final String SPEC = new File(OpenApiDocument.DEFAULT_DIR, 'openapi.yaml').toURI().toString()

    void "the OpenAPI spec parses without errors"() {
        when:
        ParseOptions options = new ParseOptions(resolve: true)
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(SPEC, null, options)

        then:
        result.openAPI != null
        // every path item $ref resolved to a path item with operations
        result.openAPI.paths.size() == new OpenApiDocument().load().operations.keySet().collect { it.split(' ', 2)[1] }.unique().size()
        result.openAPI.paths.values().every { it.readOperations() }
        result.messages.isEmpty() ?: { throw new AssertionError("OpenAPI parser messages:\n  " + result.messages.join('\n  ')) }()
    }

    void "no spec file repeats a key (the Swagger parser keeps the last one silently; the React gate's YAML reader refuses the file)"() {
        given:
        LoaderOptions options = new LoaderOptions()
        options.allowDuplicateKeys = false
        List<File> files = []
        (OpenApiDocument.DEFAULT_DIR as File).eachFileRecurse { if (it.name.endsWith('.yaml')) { files << it } }

        expect:
        files.size() > 10
        files.findAll { File file ->
            try {
                new Yaml(new SafeConstructor(options), new Representer(new DumperOptions()), new DumperOptions(), options).load(file.getText('UTF-8'))
                return false
            } catch (Exception e) {
                return true
            }
        }*.name == []
    }

    void "the OpenAPI spec loads into the request validator"() {
        when:
        OpenApiInteractionValidator.createForSpecificationUrl(SPEC).build()

        then:
        noExceptionThrown()
    }

    void "the bundled spec (what Swagger UI and the integration tests' validator load) parses without messages"() {
        given: 'the parser reports problems inside files reached through $refs less strictly than in one document'
        OpenApiBundler bundler = new OpenApiBundler({ String file ->
            File f = new File(OpenApiDocument.DEFAULT_DIR, file)
            f.exists() ? f.getText('UTF-8') : null
        })
        String bundled = JsonOutput.toJson(bundler.bundle())

        when:
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(bundled, null, new ParseOptions(resolve: true))

        then:
        bundler.problems.isEmpty()
        result.openAPI != null
        result.messages.isEmpty() ?: { throw new AssertionError("OpenAPI parser messages:\n  " + result.messages.join('\n  ')) }()

        when:
        OpenApiInteractionValidator.createForInlineApiSpecification(bundled).build()

        then:
        noExceptionThrown()
    }
}
