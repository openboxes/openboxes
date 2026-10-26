package org.pih.warehouse.regression

import org.codehaus.groovy.runtime.ConvertedClosure
import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

import java.lang.annotation.Annotation
import java.lang.reflect.Proxy

/**
 * {@code ./gradlew regressionTest regressionIntegrationTest -PrunPending}: runs every {@code @PendingFeature} feature
 * of the regression suite as a NORMAL feature, so one run shows each known defect that is still there as a failure
 * and each one that got fixed as a pass.
 * Inert unless the test JVM has {@code -Dregression.runPending=true}. Then, just before a spec runs (after every
 * annotation-driven extension has attached its interceptors), it removes the interceptors Spock's
 * PendingFeatureExtension added, and records each feature's outcome in
 * {@code ${regression.reports.dir}/pending/${regression.testTask}-<test worker>.tsv} (one file per test JVM):
 *   OPEN        a pending feature that fails (the defect is still there)
 *   PASSING     a pending feature that passes (fixed: remove its annotation)
 *   UNEXPECTED  any other feature (or spec fixture) that fails
 */
class RunPendingExtension implements IGlobalExtension {

    static final String PROPERTY = 'regression.runPending'
    private static final List<String> GAP_ANNOTATIONS = ['spock.lang.PendingFeature']
    private static final List<String> GAP_INTERCEPTOR_PREFIXES = [
            'org.spockframework.runtime.extension.builtin.PendingFeature',
    ]

    static boolean enabled() {
        return Boolean.getBoolean(PROPERTY)
    }

    @Override
    void start() {}

    @Override
    void stop() {}

    @Override
    void visitSpec(SpecInfo spec) {
        // Only the regression suite's specs.
        String name = spec.reflection.name
        if (!enabled() || !(name.startsWith('org.pih.warehouse.regression.') || name.startsWith('org.pih.warehouse.api.regression.'))) {
            return
        }
        spec.addInterceptor({ IMethodInvocation invocation ->
            spec.allFeatures.each { FeatureInfo feature -> stripGapInterceptors(feature) }
            invocation.proceed()
        } as IMethodInterceptor)
        spec.addListener(new Recorder())
    }

    /** The @PendingFeature annotation on a feature, or null. */
    static Annotation gapAnnotation(FeatureInfo feature) {
        return feature.featureMethod.reflection?.annotations?.find { it.annotationType().name in GAP_ANNOTATIONS }
    }

    static void stripGapInterceptors(FeatureInfo feature) {
        feature.interceptors.removeAll { isGapInterceptor(it) }
        feature.featureMethod.interceptors.removeAll { isGapInterceptor(it) }
        feature.iterationInterceptors.removeAll { isGapInterceptor(it) }
    }

    /** Spock's own interceptor classes (or a closure coerced into one: a Proxy over a ConvertedClosure). */
    static boolean isGapInterceptor(IMethodInterceptor interceptor) {
        String origin = interceptor.getClass().name
        if (Proxy.isProxyClass(interceptor.getClass())) {
            def handler = Proxy.getInvocationHandler(interceptor)
            if (handler instanceof ConvertedClosure) {
                origin = ((ConvertedClosure) handler).delegate.getClass().name
            }
        }
        return GAP_INTERCEPTOR_PREFIXES.any { origin.startsWith(it) }
    }

    private static class Recorder extends AbstractRunListener {
        private final Set<FeatureInfo> failed = Collections.synchronizedSet(new HashSet<FeatureInfo>())

        @Override
        void error(ErrorInfo error) {
            FeatureInfo feature = error.method?.feature
            if (feature == null) {
                write('UNEXPECTED', error.method?.parent?.reflection?.name ?: '?', error.method?.name ?: 'fixture',
                        error.exception.toString())
            } else {
                failed << feature
            }
        }

        @Override
        void afterFeature(FeatureInfo feature) {
            Annotation gap = gapAnnotation(feature)
            boolean didFail = failed.contains(feature)
            String reason = gap ? reasonOf(gap) : ''
            if (gap) {
                write(didFail ? 'OPEN' : 'PASSING', feature.spec.reflection.name, feature.name, reason)
            } else if (didFail) {
                write('UNEXPECTED', feature.spec.reflection.name, feature.name, '')
            }
        }

        private static String reasonOf(Annotation gap) {
            return gap.reason()
        }

        private static synchronized void write(String outcome, String spec, String feature, String detail) {
            String dir = System.getProperty('regression.reports.dir')
            if (!dir) {
                return
            }
            File out = new File(dir, "pending/${System.getProperty('regression.testTask', 'test')}-${System.getProperty('org.gradle.test.worker', '0')}.tsv")
            out.parentFile.mkdirs()
            out << [outcome, "${spec}#${feature}", detail].collect { (it as String).replaceAll(/[\t\r\n]+/, ' ').take(400) }.join('\t') + '\n'
        }
    }
}
