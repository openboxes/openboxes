// Vendored from org.grails.plugins:ajax-tags (Apache-2.0); the plugin has no Grails 7 release.
// Source: github.com/grails-plugins/ajax-tags, branch 8.0.x, commit f715950b03c5. Unmodified except this header and the package.
// Full license text: grails-app/taglib/org/pih/warehouse/ajaxtags/LICENSE.txt
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

import org.grails.encoder.CodecLookup
import org.grails.encoder.Encoder
import org.springframework.beans.factory.annotation.Autowired

class AjaxTagLib {

    static encodeAsForTags = [remoteFunction: 'raw',
                              remoteLink    : 'raw',
                              remoteField   : 'raw'
    ]

    @Autowired(required = false)
    JavascriptProvider provider = new JQueryJavascriptProvider()

    @Autowired
    CodecLookup codecLookup

    Closure formRemote = { attrs, body ->
        if (!attrs.name) {
            throwTagError("Tag [formRemote] is missing required attribute [name]")
        }
        if (!attrs.url) {
            throwTagError("Tag [formRemote] is missing required attribute [url]")
        }
        if (attrs.params != null) {
            throwTagError("Tag [formRemote] does not support the [params] attribute - add a 'params' key to the [url] attribute instead.")
        }

        def url = attrs.url
        if (!(url instanceof CharSequence)) {
            url = deepClone(attrs.url)
        }

        // prepare form settings
        provider.prepareAjaxForm(attrs)

        def params = [onsubmit: remoteFunction(attrs) + 'return false',
                      method  : (attrs.method ? attrs.method : 'post'),
                      action  : (attrs.action ? attrs.action : url instanceof CharSequence ? url.toString() : createLink(url))]
        attrs.remove('url')
        params.putAll(attrs)
        if (params.name && !params.id) {
            params.id = params.name
        }

        // The <form> element shouldn't have a 'name' attribute.
        // See http://jira.codehaus.org/browse/GRAILS-2839
        params.remove 'name'

        writeTag('form', params, body)
    }

    /**
     * Creates a remote function call.
     *
     * @emptyTag
     *
     * @attr before The javascript function to call before the remote function call
     * @attr after The javascript function to call after the remote function call
     * @attr update Either a map containing the elements to update for 'success' or 'failure' states, or a string with the element to update in which cause failure events would be ignored
     * @attr action the name of the action to use in the link, if not specified the default action will be linked
     * @attr controller the name of the controller to use in the link, if not specified the current controller will be linked
     * @attr id The id to use in the link
     * @attr asynchronous Whether to do the call asynchronously or not (defaults to true, specified in 'options' array)
     * @attr method The method to use the execute the call (defaults to "post")
     */
    Closure remoteFunction = { attrs ->
        // before remote function
        def after = ''
        if (attrs.before) {
            out << "${attrs.remove('before')};"
        }
        if (attrs.after) {
            after = "${attrs.remove('after')};"
        }

        provider.doRemoteFunction(owner, attrs, out)
        attrs.remove('update')
        // after remote function
        if (after) {
            out << after
        }
    }


    /**
     * Creates a link to a remote uri that can be invoked via ajax.
     *
     * @attr update Either a map containing the elements to update for 'success' or 'failure' states, or a string with the element to update in which cause failure events would be ignored
     * @attr before The javascript function to call before the remote function call
     * @attr after The javascript function to call after the remote function call
     * @attr asynchronous Whether to do the call asynchronously or not (defaults to true)
     * @attr method The method to use the execute the call (defaults to "post")
     * @attr controller The name of the controller to use in the link, if not specified the current controller will be linked
     * @attr action The name of the action to use in the link, if not specified the default action will be linked
     * @attr uri relative URI
     * @attr url A map containing the action,controller,id etc.
     * @attr base Sets the prefix to be added to the link target address, typically an absolute server URL. This overrides the behaviour of the absolute property, if both are specified.
     * @attr absolute If set to "true" will prefix the link target address with the value of the grails.serverURL property from Config, or http://localhost:&lt;port&gt; if no value in Config and not running in production.
     * @attr id The id to use in the link
     * @attr fragment The link fragment (often called anchor tag) to use
     * @attr params A map containing URL query parameters
     * @attr mapping The named URL mapping to use to rewrite the link
     * @attr elementId the DOM element id
     */
    Closure remoteLink = { attrs, body ->
        Encoder encoder = getHtmlEncoder()
        out << '<a href="'

        def cloned = deepClone(attrs)
        out << createLink(cloned)

        out << '" onclick="'
        // create remote function
        out << remoteFunction(attrs)
        out << 'return false;"'

        // These options should not be included as attributes of the anchor element.
        attrs.remove('method')
        attrs.remove('url')
        attrs.remove('action')
        attrs.remove('controller')

        // handle elementId like link
        def elementId = attrs.remove('elementId')
        if (elementId) {
            out << " id=\"${encoder.encode(elementId)}\""
        }

        // process remaining attributes
        attrs.each { k, v ->
            out << ' ' << encoder.encode(k) << "=\"" << encoder.encode(v) << "\""
        }
        out << ">"
        // output the body
        out << body()

        // close tag
        out << "</a>"
    }

    /**
     * A field that sends its value to a remote link.
     *
     * @emptyTag
     *
     * @attr name REQUIRED the name of the field
     * @attr value The initial value of the field
     * @attr paramName The name of the parameter send to the server
     * @attr action the name of the action to use in the link, if not specified the default action will be linked
     * @attr controller the name of the controller to use in the link, if not specified the current controller will be linked
     * @attr id The id to use in the link
     * @attr update Either a map containing the elements to update for 'success' or 'failure' states, or a string with the element to update in which cause failure events would be ignored
     * @attr before The javascript function to call before the remote function call
     * @attr after The javascript function to call after the remote function call
     * @attr asynchronous Whether to do the call asynchronously or not (defaults to true)
     * @attr method The method to use the execute the call (defaults to "post")
     */
    Closure remoteField = { attrs, body ->
        Encoder encoder = getHtmlEncoder()
        def paramName = attrs.paramName ? attrs.remove('paramName') : 'value'
        def value = attrs.remove('value')
        if (!value) value = ''

        out << "<input type=\"text\" name=\"${encoder.encode(attrs.remove('name'))}\" value=\"${encoder.encode(value)}\" onkeyup=\""

        if (attrs.params) {
            if (attrs.params instanceof Map) {
                attrs.params[paramName] = new JavascriptValue('this.value')
            } else {
                attrs.params += "+'${paramName}='+this.value"
            }
        } else {
            attrs.params = "'${paramName}='+this.value"
        }
        out << remoteFunction(attrs)
        attrs.remove('params')
        out << "\""
        attrs.remove('url')
        attrs.each { k, v ->
            out << ' ' << encoder.encode(k) << "=\"" << encoder.encode(v) << "\""
        }
        out << " />"
    }

    /**
     * Creates a form submit button that submits the current form to a remote ajax call.
     *
     * @attr url The url to submit to, either a map contraining keys for the action,controller and id or string value
     * @attr update Either a map containing the elements to update for 'success' or 'failure' states, or a string with the element to update in which cause failure events would be ignored
     * @attr before The javascript function to call before the remote function call
     * @attr after The javascript function to call after the remote function call
     * @attr asynchronous Whether to do the call asynchronously or not (defaults to true)
     * @attr method The method to use the execute the call (defaults to "post")
     */
    Closure submitToRemote = { attrs, body ->
        // get javascript provider
        // prepare form settings
        attrs.forSubmitTag = ".form"
        provider.prepareAjaxForm(attrs)
        def params = [onclick: remoteFunction(attrs) + 'return false',
                      type   : 'button',
                      name   : attrs.remove('name'),
                      value  : attrs.remove('value'),
                      id     : attrs.remove('id'),
                      'class': attrs.remove('class')]

        writeTag('input', params, body)
    }

    /**
     * Writes an element and its body, skipping attributes with falsy values.
     *
     * This does what the g:withTag helper does, but inline. withTag is both a method on
     * ApplicationTagLib and a tag in the 'g' namespace, so calling it from another taglib
     * resolves either to the method - which writes to out and returns the writer - or to
     * the injected tag, which captures its output and returns it instead. Which one you
     * get depends on call order, so neither 'withTag(..)' nor 'out << withTag(..)' is
     * correct in both cases.
     */
    private void writeTag(String name, Map attrs, Closure body) {
        out << "<$name"
        attrs.each { k, v ->
            if (!v) return
            if (v instanceof Closure) {
                out << " $k=\""
                v()
                out << '"'
            } else {
                out << " $k=\"$v\""
            }
        }
        out << '>'
        out << body()
        out << "</$name>"
    }

    private Encoder getHtmlEncoder() {
        if (!codecLookup) {
            throwTagError("The codecLookup property in AjaxTagLib has not been initialized.")
        }
        Encoder encoder = codecLookup.lookupEncoder('HTML')
        if (!encoder) {
            throwTagError("HTML Encoder could not be loaded.")
        }
        encoder
    }

    /**
     * Normal map implementation does a shallow clone. This implements a deep clone for maps
     * using recursion.
     */
    private deepClone(Map map) {
        def cloned = [:]
        map.each { k, v ->
            if (v instanceof Map) {
                cloned[k] = deepClone(v)
            } else {
                cloned[k] = v
            }
        }
        cloned
    }
}

class JavascriptValue {
    def value

    JavascriptValue(value) {
        this.value = value
    }

    String toString() { "'+$value+'" }
}

