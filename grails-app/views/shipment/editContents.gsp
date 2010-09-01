<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Edit Shipment Contents</content>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>
		<script type="text/javascript">
			jQuery(function() {
				$('.accordion .container').click(function() {
					$(this).next().toggle();
					// Save the ID 
					var id = $(this).attr('id');	
					var isClicked = $.cookie(id);
					if (isClicked) $.removeCookie(id);
					else $.setCookie(id, "clicked");
					return false;			
				}).next().hide();		
			});
		
			jQuery(document).ready(function() {
				//var openContainers = $.cookie("openContainers");
				//alert("openContainers: " + openContainers);
		
				$('.accordion .container').each(function(index, value){  
					var id = $(this).attr('id')
					var isClicked = $.cookie(id);
					if(isClicked) { 
						$(this).next().show();
					}
				});	
		
				$('.toggle-button').click(function() {
					$(this).next(".toggleable").slideToggle('fast');
				});
				
			});
		</script>
		<div id="containers" class="section">		
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<div style="padding-bottom: 10px;">
								<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
								 &nbsp; &raquo; &nbsp;
								<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}">Packages</g:link>
								 &nbsp; &raquo; &nbsp; 
								<span style="font-size: 90%">Edit Contents</span>
							</div>					
							<%-- 
							<g:if test="${containerInstance}">			
								<div style="padding-bottom: 10px;">
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
									 &nbsp; &raquo; &nbsp;
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}">Shipment Units</g:link>
									 &nbsp; &raquo; &nbsp; 
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':containerInstance.id]">
										${containerInstance?.containerType?.name} ${containerInstance?.name}</g:link>
									&nbsp; &raquo; &nbsp;
									<span style="font-size: 90%">Items</span>
								</div>					
							</g:if>
							<g:else>
								<div style="padding-bottom: 10px;">
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
									&raquo; <span style="font-size: 90%">Shipment Units</span>
								</div>							
							</g:else>
							--%>
						</td>
					</tr>					
					<tr>
						<td style="width: 60%" valign="top">
							<div style="border: 1px solid #ccc">
										<g:render template="summary"/>
										<div style="border-top: 1px solid #ccc; border-bottom: 1px solid #ccc;">									
											<table>
												<tr>
													<td>	
														<div style="padding: 10px;">																	
															<span>Packages:</span>				
															<g:each in="${shipmentInstance?.containers}" var="container" status="i">															
																<g:if test="${container?.id == containerInstance?.id}">
																	<span style="border: 1px solid black; padding: 5px;">
																		&nbsp;${container.name}
																	</span>													
																</g:if>
																<g:else>
																	<span style="padding: 5px;">
																		&nbsp;<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">${container.name}</g:link>
																	</span>
																</g:else>				
															</g:each>
														</div>
													</td>
												</tr>
											</table>
										</div>
										<div>									
										<table>
											<tr>
												<td>
													<g:if test="${containerInstance}">			
														<div id="container-${containerInstance?.id}" class="details">																									
															<script type="text/javascript">
															$(function() {
																$("#tabs").tabs();
															});
															</script>
																				
																							
															<div class="demo">								
																<div id="tabs">
																	<ul>
																		<li><a href="#tabs-1">Contents</a></li>
																		<li><a href="#tabs-2">Details</a></li>
																		<li><a href="#tabs-3">Clone</a></li>
																	</ul>
																	<div id="tabs-1">
																		<g:render template="containerSummary" />																
																		
																		<div>
																																					
																				<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	
																					<g:hiddenField name="container.id" value="${containerInstance?.id}"></g:hiddenField>
																				
																					<div>																
																						<table>
																							<tr class="prop">
																								<th></th>
																								<th>Qty</th>
																								<th>Item</th>
																								<th>Serial No</th>
																								<th>Recipient</th>
																								<th></th>
																							</tr>
																							<tr class="prop">
																								<td width="5%"> &nbsp; &nbsp; &nbsp; </td>
																								<td width="10%">													
																									<g:textField name="quantity" value="1" size="2" />
																								</td>
																								<td width="20%">											
																									<%-- 		
																									<gui:autoComplete size="10" 
																										id="selectedItem" name="selectedItem" 
																										controller="shipment" action="availableItems" />--%>
																									<g:textField name="selectedItem" value="" size="10" style="" />
																								</td>
																								<td width="20%">													
																									<g:textField name="serialNumber" value="" size="10" style="" />
																								</td>
																								<td width="20%">													
																									<g:select name="recipient.id" 
																										noSelection="${['null':'']}"
																										from="${org.pih.warehouse.core.Person.list()}"
																										optionValue="${{it.username}}"
																										optionKey="id"
																										value="${shipmentInstance?.recipient?.id}"/>
																														
																								</td>
																								<td  width="25%" valign="top" nowrap="nowrap">
																									<div class="buttons" style="padding: 0px">
																										<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add"/> Add</button>
																									</div>
																								</td>
																							</tr>	
																						</table>									
																					</div>																												
																				</g:form>
																													
																			
																				<g:form action="editContainer">
																					<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"/>
																					<g:hiddenField name="containerId" value="${containerInstance?.id}"/>
																						<table>
																							<tbody>
																								<g:if test="${containerInstance?.shipmentItems}">
																									<g:each var="item" in="${containerInstance.shipmentItems}" status="k">
																										<tr class="prop ${(k % 2) == 0 ? 'odd' : 'even'}">
																											<td style="5%">${k+1}.</td>
																											<td width="10%">
																												<g:hiddenField name="shipmentItems[${k}].id" value="${item.id}"></g:hiddenField>												    
																												<g:textField name="shipmentItems[${k}].quantity" value="${item.quantity}" size="2" />
																											</td>
																											<td width="20%">
																												${item?.product?.name} 
																												<g:if test="${item?.product?.unverified}">
																													<span class="fade">(unverified)</span>
																												</g:if> 
																											</td>
																											<td width="20%">
																												<g:textField name="shipmentItems[${k}].serialNumber" value="${item.serialNumber}" size="10" />																									
																											</td>
																											<td width="20%">
																												<g:select name="shipmentItems[${k}].recipient.id" 
																													noSelection="${['null':'']}"
																													from="${org.pih.warehouse.core.Person.list()}"
																													optionValue="${{it.username}}"
																													optionKey="id"
																													value="${item?.recipient?.id}"/>
																												
																												
																												
																												<%-- 
																												<g:textField name="shipmentItems[${k}].recipient" value="${item.recipient}" size="10" />
																												<gui:autoComplete size="20" 
																													id="shipmentItems[${k}].recipient" name="shipmentItems[${k}].recipient" 
																													controller="shipment" action="availableContacts"/>																																							
																												 --%>
																												 
																												 
																											</td>
																											<td width="25%"></td>
																										</tr>							
																									</g:each>																																														
																									<tr class="prop">
																										<td colspan="5" style="text-align: right;">
																											<span class="fade">(enter '0' in <b>Qty</b> field to remove item)</span>
																										</td>
																									</tr>
																								</g:if>
																								<g:else>
																									<tr>
																										<td style="text-align: center" colspan="5">
																											<span class="fade">(empty)</span>
																										</td>
																									</tr>													
																								</g:else>															
																								<tr>																	
																									<td colspan="5">
																										<div class="buttons">
																											<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																											<%-- <g:link class="negative" controller="shipment" action="deleteContainer" id="${containerInstance.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;--%>
																										</div>
																									</td>
																								</tr>															
																								
																							</tbody>
																						</table>	
																					</g:form>																		
																		

																		</div>
																	</div><!-- tabs-1 -->	 
																	
																		
																	<div id="tabs-2">
																	
																		<g:render template="containerSummary" />	
																		<g:form action="editContainer">		
																			<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"></g:hiddenField>												    
																			<g:hiddenField name="containerId" value="${containerInstance?.id}"></g:hiddenField>										
																						
																	    	<table>
																				<tr class="prop">										
																					<td class="name"><label>${containerInstance?.containerType?.name} #</label></td>
																					<td class="value">
																						<g:textField name="name" value="${containerInstance?.name}" size="2" />
																					</td>
																				</tr>
																				<tr class="prop">
																					<td class="name"><label class="optional">Identifier #</label></td>
																					<td class="value">
																						<g:textField name="containerNumber" value="${containerInstance.containerNumber}" size="15"/> &nbsp;		
																						<span class="fade"></span>																
																					</td>													
																				</tr>
																				<tr class="prop">
																					<td class="name"><label class="optional">Recipient</label></td>
																					<td class="value">
																						<g:textField name="recipient" value="${containerInstance?.recipient}" size="15"/> &nbsp;
																						<span class="fade">enter contact's name or email</span>
																					</td>		
																				</tr>
																				<tr class="prop">										
																					<td class="name"><label class="optional">Weight</label></td>
																					<td class="value">
																						<g:textField name="weight" value="${containerInstance?.weight}" size="7"/> 
																						<g:select name="weightUnits" 
																							from="${[' ', 'kg', 'lb']}"
																							value="${containerInstance?.weightUnits}">
																						</g:select>																	
																						<span class="fade">e.g. '100 lb' or '120 kg' </span>
																					</td>
																				</tr>
																				<tr class="prop">
																					<td class="name"><label class="optional">Dimensions</label></td>
																					<td class="value">
																						<g:textField name="height" value="${containerInstance?.height}" size="2"/> x
																						<g:textField name="width" value="${containerInstance?.width}" size="2"/> x
																						<g:textField name="length" value="${containerInstance?.length}" size="2"/> 																																								
																						<g:select name="volumeUnits" 
																							from="${['', 'in', 'ft', 'cm']}"
																							value="${containerInstance?.volumeUnits}">																							
																						</g:select>
																						
																						 <span class="fade">e.g. '10.1" x 4.2" x 2.8"'</span>
																					</td>		
																				</tr>
																				<tr class="prop">
																					<td class="name"><label class="optional">Description</label></td>
																					<td class="value">
																						<g:textField name="description" value="${containerInstance?.description}" size="40"/> &nbsp;
																					</td>
																				</tr>
																				<tr class="prop">																	
																					<td class=""></td>
																					<td class="value" colspan="2">
																						<div class="buttons">
																							<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																							<g:link class="negative" controller="shipment" action="deleteContainer" id="${containerInstance.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;
																						</div>
																					</td>
																				</tr>
																			</table>					
																		</g:form>		
																	</div><!-- tabs-2 -->
															
																	<div id="tabs-3">
																	
																		<g:render template="containerSummary" />	
																		<g:form action="copyContainer">
																			<g:hiddenField name="containerId" value="${containerInstance?.id}" />	
																			<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />															
																			<table>
																				<tbody>
																					<tr class="prop">
																                           <td valign="top" class="name"><label><g:message code="container.name.label" default="Copying unit" /></label></td>                            
																                           <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																							${containerInstance.containerType.name}-${containerInstance.name}
																						</td>
																                       </tr>  	          
																						<tr class="prop">
																                           <td valign="top" class="name"><label><g:message code="container.copies.label" default="How Many?" /></label></td>                            
																                           <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																							<g:textField name="copies" value="1" size="3"/>
																                              </td>
																                       </tr>  	          
																                       <tr class="prop">
																					    <td class="name"></td>
																					    <td class="value">
																							<div class="buttons">
																								<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Copy</button>
																								</div>	
																						    </td>					                        
																                        </tr>         
																                    </tbody>
																                </table>																			                	               
																		    </g:form>
																		</div><!-- tabs-3 -->
																	</div><!-- tabs -->
																</div><!-- demo -->
															</div><!-- details -->
													</g:if>
													<g:else>												
														<div style="padding: 15px;" class="notice">
															Choose a package to edit
														</div>												
													</g:else>												
												</td>
											</tr>																										
										</table>
									</div>								
							</div>		
						</td>						
						<td valign="top" width="20%">							
							<g:render template="sidebar" />
						</td>
					</tr>
				</tbody>
			</table>							
		</div>
	</div>
</body>
</html>


