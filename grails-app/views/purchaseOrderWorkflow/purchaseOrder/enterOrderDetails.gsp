
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.enterOrderDetails.label"/></title>
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
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
		<g:each var="orderItem" in="${orderItems}" status="i">
			<g:hasErrors bean="${orderItem}">
				<div class="errors">
					<g:renderErrors bean="${orderItem}" as="list" />
				</div>
			</g:hasErrors>
		</g:each>
		<g:form action="purchaseOrder" method="post">
			<div class="dialog">
			
				<fieldset>
            		<g:render template="/order/summary" model="[orderInstance:order]"/>
			
					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'><label for='description'><warehouse:message code="default.description.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'description','errors')}'>
									<input type="text" name='description' value="${order?.description?.encodeAsHTML()}" size="30"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'><warehouse:message code="order.orderFrom.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'origin','errors')}'>
									<%-- 
									<g:if test="${suppliers }">
										<g:select name="origin.id" from="${suppliers?.sort()}" optionKey="id" value="${order?.origin?.id}" noSelection="['':'']"/>
									</g:if>
									<g:else>
										<span class="fade"><warehouse:message code="order.thereAreNoSuppliers.label"/></span> 
									</g:else>
									--%>
									<g:selectOrderSupplier name="origin.id" 
										optionKey="id" value="${order?.origin?.id}" noSelection="['null':'']"/>
									
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination"><warehouse:message code="order.orderFor.label"/>:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'destination','errors')}'>
									<g:if test="${order.destination }">
										${order?.destination?.name }
										<g:hiddenField name="destination.id" value="${order?.destination?.id}"/>									
									</g:if>
									<g:else>
										${session.warehouse?.name }
										<g:hiddenField name="destination.id" value="${session.warehouse?.id}"/>									
									</g:else>									
									
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='orderedBy'><warehouse:message code="order.orderedBy.label"/>:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:order,field:'orderedBy','errors')}'>
									<g:select class="comboBox" name="orderedBy.id" from="${org.pih.warehouse.core.Person.list().sort{it.lastName}}" optionKey="id" value="${order?.orderedBy?.id}" noSelection="['':'']"/>
	
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'><warehouse:message code="order.orderedOn.label"/>:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:order,field:'dateOrdered','errors')}'>								
									<g:jqueryDatePicker 
										id="dateOrdered" 
										name="dateOrdered" 
										value="${order?.dateOrdered }" 
										format="MM/dd/yyyy"
										size="8"
										showTrigger="false" />								
								</td>
							</tr>							
	
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="next" value="${warehouse.message(code:'order.addItems.label')}"></g:submitButton> 
						<%-- 
						<g:submitButton name="finish" value="Save & Exit"></g:submitButton> 
						--%>
						<g:link action="purchaseOrder" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>