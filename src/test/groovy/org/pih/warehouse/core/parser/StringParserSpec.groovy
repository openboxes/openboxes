package org.pih.warehouse.core.parser

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class StringParserSpec extends Specification {

    @Shared
    StringParser parser

    void setup() {
        parser = new StringParser()
    }

    void "parse should return #expectedResult when given #toParse and a defaultValue of #defaultValue"() {
        given:
        ParserContext<String> context = new ParserContext(
                defaultValue: defaultValue,
                errorOnParseFailure: true,
        )

        expect:
        assert parser.parse(toParse, context) == expectedResult

        where:
        toParse | defaultValue || expectedResult
        null    | null         || null
        null    | ""           || ""
        ""      | null         || null
        ""      | ""           || ""
        "hi hi" | null         || "hi hi"
        1.1     | null         || "1.1"
        1.1d    | null         || "1.1"
        1       | null         || "1"
        1l      | null         || "1"
        "1"     | null         || "1"
        "1.1"   | null         || "1.1"
        "你好"   | null         || "你好"
    }
}
