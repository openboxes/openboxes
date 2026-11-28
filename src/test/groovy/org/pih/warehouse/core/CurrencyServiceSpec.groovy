package org.pih.warehouse.core

import grails.testing.services.ServiceUnitTest
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class CurrencyServiceSpec extends Specification implements ServiceUnitTest<CurrencyService> {

    void setup() {
        config.openboxes.locale.currencyApi.url = "https://api.example.com/latest?base=%s"
        config.openboxes.locale.defaultCurrencyCode = "USD"
    }

    void 'buildUrl should return #expected for template #template, key #apiKey and parameter #parameter'() {
        expect:
        assert CurrencyService.buildUrl(template, "USD", apiKey, parameter) == expected

        where:
        template                                               | apiKey | parameter    || expected
        "https://api.example.com/latest?base=%s"               | ""     | "access_key" || "https://api.example.com/latest?base=USD"
        "https://api.example.com/latest?base=%s"               | null   | "access_key" || "https://api.example.com/latest?base=USD"
        "https://api.example.com/latest?base=%s"               | "k1"   | "access_key" || "https://api.example.com/latest?base=USD&access_key=k1"
        "https://api.example.com/latest?base=%s"               | "k1"   | "apikey"     || "https://api.example.com/latest?base=USD&apikey=k1"
        "https://api.example.com/latest/%s"                    | "k1"   | "access_key" || "https://api.example.com/latest/USD?access_key=k1"
        "https://api.example.com/latest?access_key=k0&base=%s" | "k1"   | "access_key" || "https://api.example.com/latest?base=USD&access_key=k1"
        "https://api.example.com/latest?base=%s"               | "k1"   | null         || "https://api.example.com/latest?base=USD"
    }

    void 'getExchangeRates should throw when the API returns an error instead of rates'() {
        given:
        service.apiClientService = Stub(ApiClientService) {
            get(_) >> new JSONObject('{"success": false, "error": {"code": 101, "type": "missing_access_key"}}')
        }

        when:
        service.getExchangeRates()

        then:
        ApiException e = thrown()
        assert e.message.contains("missing_access_key")
    }

    void 'getExchangeRates should return the response when it has rates'() {
        given:
        service.apiClientService = Stub(ApiClientService) {
            get(_) >> new JSONObject('{"base": "USD", "rates": {"EUR": 0.91}}')
        }

        expect:
        assert service.getExchangeRates().rates.EUR == 0.91
    }
}
