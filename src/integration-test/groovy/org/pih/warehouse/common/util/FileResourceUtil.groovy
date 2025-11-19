package org.pih.warehouse.common.util

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

class FileResourceUtil {

    /**
     * Fetch a file from the resources folder.
     */
    static getFile(String path) {
        Resource resource = new ClassPathResource(path)
        File file = resource.getFile()

        assert file?.exists() == true

        return file
    }
}
