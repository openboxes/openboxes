package org.pih.warehouse.api.regression.obaf.events.support

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * A loopback HTTP receiver standing in for a webhook endpoint (127.0.0.1, ephemeral port).
 * Like {@link org.pih.warehouse.api.regression.support.CaptureServers.Labelary} (which records bodies only),
 * but it also records each request's headers and can answer badly: any status and body, or never
 * ({@link #hang}: the request is read and no response is ever written until {@link #close}).
 *
 * Nothing leaves the machine.
 */
class LoopbackReceiver implements Closeable {

    static class Hit {
        String method
        String path
        Map<String, String> headers   // lower-cased names, first value
        String body
    }

    final HttpServer server
    final List<Hit> hits = new CopyOnWriteArrayList<>()
    volatile int status = 200
    volatile String responseBody = '{"message":"Workflow was started"}'
    volatile String contentType = 'application/json'
    volatile boolean hang = false
    private final CountDownLatch released = new CountDownLatch(1)

    LoopbackReceiver() {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
        server.executor = Executors.newCachedThreadPool()
        server.createContext('/') { HttpExchange ex ->
            Map<String, String> headers = [:]
            ex.requestHeaders.each { String k, List<String> v -> headers[k.toLowerCase()] = v ? v[0] : null }
            hits << new Hit(method: ex.requestMethod, path: ex.requestURI.toString(), headers: headers,
                    body: ex.requestBody.getText('UTF-8'))
            if (hang) {
                released.await()
                ex.close()
                return
            }
            byte[] out = (responseBody ?: '').getBytes('UTF-8')
            ex.responseHeaders.add('Content-Type', contentType)
            ex.sendResponseHeaders(status, out.length ?: -1)
            if (out.length) {
                ex.responseBody.write(out)
            }
            ex.close()
        }
        server.start()
    }

    String getUrl() {
        return "http://127.0.0.1:${server.address.port}/webhook/test"
    }

    List<String> getBodies() {
        return hits*.body
    }

    /** A free loopback port nothing listens on (connection refused). */
    static int closedPort() {
        ServerSocket s = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        int port = s.localPort
        s.close()
        return port
    }

    @Override
    void close() {
        released.countDown()
        server.stop(0)
        (server.executor as java.util.concurrent.ExecutorService).shutdownNow()
    }
}
