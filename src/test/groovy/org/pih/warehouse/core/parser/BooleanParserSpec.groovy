package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BooleanParserSpec extends Specification {

    @Shared
    BooleanParser parser

    void setup() {
        parser = new BooleanParser()
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<Boolean> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: false,
        )

        expect:
        assert parser.parse(toParse, context) == expectedResult

        where:
        toParse | defaultValue || expectedResult
        null    | null         || null
        null    | false        || false
        true    | null         || true
        1       | null         || true
        "true"  | null         || true
        "t"     | null         || true
        "1"     | null         || true
        "y"     | null         || true
        "yes"   | null         || true
        ""      | null         || null
        "bad"   | null         || null
        "bad"   | false        || null
    }

    void "parse should error when errorOnParseFailure==true and given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<Boolean> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: true,
        )

        when:
        parser.parse(toParse, context)

        then:
        thrown(expectedException)

        where:
        toParse | defaultValue || expectedException
        "bad"   | null         || IllegalArgumentException
        "bad"   | false        || IllegalArgumentException
    }
}
