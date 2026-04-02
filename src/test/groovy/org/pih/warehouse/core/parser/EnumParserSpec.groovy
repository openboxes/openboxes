package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.PutawayStatus

@Unroll
class EnumParserSpec extends Specification {

    @Shared
    EnumParser parser

    void setup() {
        parser = new EnumParser<PutawayStatus>()
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        expect:
        assert parser.parse(toParse, PutawayStatus) == expectedResult

        where:
        toParse || expectedResult
        null    || null
        ""      || null
        " "     || null
        "READY" || PutawayStatus.READY
        "rEaDy" || PutawayStatus.READY
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<PutawayStatus> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: false,
        )

        expect:
        assert parser.parse(toParse, context) == expectedResult

        where:
        toParse | defaultValue        || expectedResult
        null    | null                || null
        null    | PutawayStatus.READY || PutawayStatus.READY
        1       | null                || null
        1       | PutawayStatus.READY || null
    }

    void "parse should error when errorOnParseFailure==true and given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<PutawayStatus> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: true,
        )

        when:
        parser.parse(toParse, context)

        then:
        thrown(expectedException)

        where:
        toParse | defaultValue        || expectedException
        1       | null                || IllegalArgumentException
        1       | PutawayStatus.READY || IllegalArgumentException
    }
}
