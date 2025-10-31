package org.pih.warehouse.api.util

import io.restassured.path.json.JsonPath
import org.springframework.boot.test.context.TestComponent

@TestComponent
class JsonPathUtil {

    /**
     * Given a JsonPath that is a list of elements, extract a field's value from the first element that matches the
     * given filter condition.
     */
    String extractFieldFromListGivenCriteria(String keyToExtract, String keyToMatch, String valueToMatch, JsonPath json) {
        Map<String, String> element = extractElementFromListGivenCriteria(keyToMatch, valueToMatch, json)
        return element.get(keyToExtract)
    }

    /**
     * Given a JsonPath that is a list of elements, extract the first element that matches the given filter condition.
     */
    Map<String, String> extractElementFromListGivenCriteria(String keyToMatch, String valueToMatch, JsonPath json) {
        // https://www.javadoc.io/doc/com.jayway.restassured/json-path/2.8.0/com/jayway/restassured/path/json/JsonPath.html
        String filter = "data.find { it.${keyToMatch} == valueToMatch }"
        return json.param('valueToMatch', valueToMatch).getMap(filter)
    }
}
