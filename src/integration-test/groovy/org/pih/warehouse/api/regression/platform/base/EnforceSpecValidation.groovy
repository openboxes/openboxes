package org.pih.warehouse.api.regression.platform.base

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Per-spec switch for {@link SpecValidationFilter}: while a FEATURE method of the annotated spec (or a subclass)
 * runs, an OpenAPI / error-contract violation fails the call. Fixtures (setup, setupData,
 * cleanup) stay report-only: every RegressionApiSpec logs in with POST /api/login, which answers text/html on OBAF.
 *
 * The global switch (-Pregression.specValidation=enforce) cannot be used on OBAF for that reason; this annotation is how a
 * spec family turns enforcement on once its specs match OBAF. Violations every call of the family shares with a
 * known OBAF defect are listed in {@link #tolerate}, one entry per rule:
 *
 * <pre>
 *   'known response GET /stockMovements'          // kind, method, path regex (after /openboxes/api, anchored)
 *   'known html PUT /chooseLocation/.*'
 * </pre>
 *
 * A tolerated violation is still written to the report.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@Inherited
@interface EnforceSpecValidation {

    /** "known &lt;kind&gt; &lt;METHOD&gt; &lt;path regex&gt;": violations not enforced (the first token marks the
     * entry as a known defect; it is not parsed). */
    String[] tolerate() default []
}
