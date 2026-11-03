/**
 * Copyright (c) 2026 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.concurrent

import java.util.concurrent.ExecutionException
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.Future

import org.pih.warehouse.jobs.GparsService

/**
 * Groovy extension methods that replace the GPars collection enhancements
 * (eachParallel / collectParallel) used inside GparsService.withPool closures.
 *
 * GPars is unmaintained and incompatible with Groovy 4, so these methods
 * reproduce the two enhancements OpenBoxes actually uses, backed by the
 * java.util.concurrent pool that GparsService binds to the calling thread.
 *
 * Registered via META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule.
 */
class ParallelExtensions {

    /**
     * Invoke the closure once per element, running invocations concurrently
     * on the pool bound by GparsService.withPool(). Blocks until every
     * invocation finishes. Rethrows the first failure, like GPars did.
     */
    static <T> Iterable<T> eachParallel(Iterable<T> self, Closure closure) {
        submitAll(self, closure)
        return self
    }

    /**
     * Transform each element concurrently on the pool bound by
     * GparsService.withPool(), preserving element order in the result.
     */
    static <T> List collectParallel(Iterable<T> self, Closure closure) {
        return submitAll(self, closure)
    }

    private static <T> List submitAll(Iterable<T> self, Closure closure) {
        ForkJoinPool pool = GparsService.currentPool()
        List<Future> futures = self.collect { element ->
            pool.submit({ -> closure.call(element) } as java.util.concurrent.Callable)
        }
        try {
            return futures.collect { it.get() }
        } catch (ExecutionException e) {
            throw e.cause ?: e
        }
    }
}
