package org.pih.warehouse.core.identification

import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RandomIdentifierGenerator {

    @Value('${openboxes.identifier.numeric}')
    String numericValues

    @Value('${openboxes.identifier.alphabetic}')
    String alphabeticValues

    @Value('${openboxes.identifier.alphanumeric}')
    String alphanumericValues

    /**
     * Generates a randomized id based on the given format.
     *
     * The exact output depends on how numeric and alphabetic is defined in the configuration, but
     * a typical config will output something like "ATG-583" when given a format "LLL-NNN"
     */
    String generate(String format) {
        if (StringUtils.isBlank(format)) {
            return null
        }

        String identifier = ""
        for (int i = 0; i < format.length(); i++) {
            switch (format[i]) {
                case 'N':  // numeric
                case 'D':  // digit
                    identifier += RandomStringUtils.random(1, numericValues)
                    break
                case 'L':  // letter
                    identifier += RandomStringUtils.random(1, alphabeticValues)
                    break
                case 'A':  // alphanumeric
                    identifier += RandomStringUtils.random(1, alphanumericValues)
                    break
                default:
                    // For any other character, use it as provided.
                    identifier += format[i]
            }
        }

        return identifier
    }
}
