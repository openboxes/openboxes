package org.pih.warehouse.core.http

import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Identifies when the object is serialized by a ResponseMapper, which instructs how to format the object as
 * HTTP response body contents (such as JSON or XML).
 *
 * Implementing this interface is not strictly required. It is meant purely as a visual indicator for developers,
 * showing that the object has an associated ResponseMapper.
 */
interface ResponseBodyMapped<T extends ResponseMapper> {

}
