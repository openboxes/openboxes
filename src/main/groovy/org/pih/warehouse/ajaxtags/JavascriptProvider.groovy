// Vendored from org.grails.plugins:ajax-tags (Apache-2.0); the plugin has no Grails 7 release.
// Source: github.com/grails-plugins/ajax-tags, branch 8.0.x, commit f715950b03c5. Unmodified except this header and the package.
// Full license text: src/main/resources/META-INF/licenses/ajax-tags/LICENSE.txt
/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pih.warehouse.ajaxtags

/**
 * Defines methods that a JavaScript provider must implement.
 *
 * @author Graeme Rocher
 */
interface JavascriptProvider {

    /**
     * Creates a remote function call
     *
     * @param The attributes to use
     * @param The output to write to
     */
    def doRemoteFunction(taglib,attrs, out)

    def prepareAjaxForm(attrs)
}
