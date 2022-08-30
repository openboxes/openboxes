/**
 * Copyright (c) 2022 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse

/**
 * Methods for controlling word and line breaks when rendering tabular data.
 */
class WordWrapTagLib {

    static String namespace = 'wordwrap'

    /**
     * Enclose `body` in an html element that wraps only on spaces.
     *
     * TIL you can't (currently) ask CSS to do this directly. Its default
     * word-wrapping behavior allows breaks at slashes and hyphens,
     * which can make a mess of tabular output containing dates and ID's.
     *
     * If we enclose each word in a span that says "do not wrap me", then
     * join said spans with spaces, we get consistent behavior at the cost
     * of somewhat unwieldy markup.
     *
     * https://developer.mozilla.org/en-US/docs/Web/CSS/white-space
     * https://stackoverflow.com/questions/18136684/break-lines-by-whitespace-only
     *
     * P.S. This implementation assumes that `body` contains no nested tags.
     */
    void wordWrapTagContents(String tagName, Map attrs, Closure body) {

        List<GString> words = body()?.toString()?.split()?.collect {word ->
            "<span style='display: inline-block; white-space: nowrap;'>${word}</span>"
        }

        String tagAttrs = attrs.collect {
            " ${it.key}='${it.value}'"
        }.join()

        out << "<${tagName}${tagAttrs}>" << words.join(' ') << "</${tagName}>"
    }

    /**
     * Enclose `body` in a <div> element that wraps only on spaces.
     */
    def div = { attrs, body ->
        wordWrapTagContents('div', attrs, body)
    }

    /**
     * Enclose `body` in a <td> element that wraps only on spaces.
     */
    def td = { attrs, body ->
        wordWrapTagContents('td', attrs, body)
    }
}
