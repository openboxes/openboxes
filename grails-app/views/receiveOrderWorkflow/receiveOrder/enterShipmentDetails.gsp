
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

			<g:render template="../order/summary" model="[orderInstance:order, currentState:'enterShipmentDetails']"/>

			<div class="box">
					<h2>${warehouse.message(code:'order.wizard.enterShipmentDetails.label', default: 'Enter shipment details')}</h2>
					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='origin'><warehouse:message code="order.origin.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<div id="origin">
										${order?.origin?.name}

									</div>
								</td>
							</tr>

							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shipmentType.id'><warehouse:message code="order.shipmentType.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<div style="width:300px">
										<g:select name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" class="chzn-select-deselect"
												  optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${orderCommand?.shipmentType?.id }" noSelection="['':'']" />

									</div>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='destination'><warehouse:message code="order.destination.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<div id="destination">
										${order?.destination?.name}

									</div>
								</td>
							</tr>

							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='recipient.id'><warehouse:message code="shipping.recipient.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<div  style="width:300px">
										<g:select class="chzn-select-deselect" name="recipient.id" from="${org.pih.warehouse.core.Person.list().sort()}"
											optionKey="id" optionValue='${{ it.lastName + ", " + it.firstName + " (" + it.email + ")" }}' value="${orderCommand?.recipient?.id }" noSelection="['':'']" />
									</div>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='dateOrdered'><warehouse:message code="order.dateOrdered.label"/>:</label>
								</td>
								<td valign='top'class='value'>
									<div id="dateOrdered"><g:formatDate date="${orderCommand.dateOrdered}" format="MMM dd, yyyy"/></div>
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
										class="updateable large text"
										size="30"
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
										size="30"
										value="${orderCommand?.deliveredOn }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</td>
							</tr>									
	
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<span class="formButton"> 
							<g:submitButton class="button" name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
						</span>
					</div>
					
					
				</div>
			</g:form>
		</div>
	</div>
</body>
</html>
