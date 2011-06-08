
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
		<g:hasErrors bean="${orderCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="receiveOrder" method="post">
			<div class="dialog">
				
				<g:render template="progressBar" model="['state':'enterShipmentDetails']"/>	
				<fieldset>
					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='id'>Order number</label>
								</td>
								<td valign='top'class='value'>
									${orderCommand.order.orderNumber }
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='id'>Status</label>
								</td>
								<td valign='top'class='value'>
									<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
									${ (orderCommand?.order?.isComplete()) ? "Complete" : "Pending" }
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'>Shipment type</label>
								</td>
								<td valign='top'class='value'>
									<g:select class="combobox updateable" name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
										optionKey="id" optionValue="name" value="${orderCommand?.shipmentType?.id }" noSelection="['':'']" />
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'>Receipient</label>
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
									<label for='shippedOn'>Shipped on</label>
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
					<div class="buttons">
						<span class="formButton"> 
							<g:submitButton name="next" value="Next"></g:submitButton> 
						</span>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>