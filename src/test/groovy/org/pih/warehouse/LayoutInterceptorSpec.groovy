package org.pih.warehouse

import spock.lang.Specification
import spock.lang.Unroll

/**
 * The ?layout= parameter exists so the original and unified layouts can be
 * compared on a running instance. It is not a general layout selector: before
 * the allowlist it accepted any name-shaped value, and several of those strip
 * a page's chrome for anyone who appends them — ?layout=_none_ is SiteMesh's
 * reserved "render undecorated", and print/email/mobile are real layouts with
 * no navigation.
 */
@Unroll
class LayoutInterceptorSpec extends Specification {

    void "'#requested' is #outcome as a ?layout= value"() {
        expect:
        LayoutInterceptor.isSelectable(requested) == (outcome == 'accepted')

        where:
        requested  | outcome
        // the two the comparison feature exists for
        'custom'   | 'accepted'
        'default'  | 'accepted'

        // SiteMesh's reserved name: renders the page with no decoration at all
        '_none_'   | 'rejected'

        // real layouts that carry no navigation
        'print'    | 'rejected'
        'email'    | 'rejected'
        'mobile'   | 'rejected'
        'react'    | 'rejected'
        'analytics' | 'rejected'

        // path traversal, which the old regex did catch
        '../custom' | 'rejected'
        '/etc/passwd' | 'rejected'
        'a/b'      | 'rejected'

        // exact match only — no case folding, no padding
        'Custom'   | 'rejected'
        'DEFAULT'  | 'rejected'
        ' custom'  | 'rejected'
        'custom '  | 'rejected'
        'customx'  | 'rejected'

        // absent or empty means "no opinion", not "pick something"
        null       | 'rejected'
        ''         | 'rejected'
    }
}
