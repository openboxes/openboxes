package org.pih.warehouse

import grails.converters.JSON
import io.keen.client.java.JavaKeenClientBuilder
import io.keen.client.java.KeenClient
import io.keen.client.java.KeenLogging
import io.keen.client.java.KeenProject
import io.keen.client.java.KeenQueryClient
import io.keen.client.java.RelativeTimeframe
import org.apache.commons.lang.math.RandomUtils

class KeenController {

    def keenService

    def index = {
        render (["success":"true", client: KeenClient.client()] as JSON)
    }


    def track = {
        // In a totally separate piece of application logic:
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("string", UUID.randomUUID().toString());
        event.put("number", RandomUtils.nextInt())
        event.put("date", new Date());

        // Add it to the "purchases" collection in your Keen Project.
        KeenClient.client().addEvent("things", event);
        render ([success:true, event:event] as JSON)
    }

    def query = {
        KeenProject project = new KeenProject("57f676998db53dfda8a72b1d",
                "44E94FD79F2E1DECE46B693F3D184D11FA50B3B6DD538F924FF954193F70564294BBDE15587F6705583A6D1B8F69B5C90F9717F475CC89642011D9F284B8DDF2913ABDF13BC9609D14DD7294B5C9DB9078C2AFCB54856AE7C11F2588AA642EE7",
                "D7BD0A0291B70B84ECDB42F9E5E2559E4C0A8022C94A0DAD0D2908733498674813BE6E0D76B9596EAA2F206600DF31CA3A5F69BFCAEE1A3517278445606E7DB871E7FF8A757A9952D5E5590FABC37C8C2A0C203CF298686F712B2391E4771D2D");

        KeenQueryClient queryClient = new KeenQueryClient.Builder(project).build();

        long count = queryClient.count("things", new RelativeTimeframe("this_week"));
        render ([success:true, count:count] as JSON)
    }


    def dashboard = {

        [test:"value1"]

    }
}