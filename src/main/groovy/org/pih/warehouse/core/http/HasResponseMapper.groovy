package org.pih.warehouse.core.http

import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Identifies when the object is serialized by a {@link ResponseMapper}.
 *
 * Implementing this interface is not strictly required since it provides no direct functionality.
 * It is meant purely as a visual indicator for developers.
 */
interface HasResponseMapper<T extends ResponseMapper> {

}
