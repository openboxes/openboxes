package org.pih.warehouse

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class GoogleAnalyticsTagLib {

    static namespace = "g"

    /**
    * Injecting gtag:
        <!-- Google tag (gtag.js) -->
        <script async src="https://www.googletagmanager.com/gtag/js?id=TRACKING_ID"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());

            gtag('config', TRACKING_ID);
        </script>
    * */
    def gtag = {attrs ->
        if (!getTrackingIds()) {
            out << ""
            return
        }

        out << """
            <!-- Google tag (gtag.js) -->
            <script async src="https://www.googletagmanager.com/gtag/js?id=${getMainTrackingId()}"></script>
            <script>
                window.dataLayer = window.dataLayer || [];
                function gtag(){dataLayer.push(arguments);}
                gtag('js', new Date());
                
                ${injectConfig()}
            </script>
        """
    }

    /**
     * Get tracking ids from config file. Tracking ids can be a single tracking id as string or multiple
     * tracking ids as list of strings
     * */
    private getTrackingIds() {
        return ConfigurationHolder.config.google.gtag.trackingIds
    }

    /**
     * In case there is a list of tracking ids, we have to pull the main one for the GTM (first script from gtag
     * tracking code)
     * */
    private getMainTrackingId() {
        def mainTrackingId = ""
        def trackingIds = getTrackingIds()

        if (trackingIds instanceof String) {
            mainTrackingId = trackingIds
        } else if (trackingIds instanceof List && trackingIds.size() > 0) {
            mainTrackingId = trackingIds.first()
        }

        return mainTrackingId
    }

    private injectConfig() {
        def gtagConfig = ""
        def trackingIds = getTrackingIds()

        if (trackingIds instanceof String) {
            gtagConfig = "gtag('config', '${trackingIds}');"
        } else if (trackingIds instanceof List) {
            trackingIds.each { gtagConfig += "gtag('config', '${it}');\n" }
        }

        return gtagConfig
    }
}
