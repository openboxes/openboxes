<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title>${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
    <style>
        .chart-wrapper { height: 400px; width: 600px }
    </style>

</head>
<body>
<div class="body">

    <div id="count-chart-wrapper" class="chart-wrapper"></div>


</div>
<script src="https://d26b395fwzu5fz.cloudfront.net/3.4.1/keen.min.js"></script>
<script type="text/javascript">
    var client = new Keen({
        projectId: "57f676998db53dfda8a72b1d",       // String (required)
        writeKey: "44E94FD79F2E1DECE46B693F3D184D11FA50B3B6DD538F924FF954193F70564294BBDE15587F6705583A6D1B8F69B5C90F9717F475CC89642011D9F284B8DDF2913ABDF13BC9609D14DD7294B5C9DB9078C2AFCB54856AE7C11F2588AA642EE7", // String (required for sending data)
        readKey: "D7BD0A0291B70B84ECDB42F9E5E2559E4C0A8022C94A0DAD0D2908733498674813BE6E0D76B9596EAA2F206600DF31CA3A5F69BFCAEE1A3517278445606E7DB871E7FF8A757A9952D5E5590FABC37C8C2A0C203CF298686F712B2391E4771D2D",   // String (required for querying data)
        protocol: "https",                  // String (optional: https | http | auto)
        host: "api.keen.io/3.0",            // String (optional)
        requestType: "jsonp"                // String (optional: jsonp, xhr, beacon)
    });

    Keen.ready(function(){
        var query = new Keen.Query("count", {
            event_collection: "things",//"pageviews",
            timeframe: "this_14_days",
            interval: "daily"
        });

        var chart = client.draw(query, document.getElementById("count-chart-wrapper"), {
            title: "Custom chart title",
            chartType: "areachart"
        });

    });

</script>
</body>

</html>