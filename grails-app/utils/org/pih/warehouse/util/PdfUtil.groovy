/**
 * Copyright (c) 2023 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.util

import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfName
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PdfUtil {

    private static final Logger log = LoggerFactory.getLogger(PdfUtil)

    static final Map PDF_BASE14_FONTS = [
        (BaseFont.COURIER)              : PdfName.COURIER,
        (BaseFont.COURIER_BOLD)         : PdfName.COURIER_BOLD,
        (BaseFont.COURIER_BOLDOBLIQUE)  : PdfName.COURIER_BOLDOBLIQUE,
        (BaseFont.COURIER_OBLIQUE)      : PdfName.COURIER_OBLIQUE,
        (BaseFont.HELVETICA)            : PdfName.HELVETICA,
        (BaseFont.HELVETICA_BOLD)       : PdfName.HELVETICA_BOLD,
        (BaseFont.HELVETICA_BOLDOBLIQUE): PdfName.HELVETICA_BOLDOBLIQUE,
        (BaseFont.HELVETICA_OBLIQUE)    : PdfName.HELVETICA_OBLIQUE,
        (BaseFont.SYMBOL)               : PdfName.SYMBOL,
        (BaseFont.TIMES_BOLD)           : PdfName.TIMES_BOLD,
        (BaseFont.TIMES_BOLDITALIC)     : PdfName.TIMES_BOLDITALIC,
        (BaseFont.TIMES_ITALIC)         : PdfName.TIMES_ITALIC,
        (BaseFont.TIMES_ROMAN)          : PdfName.TIMES_ROMAN,
        (BaseFont.ZAPFDINGBATS)         : PdfName.ZAPFDINGBATS,
    ]

    /**
     * Ensure that BaseFont.BuiltinFonts14 contains all the fonts it should.
     *
     * XDocReport may remove some of these fonts when it runs (OBPIH-5426).
     */
    static void restoreBaseFonts() {
        int numRestored = 0

        PDF_BASE14_FONTS.each { k, v ->
            if (!BaseFont.BuiltinFonts14.containsKey(k)) {
                BaseFont.BuiltinFonts14[k] = v
                numRestored += 1
            }
        }

        if (numRestored) {
            log.info "restored ${numRestored} missing base fonts (PDF)"
        }
    }
}
