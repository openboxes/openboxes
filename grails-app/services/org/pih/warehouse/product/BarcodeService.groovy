/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix

/**
 * @author Michael Astreiko
 */
class BarcodeService {
    static transactional = false
    MultiFormatWriter barCodeWriter = new MultiFormatWriter()

    void renderImage(response, String data, int width, int height, BarcodeFormat format = BarcodeFormat.QR_CODE) {
        Hashtable hints = [(EncodeHintType.CHARACTER_SET): 'UTF8']
        BitMatrix bitMatrix = barCodeWriter.encode(data, format, width, height, hints)
        MatrixToImageWriter.writeToStream(bitMatrix, "png", response.outputStream)
    }

    void renderImageToFile(File file, String data, int width, int height, BarcodeFormat format = BarcodeFormat.QR_CODE) {
        Hashtable hints = [(EncodeHintType.CHARACTER_SET): 'UTF8']
        BitMatrix bitMatrix = barCodeWriter.encode(data, format, width, height, hints)
        MatrixToImageWriter.writeToFile(bitMatrix, "png", file)
    }

}