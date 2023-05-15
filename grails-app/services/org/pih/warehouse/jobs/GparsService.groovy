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

import groovyx.gpars.GParsPool
import groovyx.gpars.util.PoolUtils
import jsr166y.ForkJoinPool
import jsr166y.ForkJoinWorkerThread
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.TimeUnit

/**
 * A thin service that wraps GPars to improve logging and exception handling.
 *
 * gparsService.withPool() behaves just like GParsPool.withPool(),
 * with the following modifications:
 *
 * (a) it assigns names to pool workers to clarify log messages,
 * (b) it logs uncaught exceptions instead of printing them.
 *
 * Cf. https://github.com/GPars/GPars/blob/release-0.12/src/main/groovy/groovyx/gpars/GParsPool.groovy
 */
class GparsService {

    private static final Logger logger = LoggerFactory.getLogger(GparsService)

    /**
     * Factory that creates named threads for use by GPars.
     */
    private static class GParsForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        private static int indexCounter = 0

        private final String poolName
        private final int poolIndex

        GParsForkJoinWorkerThreadFactory(String poolName = null) {
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
     * Create threads for use by GPars; the poolName field makes logs clearer.
     */
    private static ForkJoinPool.ForkJoinWorkerThreadFactory createForkJoinWorkerThreadFactoryForPool(String poolName) {
        return new GParsForkJoinWorkerThreadFactory(poolName)
    }

    /**
     * Log any uncaught exceptions from pool workers.
     *
     * GPars 0.12 prints them to stdout, which is invisible to Sentry.
     */
    private static class LogEveryUncaughtExceptionHandler implements UncaughtExceptionHandler {

        @Override
        void uncaughtException(Thread failedThread, Throwable throwable) {
            logger.error("GPars thread ${failedThread.name} threw ${throwable.message}", throwable)
        }
    }

    /**
     * Create a named pool for GPars with `poolSize` workers.
     *
     * GPars 0.12's implementation (q.v.) defaults to one more than the
     * core count, which is high for a host that also runs a database.
     */
    private static ForkJoinPool createPool(String poolName, int poolSize) {
        logger.info("creating new pool '${poolName}' of size ${poolSize}")
        return new ForkJoinPool(
            poolSize,
            createForkJoinWorkerThreadFactoryForPool(poolName),
            new LogEveryUncaughtExceptionHandler(),
            false
        )
    }

    /**
     * Create a pool, run a closure within it and wait for it to finish.
     *
     * Behaves like GParsPool.withPool().
     */
    static withPool(String poolName, int poolSize, Closure cl) {
        ForkJoinPool pool = createPool(poolName, poolSize)
        try {
            return GParsPool.withExistingPool(pool, cl)
        } finally {
            logger.info("draining pool '${poolName}' of size ${poolSize}")
            pool.shutdown()
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            logger.debug("finished draining pool '${poolName}'")
        }
    }

    /**
     * Create a pool, run a closure within it and wait for it to finish.
     *
     * Behaves like GParsPool.withPool().
     */
    static withPool(String poolName, Closure cl) {
        return withPool(poolName, PoolUtils.retrieveDefaultPoolSize(), cl)
    }
}
