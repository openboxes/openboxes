package util

import grails.util.Holders

import javax.servlet.http.HttpServletRequest

class RedirectUtil {

    /**
     * Sends the user back to the page they came from (the request's Referer) when that page is inside this
     * application; otherwise to the stock card of the product, the location list, or the home page.
     */
    static void redirect(controller, context) {
        String referer = controller.request.getHeader("Referer")
        if (isApplicationUrl(controller.request, referer)) {
            controller.redirect(url: referer)
            return
        }

        if (context?.product) {
            controller.redirect(controller: "inventoryItem", action: "showStockCard", id: context?.product?.id)
        } else if (context?.location) {
            controller.redirect(controller: "location", action: "list")
        } else {
            controller.redirect(uri: "/")
        }
    }

    /**
     * True when url points at a page of this application: an http(s) URL on one of the application's hosts, or a
     * path without scheme and host, and in both cases (for an absolute path) under the application's context path.
     *
     * The application's hosts are the request's server name and the host of grails.serverURL when it is
     * configured.
     */
    static boolean isApplicationUrl(HttpServletRequest request, String url) {
        if (!url) {
            return false
        }
        // The query and fragment do not decide where the URL leads, and may hold characters URI does not parse
        int end = url.length()
        ['?', '#'].each { String separator ->
            int index = url.indexOf(separator)
            if (index >= 0 && index < end) {
                end = index
            }
        }
        String target = url.substring(0, end)
        // Browsers read a backslash as a slash, and a path starting with two slashes as a host. Encoded dots,
        // slashes and backslashes are refused too: a browser may resolve them to another path
        // Path parameters (";") are refused: the server may drop them and resolve the path differently
        if (target.contains('\\') || target.contains(';') || target.startsWith('//') || target =~ /(?i)%(2e|2f|5c|3b)/) {
            return false
        }
        URI uri
        try {
            uri = new URI(target)
        } catch (URISyntaxException ignored) {
            return false
        }
        if (uri.opaque) {
            return false
        }
        if (uri.scheme) {
            boolean isWeb = uri.scheme.toLowerCase() in ['http', 'https']
            // exactly "scheme://host", not "scheme:///host" or "scheme:/host"
            boolean hasHost = target.substring(uri.scheme.length()).startsWith('://') &&
                    !target.substring(uri.scheme.length() + 3).startsWith('/')
            String host = hostOf(uri)
            if (!isWeb || !hasHost || !host || !applicationHosts(request).contains(host)) {
                return false
            }
        } else if (uri.rawAuthority) {
            return false
        }
        // The path as the browser resolves it: without "." and ".." segments
        String path = uri.normalize().rawPath ?: ''
        if (uri.scheme && !path) {
            path = '/'
        } else if (!path) {
            return false
        } else if (!path.startsWith('/')) {
            // A relative path stays on the current page's host; it must not climb out of the current path
            return !uri.scheme && path != '..' && !path.startsWith('../')
        }
        String contextPath = request.contextPath ?: ''
        return !contextPath || path == contextPath || path.startsWith(contextPath + '/')
    }

    /**
     * The URI's host in lower case. java.net.URI leaves the host out for names it does not accept as a host name
     * (an underscore, for example); the host is then taken from the authority, without user info and port.
     */
    static String hostOf(URI uri) {
        String host = uri.host
        if (!host && uri.rawAuthority) {
            String authority = uri.rawAuthority
            authority = authority.substring(authority.lastIndexOf('@') + 1)
            host = authority.startsWith('[') ? authority : authority.replaceFirst(/:\d*$/, '')
        }
        return host ? host.toLowerCase() : null
    }

    private static Set<String> applicationHosts(HttpServletRequest request) {
        Set<String> hosts = [] as Set
        if (request.serverName) {
            hosts << request.serverName.toLowerCase()
        }
        String serverUrl = Holders.config?.getProperty('grails.serverURL', String)
        if (serverUrl) {
            try {
                String host = hostOf(new URI(serverUrl))
                if (host) {
                    hosts << host
                }
            } catch (URISyntaxException ignored) {
                // an unusable serverURL adds no host
            }
        }
        return hosts
    }
}
