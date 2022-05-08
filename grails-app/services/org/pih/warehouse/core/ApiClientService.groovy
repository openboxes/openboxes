/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

import grails.plugin.springcache.annotations.Cacheable
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.codehaus.groovy.grails.web.json.JSONObject

class ApiClientService {

    boolean transactional = false

    HttpRequestBase buildGet(String url, Map requestData = [:]) {
        return buildGet(new HttpGet(url), requestData)
    }

    HttpRequestBase buildGet(HttpRequestBase request, Map requestData = [:]) {
        log.info "request data " + requestData
        List<NameValuePair> params = !requestData?.isEmpty() ?
                requestData.collect { return new BasicNameValuePair(it.key, it.value) } : []

        URI uri = new URIBuilder(request.getURI()).addParameters(params).build()
        request.setURI(uri)
        return request
    }

    JSONObject get(String url) {
        return execute(new HttpGet(url))
    }

    JSONObject post(String url, Map requestData, Map requestHeaders = [:]) {
        return execute(new HttpPost(url), requestData, requestHeaders)
    }

    def delete(String url) {
        return execute(new HttpDelete(url))
    }

    def put(String url, Map requestData) {
        return execute(new HttpPut(url), requestData)
    }

    JSONObject execute(HttpEntityEnclosingRequestBase request, Map requestData, Map requestHeaders = [:]) {
        log.info "Executing request ${request.method} ${request.URI}"
//        if (requestData) {
//            JSONObject jsonObject = new JSONObject(requestData)
//            StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8")
//            BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded");
//            entity.setContentType(basicHeader);
//            request.setEntity(entity)
//        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        requestData.each { key, value ->
            params.add(new BasicNameValuePair(key, value));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8)
        BasicHeader contentType = new BasicHeader(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded")
        entity.setContentType(contentType)
        request.setEntity(entity);
        return execute(request)
    }

    JSONObject execute(HttpRequestBase request) {

        try {
            // Request config
            request.setConfig(requestConfig)

            // Execute request
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(request)

            if (response.statusLine.statusCode == 200) {
                // Process response
                log.info "response ${response.entity.contentType}"
                InputStream is = response.entity.content
                String data = IOUtils.toString(is, "UTF-8")
                return new JSONObject(data)
            } else {
                throw new IllegalArgumentException(response.statusLine.reasonPhrase)
            }

        } catch (IOException e) {
            throw e;
        }
    }

    static private getRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(10000).build();
    }
}
