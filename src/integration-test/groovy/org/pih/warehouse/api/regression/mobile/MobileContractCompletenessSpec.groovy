package org.pih.warehouse.api.regression.mobile

import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Data-driven completeness: every contract/mobile-contract.json entry must be
 * named by @Covers on at least one feature of a spec in this package, and every
 * @Covers id must exist in the contract. No server boot (plain Specification).
 */
class MobileContractCompletenessSpec extends Specification {

    static List<Class> specClasses() {
        def resolver = new PathMatchingResourcePatternResolver(MobileContractCompletenessSpec.classLoader)
        String base = 'org/pih/warehouse/api/regression/mobile/'
        Resource[] found = resolver.getResources("classpath*:${base}**/*Spec.class")
        return found.collect { Resource r ->
            String u = r.URL.toString()
            String name = u.substring(u.indexOf(base)).replace('/', '.').replaceAll(/\.class$/, '')
            Class.forName(name, false, MobileContractCompletenessSpec.classLoader)
        }.findAll { Class c -> !Modifier.isAbstract(c.modifiers) && Specification.isAssignableFrom(c) && c != MobileContractCompletenessSpec }
                .unique().sort { it.name }
    }

    static Map<String, List<String>> coverage() {
        Map<String, List<String>> byId = [:].withDefault { [] }
        specClasses().each { Class c ->
            c.declaredMethods.each { Method m ->
                Covers covers = m.getAnnotation(Covers)
                if (covers) {
                    covers.value().each { String id ->
                        String pending = m.getAnnotation(PendingFeature) ? ' (pending)' : ''
                        byId[id] << "${c.simpleName}.${m.name}${pending}".toString()
                    }
                }
            }
        }
        return byId
    }

    void 'the contract names the mobile app commit it was derived from'() {
        expect:
        MobileContract.load().mobile.repo == 'openboxes/openboxes-mobile'
        MobileContract.load().mobile.sha ==~ /[0-9a-f]{40}/
    }

    void 'the package has contract specs to scan'() {
        expect:
        specClasses().size() >= 5
    }

    @Unroll
    void 'contract entry #id is exercised by a spec feature'() {
        expect:
        coverage().containsKey(id)

        where:
        id << MobileContract.entries()*.id
    }

    void 'every @Covers id names a contract entry'() {
        given:
        Set<String> known = MobileContract.entries()*.id as Set

        expect:
        (coverage().keySet() - known).isEmpty()
    }

    void 'print the coverage map'() {
        when:
        Map<String, List<String>> cov = coverage()
        cov.sort().each { id, features -> println "[test-contract-coverage] ${id} <- ${features.join(', ')}" }

        then:
        true
    }
}
