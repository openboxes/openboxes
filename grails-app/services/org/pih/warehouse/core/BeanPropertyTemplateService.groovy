/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookup
import org.springframework.beans.BeanWrapperImpl

import java.security.ProtectionDomain

/**
 * A template service that resolves ${expression} placeholders by walking
 * JavaBean property paths on model objects and doing nothing else.
 *
 * Unlike GroovyPagesTemplateEngine (which evaluates ${...} as arbitrary
 * Groovy code), this service only performs property access via
 * Spring's BeanWrapper. Expressions like
 * ${inventoryItem.product.productCode} work; expressions like
 * ${"cmd".execute()} or ${7*7} do not -- they throw IllegalArgumentException.
 *
 * Limitations:
 * - No method calls, arithmetic, or Groovy language features -- throws
 *   IllegalArgumentException
 * - No GSP tags (<g:if>, <g:each>, <% %>, etc.) -- throws IllegalArgumentException
 * - Bracket indexing is allowed within a property path (e.g. ${items[0].name}).
 *   Root-level indexing / map-style lookups (e.g. ${items[0]} or ${items['key']}) are rejected
 * - No Groovy safe-navigation (?.) -- throws IllegalArgumentException
 * - To include a literal ${...} in output, use $${...} (StringSubstitutor's
 *   built-in escape). There is no backslash escape.
 * - If a variable resolves to a string that itself contains ${...}, nice try!
 *   Resolved values are escaped so they won't be re-interpreted as templates.
 * - Callers declare their bindings via a Map (like Python's locals()). Only
 *   keys in ALLOWED_BINDING_NAMES are permitted; passing anything else is a
 *   caller error. Templates may only dereference the contents of bindings;
 *   anything else is a template error.
 * - The properties 'class' and 'metaClass' are blocked. Any expression that
 *   resolves to a Class, ClassLoader, ProtectionDomain, or MetaClass also
 *   throws IllegalArgumentException, regardless of the property name used to
 *   reach it. This prevents Spring4Shell-style classloader traversal.
 *
 * Use this service instead of GroovyPagesTemplateEngine when template
 * content comes from user-editable sources (e.g. uploaded documents).
 */
class BeanPropertyTemplateService {

    // properties that should never be resolved from user-provided templates
    private static final Set<String> BLOCKED_PROPERTIES = [
        'class', 'metaClass',
        'properties', 'domainClass', 'constraints', 'mapping', 'errors',
    ] as Set

    // types we should never resolve, could expose Java internals
    private static final List<Class> BLOCKED_TYPES = [
        Class, ClassLoader, ProtectionDomain,
        groovy.lang.MetaClass,
    ]

    /*
     * Currently this set is the maximum we'd want to support for Zebra
     * templating, but it can be expanded (with care) in the future. The goal
     * is a minimal list of useful variables that can be dereferenced safely
     * and securely in the templating engine.
     */
    static final Set<String> ALLOWED_BINDING_NAMES = [
        'document', 'inventoryItem', 'location', 'product', 'facility',
    ] as Set

    String renderTemplate(Document document, Map bindings) {
        if (document.fileContents == null) {
            throw new IllegalArgumentException(
                "Document '${document.name}' has no file contents to render")
        }
        String templateContents = new String(document.fileContents, java.nio.charset.StandardCharsets.UTF_8)
        renderTemplateContents(templateContents, document.name, bindings)
    }

    String renderTemplateContents(String templateContents, String pageName, Map bindings) {
        Set<String> unrecognizedBindings = bindings.keySet() - ALLOWED_BINDING_NAMES
        if (unrecognizedBindings) {
            throw new IllegalArgumentException(
                "Bindings contain keys the engine does not permit: ${unrecognizedBindings}")
        }

        // ZPL templates should not contain GSP tags. This regex matches GSP scriptlet
        // tags (<% ... %>) and Grails tag library calls (<g:each>, <g:if>, etc.).
        if (templateContents =~ /<%|<g:/) {
            throw new IllegalArgumentException(
                "Template '${pageName}' contains GSP tags (<% %> or <g:...>), " +
                "which are not supported by the property template engine. " +
                "Remove GSP tags and use \${property.path} expressions instead.")
        }

        // can't be class-level, needs to access the bindings Map to resolve expressions
        StringSubstitutor substitutor = new StringSubstitutor(new StringLookup() {
            @Override
            String lookup(String expression) {
                resolveExpression(expression, bindings)
            }
        })
        return substitutor.replace(templateContents)
    }

    private String resolveExpression(String expression, Map bindings) {
        String trimmed = expression.trim()

        String[] segments = trimmed.split('\\.')
        String rootKey = segments[0]

        if (!bindings.containsKey(rootKey)) {
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': '${rootKey}' was not provided as a binding")
        }
        if (bindings.get(rootKey) == null) {
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': binding '${rootKey}' is null")
        }

        for (String segment : segments) {
            // strip bracket suffixes so that e.g. constraints[0] is still blocked
            String propertyName = segment.replaceFirst('\\[.*', '')
            if (BLOCKED_PROPERTIES.contains(propertyName)) {
                throw new IllegalArgumentException(
                    "Template expression '\${${expression}}': " +
                    "property '${propertyName}' is not allowed")
            }
        }

        Object root = bindings.get(rootKey)
        Object result = root
        if (segments.length > 1) {
            String propertyPath = segments[1..-1].join('.')
            result = new BeanWrapperImpl(root).getPropertyValue(propertyPath)
        }

        if (result != null && BLOCKED_TYPES.any { it.isAssignableFrom(result.getClass()) }) {
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': " +
                "resolved to blocked type ${result.getClass().name}")
        }

        // escape ${ so StringSubstitutor won't re-interpret resolved values as expressions
        String value = result?.toString() ?: ''
        return value.replaceAll(/(?<!\$)\$\{/, '\\$\\${')
    }
}
