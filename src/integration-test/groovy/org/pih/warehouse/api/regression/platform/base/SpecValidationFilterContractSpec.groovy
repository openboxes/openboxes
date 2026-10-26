package org.pih.warehouse.api.regression.platform.base

import spock.lang.Specification
import spock.lang.Unroll

/**
 * The API's promises the spec validator cannot check on its own (runs without the application).
 *
 * The filter classifies a problem as a kind (see SpecValidationFilter) instead of a message text, and tells OBAF's
 * auth redirects apart; each expectation row names the kind it maps to. The last feature pins the per-spec enforcement rules (@EnforceSpecValidation tolerate entries).
 */
class SpecValidationFilterContractSpec extends Specification {

    @Unroll
    void 'status #status, #contentType, #body -> #expectation'() {
        when:
        Map problem = SpecValidationFilter.contractProblem(status, contentType, body, location)

        then:
        expectation == 'ok' ? problem == null : problem?.kind == expectation

        where:
        status | contentType               | body                                                          | location                                      || expectation
        200    | 'application/json'        | '{"data":[]}'                                                 | null                                          || 'ok'
        204    | null                      | ''                                                            | null                                          || 'ok'
        302    | null                      | ''                                                            | null                                          || 'redirect'
        302    | null                      | ''                                                            | 'http://h/openboxes/errors/handleUnauthorized' || 'obaf-auth-redirect'
        200    | 'text/html;charset=UTF-8' | '<html>login</html>'                                          | null                                          || 'html'
        404    | 'application/json'        | '{"errorCode":404,"errorMessage":"gone"}'                     | null                                          || 'ok'
        400    | 'application/json'        | '{"errorCode":400,"errorMessage":"bad","errorMessages":["a"]}' | null                                          || 'ok'
        401    | null                      | ''                                                            | null                                          || 'error-shape'
        500    | 'text/plain'              | 'boom'                                                        | null                                          || 'error-shape'
        500    | 'application/json'        | 'not json'                                                    | null                                          || 'error-shape'
        404    | 'application/json'        | '{"errorMessage":"gone"}'                                     | null                                          || 'error-code'
        409    | 'application/json'        | '{"errorCode":500,"errorMessage":"x"}'                        | null                                          || 'error-code'
        400    | 'application/json'        | '["a"]'                                                       | null                                          || 'error-shape'
    }

    @EnforceSpecValidation(tolerate = ['known response GET /stockMovements', 'known html PUT /chooseLocation/.*'])
    private static class Annotated {}

    @Unroll
    void 'an enforced spec tolerates #kind #method #path: #tolerated'() {
        given:
        SpecValidationFilter.specEnforcement = Annotated.getAnnotation(EnforceSpecValidation)

        expect:
        SpecValidationFilter.tolerated([kind: kind], method, path) == tolerated

        cleanup:
        SpecValidationFilter.specEnforcement = null

        where:
        kind       | method | path                                    || tolerated
        'response' | 'GET'  | '/openboxes/api/stockMovements'         || true
        'response' | 'POST' | '/openboxes/api/stockMovements'         || false
        'request'  | 'GET'  | '/openboxes/api/stockMovements'         || false
        'response' | 'GET'  | '/openboxes/api/stockMovements/1'       || false
        'html'     | 'PUT'  | '/openboxes/api/chooseLocation/abc'     || true
        'html'     | 'put'  | '/openboxes/api/chooseLocation/abc'     || true
    }
}
