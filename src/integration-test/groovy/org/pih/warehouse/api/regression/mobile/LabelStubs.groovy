package org.pih.warehouse.api.regression.mobile

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import grails.core.GrailsApplication

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Keeps label calls off the network. ZebraService.printDocument opens a raw TCP
 * socket to openboxes.barcode.printer.ipAddress:port (default 127.0.0.1:9100)
 * and ZebraService.renderDocument POSTs the ZPL to
 * openboxes.barcode.labelaryApi.url (default https://api.labelary.com/...).
 * Both are pointed at in-JVM stubs on ephemeral loopback ports: a TCP sink that
 * records what the "printer" received, and an HTTP server that answers a 1x1
 * PNG for the "labelary" render. The document label preview and viewer URLs
 * (openboxes.barcode.labelaryApi.documentPreviewUrl and viewerUrl) point at the
 * same HTTP stub. No dependency is added (JDK classes only).
 */
class LabelStubs {

    static final byte[] PNG_1X1 = Base64.decoder.decode(
            'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==')

    final List<String> printed = new CopyOnWriteArrayList<>()
    final List<String> rendered = new CopyOnWriteArrayList<>()
    ServerSocket printer
    HttpServer labelary
    Thread printerThread

    LabelStubs start() {
        printer = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())
        printerThread = Thread.start('test-fake-printer') {
            while (!printer.closed) {
                try {
                    Socket s = printer.accept()
                    s.withCloseable { printed << it.inputStream.getText('UTF-8') }
                } catch (IOException ignored) {
                    // closed
                }
            }
        }
        labelary = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
        labelary.createContext('/') { HttpExchange ex ->
            rendered << ex.requestBody.getText('UTF-8')
            ex.responseHeaders.add('Content-Type', 'image/png')
            ex.sendResponseHeaders(200, PNG_1X1.length)
            ex.responseBody.withCloseable { it.write(PNG_1X1) }
        }
        labelary.start()
        return this
    }

    String getLabelaryUrl() {
        return "http://127.0.0.1:${labelary.address.port}/v1/printers/12dpmm/labels/2x1/0/".toString()
    }

    /** Points the running app's config at the stubs; returns the previous values for restore(). */
    Map apply(GrailsApplication app) {
        Map before = [
                ip                : app.config.getProperty('openboxes.barcode.printer.ipAddress'),
                port              : app.config.getProperty('openboxes.barcode.printer.port', Integer),
                url               : app.config.getProperty('openboxes.barcode.labelaryApi.url'),
                documentPreviewUrl: app.config.getProperty('openboxes.barcode.labelaryApi.documentPreviewUrl'),
                viewerUrl         : app.config.getProperty('openboxes.barcode.labelaryApi.viewerUrl'),
        ]
        set(app, '127.0.0.1', printer.localPort, labelaryUrl)
        setLabelary(app, 'documentPreviewUrl', "http://127.0.0.1:${labelary.address.port}/v1/printers/8dpmm/labels/3x2/0/".toString())
        setLabelary(app, 'viewerUrl', "http://127.0.0.1:${labelary.address.port}/viewer.html".toString())
        // what ZebraService will actually read (navigable map access)
        assert app.config.openboxes.barcode.printer.port == printer.localPort
        assert app.config.openboxes.barcode.labelaryApi.url == labelaryUrl
        return before
    }

    void restore(GrailsApplication app, Map before) {
        set(app, before.ip as String, before.port as Integer, before.url as String)
        setLabelary(app, 'documentPreviewUrl', before.documentPreviewUrl)
        setLabelary(app, 'viewerUrl', before.viewerUrl)
    }

    /** Both views of the config: getProperty (flat keys) and the navigable map ZebraService reads. */
    static void set(GrailsApplication app, String ip, Integer port, String url) {
        app.config.put('openboxes.barcode.printer.ipAddress', ip)
        app.config.put('openboxes.barcode.printer.port', port)
        app.config.put('openboxes.barcode.labelaryApi.url', url)
        app.config.openboxes.barcode.printer.ipAddress = ip
        app.config.openboxes.barcode.printer.port = port
        app.config.openboxes.barcode.labelaryApi.url = url
    }

    /** One openboxes.barcode.labelaryApi option, in both views of the config (null removes it). */
    static void setLabelary(GrailsApplication app, String name, Object value) {
        String key = "openboxes.barcode.labelaryApi.${name}".toString()
        if (value == null) {
            app.config.remove(key)
            app.config.openboxes.barcode.labelaryApi.remove(name)
            return
        }
        app.config.put(key, value)
        app.config.openboxes.barcode.labelaryApi."${name}" = value
    }

    void stop() {
        printer?.close()
        labelary?.stop(0)
    }
}
