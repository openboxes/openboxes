package org.pih.warehouse.product

import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

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