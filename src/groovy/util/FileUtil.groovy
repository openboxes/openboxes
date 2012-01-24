package util

class FileUtil {

	static void copyFile(File source, File destination) {
		def reader = source.newReader()
		destination.withWriter { writer -> writer << reader }
		reader.close()
	}
}
