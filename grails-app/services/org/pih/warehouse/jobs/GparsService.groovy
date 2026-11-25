/**
 * Copyright (c) 2023 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.jobs

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.TimeUnit

/**
 * A thin service that provides named worker pools with logged exceptions.
 *
 * Historically this wrapped GPars; GPars is unmaintained and does not run
 * on Groovy 4, so the same behavior is now implemented directly on
 * java.util.concurrent. Within a withPool() closure, collections gain
 * eachParallel/collectParallel via org.pih.warehouse.concurrent.ParallelExtensions,
 * mirroring the two GPars enhancements OpenBoxes used:
 *
 * (a) pool workers get names to clarify log messages,
 * (b) uncaught exceptions are logged instead of printed.
 */
class GparsService {

    private static final Logger logger = LoggerFactory.getLogger(GparsService)

    /**
     * The pool bound to the thread currently executing a withPool() closure.
     * ParallelExtensions reads this to dispatch eachParallel/collectParallel.
     */
    private static final ThreadLocal<ForkJoinPool> CURRENT_POOL = new ThreadLocal<>()

    static ForkJoinPool currentPool() {
        ForkJoinPool pool = CURRENT_POOL.get()
        if (pool == null) {
            throw new IllegalStateException(
                'eachParallel/collectParallel must be called inside GparsService.withPool()')
        }
        return pool
    }

    /**
     * Factory that creates named threads for the pool.
     */
    private static class NamedForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        private static int indexCounter = 0

        private final String poolName
        private final int poolIndex

        NamedForkJoinWorkerThreadFactory(String poolName = null) {
            this.poolIndex = indexCounter++
            this.poolName = poolName ?: 'OpenBoxes'
        }

        @Override
        ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
            worker.name = "${poolName}GParsPool_Worker-${poolIndex}.${worker.poolIndex}"
            return worker
        }
    }

    /**
     * Log any uncaught exceptions from pool workers so they reach Sentry.
     */
    private static class LogEveryUncaughtExceptionHandler implements UncaughtExceptionHandler {

        @Override
        void uncaughtException(Thread failedThread, Throwable throwable) {
            logger.error("Worker thread ${failedThread.name} threw ${throwable.message}", throwable)
        }
    }

    /**
     * Create a named pool with `poolSize` workers.
     *
     * Defaults used to come from GPars, which picked one more than the
     * core count; we keep that default (see withPool below), which is
     * high for a host that also runs a database.
     */
    private static ForkJoinPool createPool(String poolName, int poolSize) {
        logger.info("creating new pool '${poolName}' of size ${poolSize}")
        return new ForkJoinPool(
            poolSize,
            new NamedForkJoinWorkerThreadFactory(poolName),
            new LogEveryUncaughtExceptionHandler(),
            false
        )
    }

    /**
     * Create a pool, run a closure within it and wait for it to finish.
     *
     * Behaves like GParsPool.withPool() did: collections used inside the
     * closure support eachParallel/collectParallel on this pool.
     */
    static withPool(String poolName, int poolSize, Closure cl) {
        ForkJoinPool pool = createPool(poolName, poolSize)
        ForkJoinPool previous = CURRENT_POOL.get()
        CURRENT_POOL.set(pool)
        try {
            return cl.call()
        } finally {
            if (previous == null) {
                CURRENT_POOL.remove()
            } else {
                CURRENT_POOL.set(previous)
            }
            logger.info("draining pool '${poolName}' of size ${poolSize}")
            pool.shutdown()
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            logger.debug("finished draining pool '${poolName}'")
        }
    }

    /**
     * Create a pool, run a closure within it and wait for it to finish.
     */
    static withPool(String poolName, Closure cl) {
        return withPool(poolName, Runtime.runtime.availableProcessors() + 1, cl)
    }
}
