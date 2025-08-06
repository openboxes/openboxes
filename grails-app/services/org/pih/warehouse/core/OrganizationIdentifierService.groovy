package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import org.apache.commons.lang.WordUtils

// TODO: Try to merge this logic into IdentifierService. We'd need to support a new ${abbreviation} keyword in the
//       format that would get injected to based on the same logic that we have here. Then we'd add a new
//       identifier.x.abbreviation.field = "path.to.field" config that we can use to extract the field from the entity.
//       Or if that's too challenging, we can simply return the string to use via abstract method in IdentifierService.
@Transactional
class OrganizationIdentifierService {

    ConfigService configService

    /**
     * Generates a unique id that resembles the given name.
     *
     * Ex: Given a name "Big Bad Guys" it might generate "BBG", or "BB0" if "BBG" already exists (up to "BB9").
     * Ex: Given a name "Biggie" it might generate "BIG", or "BI0" if "BIG" already exists (up to "BB9").
     */
    String generate(String name) {
        Integer minSize = configService.getProperty('openboxes.identifier.organization.minSize', Integer)
        Integer maxSize = configService.getProperty('openboxes.identifier.organization.maxSize', Integer)

        // There are some organization names formatted like: "Name, Inc." so we trim everything after the comma
        // to get a cleaner name.
        String sanitizedName = name.split(",")[0]

        // This turns strings like "big bad guys" into "bbg"
        String identifier = WordUtils.initials(sanitizedName)?.replaceAll("[^a-zA-Z0-9]", "")

        // If there are too few initials, take the original identifier and chop off words until you have the smallest
        // that fits within the limit, then trim further if needed. For example, given the string "biggie bad":
        // outputs "biggi" if maxSize == 5 (and "bigg" if maxSize == 4, "big" if maxSize == 3, ...)
        // outputs "biggie" if maxSize is between 6-9
        // outputs "biggie bad" if maxSize > 9.
        if (identifier.length() < minSize) {
            identifier = WordUtils.abbreviate(sanitizedName, minSize, maxSize, null)
        }

        // If the given name had too many words and we end up with a string of initials that is still too long,
        // trim it until it fits. Ex: If maxSize == 3 and we're given the initials "rbbg" trim them down to "rbb"
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
            // TODO: If suffix is '9', doing suffix++ produces ':', which is garbage! We need to either error or make
            //       '9' become '10' but need to also consider maxSize. Ex: if code is "BB9" and maxSize is 3, we could
            //       either make code "B10" or error.
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
        List<String> organizationCodes = Organization.withCriteria {
            projections {
                property('code')
            }
            like('code', identifier + '%')
        } as List<String>

        // Filter down to only the codes ending in a digit. Ex: ["a", "a0", "a1"] will filter to ["a0", "a1"]
        organizationCodes = organizationCodes.findAll { Character.isDigit(it.charAt(it.size() - 1)) }

        // Return the code with the largest numerical suffix. Ex: given ["a0", "a1", ..., "a99"], will return "a99"
        return organizationCodes ? organizationCodes.sort()?.last() : null
    }
}
