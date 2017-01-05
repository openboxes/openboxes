/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package util

import com.opensymphony.clickstream.Clickstream

import java.text.SimpleDateFormat

class ClickstreamUtil {

	static String getClickstreamAsString(Clickstream clickstream) {
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String value = ""
        if (clickstream) {
            clickstream?.stream?.reverse()?.each { entry ->
                def timestampAsString = dateFormat.format(entry?.timestamp)
                value += timestampAsString + " | "
                value += "http://" + entry.toString() + "\n"
            }
        }
        return value;

	}

    static String getClickstreamAsCsv(Clickstream clickstream) {
        def csv = "";
        //csv += '"' + "${warehouse.message(code: 'clickstream.timestamp.label', default: 'Timestamp')}" + '"' + ","
        //csv += '"' + "${warehouse.message(code: 'clickstream.serverName.label', default: 'Server name')}" + '"' + ","
        //csv += '"' + "${warehouse.message(code: 'clickstream.serverPort.label', default: 'Server port')}" + '"' + ","
        //csv += '"' + "${warehouse.message(code: 'clickstream.requestURI.label', default: 'Request URI')}" + '"' + ","
        //csv += '"' + "${warehouse.message(code: 'clickstream.queryString.label', default: 'Query string')}" + '"' + ","
        //csv += '"' + "${warehouse.message(code: 'clickstream.remoteUser.label', default: 'Remote user')}" + '"' + ","
        //csv += '"' + "${warehouse.message(code: 'clickstream.request.label', default: 'Request')}" + '"'
        csv += '"' + "Timestamp" + '"' + ","
        csv += '"' + "Server name" + '"' + ","
        csv += '"' + "Server port" + '"' + ","
        csv += '"' + "Request URI" + '"' + ","
        csv += '"' + "Query string" + '"' + ","
        csv += '"' + "Remote user" + '"' + ","
        csv += '"' + "Request" + '"'
        csv += "\n"

        clickstream?.stream?.each { entry ->
            csv += '"' + (entry?.timestamp?:"")  + '"' + ","
            csv += '"' + (entry?.serverName?:"")  + '"' + ","
            csv += '"' + (entry?.serverPort?:"")  + '"' + ","
            csv += '"' + (entry?.requestURI?:"")  + '"' + ","
            csv += '"' + (entry?.queryString?:"")  + '"' + ","
            csv += '"' + (entry?.remoteUser?:"")  + '"' + ","
            csv += '"' + ("http://" + entry?.toString()?:"")  + '"'
            csv += "\n"
        }
        return csv
    }
}
