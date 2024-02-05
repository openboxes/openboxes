package org.pih.warehouse

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
            request.setAttribute("exception", e)
            throw(e)
        }
    }
}
