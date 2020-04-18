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
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HTTP
import org.codehaus.groovy.grails.web.json.JSONObject

class ApiClientService {

    boolean transactional = true

    @Cacheable("apiResponseCache")
    JSONObject get(String url, JSONObject jsonObject) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).build();
        httpGet.setConfig(requestConfig);

        HttpResponse response = httpClient.execute(httpGet)
        StringEntity entity = new StringEntity(jsonObject.toMapString(), "UTF-8");
        BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE,"application/json");
        entity.setContentType(basicHeader);

        InputStream is = response.entity.content
        String data = IOUtils.toString(is, "UTF-8")
        return new JSONObject(data)
    }

    def post() {

    }
}
