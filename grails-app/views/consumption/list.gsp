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

            <div class="summary">
                <p class="title"><g:message code="inventory.consumption.label"/></p>

            </div>

            <div class="button-bar">
                <g:link controller="consumption" action="list" class="button">
                    <img src="${resource(dir:'images/icons/silk',file:'report.png')}" style="vertical-align: middle"/>
                    <warehouse:message code="consumption.report.show.label" default="Show Consumption Report"/>
                </g:link>
				<g:link controller="consumption" action="pivot" class="button">
					<img src="${resource(dir:'images/icons/silk',file:'report_edit.png')}" style="vertical-align: middle"/>
					<warehouse:message code="consumption.report.configure.label" default="Configure Consumption Report"/>
				</g:link>

                <g:isSuperuser>
                    <div class="right">
                        <g:link controller="consumption" action="refresh" class="button">
                            <img src="${resource(dir:'images/icons/silk',file:'table_refresh.png')}" />
                            <warehouse:message code="consumption.refreshData.label"/>
                        </g:link>
                    </div>
                </g:isSuperuser>

            </div>

			<div class="box">
				<h2><g:message code="consumption.label"/></h2>

				<g:form action="list" method="get">
					<table border="0">
						<tr>
                            <th><warehouse:message code="consumption.category.label" default="Category"/></th>
                            <th><warehouse:message code="consumption.location.label" default="Location"/></th>
                            <th><warehouse:message code="consumption.dateRange.label" default="Date Range"/></th>
                            <th></th>
						</tr>
						<tr>
                            <td>
                                <g:selectCategory id="category" class="chzn-select-deselect filter"
                                    name="category" noSelection="['':'']"
                                    value="${command?.category?.id}"
                                    />
                            </td>
                            <td>
                                <g:selectDepot id="location" class="chzn-select-deselect filter"
                                                  name="location" noSelection="['':'']"
                                                  value="${command?.location?.id?:session?.warehouse?.id}"/>
                            </td>

							<td>
								<g:jqueryDatePicker
									id="startDate"
									name="startDate"
                                    cssClass="filter"
									changeMonthAndYear="true"
                                    size="20"
									value="${command?.startDate }"
									format="MM/dd/yyyy"
									showTrigger="false"
                                />

								<g:jqueryDatePicker
									id="endDate"
									name="endDate"
                                    cssClass="filter"
									changeMonthAndYear="true"
                                    size="20"
									value="${command?.endDate }"
									format="MM/dd/yyyy"
									showTrigger="false"
                                />

                                <button id="btn-execute" name="execute" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'lightning.png')}"/>
                                    &nbsp;<warehouse:message code="default.button.execute.label"/> </button>

                                <g:link controller="consumption" action="list" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh_small.png')}"/>
                                    <g:message code="default.button.clear.label"/>
                                </g:link>

                            </td>
							<td class="right">

                                <button id="btn-download" name="download" class="button" value="true">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}"/>
                                    &nbsp;<warehouse:message code="default.button.download.label"/></button>

							</td>
						</tr>
					</table>
				</g:form>

                <div class="list dialog">
                    <div id="results">
                    </div>

                </div>
			</div>
		</div>
    %{--<script src="${createLinkTo(dir:'js/pivottable', file:'pivot.js')}" type="text/javascript" ></script>--}%
    <script src="https://cdnjs.cloudflare.com/ajax/libs/pivottable/1.6.3/pivot.min.js" type="text/javascript" ></script>


    <script type="text/javascript">
        var utils = $.pivotUtilities;
        var sum = $.pivotUtilities.aggregatorTemplates.sum;
        var average = $.pivotUtilities.aggregatorTemplates.average;
        var numberFormat = $.pivotUtilities.numberFormat;
        var intFormat = numberFormat({digitsAfterDecimal: 0});
//        var heatmap =  utils.renderers["Heatmap"];


        $("#btn-execute").click(function(event) {
            event.preventDefault();
            refreshPivotTable()
        });

        $(".filter").change(function(event) {
            refreshPivotTable()
        });

        function refreshPivotTable() {
            let location = $("#location").val();
            let category = $("#category").val();
            let startDate = $("#startDate").val();
            let endDate = $("#endDate").val();
            drawPivotTable(location, category, startDate, endDate);
        }


        function drawPivotTable(location, category, startDate, endDate) {

            // Display loading message
            $("#results").html("<div class=\"loading\">Loading...</div>");

            // Get consumption data
            $.getJSON('${request.contextPath}/consumption/aggregate', {
                location: location,
                category: category,
                startDate: startDate,
                endDate: endDate
            }, function (data) {
                $("#results").pivot(
                    data, {
                        rows: ["categoryName", "productName"],
                        cols: ["year", "month"],
                        aggregator: sum(intFormat)(["quantityIssued"])
                    });
            }).fail(function(xhr, textStatus, error) {
                console.log(xhr, textStatus, error);
                $("#results").html("An unexpected error has occurred").addClass("error")
            });
        }
        refreshPivotTable()

    </script>

	</body>

</html>
