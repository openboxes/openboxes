package org.pih.warehouse.core

import com.opensymphony.module.sitemesh.Decorator
import com.opensymphony.module.sitemesh.Page
import com.opensymphony.sitemesh.Content
import grails.util.Holders
import groovy.transform.CompileStatic
import javax.servlet.http.HttpServletRequest
import org.grails.web.sitemesh.GSPSitemeshPage
import org.grails.web.sitemesh.GroovyPageLayoutFinder
import org.pih.warehouse.common.base.IntegrationSpec
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Unroll

/**
 * The unit specs cover the substitution decision in isolation. What they cannot
 * cover is the part that only exists once Spring and SiteMesh are real:
 *
 *  1. that the post-processor actually repointed Grails' own bean, with the GSP
 *     plugin's wiring intact — a bean that lost its view resolver would resolve
 *     no decorators at all;
 *  2. that both of SiteMesh's entry points end up in our decision. Grails calls
 *     findLayout(request, Content); the Page overload is the one we override,
 *     and the Content overload reaching it is an implementation detail of a
 *     third-party class, not something we control;
 *  3. that resolution terminates. Overriding both overloads, and later calling
 *     super without @CompileStatic, each produced a StackOverflowError that
 *     compiled cleanly and passed every unit test — Groovy's dynamic dispatch
 *     re-entered the subclass instead of the superclass. Nothing but a real
 *     call through the real bean catches that, which is why it is pinned here
 *     rather than left to a comment on the class.
 */
class UnifiedLayoutFinderIntegrationSpec extends IntegrationSpec {

    GroovyPageLayoutFinder groovyPageLayoutFinder

    private boolean originalSetting

    def setup() {
        originalSetting = finder.unifiedLayoutEnabled
    }

    def cleanup() {
        optIn(originalSetting)
    }

    private UnifiedLayoutFinder getFinder() {
        return (UnifiedLayoutFinder) groovyPageLayoutFinder
    }

    /**
     * The flag is a @Value field bound once at startup, so the tests toggle
     * the bean's own property — there is no runtime config path to it, by
     * design (changing the flag on a real instance is a restart).
     */
    private void optIn(boolean enabled) {
        finder.unifiedLayoutEnabled = enabled
    }

    /** A page as SiteMesh presents it, declaring the layout in its meta tag. */
    private static GSPSitemeshPage pageDeclaring(String layout) {
        GSPSitemeshPage page = new GSPSitemeshPage()
        if (layout != null) {
            page.addProperty('meta.layout', layout)
        }
        return page
    }

    /**
     * Grails only resolves a layout while it is rendering a view: with neither
     * this attribute nor an explicit layout on the request, findLayout returns
     * null for every page before it ever looks at meta.layout. That is the
     * superclass's own short-circuit, so a request standing in for a render
     * has to carry the same flag Grails sets during one.
     */
    private static MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.setAttribute(GroovyPageLayoutFinder.RENDERING_VIEW_ATTRIBUTE, Boolean.TRUE)
        return request
    }

    /*
     * A GSPSitemeshPage is both a Page and a Content, so Groovy cannot pick
     * between the two overloads at runtime. Grails picks statically, from
     * Java, and these two helpers do the same — which is the point: each one
     * pins one of SiteMesh's entry points.
     */

    @CompileStatic
    private static Decorator viaPageEntryPoint(
            GroovyPageLayoutFinder finder, HttpServletRequest request, Page page) {
        return finder.findLayout(request, page)
    }

    @CompileStatic
    private static Decorator viaContentEntryPoint(
            GroovyPageLayoutFinder finder, HttpServletRequest request, Content content) {
        return finder.findLayout(request, content)
    }

    void "the application's layout finder is the unified one"() {
        expect: 'the post-processor ran against the real context'
        groovyPageLayoutFinder instanceof UnifiedLayoutFinder
    }

    void "the flag field was bound from configuration at startup"() {
        // compared against resolved config, not a literal — a developer
        // machine may carry an external openboxes-config.properties that
        // flips the flag for local demo runs
        expect: 'the @Value binding agrees with resolved configuration'
        finder.unifiedLayoutEnabled ==
                Holders.config.getProperty('openboxes.layout.unified.enabled', Boolean, Boolean.FALSE)
    }

    void "the finder kept the wiring the GSP plugin gave it"() {
        given: 'the bean the plugin declares, now backed by our class'
        Page page = pageDeclaring('custom')

        when: 'a decorator is resolved through it'
        Decorator decorator = viaPageEntryPoint(groovyPageLayoutFinder, request(), page)

        then: 'it resolved one, which it could not do without its view resolver'
        decorator != null
        decorator.page?.endsWith('.gsp')
    }

    void "an opted-out instance gets the layout the page asked for"() {
        given: 'the shipped default'
        optIn(false)

        when:
        Decorator decorator = viaPageEntryPoint(groovyPageLayoutFinder, request(), pageDeclaring('custom'))

        then: 'nothing changed for anyone who did not ask for it'
        decorator.name == 'custom'
    }

    void "an opted-in instance gets the unified layout instead"() {
        given:
        optIn(true)

        when:
        Decorator decorator = viaPageEntryPoint(groovyPageLayoutFinder, request(), pageDeclaring('custom'))

        then:
        decorator.name == 'default'
    }

    void "the Content entry point reaches the same decision"() {
        given: 'the overload Grails actually calls during rendering'
        optIn(true)
        Content content = pageDeclaring('custom')

        when:
        Decorator decorator = viaContentEntryPoint(groovyPageLayoutFinder, request(), content)

        then: 'it arrived at our override rather than bypassing it'
        decorator.name == 'default'

        when: 'and the same call with the flag off'
        optIn(false)

        then:
        viaContentEntryPoint(groovyPageLayoutFinder, request(), content).name == 'custom'
    }

    void "resolution terminates rather than recursing"() {
        given: 'both entry points, in the state that does the substitution'
        optIn(true)
        Content content = pageDeclaring('custom')

        when:
        viaPageEntryPoint(groovyPageLayoutFinder, request(), content)
        viaContentEntryPoint(groovyPageLayoutFinder, request(), content)

        then: 'the dispatch reaches the superclass, not back into the subclass'
        noExceptionThrown()
    }

    @Unroll
    void "#layout pages are left alone with the flag #enabled"() {
        given:
        optIn(enabled)

        expect: 'only pages declaring "custom" are substituted'
        viaPageEntryPoint(groovyPageLayoutFinder, request(), pageDeclaring(layout)).name == layout

        where:
        layout   | enabled
        'react'  | true
        'react'  | false
        'print'  | true
        'email'  | true
        'mobile' | true
        'main'   | true
    }

    void "a request that named its own layout keeps it"() {
        given: 'the ?layout= override the interceptor sets'
        optIn(true)
        MockHttpServletRequest request = request()
        request.setAttribute(GroovyPageLayoutFinder.LAYOUT_ATTRIBUTE, 'custom')
        request.setAttribute(UnifiedLayoutFinder.OVERRIDE_ATTRIBUTE, Boolean.TRUE)

        when:
        Decorator decorator = viaPageEntryPoint(groovyPageLayoutFinder, request, pageDeclaring('custom'))

        then: 'the reviewer comparing the two layouts gets the one they asked for'
        decorator.name == 'custom'
    }

    void "a page declaring no layout is unaffected by the flag"() {
        given: 'a page naming no layout, which falls through to Grails\' own default'
        Page page = pageDeclaring(null)

        expect: 'whatever that fallback does, the flag does not change it'
        outcomeOf(page, true) == outcomeOf(page, false)
    }

    /**
     * The no-layout fallback resolves the application default decorator, and
     * outside a controller request that throws rather than returning null.
     * What this spec has an opinion about is not which of the two it does, but
     * that the flag makes no difference to it — so record either as an outcome
     * and compare.
     */
    private String outcomeOf(Page page, boolean enabled) {
        optIn(enabled)
        try {
            return viaPageEntryPoint(groovyPageLayoutFinder, request(), page)?.name ?: 'no decorator'
        } catch (Exception e) {
            return "threw ${e.class.name}"
        }
    }
}
