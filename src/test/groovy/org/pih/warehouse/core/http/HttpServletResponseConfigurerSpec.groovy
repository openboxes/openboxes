package org.pih.warehouse.core.http

import javax.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.file.FileNameGenerator
import org.pih.warehouse.core.file.FileExtension

@Unroll
class HttpServletResponseConfigurerSpec extends Specification {

    @Shared
    HttpServletResponseConfigurer httpServletResponseConfigurer

    @Shared
    FileNameGenerator fileNameGeneratorStub

    void setup() {
        httpServletResponseConfigurer = new HttpServletResponseConfigurer()

        fileNameGeneratorStub = Stub(FileNameGenerator)
        httpServletResponseConfigurer.fileNameGenerator = fileNameGeneratorStub
    }

    void "withFile builds a file name as expected for scenario: #scenario"() {
        given:
        HttpServletResponse response = new MockHttpServletResponse()

        and:
        fileNameGeneratorStub.generate(_ as FileExtension, _ as Collection<Object>) >> fileName

        when:
        httpServletResponseConfigurer.withFile(response, contentType, [])

        then:
        assert response.getHeaderValue(HttpHeaders.CONTENT_DISPOSITION) == expectedContentDisposition
        assert response.contentType == contentType.mediaType.toString()

        where:
        contentType     | fileName              || expectedContentDisposition                                                                              | scenario
        ContentType.XLS | ""                    || "attachment; filename=\"\"; filename*=UTF-8''"                                                          | "Blank without extension"
        ContentType.XLS | ".xls"                || "attachment; filename=\".xls\"; filename*=UTF-8''.xls"                                                  | "Blank with extension"
        ContentType.XLS | "a.xls"               || "attachment; filename=\"a.xls\"; filename*=UTF-8''a.xls"                                                | "Ascii text"
        ContentType.CSV | "a.csv"               || "attachment; filename=\"a.csv\"; filename*=UTF-8''a.csv"                                                | "Different file type"
        ContentType.XLS | "a b.xls"             || "attachment; filename=\"a b.xls\"; filename*=UTF-8''a%20b.xls"                                          | "Space in name"
        ContentType.XLS | "1()&\$#@+=_-.,'.xls" || "attachment; filename=\"1()&\$#@+=_-.,'.xls\"; filename*=UTF-8''1%28%29%26%24%23%40%2B%3D_-.%2C%27.xls" | "Special characters"
        ContentType.XLS | "你好.xls"             || "attachment; filename=\".xls\"; filename*=UTF-8''%E4%BD%A0%E5%A5%BD.xls"                                | "Unicode characters"
        ContentType.XLS | "a​b.xls"          || "attachment; filename=\"ab.xls\"; filename*=UTF-8''a%E2%80%8Bb.xls"                                     | "OBS-1960: Zero width joiner"
    }
}
