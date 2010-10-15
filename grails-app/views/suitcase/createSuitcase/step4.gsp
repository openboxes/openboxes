

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
				<legend>Step 4.  Review Suitcase Shipment</legend>				
				<div class="dialog">				
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
								<td valign="middle" class="name"><label><g:message
									code="shipment.shipmentType.label" default="Type" /></label></td>
								<td valign="middle" class="value" nowrap="nowrap">
									<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}"/>
									${shipmentInstance?.shipmentType?.name}								
	
								</td>
							</tr>			
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.name.label" default="Name" /></label>
								</td>
								<td colspan="3" valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
									${shipmentInstance?.name}
								</td>
							</tr>									
							<tr class="prop">
								<td valign="top" class="name"><label>Route</label></td>
								<td valign="top"
									class="value">
										${shipmentInstance?.origin?.name}
										&nbsp;							
										<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" />
										&nbsp;							
										${shipmentInstance?.destination?.name}
								</td>
							</tr>
							<tr>
								<td colspan="2"><hr/></td>
							</tr>						

							<tr class="prop">
							
								<td class="name"><label>Contents</label></td>
								<td class="value">
									<table border="0" width="100%">
										<thead>
											<tr>
												<th></th>
												<th>Product</th>
												<th>Quantity</th>
												<th>Serial Number</th>
												<th>Lot Number</th>
												<th>Recipient</th>
											</tr>
										</thead>
										<tbody>	
											<g:set var="counter" value="${10}" />						
											<g:each var="containerInstance" in="${shipmentInstance.containers}" status="containerStatus">
												<g:if test="${containerInstance.shipmentItems}">	
													<g:each var="itemInstance" in="${containerInstance.shipmentItems}" status="itemStatus"> 
														<tr class="${counter % 2 == 0 ? 'odd':'even'}">
															<td>
																<g:if test="${itemStatus == 0}">
																	${containerInstance?.containerType?.name} ${containerInstance?.name }
																</g:if>
															</td>
															<td>
																${itemInstance?.product?.name}
															</td>
															<td>
																${itemInstance?.quantity}
															</td>
															<td>
																${itemInstance?.serialNumber}
															</td>
															<td>
																${itemInstance?.lotNumber}
															</td>
															<td>
																${itemInstance?.recipient?.name}
															</td>
														</tr>		
														<g:set var="counter" value="${counter+1}" />				
													</g:each>		
												</g:if>
												
																								
											</g:each>
										</tbody>																					
									</table>													
								</td>
							</tr>
							<tr class="prop">
								<td class=""></td>
								<td class="">
									<div class="">
									    <g:submitButton name="back" value="Back"></g:submitButton>
									    <g:submitButton name="next" value="Next"></g:submitButton>
									    
										<g:link action="createSuitcase" event="cancel" id="${shipmentInstance?.id}">Cancel</g:link>
									    
								    </div>
								</td>
							</tr>
						</tbody>
					</table>										
				</div>
			</fieldset>
		</g:form>		
	</div>
</body>
</html>