package testutil

import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

import org.pih.warehouse.core.http.ContentType

/**
 * Utility methods for accessing resources (in the /build/resource folder) during tests.
 */
class ResourceUtil {

    /**
     * Fetch a file as an input stream from the resources folder for use in tests.
     */
    static InputStream getInputStream(String path) {
        return new ClassPathResource(path).getInputStream()
    }

    /**
     * Fetch a file as a (mock) multipart file from the resource folder for use in tests.
     */
    static MultipartFile getMultipartFile(String path) {
        // We're mocking the multipart file, so we need to extract the content type the hard way, via the file name.
        String contentType = ContentType.getByFileExtension(path.split("\\.")[-1])?.mediaType?.toString()

        return new MockMultipartFile(FilenameUtils.getName(path), "", contentType, getInputStream(path))
    }
}
