package util

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

class RedirectUtilSpec extends Specification {

    private static MockHttpServletRequest request(String contextPath = '/openboxes') {
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.serverName = 'obnav.example.org'
        request.contextPath = contextPath
        return request
    }

    @Unroll
    void "#url is a page of the application"() {
        expect:
        RedirectUtil.isApplicationUrl(request(), url)

        where:
        url << [
                'https://obnav.example.org/openboxes/inventoryItem/showStockCard/p1',
                'http://obnav.example.org/openboxes/location/list',
                'https://OBNAV.example.org:8443/openboxes/product/edit/p1?tab=3#labels',
                'https://obnav.example.org/openboxes',
                'https://obnav.example.org/openboxes/location/list?q=[A]|{B}',
                '/openboxes/product/edit/p1',
                'product/edit/p1',
                '/openboxes/product/../location/list',
        ]
    }

    @Unroll
    void "#url is not a page of the application"() {
        expect:
        !RedirectUtil.isApplicationUrl(request(), url)

        where:
        url << [
                null,
                '',
                'https://elsewhere.example.com/openboxes/product/edit/p1',
                'https://obnav.example.org.elsewhere.example.com/openboxes/',
                'https://obnav.example.org@elsewhere.example.com/openboxes/',
                'https://elsewhere.example.com?https://obnav.example.org/openboxes/',
                'https://elsewhere.example.com#https://obnav.example.org/openboxes/',
                '//elsewhere.example.com/openboxes/product/edit/p1',
                '/\\elsewhere.example.com/openboxes/',
                'https:/\\elsewhere.example.com/openboxes/',
                'https:elsewhere.example.com/openboxes/',
                'https:///openboxes/product/edit/p1',
                'javascript:alert(1)//obnav.example.org/openboxes/',
                'ftp://obnav.example.org/openboxes/file',
                'https://obnav.example.org/other-app/page',
                'https://obnav.example.org/openboxes-other/page',
                '/other-app/page',
                ' https://elsewhere.example.com/',
                'https://elsewhere.example.com\t/openboxes/',
                '///elsewhere.example.com/openboxes/',
                'https:////elsewhere.example.com/openboxes/',
                '/\t/elsewhere.example.com/openboxes/',
                '/openboxes/../other-app/page',
                'https://obnav.example.org/openboxes/../other-app/page',
                '/openboxes/%2e%2e/other-app/page',
                '/openboxes/%2E%2E/other-app/page',
                '/openboxes/..%2fother-app/page',
                '/openboxes/%5c%5celsewhere.example.com',
                '../../../other-app/page',
                '/openboxes/..;/other-app/page',
                '/openboxes/%3b/page',
        ]
    }

    void "a path starting with two or three slashes is another host, also for an application at the root"() {
        expect:
        !RedirectUtil.isApplicationUrl(request(''), '///elsewhere.example.com/path')
        !RedirectUtil.isApplicationUrl(request(''), '//elsewhere.example.com/path')
        !RedirectUtil.isApplicationUrl(request(''), '/\\elsewhere.example.com/path')
    }

    void "a host name java.net.URI does not take as one (an underscore) is still compared"() {
        given:
        MockHttpServletRequest underscored = request()
        underscored.serverName = 'ob_app'

        expect:
        RedirectUtil.isApplicationUrl(underscored, 'http://ob_app:8080/openboxes/location/list')
        !RedirectUtil.isApplicationUrl(underscored, 'http://ob_app@elsewhere_host/openboxes/location/list')
        !RedirectUtil.isApplicationUrl(request(), 'http://ob_app:8080/openboxes/location/list')
    }

    void "a forwarded host header does not make another host part of the application"() {
        given:
        MockHttpServletRequest forwarded = request()
        forwarded.addHeader('X-Forwarded-Host', 'elsewhere.example.com')

        expect:
        !RedirectUtil.isApplicationUrl(forwarded, 'https://elsewhere.example.com/openboxes/location/list')
        RedirectUtil.isApplicationUrl(forwarded, 'https://obnav.example.org/openboxes/location/list')
    }

    void "any path on the host is in an application deployed at the root"() {
        expect:
        RedirectUtil.isApplicationUrl(request(''), 'https://obnav.example.org/product/edit/p1')
        RedirectUtil.isApplicationUrl(request(''), '/product/edit/p1')
        !RedirectUtil.isApplicationUrl(request(''), 'https://elsewhere.example.com/product/edit/p1')
    }
}
