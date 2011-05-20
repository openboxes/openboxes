
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter Order Details</title>
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
								<td valign='top' class='name'><label for='description'>Order Number:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'orderNumber','errors')}'>
									${order?.orderNumber?.encodeAsHTML()}
								</td>
							</tr>
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
									<g:jqueryComboBox />
									<div class="ui-widget"> 
										<g:select class="combobox" name="origin.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${order?.origin?.id}" noSelection="['':'']" />
									</div>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Destination:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:order,field:'destination','errors')}'>
									<g:jqueryComboBox />
									<div class="ui-widget"> 
										<g:select class="combobox" name="destination.id" from="${org.pih.warehouse.core.Location.list().sort()}" optionKey="id" value="${order?.destination?.id}" noSelection="['':'']"/>
									</div>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'>Order date:</label></td>
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
							<tr class='prop'>
								<td valign='top' class='name'><label for='orderedBy'>Ordered by:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:order,field:'orderedBy','errors')}'>
									<g:select class="combobox" name="orderedBy.id" from="${org.pih.warehouse.core.Person.list().sort{it.lastName}}" optionKey="id" value="${order?.orderedBy?.id}" noSelection="['':'']"/>
	
								</td>
							</tr>
	
						</tbody>
					</table>
					<div class="buttons">
						<span class="formButton"> 
							<g:submitButton name="submit" value="Next"></g:submitButton> 
						</span>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
</body>
</html>