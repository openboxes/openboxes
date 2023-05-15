package org.pih.warehouse

import grails.util.Holders


class GoogleAnalyticsTagLib {

    static namespace = "g"

    /**
     * Injecting gtag tracking code to the tamplate
     * https://developers.google.com/tag-platform/devguides/add-tag?solution=gtag
     * */
    def googleSiteTag = {attrs ->
        if (!getTrackingIds()) {
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
     * Get tracking ids from config file. Tracking ids can be a single tracking id as a string or multiple
     * tracking ids as a list of strings
     * (https://developers.google.com/analytics/devguides/collection/gtagjs#configure_additional_google_analytics_properties)
     * */
    private getTrackingIds() {
        return Holders.config.google.analytics.webPropertyID
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

    /**
     * Inject configuration for each tracking id to the google site tag tracking code:
     * gtag('config', TRACKING_ID_1);
     * gtag('config', TRACKING_ID_2);
     * ...
     * */
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
