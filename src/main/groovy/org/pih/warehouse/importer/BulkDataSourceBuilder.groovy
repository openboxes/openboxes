package org.pih.warehouse.importer

import grails.databinding.DataBindingSource
import org.springframework.web.multipart.MultipartFile

import org.pih.warehouse.core.http.ContentType

/**
 * Builder methods for constructing a {@link BulkDataSource} instance.
 */
class BulkDataSourceBuilder {

    /**
     * Constructs a {@link BulkDataSource} from a request object's {@link DataBindingSource}.
     */
    static BulkDataSource build(DataBindingSource bindingSource) {
        def dataSource = bindingSource["source"]
        switch (dataSource) {
            case String:
                return buildStringSource(bindingSource, dataSource as String)
            case MultipartFile:
                return new MultipartFileSource(source: dataSource as MultipartFile)
            default:
                return null
        }
    }

    private static StringSource buildStringSource(DataBindingSource bindingSource, String dataSource) {
        // Non-file sources must manually specify the content type of the source object. This informs the importer
        // what bulk data reader to use. We don't fail if no content type is specified however. We let the API decide
        // when the BulkDataSource validation should be triggered.
        String contentTypeString = bindingSource["contentType"]
        ContentType contentType = contentTypeString == null ? null : ContentType.valueOf(contentTypeString)

        return new StringSource(source: dataSource, contentType: contentType)
    }
}
