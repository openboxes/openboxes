package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BigDecimalParserSpec extends Specification {

    @Shared
    BigDecimalParser parser

    void setup() {
        parser = new BigDecimalParser()
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<BigDecimal> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: false,
        )

        expect:
        assert parser.parse(toParse, context) == expectedResult

        where:
        toParse | defaultValue || expectedResult
        null    | null         || null
        null    | 0.0          || 0.0
        1.1     | null         || 1.1
        1.1d    | null         || 1.1
        1       | null         || 1.0
        1l      | null         || 1.0
        "1"     | null         || 1
        "1.1"   | null         || 1.1
        "-1.1"  | null         || -1.1
        ""      | null         || null
        "bad"   | null         || null
        "bad"   | 0.0          || null
    }

    void "parse should error when errorOnParseFailure==true and given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<BigDecimal> context = new ParserContext(
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
        "bad"   | 0.0          || NumberFormatException
    }
}
