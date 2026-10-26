package org.pih.warehouse.api.regression.mobile

import groovy.json.JsonSlurper

/** One HTTP exchange made the way the app makes it, plus the contract check. */
class CallResult {

    String id
    String method
    String url
    int status
    String contentType
    String text
    Object json
    /** Set when the server answered 3xx and the call followed it like the app's XHR does. */
    String redirectedFrom

    static CallResult of(String id, String method, String url, int status, String contentType, String text) {
        Object json = null
        if (text && (contentType?.contains('json') || text.trim().startsWith('{') || text.trim().startsWith('['))) {
            try {
                json = new JsonSlurper().parseText(text)
            } catch (Exception ignored) {
                json = null
            }
        }
        return new CallResult(id: id, method: method, url: url, status: status, contentType: contentType, text: text, json: json)
    }

    /**
     * The same exchange with `data` cut down to the rows a feature made. The
     * database is shared by every spec of the run and never cleaned, so a list
     * also carries other specs' rows, built for other purposes; the contract is
     * checked on this feature's own rows (there must be at least one).
     */
    CallResult rows(Closure<Boolean> keep) {
        if (!(json instanceof Map) || !((json as Map).data instanceof List)) {
            return this
        }
        List kept = ((json as Map).data as List).findAll(keep)
        assert kept: "${id}: none of the feature's own rows came back: ${this}"
        Map copy = [*:(json as Map), data: kept]
        return new CallResult(id: id, method: method, url: url, status: status, contentType: contentType, text: text,
                json: copy, redirectedFrom: redirectedFrom)
    }

    Map getData() {
        return json instanceof Map ? (json as Map) : null
    }

    List<String> problems(Collection<String> emptyOk = [], Collection<String> except = []) {
        return ContractCheck.check(MobileContract.entry(id), status, json, emptyOk, except)
    }

    /**
     * Fails with every contract problem at once (route, status class, fields and
     * their JSON kinds). `except` names field paths (prefixes) left out: a
     * feature that asserts everything except a known mismatch, next to a
     * @PendingFeature that asserts the mismatch itself.
     */
    void holds(Collection<String> emptyOk = [], Collection<String> except = []) {
        List<String> p = problems(emptyOk, except)
        if (!p.isEmpty()) {
            throw new AssertionError("${id}: ${method} ${url} -> ${status}\n  " + p.join('\n  ') +
                    "\n  body: ${text?.take(1500)}")
        }
    }

    String toString() {
        return "${method} ${url}${redirectedFrom ? " (followed ${redirectedFrom})" : ''} -> ${status} ${text?.take(300)}"
    }
}
