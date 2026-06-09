package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DefaultTypeParserSpec extends Specification {

    @Shared
    DefaultTypeParser parser

    void setup() {
        parser = new DefaultTypeParser([
                // We're testing the DefaultTypeParser, not the individual parsers, so we only need to add
                // enough parsers for us to be able to test a few edge cases
                new IntegerParser(),
                new StringParser(),
        ])
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: true,
        )

        expect:
        assert parser.parse(toParse, typeToParseTo, context) == expectedResult

        where:
        toParse | typeToParseTo | defaultValue || expectedResult
        null    | Integer       | null         || null
        null    | Integer       | 0            || 0
        null    | String        | null         || null
        null    | String        | ""           || ""
        1       | Integer       | null         || 1
        "1"     | Integer       | null         || 1
        "hi"    | String        | null         || "hi"
        1       | String        | null         || "1"
    }

    void "parse should error when parsing a type that does not have a default"() {
        when: "given a type that the DefaultTypeParser doesn't know about"
        parser.parse("1.0", Double)

        then:
        thrown(IllegalArgumentException)
    }
}
