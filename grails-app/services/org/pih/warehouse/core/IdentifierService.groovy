package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.text.StrSubstitutor

import org.pih.warehouse.core.identification.IdentifierGeneratorParams
import org.pih.warehouse.core.identification.RandomIdentifierGenerator
import org.pih.warehouse.data.DataService

/**
 * Handles generating custom identifiers that conform to a certain format.
 */
@Transactional
abstract class IdentifierService {

    RandomIdentifierGenerator randomIdentifierGenerator
    DataService dataService
    ConfigService configService

    /**
     * Returns the number of entities (likely via database query) that are already using the given id.
     * Subclasses are left to decide the details of what uniqueness actually means here.
     */
    abstract protected Integer countDuplicates(String id)

    /**
     * @return the domain-specific key that is used in identifier properties.
     * Ex: The "product" of "openboxes.identifier.product.format"
     */
    abstract protected String getPropertyKey()

    /**
     * Generates a new identifier for the entity using the given params.
     *
     * There are multiple reserved keyword options that can be used when defining the format of an identifier:
     * - ${delimiter} filled in with the value defined in "openboxes.identifier.x.delimiter"
     * - ${sequenceNumber} filled in with a sequential value as defined in "openboxes.identifier.x.sequenceNumber"
     * - ${random} filled in with a random string as defined in "openboxes.identifier.x.random"
     * - any other keywords are populated from the template map and entities in IdentifierGeneratorParams
     */
    String generate(IdentifierGeneratorParams params=null) {
        String format = getPopulatedFormat(params)

        // Given that at this point we've populated all non-random fields of the format/template. Generate the remaining
        // randomness of the identifier in a loop to allow for retries in case of duplicates. If there's no element
        // of randomness, only try once.
        String randomFormat = configService.getProperty("openboxes.identifier.${propertyKey}.random")
        int maxAttempts = hasRandomness(format) ? configService.getProperty("openboxes.identifier.retry.max", Integer) : 1

        for (int i=0; i<maxAttempts; i++) {
            String randomIdentifier = randomIdentifierGenerator.generate(randomFormat)
            String finalFormat = StrSubstitutor.replace(format, [Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM: randomIdentifier])

            // The identifier should be fully generated at this point so if there are any unfilled template fields
            // (meaning any embedded "${}" groovy gstrings), then something went wrong.
            if (finalFormat.contains("\${")) {
                throw new IllegalArgumentException("Failed generating identifier. Check your configuration! Format was not completely filled in:  ${finalFormat}")
            }

            if (!idAlreadyExists(finalFormat)) {
                return finalFormat
            }
        }

        // We failed to generate a unique id. Let the caller decide if this is error worthy.
        return null
    }

    private String getPopulatedFormat(IdentifierGeneratorParams params) {
        String format = getInitialFormat(params)

        // Gradually build a map of values that will populate the format template
        Map values = getTemplateValues(params)
        values << getDelimiterValues()

        return StrSubstitutor.replace(format, values)
    }

    private String getInitialFormat(IdentifierGeneratorParams params) {
        String format
        if (!StringUtils.isBlank(params?.formatOverride)) {
            format = params.formatOverride
        }
        else {
            format = configService.getProperty("openboxes.identifier.${propertyKey}.format")
            if (StringUtils.isBlank(format)) {
                format = configService.getProperty("openboxes.identifier.default.format")
            }
        }

        // Also check for any custom prefix/suffix as a part of the "initial" format because they are overrides that
        // directly modify the format (instead of filling it in).
        if (configService.getProperty("openboxes.identifier.${propertyKey}.prefix.enabled", Boolean)) {
            if (StringUtils.isNotBlank(params.prefix)) {
                format = "${params.prefix}${Constants.DEFAULT_IDENTIFIER_SEPARATOR}${format}"
            }
            if (StringUtils.isNotBlank(params.suffix)) {
                format = "${format}${Constants.DEFAULT_IDENTIFIER_SEPARATOR}${params.suffix}"
            }
        }

        return format
    }

    private Map getTemplateValues(IdentifierGeneratorParams params) {
        Map values = new HashMap()

        // Add all fields from the given domain entity that are defined in the properties.
        Map properties = configService.getProperty("openboxes.identifier.${propertyKey}.properties", Map)
        if (params?.templateEntity) {
            values << dataService.transformObject(params.templateEntity, properties)
        }

        // Add any non-entity-specific fields. Note that if a field exists in both templateCustomValues and an entity,
        // the value in templateCustomValues takes precedence.
        if (params?.templateCustomValues) {
            values << params.templateCustomValues
        }

        return values
    }

    private Map getDelimiterValues() {
        String delimiter = configService.getProperty("openboxes.identifier.${propertyKey}.delimiter")
        if (StringUtils.isBlank(delimiter)) {
            delimiter = configService.getProperty("openboxes.identifier.default.delimiter")
        }
        return delimiter ? [Constants.IDENTIFIER_FORMAT_KEYWORD_DELIMITER: delimiter] : Collections.emptyMap()
    }

    private boolean hasRandomness(String format) {
        return format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM_EMBEDDED)
    }

    protected boolean idAlreadyExists(String id) {
        if (!id) {
            // If we failed to generate the id, it'll be null here. The default behaviour is to accept that silently.
            return false
        }
        Integer count = countDuplicates(id)
        return count ? (count > 0) : false
    }
}
