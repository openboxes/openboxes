package org.pih.warehouse.core

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.WordUtils

// TODO: Try to merge this logic into IdentifierService. We'd need to support a new ${abbreviation} keyword in the
//       format that would get injected to based on the same logic that we have here. Then we'd add a new
//       identifier.x.abbreviation.field = "path.to.field" config that we can use to extract the field from the entity.
//       Or if that's too challenging, we can simply return the string to use via abstract method in IdentifierService.
@Transactional
class OrganizationIdentifierService {

    GrailsApplication grailsApplication

    /**
     * Generates a unique id that resembles the given name.
     *
     * Ex: Given a name "Big Bad Guys" it might generate "BBG", or "BB0" if "BBG" already exists (up to "BB9").
     * Ex: Given a name "Biggie" it might generate "BIG", or "BI0" if "BIG" already exists (up to "BB9").
     */
    String generate(String name) {
        Integer minSize = grailsApplication.config.getProperty('openboxes.identifier.organization.minSize', Integer)
        Integer maxSize = grailsApplication.config.getProperty('openboxes.identifier.organization.maxSize', Integer)

        // There are some organization names formatted like: "Name, Inc." so we trim everything after the comma
        // to get a cleaner name.
        String sanitizedName = name.split(",")[0]

        // This turns strings like "big bad guys" into "bbg"
        String identifier = WordUtils.initials(sanitizedName)?.replaceAll("[^a-zA-Z0-9]", "")

        // If there are too few initials, take the original identifier and chop off words until you have the smallest
        // that fits within the limit. This turns strings like "biggie bad" into "biggie" if maxSize is <= 6.
        if (identifier.length() < minSize) {
            identifier = WordUtils.abbreviate(sanitizedName, minSize, maxSize, null)
        }

        // If we end up with a string like "biggie" that is still too long, trim it until it fits.
        else if (identifier.length() > maxSize) {
            identifier = identifier.substring(0, maxSize)
        }

        identifier = identifier.toUpperCase()

        if (!idAlreadyExists(identifier)) {
            return identifier
        }

        // If the identifier already exist, change its last character to the lowest available digit. Ex: "BI0"
        // TODO: This fails if we ever get a prefix that is used more than 10 times. Once this is merged with the
        //       IdentifierService we should be able to be slightly smarter about this and allow a more configurable
        //       sequence number in the case where duplicates are found.
        String identifierWithHighestNumber = getIdentifierWithHighestSuffix(identifier.substring(0, identifier.size() - 1))
        if (identifierWithHighestNumber) {
            char suffix = identifierWithHighestNumber.charAt(identifierWithHighestNumber.size() - 1)
            suffix++

            return identifier.toUpperCase().substring(0, identifier.size() -1) + suffix
        }
        return identifier.length() < maxSize ? identifier.toUpperCase() + '0': identifier.toUpperCase().substring(0, maxSize - 1) + '0'
    }

    private boolean idAlreadyExists(String id) {
        Integer count = Organization.countByCode(id)
        return count ? (count > 0) : false
    }

    private String getIdentifierWithHighestSuffix(String identifier) {
        List organizations = Organization.executeQuery(
                "select o.code from Organization o where code like :identifier", [identifier: identifier + '%'] )
        organizations = organizations.findAll { Character.isDigit(it.charAt(it.size() - 1)) }
        return organizations ? organizations.sort()?.last() : null
    }
}
