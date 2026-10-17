package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener

import java.nio.charset.StandardCharsets

/**
 * Checks the stored label (Zebra) templates when the application starts and logs a warning
 * for each one that BeanPropertyTemplateService will refuse to render, so they can be
 * fixed before someone tries to print. Never stops the application from starting.
 */
class LabelTemplateCheckService implements ApplicationListener<ApplicationReadyEvent> {

    // ${...} not preceded by the $$ escape (StringSubstitutor syntax)
    private static final String EXPRESSION = /(?<!\$)\$\{([^}]*)\}/
    // a property path such as product.productCode or product.tags[0].name
    private static final String PROPERTY_PATH = /[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*(\[[0-9]+\])?)*/

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
        if (!contents) {
            problems << "it is empty"
            return problems
        }
        if (contents =~ /<%|<g:/) {
            problems << "it uses GSP tags (<% %> or <g:...>)"
        }
        (contents =~ EXPRESSION).each { List<String> match ->
            String expression = match[1].trim()
            String placeholder = match[0]
            if (expression.contains("?.")) {
                problems << "${placeholder} uses ?. (write \${a.b} instead)".toString()
            } else if (!(expression ==~ PROPERTY_PATH)) {
                problems << "${placeholder} is an expression; only \${name.property} paths are supported".toString()
            } else {
                String root = expression.tokenize(".")[0]
                if (!BeanPropertyTemplateService.ALLOWED_BINDING_NAMES.contains(root)) {
                    problems << ("${placeholder} reads '${root}', which label templates cannot use " +
                            "(allowed: ${BeanPropertyTemplateService.ALLOWED_BINDING_NAMES.sort().join(', ')})").toString()
                }
            }
        }
        return problems.unique()
    }
}
