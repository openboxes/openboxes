package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.text.StrSubstitutor
import org.grails.datastore.gorm.GormEntity

import org.pih.warehouse.core.identification.IdentifierGeneratorContext
import org.pih.warehouse.core.identification.RandomCondition
import org.pih.warehouse.core.identification.RandomIdentifierGenerator
import org.pih.warehouse.data.DataService

/**
 * Handles generating custom identifiers that conform to a certain format.
 */
@Transactional
abstract class IdentifierService<T extends GormEntity> {

    RandomIdentifierGenerator randomIdentifierGenerator
    DataService dataService
    ConfigService configService

    /**
     * Returns the number of entities (likely via database query) that are already using the given id.
     * Subclasses are left to decide the details of what uniqueness actually means here.
     */
    abstract protected Integer countByIdentifier(String id)

    /**
     * @return the domain-specific name/keyword that is used in identifier properties.
     * Ex: The "product" of "openboxes.identifier.product.format"
     */
    abstract protected String getIdentifierName()

    /**
     * Generates a new identifier for the entity using the given configuration.
     *
     * The format is defined under "openboxes.identifier.x.format"
     *
     * There are multiple reserved keyword options that can be used when defining the format of an identifier:
     * - ${delimiter} filled in with the value defined in "openboxes.identifier.x.delimiter"
     * - ${sequenceNumber} filled in with a sequential value as defined in "openboxes.identifier.x.sequenceNumber"
     * - ${random} filled in with a random string as defined in "openboxes.identifier.x.random.template"
     * - any other keywords are populated from the template map and entities in IdentifierGeneratorContext
     */
    String generate(T entity, IdentifierGeneratorContext context=null) {
        // Fetch and fill the constant/non-random values of the format/template.
        String format = getPopulatedFormat(entity, context)

        // If there isn't any randomness in the format, we're done, so use the identifier as is.
        boolean hasRandom = format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM_EMBEDDED)
        if (!hasRandom) {
            return isIdentifierValidAndUnique(format) ? format : null
        }

        // Otherwise there is randomness. If the randomness should always be used, generate it right away.
        RandomCondition randomCondition = getIdentifierPropertyWithDefault('random.condition', RandomCondition)
        if (randomCondition == RandomCondition.ALWAYS) {
            return generateAndFillRandomInFormat(format, context?.randomTemplateOverride)
        }

        // Otherwise there's no mandatory randomness. Before adding any, first check if the identifier is unique as is.
        String formatNoRandom = StrSubstitutor.replace(format, [(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM): ""])
        if (isIdentifierValidAndUnique(formatNoRandom)) {
            return formatNoRandom
        }

        // If the identifier is not unique, add in the randomness to make it unique.
        return generateAndFillRandomInFormat(format, context?.randomTemplateOverride)
    }

    /**
     * Generate randomness as configured in the .random property and add it to the template (in the ${random} GString).
     * Generate the randomness in a loop to allow for retries in case of duplicates.
     */
    private String generateAndFillRandomInFormat(String format, String randomTemplateOverride=null) {
        // Use the randomness override if it is provided, otherwise fetch the template.
        String randomFormat = StringUtils.isBlank(randomTemplateOverride) ?
                getIdentifierPropertyWithDefault('random.template') :
                randomTemplateOverride

        int maxAttempts = configService.getProperty("openboxes.identifier.attempts.max", Integer)
        for (int i=0; i<maxAttempts; i++) {
            String randomIdentifier = randomIdentifierGenerator.generate(randomFormat)
            String finalFormat = StrSubstitutor.replace(format, [(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM): randomIdentifier])

            if (isIdentifierValidAndUnique(finalFormat)) {
                return finalFormat
            }
        }

        // We failed to generate a unique id. Let the caller decide if this is error worthy.
        return null
    }

    private boolean isIdentifierValidAndUnique(String format) {
        // If there are unfilled template fields (ie any embedded "${}" GStrings), then something went wrong.
        if (format.contains("\${")) {
            throw new IllegalArgumentException("Failed generating identifier. Check your configuration! Format was not completely filled in:  ${format}")
        }

        return !idAlreadyExists(format)
    }

    /**
     * Fetch the identifier format/template and fill it in with values.
     *
     * Ex: Given a format/template like "PO${delimiter}${custom.code}", you might get: "PO-123"
     */
    private String getPopulatedFormat(T entity, IdentifierGeneratorContext context) {
        String format = getInitialFormat(context)

        // Gradually build a map of values that will populate the format template
        Map values = new HashMap()
        values.putAll(getEntityValues(entity))
        values.putAll(getCustomValues(context))
        values.putAll(getDelimiterValues())

        format = StrSubstitutor.replace(format, values)
        return sanitizeFormat(format)
    }

    private String getInitialFormat(IdentifierGeneratorContext context) {
        // If a custom format override was provided, use that, otherwise fetch the format from properties.
        String format = StringUtils.isNotBlank(context?.formatOverride) ? context.formatOverride :
                getIdentifierPropertyWithDefault('format')

        // Also check for any custom prefix/suffix as a part of the "initial" format because they are overrides that
        // directly modify the format (instead of filling it in).
        if (getIdentifierPropertyWithDefault("prefix.enabled", Boolean)) {
            String delimiter = getIdentifierPropertyWithDefault(Constants.IDENTIFIER_FORMAT_KEYWORD_DELIMITER)
            if (StringUtils.isNotBlank(context?.prefix)) {
                format = "${context.prefix}${delimiter}${format}"
            }
            if (StringUtils.isNotBlank(context?.suffix)) {
                format = "${format}${delimiter}${context.suffix}"
            }
        }

        return format
    }

    /**
     * Takes the entity that we're generating the identifier for and builds a map of its fields to include in
     * the template as defined in the "openboxes.identifier.<entity>.properties" property.
     */
    private Map getEntityValues(T entity) {
        if (!entity) {
            return Collections.emptyMap()
        }

        Map properties = configService.getProperty("openboxes.identifier.${identifierName}.properties", Map)
        return properties ? dataService.transformObject(entity, properties) : Collections.emptyMap()
    }

    private Map getCustomValues(IdentifierGeneratorContext context) {
        if (!context?.customProperties) {
            return Collections.emptyMap()
        }

        Map values = new HashMap()
        for (Map.Entry entry in context.customProperties) {
            // TODO: For now we're passing sequenceNumber in as a custom field, so don't add the "custom." prefix to it
            //       to make sure that still matches with configured properties. Once we fully integrate sequence number
            //       into this service (and stop treating it as a custom field), this "if" check can be removed.
            if (entry.key == Constants.IDENTIFIER_FORMAT_KEYWORD_SEQUENCE_NUMBER) {
                values[entry.key] = entry.value
            }
            else {
                // Add the "custom." prefix to each custom property. We do this to keep them visually distinct from
                // non-custom properties (which don't use a prefix). This hopefully helps prevent misconfiguration.
                values["custom.${entry.key}"] = entry.value
            }
        }
        return values
    }

    private Map getDelimiterValues() {
        String delimiter = getIdentifierPropertyWithDefault(Constants.IDENTIFIER_FORMAT_KEYWORD_DELIMITER)
        return delimiter ? [(Constants.IDENTIFIER_FORMAT_KEYWORD_DELIMITER): delimiter] : Collections.emptyMap()
    }

    protected <Clazz> Clazz getIdentifierPropertyWithDefault(String propertyName, Class<Clazz> type=String) {
        // If there's a custom property defined for the entity, use that. Ex: 'openboxes.identifier.product.format'
        Clazz property = configService.getProperty("openboxes.identifier.${identifierName}.${propertyName}", type)

        // Otherwise use the default/fallback option. Ex: 'openboxes.identifier.default.format'
        return property ?: configService.getProperty("openboxes.identifier.default.${propertyName}", type)

    }

    /**
     * Any cleanup work on the identifier format that is required in advance of using it.
     */
    private String sanitizeFormat(String format) {
        StringBuilder formatBuilder = new StringBuilder()

        // Clean up any occurrences of dangling or doubled up delimiters.
        // Ex: If the delimiter character is "-", then "-x--y-" becomes "x-y"
        String delimiter = getIdentifierPropertyWithDefault(Constants.IDENTIFIER_FORMAT_KEYWORD_DELIMITER)
        Iterator<String> formatIterator = StringUtils.splitByWholeSeparator(format, delimiter).iterator()
        while (formatIterator.hasNext()) {
            String current = formatIterator.next()

            // This can happen if there are empty spaces at the ends of the format.
            if (StringUtils.isBlank(current)) {
                continue
            }

            // Add the delimiter back in before the current element unless this is the first element.
            if (!formatBuilder.isAllWhitespace()) {
                formatBuilder.append(delimiter)
            }
            formatBuilder.append(current)
        }

        return formatBuilder.toString()
    }

    private boolean idAlreadyExists(String id) {
        Integer count = countByIdentifier(id)
        return count ? (count > 0) : false
    }
}
