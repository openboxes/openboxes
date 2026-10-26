package org.pih.warehouse.api.regression.support

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Loopback-only stand-ins for the two places OBAF's label code talks to the network:
 * an HTTP server standing in for labelary.com (ZebraService.renderDocument POSTs ZPL to
 * `openboxes.barcode.labelaryApi.url`), and a raw TCP listener standing in for a Zebra
 * printer (ZebraService.printDocument writes ZPL to `openboxes.barcode.printer.ipAddress:port`).
 * Both bind 127.0.0.1 on an ephemeral port and record what they received, so a spec can
 * assert what WOULD have left the machine while nothing does.
 */
class CaptureServers {

    static class Labelary implements Closeable {
        final HttpServer server
        final List<String> bodies = new CopyOnWriteArrayList<>()
        final List<String> requests = new CopyOnWriteArrayList<>()

        Labelary() {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
            server.createContext('/') { HttpExchange ex ->
                requests << "${ex.requestMethod} ${ex.requestURI}".toString()
                bodies << ex.requestBody.getText('UTF-8')
                byte[] png = [0x89, 0x50, 0x4E, 0x47] as byte[]
                ex.responseHeaders.add('Content-Type', 'image/png')
                ex.sendResponseHeaders(200, png.length)
                ex.responseBody.write(png)
                ex.close()
            }
            server.start()
        }

        /** Base URL of the stub, no path. */
        String getOrigin() {
            return "http://127.0.0.1:${server.address.port}"
        }

        String getUrl() {
            return "http://127.0.0.1:${server.address.port}/v1/printers/12dpmm/labels/2x1/0/"
        }

        @Override
        void close() {
            server.stop(0)
        }
    }

    static class Printer implements Closeable {
        final ServerSocket socket
        final List<String> jobs = new CopyOnWriteArrayList<>()
        private final Thread thread

        Printer() {
            socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())
            thread = Thread.start('test-printer-stub') {
                while (!socket.closed) {
                    try {
                        Socket s = socket.accept()
                        s.withCloseable { jobs << it.inputStream.getText('UTF-8') }
                    } catch (IOException ignored) {
                        // closed
                    }
                }
            }
        }

        int getPort() {
            return socket.localPort
        }

        /** Printing is asynchronous on the socket: give the reader a moment. */
        List<String> awaitJobs(int expected = 1, long timeoutMs = 3000) {
            long deadline = System.currentTimeMillis() + timeoutMs
            while (jobs.size() < expected && System.currentTimeMillis() < deadline) {
                Thread.sleep(50)
            }
            return new ArrayList<>(jobs)
        }

        @Override
        void close() {
            socket.close()
        }
    }
}
