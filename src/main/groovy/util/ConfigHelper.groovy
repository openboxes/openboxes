/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package util

import grails.util.Holders
import groovy.json.JsonOutput
import org.springframework.boot.info.GitProperties

import org.pih.warehouse.core.parser.DoubleParser

// See http://jira.codehaus.org/browse/GRAILS-6515
class ConfigHelper {

    /**
     * Figure out the branch name from bamboo env. vars or Spring Boot.
     */
    static String getBranchName(GitProperties gitProperties) {

        String customRevision = Holders.grailsApplication.metadata.getProperty('build.git.revision.custom')
        String defaultBranch = Holders.grailsApplication.metadata.getProperty('build.git.branch.default')
        String sha = Holders.grailsApplication.metadata.getProperty('build.git.sha')

        if (customRevision) {
            /*
             * When using bamboo's "Run customized build", defaultBranch
             * is misleading (it tells us the branch bamboo checks out by
             * default, not the actual branch used for a customized build).
             */
            if (sha?.startsWith(customRevision)) {
                // get branch info elsewhere, as customRevision is a sha
                return gitProperties.branch
            }
            return customRevision
        }

        /*
         * Absent signs of a custom build, get the branch name from bamboo's
         * default branch name, if present, or Spring Boot's git integration.
         */
        return defaultBranch ?: gitProperties.branch
    }

    static getContextPath() {
        String contextPath = Holders.grailsApplication.config.server.contextPath
        return (contextPath != '/') ? contextPath : ''
    }

    /**
     * Fetch the frontend (React web) Sentry config as JSON.
     *
     * We don't pass these settings via ApiController.getAppContext() because we need it as early as possible so that
     * we can capture data from the true start of the session. See webpack.config.js for usages.
     */
    static String getSentryWebConfigJson() {
        DoubleParser doubleParser = new DoubleParser()
        Map config = [
                dsn: Holders.config.getProperty('sentry.web.dsn') ?: null,
                environment: Holders.config.getProperty('sentry.web.environment') ?: null,
                tracesSampleRate: doubleParser.parse(Holders.config.getProperty('sentry.web.tracesSampleRate')),
                replaysSampleRate: doubleParser.parse(Holders.config.getProperty('sentry.web.replaysSampleRate')),
                replaysErrorSampleRate: doubleParser.parse(Holders.config.getProperty('sentry.web.replaysErrorSampleRate')),
        ]
        // Escape "</" so that config values can't close the <script> tag that the JSON is wrapped with.
        return JsonOutput.toJson(config).replace('</', '<\\/')
    }

    static booleanValue(def value) {
        if (value.class == java.lang.Boolean) {
            // because 'true.toBoolean() == false' !!!
            return value
        } else {
            return value.toBoolean()
        }
    }

    static listValue(def value) {
        if (value instanceof java.lang.String) {
            return value?.split(",")
        } else {
            return value
        }

    }

    static findAccessRule(String controllerName, String actionName) {
        def rules = Holders.config.openboxes.security.rbac.rules

        ArrayList<Closure> filters = [
            { it.controller == controllerName && it.actions.contains(actionName) },
            { it.controller == controllerName && it.actions.contains("*") },
            { it.controller == "*" && it.actions.contains("*") }
        ]
        // Find the rules for each filter and find the first non empty from the top (filters on the top are higher priority)
        def rule = filters
                    .collect { rules.findAll(it) }
                    .find { it.size() != 0 }

        // If there is more than one rule specified for the same controller and action, determine which Role is "higher" by sortOrder of RoleType enum, and return the "higher" one
        return rule?.sort { it?.accessRules?.minimumRequiredRole?.sortOrder }?.first()
    }

    static Date getMinimumExpirationDate() {
        return Holders.config.getProperty("openboxes.expirationDate.minValue", Date.class)
    }
}
