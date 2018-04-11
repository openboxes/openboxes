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
            <warehouse:message code="consumption.report.edit.label" default="Design Consumption Report"/>
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

        <g:form action="list" method="get">
            <table border="0">
                <tr>
                    <th><warehouse:message code="consumption.location.label" default="Location"/></th>
                    <th><warehouse:message code="consumption.dateRange.label" default="Date Range"/></th>
                    <th><warehouse:message code="consumption.groupBy.label"/></th>
                    <th></th>
                </tr>
                <tr>
                    <td>
                        <g:selectLocation class="chzn-select-deselect"
                                          name="location.id" noSelection="['':'']"
                                          value="${command?.location?.id?:session?.warehouse?.id}"/>
                    </td>

                    <td>
                        <g:jqueryDatePicker
                                id="startDate"
                                name="startDate"
                                changeMonthAndYear="true"
                                size="20"
                                value="${command?.startDate }"
                                format="MM/dd/yyyy"
                                showTrigger="false"
                        />

                        <g:jqueryDatePicker
                                id="endDate"
                                name="endDate"
                                changeMonthAndYear="true"
                                size="20"
                                value="${command?.endDate }"
                                format="MM/dd/yyyy"
                                showTrigger="false"
                        />
                    </td>
                    <td>
                        <g:select name="groupBy" class="chzn-select-deselect"
                                  from="[	'daily': warehouse.message(code:'consumption.daily.label'),
                                             'weekly': warehouse.message(code:'consumption.weekly.label'),
                                             'monthly': warehouse.message(code:'consumption.monthly.label'),
                                             'yearly': warehouse.message(code:'consumption.annually.label')]"
                                  optionKey="key" optionValue="value" value="${command?.groupBy}"
                                  noSelection="['default': warehouse.message(code:'default.label')]" />
                    </td>
                    <td class="right">
                        <button id="btn-run" name="filter" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'lightning.png')}"/>
                            &nbsp;<warehouse:message code="default.button.run.label"/> </button>


                        <g:link controller="consumption" action="pivot" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh_small.png')}"/>
                            <g:message code="default.button.clear.label"/>
                        </g:link>
                    </td>
                </tr>
            </table>
        </g:form>

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
            cols: ["Monthly"],
            rows: ["Category Name", "Product Name"],
            vals: ["Issued"],
            aggregatorName: "Sum",
            hiddenAttributes: [],
            renderers: renderers
        }, true);
    });


</script>

</body>

</html>
