<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></title>        
	<content tag="pageTitle"><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></content>
	<style>
	</style>
</head>

<body>    
	<div class="body">

		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:hasErrors bean="${shipmentInstance}">
            <div class="errors">
                <g:renderErrors bean="${shipmentInstance}" as="list" />
            </div>
        </g:hasErrors>							
		
		<g:form action="createSuitcase">		
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
			
			<fieldset>
				<legend>Step 1.  Basic Information</legend>
				<table>
					<tbody>
					
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.name.label" default="Shipment Number" /></label>
							</td>
							<td colspan="3" valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								<span style="line-height: 1.5em">${shipmentInstance?.shipmentNumber}</span>
							</td>
						</tr>
					
						<tr class="prop">
							<td valign="top" class="name"><label>Type</label></td>
							<td valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentType', 'errors')}">
								<g:if test="${shipmentInstance?.shipmentType}">
									<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}" />
									${shipmentInstance?.shipmentType?.name }																	
								</g:if>
								<g:else>
									<g:select
										name="shipmentType.id"
										from="${org.pih.warehouse.shipping.ShipmentType.list()}"
										optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentType?.id}" />								
								</g:else>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label><g:message
								code="shipment.name.label" default="Name" /></label>
							</td>
							<td colspan="3" valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								<g:textField name="name" value="${shipmentInstance?.name}" size="40" />
							</td>
						</tr>									
						<tr class="prop">
							<td valign="top" class="name"><label>Route</label></td>
							<td valign="top"
								class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')} ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
									<g:select name="origin.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.origin?.id}"  />					
									&nbsp;							
									<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" />
									&nbsp;							
									<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.destination?.id}"  />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class=""></td>
							<td valign="top" class="value">
								<div class="">
								    <g:submitButton name="next" value="Next"></g:submitButton>
								    &nbsp;								    
									<g:link action="createSuitcase" event="cancel" id="${shipmentInstance?.id}">Cancel</g:link>																			
								    
							    </div>
							    
							    
							</td>
						</tr>
					</tbody>
				</table>
			</fieldset>
		</g:form>
	</div>
</body>
</html>