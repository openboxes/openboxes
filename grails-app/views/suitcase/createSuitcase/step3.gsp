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
				<legend>Step 3. Add Contents</legend>
				
				<div class="">				
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
									<g:each var="containerInstance" in="${shipmentInstance.containers}" status="containerStatus">
										<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: 1px solid #aaa">
											<tr>
												<td colspan="2" style="background-color: #eee;">
													<a name="${containerInstance?.containerType?.name}-${containerInstance.name}">
														<b>${containerInstance?.containerType?.name} ${containerInstance?.name }</b>	
													</a>												
													<span style="float: right">
														<g:link action="createSuitcase" event="addItem" id="${shipmentInstance?.id}"
															params="['container.id':containerInstance?.id]">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Item" style="vertical-align: middle"/>&nbsp;
															Add Item</g:link>		
																
														&nbsp; <span class="fade">|</span> &nbsp;  
															
														<g:if test="${containerInstance?.containerType?.name != 'Suitcase' }">
															<g:link action="createSuitcase" event="deleteBox" id="${shipmentInstance?.id}"
																params="['container.id':containerInstance?.id]">
																	<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Remove Box" style="vertical-align: middle"/>&nbsp;Delete Box</g:link>																				
														</g:if>
														<g:else>
															<g:link action="createSuitcase" event="addBox" id="${shipmentInstance?.id}">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Box" style="vertical-align: middle"/>&nbsp;
																Add Box</g:link>		
														</g:else>							
													</span>							
												</td>
											</tr>
											<tr>
												<td>
													<table border="0" width="100%">
														<thead>
															<tr class="odd">
																<th>Product</th>
																<th>Quantity</th>
																<th>Serial Number</th>
																<th>Lot Number</th>
																<th>Recipient</th>
																<th>Remove?</th>																
															</tr>
														</thead>
														<tbody>		
															<g:if test="${containerInstance.shipmentItems}">	
																<g:each var="itemInstance" in="${containerInstance.shipmentItems}" status="itemStatus"> 
																	<tr class="${itemStatus % 2 == 0 ? 'odd':'even'}">
																		<td>
																			<g:hiddenField
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].id"
																				value="${itemInstance?.id}" />
																			<!--  
																			<g:hiddenField
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].product.id"
																				value="${itemInstance?.product?.id}" />
																				-->
																			<g:autoSuggest id="containers${containerStatus}-shipmentItems${itemStatus}-product" name="containers[${containerStatus}].shipmentItems[${itemStatus}].product" jsonUrl="/warehouse/json/findProductByName" 
																				width="150" 
																				valueId="${itemInstance?.product?.id}" 
																				valueName="${itemInstance?.product?.name}"/>																								
																				
																		</td>
																		<td>
																			<g:textField size="3"
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].quantity"
																				value="${itemInstance?.quantity}" />												 
																		</td>
																		<td>
																			<g:textField size="10"
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].serialNumber"
																				value="${itemInstance?.serialNumber}" />
																		</td>
																		<td>
																			<g:textField size="10"
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].lotNumber"
																				value="${itemInstance?.lotNumber}" />
																		</td>
																		<td>
																			<g:autoSuggest id="containers${containerStatus}-shipmentItems${itemStatus}-recipient" 
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].recipient" jsonUrl="/warehouse/json/findPersonByName" 
																				width="150" 
																				valueId="${itemInstance?.recipient?.id}" 
																				valueName="${itemInstance?.recipient?.name}"/>												
																		</td>																		
																		<td>
																			<g:link action="createSuitcase" event="deleteItem" id="${shipmentInstance?.id}"
																				params="['container.id':containerInstance?.id, 'item.id':itemInstance?.id]">Remove</g:link>																			
																		</td>
																	</tr>			
																</g:each>		
																<g:if test="${containerInstance?.containerType?.name == 'Suitcase' }">
																	<g:if test="${shipmentInstance?.containers?.size() > 1}">
																		<tr>
																			<td colspan="6" style="text-align: left"> Including boxes 
																				<g:each var="innerContainerInstance" in="${shipmentInstance.containers}" status="innerContainerStatus">
																					<g:if test="${innerContainerInstance?.containerType?.name != 'Suitcase' }">
																						 <a href="#${innerContainerInstance?.containerType?.name}-${innerContainerInstance.name}">
																								${innerContainerInstance?.containerType?.name} ${innerContainerInstance.name}</a> &nbsp;
																					</g:if>
																				</g:each>
																			</td>
																		</tr>
																	</g:if>																
																</g:if>
															</g:if>
															<g:else><!-- box is empty -->
																<tr>
																	<td colspan="6" style="text-align: center; height: 25px;">
																		<span class="fade">empty</span></td>
																</tr>
															</g:else>															
														</tbody>																					
													</table>													
												</td>												
											</tr>
										</table>	
										<br/>														
									</g:each>
									
									<span class="fade">Click 'Save' before adding a box or item.</span>
								</td>
							</tr>
							<tr class="prop">
								<td class=""></td>
								<td class="">
									<div class="">									
										<g:submitButton name="save" value="Save"></g:submitButton>
										&nbsp;|&nbsp;
											
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