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

import org.junit.Test

class CustomDateEditorTests extends GroovyTestCase {

    @Test
    void testCustomDateEditor_shouldHandleDateFormat() {
        CustomDateEditor customDateEditor = new CustomDateEditor(["MM/dd/yyyy", "yyyy-MM-dd HH:mm:ss z"], true)
        customDateEditor.setAsText("01/01/2020")
        println customDateEditor.value
        assertEquals "01/01/2020", customDateEditor.getAsText()
    }

    @Test
    void testCustomDateEditor_shouldHandleDatetimeFormat() {
        CustomDateEditor customDateEditor = new CustomDateEditor(["MM/dd/yyyy", "yyyy-MM-dd HH:mm:ss z"], true)
        customDateEditor.setAsText("2020-01-01 12:02:01 CDT")
        println customDateEditor.value
        println customDateEditor.asText
        assertEquals "01/01/2020", customDateEditor.getAsText()
    }

    @Test
    void testCustomDateEditor_shouldFailUnparseableDate() {
        String dateString = new String("01/Jan/2020")
        CustomDateEditor customDateEditor = new CustomDateEditor(["MM/dd/yyyy", "yyyy-MM-dd HH:mm:ss z"], true)
        String message = shouldFail(IllegalArgumentException) {
            customDateEditor.setAsText(dateString)
        }
        println customDateEditor.value
        println customDateEditor.asText
        assertEquals message, "Could not parse date: Unparseable date: \"01/Jan/2020\""
    }

}
