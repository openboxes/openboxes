
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter shipment details</title>
<style>
	
</style>
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a href="${createLinkTo(dir:'')}">Home</a>
		</span>
	</div>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestCommand}">
			<div class="errors">
				<g:renderErrors bean="${requestCommand}" as="list" />
			</div>
		</g:hasErrors>
		<div class="dialog">
			
			<g:render template="progressBar" model="['state':'packRequestItems']"/>	
			<br clear='all'/>
			
			<g:form action="fulfillRequest" method="post">
				<g:hiddenField name="request.id" value="${requestCommand?.request?.id }"/>
				<g:hiddenField name="dateRequested" value="${requestCommand?.dateRequested }"/>			
			
				<fieldset>
					<g:render template="../request/summary" model="[requestInstance:request]"/>
					<table>
						<tbody>
						
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='requestedBy'>Shipment type</label>
								</td>
								<td valign='top'class='value'>
									<g:select name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
										optionKey="id" optionValue="name" value="${requestCommand?.shipmentType?.id }" noSelection="['':'']" />
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='requestedBy'>Receipient</label>
								</td>
								<td valign='top'class='value'>
									<div class="ui-widget">
										<g:select class="combobox updateable" name="recipient.id" from="${org.pih.warehouse.core.Person.list()}" 
											optionKey="id" optionValue="name" value="${requestCommand?.recipient?.id }" noSelection="['':'']" />
									</div>									
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shippedOn'>Shipped on</label>
								</td>
								<td valign='top'class='value'>									
									<g:jqueryDatePicker 
										id="shippedOn" 
										name="shippedOn" 
										class="updateable"
										value="${requestCommand?.shippedOn }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</td>
							</tr>								
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='deliveredOn'>Delivered on</label>
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
							<g:submitButton name="next" value="Next"></g:submitButton> 
							<g:link action="fulfillRequest" event="cancel">Cancel</g:link>
						</span>
					</div>
					
					
				</fieldset>
			</g:form>
		</div>

	</div>
</body>
</html>