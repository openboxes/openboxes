/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MaxUploadSizeExceededException

class MultipartMaxSizeException extends RuntimeException {
    String message
    private final long maxUploadSize

    MultipartMaxSizeException(String message) {
        this.message = message
    }

    MultipartMaxSizeException(MaxUploadSizeExceededException exception) {
        this.maxUploadSize = exception.maxUploadSize
        this.message = "File size must not exceed ${FileUtils.byteCountToDisplaySize(exception.maxUploadSize)}"
    }
}
