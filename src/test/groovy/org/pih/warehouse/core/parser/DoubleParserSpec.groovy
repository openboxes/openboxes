package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DoubleParserSpec extends Specification {

    @Shared
    DoubleParser parser

    void setup() {
        parser = new DoubleParser()
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<Double> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: false,
        )

        expect:
        assert parser.parse(toParse, context) == expectedResult

        where:
        toParse | defaultValue || expectedResult
        null    | null         || null
        null    | 0.0d         || 0.0d
        1.1     | null         || 1.1d
        1.1d    | null         || 1.1d
        1       | null         || 1.0d
        1l      | null         || 1.0d
        "1"     | null         || 1.0d
        "1.1"   | null         || 1.1d
        "-1.1"  | null         || -1.1d
        ""      | null         || null
        "bad"   | null         || null
        "bad"   | 0.0d         || null
    }

    void "parse should error when errorOnParseFailure==true and given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<Double> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: true,
        )

        when:
        parser.parse(toParse, context)

        then:
        thrown(expectedException)

        where:
        toParse | defaultValue || expectedException
        "bad"   | null         || NumberFormatException
        "bad"   | 0.0d         || NumberFormatException
    }
}
