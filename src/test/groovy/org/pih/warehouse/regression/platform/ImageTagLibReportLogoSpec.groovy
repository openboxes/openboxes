package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Ignore
import spock.lang.Specification

class ImageTagLibReportLogoSpec extends Specification implements TagLibUnitTest<ImageTagLib> {

    void 'printed documents show the configured report logo'() {
        given:
        config.openboxes.report.logo.url = 'https://example.org/our-logo.png'

        when:
        String html = applyTemplate('<g:displayReportLogo/>')

        then:
        html.contains('src="https://example.org/our-logo.png"')
    }

}
