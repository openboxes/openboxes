package org.pih.warehouse.core

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.WordUtils
import org.springframework.stereotype.Component

/**
 * A utility component for abbreviating a String.
 */
@Component
class StringAbbreviator {

    /**
     * Abbreviates the given String.
     *
     * For example:
     *     Given "Big Bad Guys"                -> "BBG"
     *     Given "Big Bad Guys" and maxSize=2  -> "BB"
     *     Given "Biggie" and maxSize=3        -> "BIG"
     *     Given "My Org, Inc" and splitOn="," -> "MO"
     *
     * @param toAbbreviate The String to abbreviate
     * @param minSize The minimum size of the abbreviated String
     * @param maxSize The maximum size of the abbreviated String
     * @param ignoreAfter Splits the input string by this value, ignoring all text after it
     */
    String abbreviate(String toAbbreviate, Integer minSize=null, Integer maxSize=null, String ignoreAfter=null) {
        if (StringUtils.isBlank(toAbbreviate)) {
            return null
        }

        String toAbbreviateSanitized = toAbbreviate

        if (ignoreAfter != null) {
            toAbbreviateSanitized = toAbbreviateSanitized.split(ignoreAfter)[0]
        }

        // TODO: This is unfriendly to non-english locales. Can we opt to filter out just some special characters?
        toAbbreviateSanitized = toAbbreviateSanitized.replaceAll("[^a-zA-Z0-9 ]", "").toUpperCase()

        // Now that we've sanitized the String, check if we've filtered the whole thing out.
        if (StringUtils.isBlank(toAbbreviateSanitized)) {
            return null
        }

        String initials = WordUtils.initials(toAbbreviateSanitized)

        // If the initials are too short to use, simply trim the original String until it fits.
        if (initials.length() < minSize) {
            // TODO: If this is still too short, consider either erroring or padding with a special character.
            //       Ex: if minLength == 5 and you have "HI" -> "HI000"
            return StringUtils.substring(StringUtils.deleteWhitespace(toAbbreviateSanitized), 0, maxSize)
        }

        // If the initials of the given name are too long, trim them until they fit.
        if (initials.length() > maxSize) {
            return StringUtils.substring(initials, 0, maxSize)
        }

        // Otherwise the initials fit, so use them.
        return initials
    }
}
