package org.pih.warehouse

import org.pih.warehouse.core.MultipartMaxSizeException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartResolver

import javax.servlet.http.HttpServletRequest

class MultipartResolver extends CommonsMultipartResolver {

    @Override
    MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        try {
            return super.resolveMultipart(request);
        } catch (MaxUploadSizeExceededException e) {
            Throwable exception = new MultipartMaxSizeException(e)
            request.setAttribute("exception", exception)
            throw(exception)
        }
    }
}
