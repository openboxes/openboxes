
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.enterShipmentDetails.label"/></title>
<style>
	
</style>
</head>
<body>
	<div class="nav">
		<span class="linkButton"><a href="${createLinkTo(dir:'')}"><warehouse:message code="default.home.label"/></a>
		</span>
	</div>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="receiveOrder" method="post">
			<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
			<g:hiddenField name="dateOrdered" value="${orderCommand?.dateOrdered }"/>			
			<div class="dialog">
				<fieldset>
					<g:render template="../order/summary" model="[orderInstance:order]"/>
					<g:render template="progressBar" model="['state':'enterShipmentDetails']"/>	
					<table>
						<tbody>
						
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'><warehouse:message code="order.shipmentType.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<g:select name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
										optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${orderCommand?.shipmentType?.id }" noSelection="['':'']" />
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'><warehouse:message code="shipping.recipient.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<div class="ui-widget">
										<g:select class="combobox updateable" name="recipient.id" from="${org.pih.warehouse.core.Person.list()}" 
											optionKey="id" optionValue="name" value="${orderCommand?.recipient?.id }" noSelection="['':'']" />
									</div>									
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shippedOn'><warehouse:message code="shipping.shippedOn.label"/>:</label>
								</td>
								<td valign='top'class='value'>									
									<g:jqueryDatePicker 
										id="shippedOn" 
										name="shippedOn" 
										class="updateable"
										value="${orderCommand?.shippedOn }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</td>
							</tr>								
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='deliveredOn'><warehouse:message code="shipping.deliveredOn.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<g:jqueryDatePicker 
										id="deliveredOn" 
										name="deliveredOn" 
										class="updateable"
										value="${orderCommand?.deliveredOn }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</td>
							</tr>									
	
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<span class="formButton"> 
							<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton> 
							<g:link action="receiveOrder" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
						</span>
					</div>
					
					
				</fieldset>
			</g:form>
		</div>
	</div>
</body>
</html>