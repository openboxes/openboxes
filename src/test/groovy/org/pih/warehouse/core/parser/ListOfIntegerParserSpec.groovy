package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ListOfIntegerParserSpec extends Specification {

    @Shared
    ListOfIntegerParser parser

    void setup() {
        parser = new ListOfIntegerParser(new IntegerParser())
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ListParserContext<Integer> context = new ListParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: false,
                listElementParserContext: new ParserContext<Integer>(
                        defaultValue: null,
                        errorOnParseFailure: false,
                ),
        )

        expect:
        assert parser.parse(toParse, context) == expectedResult

        where:
        toParse      | defaultValue || expectedResult
        null         | null         || null
        null         | []           || []
        1.1          | null         || [1]
        1.1d         | null         || [1]
        1            | null         || [1]
        1l           | null         || [1]
        "1"          | null         || [1]
        "1.1"        | null         || [1]
        "1,2"        | null         || [1, 2]
        " ,1 ,, 2,"  | null         || [1, 2]
        [1, 2]       | null         || [1, 2]
        ["1", "2"]   | null         || [1, 2]
        ["1", 2]     | null         || [1, 2]
        ""           | null         || null
        ""           | []           || []
        ","          | null         || []
        ","          | []           || []
        " "          | null         || null
        " "          | []           || []
        "bad"        | null         || []
        "bad"        | []           || []
        ["bad"]      | null         || []
        ["bad"]      | []           || []
    }

    void "parse should error when errorOnParseFailure==true and given #toParse and a defaultValue of #defaultValue"() {
        given:
        ListParserContext<Integer> context = new ListParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: true,
                listElementParserContext: new ParserContext<Integer>(
                        defaultValue: null,
                        errorOnParseFailure: true,
                ),
        )

        when:
        parser.parse(toParse, context)

        then:
        thrown(expectedException)

        where:
        toParse | defaultValue || expectedException
        "bad"   | null         || NumberFormatException
        "bad"   | []           || NumberFormatException
        ["bad"] | null         || NumberFormatException
        ["bad"] | []           || NumberFormatException
    }
}
