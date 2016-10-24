<div class="box">
    <h2>
        <warehouse:message code="dashboard.things.label" default="Things" />
    </h2>
    <div class="widget-content" style="padding: 0; margin: 0">
        <div id="things-chart-wrapper" class="chart-wrapper"></div>
    </div>
</div>
<script src="https://d26b395fwzu5fz.cloudfront.net/3.4.1/keen.min.js"></script>
<script type="text/javascript">
    var client = new Keen({
        projectId: "${grailsApplication.config.openboxes.keenio.projectId}",
        writeKey: "${grailsApplication.config.openboxes.keenio.writeKey}",
        readKey: "${grailsApplication.config.openboxes.keenio.readKey}",
        protocol: "https",
        host: "api.keen.io/3.0",
        requestType: "jsonp"
    });

    Keen.ready(function(){
        var query = new Keen.Query("count", {
            event_collection: "things",//"pageviews",
            timeframe: "this_14_days",
            interval: "daily"
        });

        var chart = client.draw(query, document.getElementById("things-chart-wrapper"), {
            title: "Things per day",
            chartType: "areachart"
        });

    });

</script>