<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		Edit Shipment Contents
	</content>
	
	
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
				/*$("#accordion").accordion();*/
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
								<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}">Shipment Units</g:link>
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
							<div>
								<fieldset>							
								
									<g:render template="summary"/>
								
								
								<br/>
								
							<div class="accordion">									
								<table>
									<tr>
										<td colspan="2">
											<h3>Shipment Units</h3>									
										</td>
									</tr>
																					
									<g:each in="${shipmentInstance.containers}" var="container" status="i">	
										<tr>
											<td>	
													<div id="container-${container?.id}" class="container">
														<table style="border: 1px dotted #CCC">
															<tr>
																<td>
																	<g:if test="${container?.containerType?.name=='Pallet'}">
																		<img src="${createLinkTo(dir:'images/icons',file:'pallet.jpg')}"
																			alt="pallet"
																			style="vertical-align: middle; width: 24px; height: 24px;" />
																	</g:if>
																	<g:elseif test="${container?.containerType?.name=='Suitcase'}">
																		<img src="${createLinkTo(dir:'images/icons',file:'suitcase.jpg')}"
																			alt="suitcase"
																			style="vertical-align: middle; width: 24px; height: 24px;" />
																	</g:elseif>
																	<g:elseif test="${container?.containerType?.name=='Container'}">
																		<img src="${createLinkTo(dir:'images/icons',file:'container.jpg')}"
																			alt="container" style="vertical-align: middle; width: 24px; height: 24px;" />
																	</g:elseif>								
																	<g:else>														
																		<img src="${createLinkTo(dir:'images/icons',file:'box_24.gif')}"
																			alt="box" style="vertical-align: middle; width: 24px; height: 24px;" />														
																	</g:else>
																	&nbsp;
																		${container?.containerType?.name} ${container?.name} &nbsp; <span class="fade">(click to open/close)</span>
																</td>
																<td style="text-align: right;">
																	 <span style="font-size: 1.25em">${container.shipmentItems.size()}</span> items
																</td>
															</tr>
														</table>														
													</div> 
												    <div class="details" style="background-color: white;">
												 		<g:form action="editContainer">					
													 		<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"></g:hiddenField>												    
															<g:hiddenField name="containerId" value="${container?.id}"></g:hiddenField>												    
													    	<table>
																<tr class="prop">										
																	<td class="name"><label>${container?.containerType?.name} #</label></td>
																	<td class="value">
																		<g:textField name="name" value="${container?.name}" size="2" />
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label class="optional">Identifier #</label></td>
																	<td class="value">
																		<g:textField name="containerNumber" value="${container.containerNumber}" size="15"/> &nbsp;		
																		<span class="fade">ya know, like the official one</span>																
																	</td>													
																</tr>
																<tr class="prop">
																	<td class="name"><label class="optional">Recipient</label></td>
																	<td class="value">
																		<g:textField name="recipient" value="${container?.recipient}" size="15"/> &nbsp;
																		<span class="fade">enter contact's name or email</span>
																	</td>		
																</tr>
																<tr class="prop">										
																	<td class="name"><label class="optional">Weight</label></td>
																	<td class="value">
																		<g:textField name="weight" value="${container?.weight}" size="7"/> 
																		<g:textField name="weightUnits" value="${container?.weightUnits}" size="2"/> &nbsp;
																		<span class="fade">e.g. '100 lb' or '120 kg' </span>
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label class="optional">Dimensions</label></td>
																	<td class="value">
																		<g:textField name="height" value="${container?.height}" size="2"/> &nbsp;
																		<g:textField name="width" value="${container?.width}" size="2"/> &nbsp;
																		<g:textField name="length" value="${container?.length}" size="2"/> &nbsp;
																		<g:textField name="volumeUnits" value="${container?.volumeUnits}" size="2"/> &nbsp;																		
																		 <span class="fade">e.g. '10.1" x 4.2" x 2.8"'</span>
																	</td>		
																</tr>
																<tr class="prop">
																	<td class="name"><label class="optional">Description</label></td>
																	<td class="value">
																		<g:textArea name="description" value="${container?.description}" cols="14"/> &nbsp;
																		<span class="fade">briefly describe the contents (if you want)</span>
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Contents</label></td>
																	<td class="value" colspan="2">																
																		<div style="color: #666; font-size: .75em; padding-left: 10px;">
																			<fieldset>
																				<table>
																					<tbody>
																						<g:if test="${container?.shipmentItems}">
																							<tr>
																								<th></th>
																								<th style="text-align: center;">Qty</th>
																								<th>Item</th>
																								<th>Serial Number</th>
																								<th>Recipient</th>
																								<th></th>
																							</tr>
																							<g:each var="item" in="${container.shipmentItems}" status="k">
																								<tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
																									<td width="5%">${k+1}.</td>
																									<td style="text-align: center;">
																										<g:hiddenField name="shipmentItems[${k}].id" value="${item.id}"></g:hiddenField>												    
																										<g:textField name="shipmentItems[${k}].quantity" value="${item.quantity}" size="2" />
																									</td>
																									<td>
																										${item?.product?.name} 
																										<g:if test="${item?.product?.unverified}">
																											<span class="fade">(unverified)</span>
																										</g:if> 
																									</td>
																									<td>
																										<g:textField name="shipmentItems[${k}].serialNumber" value="${item.serialNumber}" size="10" />																									
																									</td>
																									<td>
																										<g:textField name="shipmentItems[${k}].recipient" value="${item.recipient}" size="15" />																									
																										<!-- 
																										<gui:autoComplete size="20" 
																											id="shipmentItems[${k}].recipient" name="shipmentItems[${k}].recipient" 
																											controller="shipment" action="availableContacts"/>																																							
																										 -->
																									</td>
																								</tr>							
																							</g:each>
																							<tr>
																								<td></td>
																								<td colspan="4" style="text-align: left;">
																									<span class="fade">(enter '0' to remove item)</span>
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

																					</tbody>
																				</table>	
																			</fieldset>		
																		</div>	
																	</td>
																</tr>
																<tr class="prop">																	
																	<td class="name"></td>
																	<td class="value" colspan="2">
																		<div class="buttons">
																			<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																			<g:link class="negative" controller="shipment" action="deleteContainer" id="${container.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;
																		</div>
																	</td>
																</tr>
															</table>
														</g:form>
														
													</div>
												</fieldset>
											</td>
											<!--  
											<td style="text-align: center">
												<ul>
													<li>
					 									<g:link controller="shipment" action="closeContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
													</li>
													<li>
					 									<g:link controller="shipment" action="editContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
													</li>
													<li>
					 									<g:link controller="shipment" action="copyContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
													</li>
													<li>
													</li>
												</ul>
											</td>
											-->
											
										</tr>																										
									</g:each>		


										
<%-- 
										<td>
											<g:each in="${shipmentInstance.containers}" var="container" status="i">	
												<g:if test="${containerInstance?.id == container?.id}">
												
													<div style="padding: 5px; background-color: lightblue;">
														<span style="font-size:1.25em">${container?.name}</span>													
													</div>
													<fieldset>
														<g:if test="${containerInstance}">																
															<table>
																<tbody>
																	<g:if test="${containerInstance?.shipmentItems}">
																		<tr>
																			<th></th>
																			<th>Item</th>
																			<th>Qty</th>
																			<th></th>
																		</tr>
																		<g:each var="item" in="${containerInstance.shipmentItems}" status="k">
																			<tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
																				<td width="5%">${k+1}.</td>
																				<td>
																					${item?.product?.name} 
																					<g:if test="${item?.product?.unverified}">
																						<span class="fade">(unverified)</span>
																					</g:if> 
																				
																				</td>
																				<td style="text-align: center;">
																					<g:form action="editItem">																					
																						<g:hiddenField name="id" value="${item?.id}"></g:hiddenField>
																						<g:textField name="quantity" value="${item.quantity}" size="3" />																				
																						<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button></span>
																					</g:form>
																				</td>
																				<td valign="top">
																				</td>
																			</tr>							
																		</g:each>
																	</g:if>
																	<g:else>
																		<tr>
																			<td style="text-align: center">
																				<span class="fade">(empty)</span>
																			</td>
																		</tr>													
																	</g:else>
																</tbody>
															</table>			
														</g:if>												
													</fieldset>
												</g:if>							
											</g:each>									
										</td>
										--%>
										</table>
									</div>
								</fieldset>
							</div>																	

							<br/>
							<hr/>
							<br/>

							<table>
								<tr>
									<td>
<div width="50%">
											<fieldset>
												<legend class="toggle-button">
													&nbsp; <img src="${createLinkTo(dir:'images/icons',file:'box_24.gif')}"
																alt="box" style="vertical-align: middle; width: 24px; height: 24px;" />	&nbsp;Add a Shipment Unit &nbsp;
												</legend>
												<div class="toggleable">
													<g:form action="addContainer">															
														<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />										
														<table>
															<tbody>
																<tr class="prop">
																	<td class="name"><label>Type</label></td>
																	<td class="value">														
																		<g:select 
																			id="containerTypeId" 
																			name='containerTypeId'
																		    noSelection="${['null':'Select One...']}"
																		    from='${ContainerType?.list()}' optionKey="id" optionValue="name">																													
																		</g:select> 
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"><label>Unit #</label></td>
																	<td class="value">
																		<g:textField name="name" size="2" />													
																	</td>
																</tr>											
																<tr class="prop">
																	<td class="name"><label class="optional">Weight</label></td>
																	<td class="value">	
																		<g:textField name="weight" size="15" /> kgs										
																	</td>
																</tr>	
																<tr class="prop">
																	<td class="name"><label class="optional">Dimensions</label></td>
																	<td class="value">	
																		<g:textField name="dimensions" size="15" /> 			
																	</td>
																</tr>	
																<tr class="prop">
																	<td class="name"></td>
																	<td class="value">	
																		<div class="buttons">
																			<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="add" /> Add</button>
																		</div>													
																	</td>
																</tr>	
															</tbody>
														</table>
													</g:form>														
												</div>								
											</fieldset>		
										</div>									
									
									</td>
									<td>
										<div>
											<g:form action="copyContainer">
												<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />											
												<fieldset>									
													<legend class="toggle-button">
														&nbsp; <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_copy.png')}"
																alt="clone" style="vertical-align: middle;" /> &nbsp; Clone Shipment Unit &nbsp; 
													</legend>											
													<div class="toggleable">																
														<table>
															<tbody>
																<tr class="prop">
										                            <td valign="top" class="name"><label><g:message code="container.name.label" default="Which Unit?" /></label></td>                            
										                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																		<g:select name="containerId" from="${shipmentInstance?.containers}" 
																			optionKey="id" optionValue="optionValue" value="" noSelection="['0':'']" />	
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
													</div>							                
								                </fieldset>
										    </g:form>
										</div>
									
									</td>
								</tr>
							</table>
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
