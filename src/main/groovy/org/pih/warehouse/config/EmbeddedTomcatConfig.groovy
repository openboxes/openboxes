/**
 * Copyright (c) 2025 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

package org.pih.warehouse.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configure the embedded Tomcat server to use the project's webapp directory for static resources.
 *
 * In some environments, user.dir is set to the Gradle daemon's working directory,
 * even when build.gradle specifies the workingDir. There seems to be a conflict
 * between Spring Boot, Gradle and JNA on Apple Silicon-based hosts. Here, we
 * look in catalina.base for a potential webapp directory and set docBase to that
 * directory, if it isn't already.
 *
 * Note that catalina.base is set by Spring Boot's server.tomcat.basedir property.
 */
 @Configuration
class EmbeddedTomcatConfig {

    private final static Logger log = LoggerFactory.getLogger(EmbeddedTomcatConfig)

    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer() {

        return { container ->
            if (container instanceof TomcatEmbeddedServletContainerFactory) {

                TomcatContextCustomizer customizer = new TomcatContextCustomizer() {
                    @Override
                    void customize(org.apache.catalina.Context context) {
                        try {
                            def projectDocBase = new File("${System.getProperty('catalina.base')}/src/main/webapp")
                            if (projectDocBase.exists()) {
                                if (projectDocBase.absolutePath != context.docBase) {
                                    log.info("Changing docBase from ${context.docBase} to ${projectDocBase.absolutePath}")
                                    context.docBase = projectDocBase.absolutePath
                                } else {
                                    log.debug("docBase ${context.docBase} appears correctly configured")
                                }
                            } else {
                                log.warn("cannot find project static resources: leaving docBase as-is ${context.docBase}")
                            }
                        } catch (SecurityException e) {
                            log.error("Security exception accessing webapp directory: ${e.message}", e)
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid argument setting docBase: ${e.message}", e)
                        } catch (IllegalStateException e) {
                            log.error("Tomcat context in invalid state: ${e.message}", e)
                        }
                    }
                }

                container.addContextCustomizers(customizer)
            }
        }
    }
}