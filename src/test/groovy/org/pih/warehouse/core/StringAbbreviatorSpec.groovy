package org.pih.warehouse.core

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class StringAbbreviatorSpec extends Specification {

    @Shared
    StringAbbreviator stringAbbreviator

    void setup() {
        stringAbbreviator = new StringAbbreviator()
    }

    void 'abbreviate should succeed for scenario: #scenario'() {
        expect:
        assert stringAbbreviator.abbreviate(name, minSize, maxSize, ignoreAfter) == expectedCode

        where:
        minSize | maxSize | ignoreAfter | name                    || expectedCode | scenario
        2       | 3       | null        | ""                      || null         | "Empty string is null"
        2       | 3       | null        | null                    || null         | "Null name is null"
        3       | 5       | null        | "Big Bad"               || "BIGBA"      | "Too few words merged and trimmed"
        1       | 2       | null        | "Real Big Bad"          || "RB"         | "Too long, too many words becomes shortened acronym"
        3       | 5       | ','         | "Bigger, Inc."          || "BIGGE"      | "Everything after comma ignored"
        3       | 5       | ','         | "Big Bad,"              || "BIGBA"      | "Ends in comma nothing trimmed"
        3       | 5       | ','         | "Big Bad"               || "BIGBA"      | "No comma nothing trimmed"
        3       | 5       | ','         | ", Inc."                || null         | "Starts with comma is null"
        3       | 5       | null        | "Bi*&)+^g"              || "BIG"        | "Special characters ignored"
        3       | 5       | null        | "Big *"                 || "BIG"        | "Special characters after space ignored"
        3       | 5       | null        | "***"                   || null         | "Special characters only is null"
        3       | 5       | null        | "1hi12hi"               || "1HI12"      | "Numbers allowed"
        3       | 5       | null        | " HI "                  || "HI"         | "Whitespace on ends is ignored"

        // Verifying the default settings that we provide
        2       | 6       | ','         | "Do Re Me Fa So La Ti"  || "DRMFSL"     | "Too long, too many words becomes shortened acronym"
        2       | 6       | ','         | "Big Bad Guys"          || "BBG"        | "Too long, acronym fits"
        2       | 6       | ','         | "Big Bad"               || "BB"         | "Too long, fewer words becomes shorter acronym"
        2       | 6       | ','         | "Biggerer"              || "BIGGER"     | "Too long word shortened"
        2       | 6       | ','         | "Big"                   || "BIG"        | "Exact length word used as is"
        2       | 6       | ','         | "Bi"                    || "BI"         | "Short enough word used as is"
        // Note that we can end up with codes that are shorter than the minimum! Should this be B0 instead?
        2       | 6       | ','         | "B"                     || "B"          | "Too short word used as is"

        // OBPIH-7441: Verifying old PIH prod settings.
        1       | 20      | null        | "ASuperReallyLongWordX" || "A"          | "Too long single-word becomes one letter"
        1       | 20      | null        | "Big"                   || "B"          | "Single-word becomes one letter"
        1       | 20      | null        | "B"                     || "B"          | "One letter word used as is"
    }
}
