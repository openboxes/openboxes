package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener

import java.nio.charset.StandardCharsets

/**
 * Checks the stored label (Zebra) templates when the application starts and logs a warning
 * for each one that BeanPropertyTemplateService will refuse to render, so they can be
 * fixed before someone tries to print. Never stops the application from starting. It reads only
 * ZEBRA_TEMPLATE documents (small text files). It cannot know which values a print request will
 * provide, so a template can still fail at print time; the label actions then name it.
 */
class LabelTemplateCheckService implements ApplicationListener<ApplicationReadyEvent> {

    // ${...} not preceded by the $$ escape (StringSubstitutor syntax)
    private static final String EXPRESSION = /(?<!\$)\$\{([^}]*)\}/
    // a property path such as product.productCode or product.tags[0].name
    private static final String PROPERTY_PATH = BeanPropertyTemplateService.PROPERTY_PATH
    // StringSubstitutor's default value separator: ${name.property:-default}
    private static final String DEFAULT_VALUE_SEPARATOR = ":-"
    // the property names BeanPropertyTemplateService refuses to read
    private static final Set<String> BLOCKED_PROPERTIES = [
        'class', 'metaClass', 'properties', 'domainClass', 'constraints', 'mapping', 'errors',
    ] as Set

    @Override
    void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            List<String> warnings = checkStoredTemplates()
            warnings.each { String warning -> log.warn(warning) }
            log.info("Checked the stored label templates: ${warnings.size()} problem(s) found")
        } catch (Exception e) {
            log.warn("Could not check the stored label templates: ${e.message}", e)
        }
    }

    @Transactional(readOnly = true)
    List<String> checkStoredTemplates() {
        List<DocumentType> templateTypes = DocumentType.findAllByDocumentCode(DocumentCode.ZEBRA_TEMPLATE)
        List<Document> templates = templateTypes ? Document.findAllByDocumentTypeInList(templateTypes) : []

        return templates.collectMany { Document document ->
            String contents = document.fileContents ? new String(document.fileContents, StandardCharsets.UTF_8) : ""
            findProblems(contents).collect { String problem ->
                "Label template '${document.name}' (${document.documentNumber ?: document.id}) cannot be rendered: ${problem}. " +
                        "Update the template and upload it again.".toString()
            }
        } as List<String>
    }

    /**
     * The reasons BeanPropertyTemplateService would refuse this template, independent of the
     * model it is rendered with (an absent value can still fail at print time).
     */
    List<String> findProblems(String contents) {
        List<String> problems = []
        if (contents == null) {
            problems << "it has no file contents"
            return problems
        }
        if (contents =~ /<%|<g:/) {
            problems << "it uses GSP tags (<% %> or <g:...>)"
        }
        (contents =~ EXPRESSION).each { List<String> match ->
            String placeholder = match[0]
            String expression = match[1]
            int defaultValue = expression.indexOf(DEFAULT_VALUE_SEPARATOR)
            expression = (defaultValue >= 0 ? expression.substring(0, defaultValue) : expression).trim()
            if (expression.contains("?.")) {
                problems << "${placeholder} uses ?. (write \${a.b} instead)".toString()
            } else if (!(expression ==~ PROPERTY_PATH)) {
                problems << "${placeholder} is an expression; only \${name.property} paths are supported".toString()
            } else {
                List<String> names = expression.tokenize(".").collect { it.replaceFirst(/\[.*/, "") }
                String root = names[0]
                String blocked = names.find { BLOCKED_PROPERTIES.contains(it) }
                if (blocked) {
                    problems << "${placeholder} reads '${blocked}', which label templates cannot use".toString()
                } else if (!BeanPropertyTemplateService.ALLOWED_BINDING_NAMES.contains(root)) {
                    problems << ("${placeholder} reads '${root}', which label templates cannot use " +
                            "(allowed: ${BeanPropertyTemplateService.ALLOWED_BINDING_NAMES.sort().join(', ')})").toString()
                }
            }
        }
        return problems.unique()
    }
}
