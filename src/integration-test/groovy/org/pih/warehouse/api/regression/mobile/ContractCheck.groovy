package org.pih.warehouse.api.regression.mobile

/**
 * Checks one response against a contract entry's `response`: the status class
 * the app treats as success, and every field the app reads (confidence high or
 * medium; low = TypeScript type only, not asserted) against its JSON kinds.
 *
 * Path syntax (as in the contract): `a.b` nests, `a[]` every element, `a[0]`
 * the first element, `a["b.c"]` one flat key containing dots. A value under a
 * null or missing parent is not checked (the app reads it through `?.`, and the
 * parent's own entry, when listed, carries the parent's type). An empty array
 * on the way to a leaf is a problem unless `emptyOk` names its path prefix: the
 * fixtures exist so that every list has an element to check.
 */
class ContractCheck {

    static final Object ABSENT = new Object() { String toString() { 'absent' } }

    static List<String> check(Map entry, int status, Object body, Collection<String> emptyOk = [], Collection<String> except = []) {
        List<String> problems = []
        Map response = entry.response as Map
        String expected = response.expectedStatus
        if (expected == '2xx' && !(status >= 200 && status < 300)) {
            problems << "status ${status}, the app expects ${expected}".toString()
            return problems
        }
        if (response.envelope == 'none') {
            return problems
        }
        if (body == null) {
            problems << 'response body is not JSON'.toString()
            return problems
        }
        if (response.parseResponse && body instanceof Map && (body as Map).containsKey('data')) {
            body = [*:(body as Map), data: expandFlatKeys((body as Map).data)]
        }
        (response.fields as List<Map>).findAll { it.confidence != 'low' && !except.any { String x -> (it.path as String).startsWith(x) } }.each { Map field ->
            problems.addAll(checkField(body, field.path as String, field.type as String, emptyOk))
        }
        return problems
    }

    /** The app's src/utils/utils.ts parseResponse: lodash _.set of every key, so "a.b" becomes a: {b}. */
    static Object expandFlatKeys(Object v) {
        if (v instanceof List) {
            return (v as List).collect { expandFlatKeys(it) }
        }
        if (v instanceof Map) {
            Map out = [:]
            (v as Map).each { k, val ->
                List<String> parts = (k as String).split('\\.') as List
                Map cur = out
                parts.eachWithIndex { String part, int i ->
                    if (i == parts.size() - 1) {
                        cur[part] = expandFlatKeys(val)
                    } else {
                        if (!(cur[part] instanceof Map)) {
                            cur[part] = [:]
                        }
                        cur = cur[part] as Map
                    }
                }
            }
            return out
        }
        return v
    }

    static List<String> tokens(String path) {
        List<String> out = []
        def m = path =~ /\.?([A-Za-z_$][A-Za-z0-9_$]*)|\[\]|\[0\]|\["([^"]+)"\]/
        int consumed = 0
        while (m.find()) {
            if (m.start() != consumed) {
                throw new IllegalArgumentException("bad field path ${path}")
            }
            consumed = m.end()
            String whole = m.group(0)
            if (whole == '[]' || whole == '[0]') {
                out << whole
            } else if (m.group(2) != null) {
                out << ('.' + m.group(2))
            } else {
                out << ('.' + m.group(1))
            }
        }
        if (consumed != path.length()) {
            throw new IllegalArgumentException("bad field path ${path}")
        }
        return out
    }

    static String kind(Object v) {
        if (v.is(ABSENT)) return 'absent'
        if (v == null || v.getClass().name == 'org.grails.web.json.JSONObject$Null') return 'null'
        if (v instanceof Map) return 'object'
        if (v instanceof List) return 'array'
        if (v instanceof CharSequence) return 'string'
        if (v instanceof Number) return 'number'
        if (v instanceof Boolean) return 'boolean'
        return v.getClass().simpleName
    }

    static List<String> checkField(Object body, String path, String type, Collection<String> emptyOk) {
        List<String> problems = []
        Set<String> allowed = type.split('\\|') as Set
        List<Object> values = [body]
        String walked = ''
        boolean leafChecked = false
        for (String t : tokens(path)) {
            List<Object> next = []
            for (Object v : values) {
                String k = kind(v)
                if (k == 'null' || k == 'absent') {
                    continue // under a null/missing parent: not checked here
                }
                if (t == '[]' || t == '[0]') {
                    if (k != 'array') {
                        problems << "${path}: ${walked ?: '(body)'} is ${k}, the app reads it as an array".toString()
                        continue
                    }
                    List list = v as List
                    if (list.isEmpty() && !emptyOk.any { walked.startsWith(it) || path.startsWith(it) }) {
                        problems << "${path}: ${walked ?: '(body)'} is an empty array, nothing to check (fixture gap)".toString()
                    }
                    next.addAll(t == '[0]' ? list.take(1) : list)
                } else {
                    String key = t.substring(1)
                    if (k != 'object') {
                        problems << "${path}: ${walked ?: '(body)'} is ${k}, the app reads .${key} on it".toString()
                        continue
                    }
                    Map m = v as Map
                    next << (m.containsKey(key) ? m[key] : ABSENT)
                }
            }
            walked += t
            values = next
        }
        for (Object leaf : values) {
            leafChecked = true
            String k = kind(leaf)
            if (!allowed.contains(k)) {
                String shown = leaf instanceof CharSequence || leaf instanceof Number || leaf instanceof Boolean ? " (${leaf})" : ''
                problems << "${path}: ${k}${shown}, the app expects ${type}".toString()
            }
        }
        return problems.unique()
    }
}
