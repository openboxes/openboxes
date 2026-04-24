package org.pih.warehouse.importer

import java.nio.charset.StandardCharsets

import org.pih.warehouse.core.http.ContentType

/**
 * Wraps a String containing some bulk data.
 */
class StringSource implements BulkDataSource<String> {

    String source
    ContentType contentType

    @Override
    InputStream asInputStream(){
        return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))
    }
}
