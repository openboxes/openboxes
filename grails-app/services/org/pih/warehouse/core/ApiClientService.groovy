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
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HTTP
import org.codehaus.groovy.grails.web.json.JSONObject

class ApiClientService {

    boolean transactional = false

    JSONObject get(String url) {
        return execute(new HttpGet(url))
    }

    JSONObject post(String url, Map requestData) {
        return execute(new HttpPost(url), requestData)
    }

    def delete(String url) {
        return execute(new HttpDelete(url))
    }

    def put(String url, Map requestData) {
        return execute(new HttpPut(url), requestData)
    }

    JSONObject execute(HttpEntityEnclosingRequestBase request, Map requestData) {
        if (requestData) {
            JSONObject jsonObject = new JSONObject(requestData)
            StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8")
            BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE,"application/json");
            entity.setContentType(basicHeader);
            request.setEntity(entity)
        }
        return execute(request)
    }

    JSONObject execute(HttpRequestBase request) {
        // Request config
        request.setConfig(requestConfig)

        // Execute request
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(request)

        // Process response
        InputStream is = response.entity.content
        String data = IOUtils.toString(is, "UTF-8")
        return new JSONObject(data)
    }

    static private getRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(10000).build();
    }
}
