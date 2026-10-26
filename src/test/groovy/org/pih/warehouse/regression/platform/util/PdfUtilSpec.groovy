package org.pih.warehouse.regression.platform.util

import org.pih.warehouse.util.*
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfName
import spock.lang.Specification

class PdfUtilSpec extends Specification {

    Map originalFonts

    void setup() {
        originalFonts = new HashMap(BaseFont.BuiltinFonts14)
    }

    void cleanup() {
        BaseFont.BuiltinFonts14.clear()
        BaseFont.BuiltinFonts14.putAll(originalFonts)
    }

    void 'the expected font list covers all fourteen PDF base fonts'() {
        expect:
        assert PdfUtil.PDF_BASE14_FONTS.size() == 14
        assert PdfUtil.PDF_BASE14_FONTS[BaseFont.HELVETICA] == PdfName.HELVETICA
    }

    void 'restoreBaseFonts should put back base fonts that were removed'() {
        given:
        BaseFont.BuiltinFonts14.remove(BaseFont.HELVETICA)
        BaseFont.BuiltinFonts14.remove(BaseFont.TIMES_BOLD)

        when:
        PdfUtil.restoreBaseFonts()

        then:
        assert BaseFont.BuiltinFonts14[BaseFont.HELVETICA] == PdfName.HELVETICA
        assert BaseFont.BuiltinFonts14[BaseFont.TIMES_BOLD] == PdfName.TIMES_BOLD
        assert BaseFont.BuiltinFonts14.keySet().containsAll(PdfUtil.PDF_BASE14_FONTS.keySet())
    }

    void 'restoreBaseFonts should leave fonts that are present untouched'() {
        given:
        Object existing = new PdfName('CustomHelvetica')
        BaseFont.BuiltinFonts14[BaseFont.HELVETICA] = existing
        int sizeBefore = BaseFont.BuiltinFonts14.size()

        when:
        PdfUtil.restoreBaseFonts()

        then:
        assert BaseFont.BuiltinFonts14[BaseFont.HELVETICA].is(existing)
        assert BaseFont.BuiltinFonts14.size() == sizeBefore
    }
}
