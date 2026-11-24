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
 * @author Sergey Nebolsin (nebolsin@prophotos.ru)
 * @author Finn Herpich (finn.herpich <at> marfinn-software <dot> de)
 */
class JQueryJavascriptProvider implements JavascriptProvider {

    /**
     * doRemoteFunction creates a jQuery-AJAX-Call
     *
     * @param taglib
     * @param attrs
     * @param out
     *
     * @return the jQuery-like formatted code for an AJAX-request
     */
    def doRemoteFunction(taglib, attrs, out) {
        // Optional, onLoad
        if (attrs.onLoading) {
            out << "${attrs.onLoading};"
        }

        // Start ajax
        out << /jQuery.ajax({/

        // Method
        def method = (attrs.method ? attrs.remove('method') : 'POST')
        out << "type:'$method'"

        // Optional, synchron call
        if ("false" == attrs.asynchronous) {
            out << ",async:false"
            attrs.remove('asynchronous')
        }

        // Optional, dataType to use
        if (attrs.dataType) {
            out << ",dataType:'${attrs.remove('dataType')}'"
        }

        // Additional attributes
        if (attrs.params || attrs.jsParams) {
            if (!(attrs?.params instanceof Map)) {
                // tags like remoteField don't deliver a map
                out << ",data:${attrs.remove('params')}"
            } else {
                out << ",data:{"

                boolean hasParams = false

                if (attrs?.params instanceof Map) {
                    hasParams = true
                    out << attrs.remove('params').collect { k, v ->
                        "\'" +
                                "${k}".encodeAsJavaScript() +
                                "\': \'" +
                                "${v}".encodeAsJavaScript() +
                                "\'"
                    }.join(",")
                }

                if (attrs?.jsParams instanceof Map) {
                    if (hasParams) {
                        out << ","
                    }

                    out << attrs.remove('jsParams').collect { k, v ->
                        "\'" +
                                "${k}".encodeAsJavaScript() +
                                "\': \'" +
                                "${v}".encodeAsJavaScript() +
                                "\'"
                    }.join(",")
                }

                out << "}"
            }
        }

        // build url
        def url = attrs.url ? taglib.createLink(attrs.remove('url')) : taglib.createLink(attrs)
        out << ", url:'${url}'"

        // Add callback
        buildCallback(attrs, out)

        // find all onX callback events
        def callbacks = attrs.findAll { k, v ->
            k ==~ /on(\p{Upper}|\d){1}\w+/
        }

        // remove all onX callback events
        callbacks.each { k, v ->
            attrs.remove(k)
        }

        out << "});"

        // Yeah, I know, return is not needed, but I like it
        return out
    }

    /**
     * Helper method to create callback object
     *
     * @param attrs Attributes to use for the callback
     * @param out Variable to attache the output
     */
    def buildCallback(attrs, out) {
        // TODO check for strlen
        if (out) {
            out << ','
        }

        //*** success
        out << 'success:function(data,textStatus){'

        if (attrs.onLoaded) {
            out << "${attrs.onLoaded};"
        }

        if (attrs.update instanceof Map) {
            if (attrs.update?.success) {
                out << "jQuery('#${attrs.update.success}').html(data);"
            }
        } else if (attrs.update) {
            out << "jQuery('#${attrs.update}').html(data);"
        }

        if (attrs.onSuccess) {
            out << "${attrs.onSuccess};"
        }

        out << '}'

        //*** failure
        out << ',error:function(XMLHttpRequest,textStatus,errorThrown){'

        if (attrs.update instanceof Map) {
            if (attrs.update?.failure) {
                // Applied to GRAILSPLUGINS-1919
                out << "jQuery('#${attrs.update?.failure}').html(XMLHttpRequest.responseText);"
            }
        }

        if (attrs.onFailure) {
            out << "${attrs.onFailure};"
        }

        out << '}'

        if (attrs.onComplete) {
            out << ",complete:function(XMLHttpRequest,textStatus){${attrs.onComplete}}"
        }
    }

    /**
     * Serializes the surrounding form.
     *
     * @param attrs attrs.params to serialize
     */
    def prepareAjaxForm(attrs) {
        // Fix for http://jira.codehaus.org/browse/GRAILSPLUGINS-1865
        if (attrs.forSubmitTag) {
            attrs.params = "jQuery(this).parents('form:first').serialize()".toString()
        } else {
            attrs.params = "jQuery(this).serialize()".toString()
        }
    }
}
