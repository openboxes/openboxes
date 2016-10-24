/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.analytics

import io.keen.client.java.JavaKeenClientBuilder
import io.keen.client.java.KeenClient
import io.keen.client.java.KeenLogging
import io.keen.client.java.KeenProject

class KeenService {

    boolean transactional = true

    def grailsApplication

    def initialize() {
        KeenClient client = new JavaKeenClientBuilder().build();

        String projectId = grailsApplication.config.openboxes.keenio.projectId
        String readKey = grailsApplication.config.openboxes.keenio.readKey
        String writeKey = grailsApplication.config.openboxes.keenio.writeKey

        // Move these properties to Config.groovy
        KeenProject project = new KeenProject(projectId, readKey, writeKey);
        client.setDefaultProject(project);

        KeenLogging.enableLogging()
        KeenClient.initialize(client);

    }

    /**
     * Add an event to the default keen project.
     *
     * @param eventCollection
     * @param event
     * @return
     */
    def recordEvent(String eventCollection, Map event) {
        KeenClient.client().addEventAsync(eventCollection, event);
    }



}
