<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><g:message code="inventory.consumption.label"/></title>    
    </head>    

	<body>
		<div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

            <div class="button-bar">
                <g:link controller="consumption" action="list" class="button">
                    <img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" style="vertical-align: middle"/>
                    <warehouse:message code="default.button.list.label"/>
                </g:link>
                <g:link controller="consumption" action="refresh" class="button">
                    <img src="${resource(dir:'images/icons/silk',file:'arrow_refresh.png')}" style="vertical-align: middle"/>
                    <warehouse:message code="consumption.refreshData.label"/>
                </g:link>

            </div>



			<div class="box">
				<h2><g:message code="consumption.label"/></h2>

				<g:form action="list" method="get">
					<table style="width:auto;">
						<tr>
                            <th><warehouse:message code="consumption.dateRange.label" default="Date Range"/></th>
							<th><warehouse:message code="consumption.groupBy.label"/></th>
							<th></th>
						</tr>
						<tr>
							<td>
								<g:jqueryDatePicker
									id="startDate"
									name="startDate"
									changeMonthAndYear="true"
                                    size="20"
									value="${command?.startDate }"
									format="MM/dd/yyyy"
									showTrigger="false" />

								<g:jqueryDatePicker
									id="endDate"
									name="endDate"
									changeMonthAndYear="true"
                                    size="20"
									value="${command?.endDate }"
									format="MM/dd/yyyy"
									showTrigger="false" />
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
							<td>
								<button name="filter" class="button">
									<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>


                                <g:link controller="consumption" action="list" class="button">
                                    <g:message code="default.button.clear.label"/>
                                </g:link>
							</td>
						</tr>
					</table>
				</g:form>


				<table border="0">
					<tr class="odd">
						<th>
							<warehouse:message code="product.label"/>
						</th>
						<g:each var="dateKey" in="${dateKeys }">
							<th class="center">${dateKey }</th>
						</g:each>
						<th class="center">Total</th>
					</tr>
					<g:each var="productKey" in="${productKeys }" status="i">
						<tr class="${i%2?'odd':'even' }">
							<td>
								${productKey?.productCode }
                                ${productKey?.name }
                            </td>
							<g:each var="dateKey" in="${dateKeys }">
								<g:set var="qty" value="${consumptionProductDateMap.get(productKey.id + "_" + dateKey)}"/>
								<td class="center">
									${consumptionProductDateMap.get(productKey.id + "_" + dateKey)?:0}
								</td>
							</g:each>							
							<th class="center" style="border-left: 1px solid lightgrey;">
								${consumptionProductDateMap.get(productKey.id + "_Total")?:0}
							</th>
						</tr>
					</g:each>

				</table>
			</div>
		</div>

	</body>

</html>
