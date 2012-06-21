package org.pih.warehouse

import org.pih.warehouse.util.ImageUtil;

class ResizeImageTagLib { 
	
	def resizeImage = { attrs, body ->
		//def file = downloadFile(attrs.src)
		//out << file.absolutePath
		//ImageUtil.resizeImage(file.bytes, out, 200, 200);
		def out = new FileOutputStream("/tmp/image.jpg")
		ImageUtil.resizeImage(attrs.src, out, 200, 200);
	}	
	
	def downloadFile(url) {
		def filename = url.tokenize("/")[-1]
		def fileOutputStream = new FileOutputStream(filename)
		def out = new BufferedOutputStream(fileOutputStream)
		out << new URL(url).openStream()
		out.close()
		return new File(filename)
	}
	
	
}