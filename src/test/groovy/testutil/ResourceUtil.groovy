package testutil

import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

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
    static MultipartFile getMultiPartFile(String path) {
        return new MockMultipartFile(FilenameUtils.getName(path), getInputStream(path))
    }
}
