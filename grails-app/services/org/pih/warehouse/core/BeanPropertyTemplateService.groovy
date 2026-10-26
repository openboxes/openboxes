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
import org.springframework.beans.BeanUtils
import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.BeansException
import org.springframework.beans.NullValueInNestedPathException
import org.springframework.core.MethodParameter
import org.springframework.core.ResolvableType

import java.beans.PropertyDescriptor
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
 *   every other error below, stays an error with or without a default. After a null
 *   link the default applies only when the rest of the path is readable on the
 *   declared property types (and none is a blocked type); otherwise the null link
 *   stays an error, as it is without a default. When the binding itself is null its
 *   properties cannot be checked. The default
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

    // a plain property path such as product.productCode or product.tags[0].name; a default
    // stands in for an empty link or binding only on such a path
    static final String PROPERTY_PATH = /[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*(\[[0-9]+\])?)*/

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
        // the default may stand in for an empty link or binding (instead of an error) only on a plain path
        boolean defaultForEmptyLink = hasDefault && path ==~ PROPERTY_PATH

        String[] segments = path.split('\\.')
        String rootKey = segments[0]

        if (!bindings.containsKey(rootKey)) {
            throw new IllegalArgumentException(
                "Template expression '\${${expression}}': '${rootKey}' was not provided as a binding")
        }
        if (bindings.get(rootKey) == null && !defaultForEmptyLink) {
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
        if (root == null) {
            // the binding was passed without a value, so the type its properties would have is unknown
            return defaultValue
        }

        Object result = root
        if (segments.length > 1) {
            String propertyPath = segments[1..-1].join('.')
            try {
                result = new BeanWrapperImpl(root).getPropertyValue(propertyPath)
            } catch (BeansException e) {
                if (defaultForEmptyLink && isNullLink(e) && isReadableBehindEmptyLink(root, segments[1..-1])) {
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

    // a null link in the path (product.category when reading product.category.name); for an
    // indexed link (tags[0]) Spring wraps it in an InvalidPropertyException
    private static boolean isNullLink(BeansException e) {
        return e instanceof NullValueInNestedPathException || e.cause instanceof NullValueInNestedPathException
    }

    /*
     * Whether the property path is readable although a link in it is null, so that a default
     * never hides a template error behind an empty link. Follows the actual values up to the
     * null link and the declared property types after it (with their generic arguments, and the
     * element type for indexed segments). Answers false when a declared type does not have the
     * property, is a blocked type or cannot be indexed, when an index is not an int, when a type
     * cannot tell (Object, a raw collection, a map), and when the check itself fails; the null
     * link then stays an error, as it is without a default. Reads each getter before the null
     * link once more, as the renderer did.
     */
    private static boolean isReadableBehindEmptyLink(Object root, List<String> segments) {
        try {
            Object current = root
            ResolvableType type = ResolvableType.forClass(root.getClass())
            for (String segment : segments) {
                String propertyName = segment.replaceFirst('\\[.*', '')
                Class owner = current != null ? current.getClass() : type.resolve()
                if (owner == null || owner == Object) {
                    return false
                }
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(owner, propertyName)
                if (descriptor?.readMethod == null) {
                    return false
                }
                // resolve the getter's type against the owner's generic arguments where they are known
                ResolvableType ownerType = type.resolve() == owner ? type : ResolvableType.forClass(owner)
                type = ResolvableType.forMethodParameter(new MethodParameter(descriptor.readMethod, -1), ownerType)
                if (segment.contains('[')) {
                    // an index the renderer can read: an int, on an array, a List or a Set
                    Integer.parseInt(segment.substring(segment.indexOf('[') + 1, segment.indexOf(']')))
                    Class indexed = type.resolve()
                    if (type.isArray()) {
                        type = type.getComponentType()
                    } else if (indexed != null && (List.isAssignableFrom(indexed) || Set.isAssignableFrom(indexed))) {
                        type = type.asCollection().getGeneric(0)
                    } else {
                        return false
                    }
                }
                Class resolved = type.resolve()
                if (resolved != null && BLOCKED_TYPES.any { it.isAssignableFrom(resolved) }) {
                    return false
                }
                // up to the null link the path holds values (the renderer followed it that far)
                if (current != null) {
                    try {
                        current = new BeanWrapperImpl(current).getPropertyValue(segment)
                    } catch (BeansException e) {
                        if (!isNullLink(e)) {
                            return false
                        }
                        current = null
                    }
                }
            }
            return true
        } catch (Exception ignored) {
            return false
        }
    }
}
