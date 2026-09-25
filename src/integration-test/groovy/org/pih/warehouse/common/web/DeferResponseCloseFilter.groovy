package org.pih.warehouse.common.web

import javax.servlet.FilterChain
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

import org.springframework.web.filter.OncePerRequestFilter

/**
 * Test-only servlet filter that keeps a response from completing on the wire before the request has fully unwound.
 *
 * Why: the Grails JSON/XML converters end render() by calling close() on the response writer, and closing the
 * writer makes the servlet container finish the response immediately (for a chunked response it writes the
 * terminal chunk). In a controller action that runs inside a transaction (for example a controller annotated with
 * @Transactional at the class level), the client therefore sees a complete response before the transaction has
 * committed. Our API specs fire their next request the moment the previous response completes, so that request can
 * reach the database before the commit - which shows up as intermittent "foreign key constraint fails" and
 * "unsaved transient instance" errors on entities created by the immediately preceding request.
 *
 * What it does: the response is wrapped so that close() on the response writer (called by the converters) or on
 * the output stream (called by download actions) flushes what has been written and then discards any further
 * output, which is what the container does after a real close - but the container's own writer/stream is never
 * closed by application code. The container closes it itself when the request finishes, which is after the
 * controller action has returned and its transaction has completed. Status, headers and body bytes are still sent
 * when the action flushes them; only the end of the response moves.
 *
 * This class is not component-scanned (no stereotype annotation, and IntegrationSpecConfig only scans the api,
 * slice and smoke packages); IntegrationSpecConfig constructs it in a @Bean method. It lives in the integration
 * test source set on purpose: that source set compiles to its own output directory and is not packaged into the
 * application.
 */
class DeferResponseCloseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        chain.doFilter(request, new DeferredCloseResponse(response))
    }

    static class DeferredCloseResponse extends HttpServletResponseWrapper {

        private DeferredClosePrintWriter writer
        private DeferredCloseServletOutputStream outputStream

        DeferredCloseResponse(HttpServletResponse response) {
            super(response)
        }

        @Override
        PrintWriter getWriter() throws IOException {
            if (writer == null) {
                writer = new DeferredClosePrintWriter(super.getWriter())
            }
            return writer
        }

        @Override
        ServletOutputStream getOutputStream() throws IOException {
            if (outputStream == null) {
                outputStream = new DeferredCloseServletOutputStream(super.getOutputStream())
            }
            return outputStream
        }

        /**
         * reset() clears the container's record of whether getWriter() or getOutputStream() was used, so the cached
         * wrappers must be dropped as well and re-created from the container on the next call.
         */
        @Override
        void reset() {
            super.reset()
            writer = null
            outputStream = null
        }
    }

    /**
     * PrintWriter.close() closes the underlying writer and then sets its protected "out" field to null, after which
     * every inherited print/write/flush method becomes a silent no-op via ensureOpen(). This subclass keeps the
     * second half of that behaviour and drops the first: flush, then detach, never close the delegate.
     */
    static class DeferredClosePrintWriter extends PrintWriter {

        DeferredClosePrintWriter(PrintWriter delegate) {
            super(delegate, false)
        }

        @Override
        void close() {
            synchronized (lock) {
                if (out == null) {
                    return
                }
                // checkError() flushes the delegate and reports any error it has recorded so far.
                if (((PrintWriter) out).checkError()) {
                    setError()
                }
                out = null
            }
        }
    }

    static class DeferredCloseServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream delegate
        private boolean closed = false

        DeferredCloseServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate
        }

        @Override
        void write(int b) throws IOException {
            if (!closed) {
                delegate.write(b)
            }
        }

        @Override
        void write(byte[] b, int off, int len) throws IOException {
            if (!closed) {
                delegate.write(b, off, len)
            }
        }

        @Override
        void flush() throws IOException {
            if (!closed) {
                delegate.flush()
            }
        }

        @Override
        void close() throws IOException {
            if (!closed) {
                try {
                    delegate.flush()
                } finally {
                    closed = true
                }
            }
        }

        @Override
        boolean isReady() {
            return delegate.isReady()
        }

        @Override
        void setWriteListener(WriteListener writeListener) {
            delegate.setWriteListener(writeListener)
        }
    }
}
