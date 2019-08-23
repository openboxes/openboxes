<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />

    <title><g:message code="inventory.consumption.label"/></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'js/pivottable/',file:'pivot.css')}" type="text/css" media="all" />

</head>

<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="button-bar">
        <g:link controller="consumption" action="list" class="button">
            <img src="${resource(dir:'images/icons/silk',file:'report.png')}" style="vertical-align: middle"/>
            <warehouse:message code="consumption.report.show.label" default="Show Consumption Report"/>
        </g:link>
        <g:link controller="consumption" action="pivot" class="button">
            <img src="${resource(dir:'images/icons/silk',file:'report_edit.png')}" style="vertical-align: middle"/>
            <warehouse:message code="consumption.report.edit.label" default="Edit Consumption Report"/>
        </g:link>
        <div class="right">
            <g:link controller="consumption" action="refresh" class="button">
                <img src="${resource(dir:'images/icons/silk',file:'table_refresh.png')}" style="vertical-align: middle"/>
                <warehouse:message code="consumption.refreshData.label"/>
            </g:link>
        </div>

    </div>

    <div class="box">
        <h2><g:message code="consumption.label"/></h2>
        <div id="output">
            <div class="loading">Loading...</div>

        </div>
    </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/pivottable/1.6.3/pivot.min.js" type="text/javascript" ></script>


<script type="text/javascript">
    var utils = $.pivotUtilities;
    var sum = $.pivotUtilities.aggregatorTemplates.sum;
    var numberFormat = $.pivotUtilities.numberFormat;
    var intFormat = numberFormat({digitsAfterDecimal: 0});
    var renderers = $.extend(
        $.pivotUtilities.renderers,
        $.pivotUtilities.c3_renderers,
        $.pivotUtilities.d3_renderers,
        $.pivotUtilities.export_renderers
    );
    $.getJSON('${request.contextPath}/consumption/aggregate', {}, function(data) {
        $("#output").pivotUI(data, {
            cols: ["year", "month"],
            rows: ["productName"],
            aggregator: sum(["quantity"]),
            hiddenAttributes: [],
            renderers: renderers
        }, true);
    });


</script>

</body>

</html>
