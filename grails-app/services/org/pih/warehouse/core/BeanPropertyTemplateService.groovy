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
import org.springframework.beans.BeansException
import org.springframework.beans.NullValueInNestedPathException

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
 * - ${name.property:-default} prints the default when the value is empty (null or
 *   empty text), when a link in the path is null, or when the binding was passed
 *   with a null value. ${name.property:-} prints nothing in those cases. Numbers,
 *   booleans and blank text are values. A binding that was not passed at all, and
 *   every other error below, stays an error with or without a default. The default
 *   is inserted as written (never resolved) and cannot contain '}'.
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

    // separates a property path from its default value: ${name.property:-default}
    static final String DEFAULT_VALUE_SEPARATOR = ':-'

    /*
     * Currently this set is the maximum we'd want to support for Zebra
     * templating, but it can be expanded (with care) in the future. The goal
     * is a minimal list of useful variables that can be dereferenced safely
     * and securely in the templating engine.
     */
    static final Set<String> ALLOWED_BINDING_NAMES = [
        'document', 'inventoryItem', 'location', 'product', 'facility',
        // label templates for bins and containers (see zpl-boarcodes-templates)
        'internalLocation', 'container',
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
        // hand the whole expression, default included, to the lookup: StringSubstitutor
        // applies a default only when the lookup returns null, which resolveExpression never does
        substitutor.setValueDelimiterMatcher(null)
        return substitutor.replace(templateContents)
    }

    /*
     * The default (the text after the first ':-') is returned as written. It needs no escaping:
     * StringSubstitutor ends the placeholder at the first '}', so a default never holds a
     * complete ${...} that could be resolved, and escaping its '${' would print an extra '$'.
     */
    private String resolveExpression(String expression, Map bindings) {
        int separator = expression.indexOf(DEFAULT_VALUE_SEPARATOR)
        boolean hasDefault = separator >= 0
        String path = (hasDefault ? expression.substring(0, separator) : expression).trim()
        String defaultValue = hasDefault ? expression.substring(separator + DEFAULT_VALUE_SEPARATOR.length()) : null

        String[] segments = path.split('\\.')
        String rootKey = segments[0]

        if (!bindings.containsKey(rootKey)) {
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': '${rootKey}' was not provided as a binding")
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
        if (root == null) {
            if (hasDefault) {
                return defaultValue
            }
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': binding '${rootKey}' is null")
        }

        Object result = root
        if (segments.length > 1) {
            String propertyPath = segments[1..-1].join('.')
            try {
                result = new BeanWrapperImpl(root).getPropertyValue(propertyPath)
            } catch (BeansException e) {
                // a null link in the path (product.category when reading product.category.name);
                // for an indexed link (tags[0]) Spring wraps it in an InvalidPropertyException
                boolean nullLink = e instanceof NullValueInNestedPathException ||
                    e.cause instanceof NullValueInNestedPathException
                if (hasDefault && nullLink) {
                    return defaultValue
                }
                throw e
            }
        }

        if (result != null && BLOCKED_TYPES.any { it.isAssignableFrom(result.getClass()) }) {
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': " +
                "resolved to blocked type ${result.getClass().name}")
        }

        String value = result?.toString() ?: ''
        if (hasDefault && value.isEmpty()) {
            return defaultValue
        }
        // escape ${ so StringSubstitutor won't re-interpret resolved values as expressions
        return value.replaceAll(/(?<!\$)\$\{/, '\\$\\${')
    }
}
