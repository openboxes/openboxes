
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
            		<g:render template="/order/header" model="[orderInstance:order]"/>
			
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
								<td valign='top' class='value ${hasErrors(bean:order,field:'destination','errors')}'>
									<g:if test="${suppliers }">
										<div class="ui-widget"> 
											<g:select class="combobox" name="destination.id" from="${suppliers?.sort()}" optionKey="id" value="${order?.destination?.id}" noSelection="['':'']"/>
										</div>
									</g:if>
									<g:else>
										<span class="fade">There are no suppliers</span> 
									</g:else>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Order for:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'origin','errors')}'>
									${session.warehouse?.name }
									<g:hiddenField name="origin.id" value="${session.warehouse?.id}"/>
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
						<span class="formButton"> 
							<g:submitButton name="next" value="Add Items"></g:submitButton> 
							<%-- 
							<g:submitButton name="finish" value="Save & Exit"></g:submitButton> 
							--%>
						</span>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>