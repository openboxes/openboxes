/**
 * Copyright (c) 2022 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.util

import org.apache.commons.lang.exception.ExceptionUtils

class ExceptionUtil {

    /**
     * Step through a Throwable, extracting messages and "caused by" info.
     */
    static String summarize(Throwable t) {
        Set<String> messages = new LinkedHashSet<String>()

        // stop when we run out of stack trace, or see the same message twice
        while (t && messages.add(ExceptionUtils.getMessage(t))) {
            t = ExceptionUtils.getCause(t)
        }

        return messages.join(', caused by: ')
    }
}
