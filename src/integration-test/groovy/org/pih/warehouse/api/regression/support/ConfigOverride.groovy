package org.pih.warehouse.api.regression.support

import grails.config.Config
import grails.core.GrailsApplication

/**
 * Temporarily overrides runtime config keys in the RUNNING app (the integration-test app
 * context is shared by every spec in the JVM, so every override must be restored).
 *
 * The value is written through the navigable map (what `config.openboxes.x.y` reads) and
 * verified through getProperty (what `config.getProperty('openboxes.x.y', T)` reads), so a
 * spec fails loudly instead of silently testing the default.
 */
class ConfigOverride {

    private final GrailsApplication grailsApplication
    private final Map<String, Object> originals = [:]

    ConfigOverride(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
    }

    ConfigOverride set(String dottedKey, Object value) {
        Config config = grailsApplication.config
        if (!originals.containsKey(dottedKey)) {
            originals[dottedKey] = config.getProperty(dottedKey, Object)
        }
        write(config, dottedKey, value)
        Object readBack = config.getProperty(dottedKey, Object)
        assert String.valueOf(readBack) == String.valueOf(value): "config override of ${dottedKey} did not take (read back ${readBack})"
        return this
    }

    void restore() {
        Config config = grailsApplication.config
        originals.each { String key, Object value -> write(config, key, value) }
        originals.clear()
    }

    private static void write(Config config, String dottedKey, Object value) {
        List<String> parts = dottedKey.tokenize('.')
        Object node = config
        parts[0..-2].each { String p ->
            Object next = node[p]
            if (!(next instanceof Map)) {
                node[p] = [:]
                next = node[p]
            }
            node = next
        }
        node[parts[-1]] = value
        // Some Config implementations keep a flattened view as well.
        try {
            config.put(dottedKey, value)
        } catch (Exception ignored) {
            // navigable write above is the one that matters
        }
    }
}
