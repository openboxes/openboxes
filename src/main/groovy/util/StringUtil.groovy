/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package util

import groovy.text.SimpleTemplateEngine

import java.text.MessageFormat
import org.apache.commons.lang.StringUtils

/**
 * Utility methods for parsing/formatting strings and general string manipulation.
 */
class StringUtil {

    private static final String DEFAULT_DELIMITER = ","

    static String mask(String value) {
        return mask(value, "*")
    }

    static String mask(String value, String mask) {
        return value ? value?.replaceFirst(".*", { match -> return "".padLeft(match.length(), mask) }) : value
    }

    static String renderTemplate(template, binding) {
        def engine = new SimpleTemplateEngine()
        def content = engine.createTemplate(template).make(binding)
        return content.toString()
    }

    static String format(text, args) {
        return MessageFormat.format(text, args)
    }

    static String format(String text) {
        return text.replace("_", " ")
                   .toLowerCase()
                   .capitalize()
    }

    /**
     * Splits a given string int a list of strings separated by the given separator.
     * Ex: Given "x,y,z", returns ["x","y","z"]
     */
    static List<String> split(Object value, String delimiter=DEFAULT_DELIMITER) {
        if (!value) {
            return null
        }
        if (!value instanceof String) {
            throw new IllegalArgumentException("Expected String but got ${value.class}")
        }

        String valueString = value as String
        return StringUtils.isBlank(valueString) ? [] : valueString.split(delimiter).toList()
    }
}
