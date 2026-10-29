package org.pih.warehouse.exporter

/**
 * The result of writing some bulk data to a file.
 */
abstract class BulkDataWriterResult<T> {

    /**
     * The resulting object/file that is returned from the writer. If the result is an output document,
     * make sure to call the close() method once it has been fully processed.
     */
    T result

    /**
     * Closes the output document (such as a file) that wraps the result. Failing to call close can result
     * in memory or other system resources not being freed so it's important that anything that calls into
     * a writer eventually calls close.
     *
     * Not all writers will contain an output document that needs to be closed, but we provide the close method
     * here so that we can abstract away the behaviour from the caller.
     */
    abstract void close()
}
