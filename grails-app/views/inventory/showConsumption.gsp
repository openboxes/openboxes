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
			
			<table>
				<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="showConsumption" method="get">
			            	<table style="display: inline">
			            		<tr>
			            			<th><warehouse:message code="consumption.startDate.label"/></th>
			            			<th><warehouse:message code="consumption.endDate.label"/></th>
			            			<th><warehouse:message code="consumption.groupBy.label"/></th>
			            			<th></th>
			            		</tr>
			            		<tr>
						           	<td>
										<g:jqueryDatePicker 
											id="startDate" 
											name="startDate" 
											changeMonthAndYear="true"
											value="${command?.startDate }" 
											format="MM/dd/yyyy"
											showTrigger="false" />

									</td>
						           	<td>
							           	<g:jqueryDatePicker 
											id="endDate" 
											name="endDate" 
											changeMonthAndYear="true"
											value="${command?.endDate }" 
											format="MM/dd/yyyy"
											showTrigger="false" />
									</td>
									<td>
						           		<g:select name="groupBy"
														from="[	'daily': warehouse.message(code:'consumption.daily.label'), 
																'weekly': warehouse.message(code:'consumption.weekly.label'), 
																'monthly': warehouse.message(code:'consumption.monthly.label'), 
																'yearly': warehouse.message(code:'consumption.annually.label')]"
														optionKey="key" optionValue="value" value="${command?.groupBy}" 
														noSelection="['default': warehouse.message(code:'default.label')]" />   
						           	</td>
									<td class="right" style="height: 100%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>
									</td>							           	
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
			</table>
			<br/>
			<div class="box">
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
								${productKey }							
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
			<div class="right">
				<img src="${resource(dir:'images/icons/silk',file:'arrow_refresh.png')}" style="vertical-align: middle"/> 				
				<g:link controller="inventory" action="refreshConsumptionData">
					<warehouse:message code="consumption.refreshData.label"/>
				</g:link>
			</div>
		</div>
		
		<%-- 
		<div>
			<table>
				<g:each var="result" in="${results }">
					<g:each var="inner" in="${result }">
						<tr>
							<td>${inner } ${inner.class.name }</td>
						</tr>
					</g:each>
				</g:each>
			</table>
		</div>
		--%>
		
	</body>

</html>
