
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter order details</title>
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
								<td valign='top' class='name'><label for='description'>Description:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'description','errors')}'>
									<input type="text" name='description' value="${order?.description?.encodeAsHTML()}" size="30"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Order from:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'origin','errors')}'>
									<g:if test="${suppliers }">
										<g:select name="origin.id" from="${suppliers?.sort()}" optionKey="id" value="${order?.origin?.id}" noSelection="['':'']"/>
									</g:if>
									<g:else>
										<span class="fade">There are no suppliers</span> 
									</g:else>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Order for:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'destination','errors')}'>
									${session.warehouse?.name }
									<g:hiddenField name="destination.id" value="${session.warehouse?.id}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='orderedBy'>Ordered by:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:order,field:'orderedBy','errors')}'>
									<g:select class="combobox" name="orderedBy.id" from="${org.pih.warehouse.core.Person.list().sort{it.lastName}}" optionKey="id" value="${order?.orderedBy?.id}" noSelection="['':'']"/>
	
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'>Ordered on:</label></td>
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
						<g:submitButton name="next" value="Add Items"></g:submitButton> 
						<%-- 
						<g:submitButton name="finish" value="Save & Exit"></g:submitButton> 
						--%>
						<g:link action="purchaseOrder" event="cancel">Cancel</g:link>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>