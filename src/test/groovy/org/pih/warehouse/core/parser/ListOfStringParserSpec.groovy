package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ListOfStringParserSpec extends Specification {

    @Shared
    ListOfStringParser parser

    void setup() {
        parser = new ListOfStringParser(new StringParser())
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ListParserContext<String> context = new ListParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: false,
                listElementParserContext: new ParserContext<String>(
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
        1.1          | null         || ["1.1"]
        1.1d         | null         || ["1.1"]
        1            | null         || ["1"]
        1l           | null         || ["1"]
        "1"          | null         || ["1"]
        "1.1"        | null         || ["1.1"]
        "1,2"        | null         || ["1", "2"]
        " ,1 ,, 2,"  | null         || ["1", "2"]
        [1, 2]       | null         || ["1", "2"]
        ["1", "2"]   | null         || ["1", "2"]
        ["1", 2]     | null         || ["1", "2"]
        ""           | null         || null
        ""           | []           || []
        ","          | null         || []
        " "          | null         || null
        " "          | []           || []
        "aa"         | null         || ["aa"]
        "aa,bb,11"   | null         || ["aa", "bb", "11"]
        " aa , bb "  | null         || ["aa", "bb"]
        "你,好"       | null         || ["你", "好"]
    }
}
