
<%@ page import="org.pih.warehouse.shipping.ContainerType"%>
<%@ page import="org.pih.warehouse.shipping.Document"%>
<%@ page import="org.pih.warehouse.shipping.DocumentType"%>
<%@ page import="org.pih.warehouse.shipping.EventType"%>
<%@ page import="org.pih.warehouse.core.Location"%>
<%@ page import="org.pih.warehouse.core.Organization"%>
<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.shipping.ReferenceNumberType"%>
<%@ page import="org.pih.warehouse.shipping.Shipment"%>
<%@ page import="org.pih.warehouse.user.User"%>
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
			var id = $(this).attr('id');	
			var isClicked = $.cookie(id);
			if (isClicked) { 
				$.removeCookie(id);
			}
			else { 
				$.setCookie(id, "clicked");
			}	
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
	});
	
</script>

		
		<div id="containers" class="section">		
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<g:if test="${containerInstance}">			
								<div style="padding-bottom: 10px;">
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
									 &nbsp; &raquo; &nbsp;
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}">Shipment Units</g:link>
									 &nbsp; &raquo; &nbsp; 
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':containerInstance.id]">${containerInstance?.name}</g:link>
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
						</td>
					</tr>					
					<tr>
						<td style="width: 60%" valign="top">
							<fieldset>							
								<table>
									<tbody>
										<tr>
											<td width="24px;">
												<%-- 
												<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
													valign="top" style="vertical-align: middle;" /> 
												--%>
												<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
													alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />						
											</td>
											<td>
												<span style="font-size: 1.2em;">${shipmentInstance.name}</span> 
												&nbsp; 
												<br/>
												<span style="color: #aaa; font-size: 0.8em;">
													last modified: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />	&nbsp;							
													created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" />			
												</span>	
											</td>		
											<td style="text-align: right;">
												<span class="fade">[Shipment No. ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}]</span>
											</td>
										</tr>
									</tbody>
								</table>			
									
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
												<fieldset>												
													<div id="container-${container?.id}" class="container">
														<a href="#" id="container-${container?.id}">													
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
															${container?.containerType?.name} ${container?.name } &nbsp; (${container.shipmentItems.size()} items)</a> 
													</div> 
												    <div class="details" style="background-color: white;">
												 		<g:form action="editContainer">					
													 		<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"></g:hiddenField>												    
															<g:hiddenField name="containerId" value="${container?.id}"></g:hiddenField>												    
													    	<table>
																<tr class="prop">										
																	<td class="name"><label>${container?.containerType?.name} #</label></td>
																	<td class="value">
																		<g:textField name="name" value="${container?.name}" size="3" />
																	</td>
																	<td class="name"><label>Recipient</label></td>
																	<td class="value">
																		<g:textField name="recipient" value="" size="25"/><br/>
																		<span class="fade">(e.g. 'First Last' or 'email@email.com')</span>
																	</td>													
																</tr>
																
																<tr class="prop">										
																	<td class="name"><label>Weight</label></td>
																	<td class="value">
																		<g:textField name="weight" value="${container?.weight}" size="5"/> 
																		<g:textField name="units" value="${container?.units}" size="3"/> 
																	</td>
																	<td class="name"><label>Dimensions</label></td>
																	<td class="value">
																		<g:textField name="dimensions" value="${container?.dimensions}" size="15"/> <br/>
																		<span class="fade">(e.g. 10.1" x 4.2" x 2.8")</span> 
																	</td>																
																</tr>
																<tr class="prop">
																	<td class="name"><label>Contents</label></td>
																	<td class="value" colspan="3">																
																		<div style="color: #666; font-size: .75em; padding-left: 10px;">
																			<fieldset>
																				<table>
																					<tbody>
																							<tr>
																								<th></th>
																								<th style="text-align: center;">Qty</th>
																								<th>Item</th>
																								<th>Serial Number</th>
																								<th>Recipient</th>
																								<th></th>
																							</tr>
																						<g:if test="${container?.shipmentItems}">
																							<g:each var="item" in="${container.shipmentItems}" status="k">
																								<tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
																									<td width="5%">${k+1}.</td>
																									<td style="text-align: center;">
																										<g:hiddenField name="shipmentItems[${k}].id" value="${item.id}"></g:hiddenField>												    
																										<g:textField name="shipmentItems[${k}].quantity" value="${item.quantity}" size="3" />
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
																										<g:textField name="shipmentItems[${k}].recipient" value="${item.recipient}" size="20" />																									
																										<!-- 
																										<gui:autoComplete size="20" 
																											id="shipmentItems[${k}].recipient" name="shipmentItems[${k}].recipient" 
																											controller="shipment" action="availableContacts"/>																																							
																										 -->
																									</td>
																								</tr>							
																							</g:each>
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
																				<span class="fade">(enter '0' into quantity to remove item)</span>
																			</fieldset>		
																		</div>	
																	</td>
																</tr>
																<tr class="prop">
																	<td class="name"></td>
																	<td class="value">
																		<div class="buttons">
																			<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																		</div>
																	
																	</td>
																</tr>
															</table>
														</g:form>
													</div>
												</fieldset>
											</td>
											<td valign="top" style="">				
												<div style="text-align: left">
													<ul>
														<!-- 
														<li>
						 									<g:link controller="shipment" action="closeContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
														</li>
														<li>
						 									<g:link controller="shipment" action="editContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
														</li>
														<li>
						 									<g:link controller="shipment" action="copyContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
														</li>
														 -->
														<li>
						 									<g:link controller="shipment" action="deleteContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="" style="vertical-align: middle"/></a></g:link> &nbsp;
														</li>
													</ul>
												</div>								
											</td>																
																	
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
						</td>						
						<td width="1%"></td>
						<td valign="top" width="25%">						
							
								<fieldset>
									<legend>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}"
											alt="box" style="vertical-align: middle;" />
										Add a Shipment Item
									</legend>
									<div>
										<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	
											<table>
												<tr class="prop">
													<td class="name"><label>Add item</label></td>
													<td class="value">
														<gui:autoComplete size="20" 
															id="selectedItem" name="selectedItem" 
															controller="shipment" action="availableItems" />
													</td>												
												</tr>								
											
												<tr class="prop">												
													<td class="name"><label>to ...</label></td>
													<td class="value">
														<g:select name="container.id" from="${shipmentInstance?.containers}" 
															optionKey="id" optionValue="optionValue" value="" noSelection="['0':'- all shipment units -']" />					
														<%-- 										
														<g:hiddenField name="container.id" value="${containerInstance?.id}"></g:hiddenField>
														--%>
													</td>												
												</tr>			
												<tr class="prop">
													<td class="name"><label>Quantity</label></td>
													<td class="value">
														<g:textField name="quantity" value="1" size="3" />
													</td>													
												</tr>
												<tr class="prop">
													<td class="name"><label>Recipient</label></td>
													<td class="value">
														<g:textField name="recipient" value="" size="20" style="" /><br/>
														<span class="fade">(e.g. 'First Last' or 'email@email.com')</span>
													</td>													
												</tr>
												<tr class="prop">
													<td class="name"></td>
													<td class="value">
														<div class="buttons">
															<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add</button></span>
														</div>
													</td>												
												</tr>									
											</table>									
										</g:form>			
									</div>				
								</fieldset>

							
							<%-- 
								<br/>
								<fieldset>
									<legend>Edit Shipment Unit</legend>
									<div>
										<g:form action="editContainer">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="containerId" value="${containerInstance?.id}" />
											<table>
												<tbody>
													<tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="container.name.label" default="Nickname" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
															<g:textField name="name" value="${containerInstance?.name}" />
						                                </td>
							                        </tr>  	          
													<tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="container.name.label" default="Dimensions" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'dimensions', 'errors')}">
															<g:textField name="dimensions" value="${containerInstance?.dimensions}" />
						                                </td>
							                        </tr>  	          
													<tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="container.name.label" default="Weight" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'weight', 'errors')}">
															<g:textField name="weight" value="${containerInstance?.weight}" />
						                                </td>
							                        </tr>  	          
							                        <tr class="prop">
													    <td valign="top" class="name"></td>													    
													    <td class="value">
															<div class="buttons">
																<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
															</div>
													    </td>					                        
							                        </tr>         
							                    </tbody>
							                </table>
									    </g:form>									
									</div>
								</fieldset>
							--%>
		
								<br/>
									
							<%-- 		
									<div>
										<h2>All shipment units</h2>
										<table>
											<tbody>
												<tr>
													<th>Shipment unit</th>
												</tr>
												<g:each in="${shipmentInstance.containers}" var="container" status="i">											
													<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
														<td >
															<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">															
																<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add" style="vertical-align: middle"/>
																${container.name}
															</g:link>																												
															&nbsp;
															(<%= container.getShipmentItems().size() %> items) 																												
														</td>
													</tr>
												</g:each>											
											</tbody>																		
										</table>										
									</div>
									--%>

								<g:form action="copyContainer">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />											
									<fieldset>
										<div>
											<legend>
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_copy.png')}"
													alt="clone" style="vertical-align: middle;" />	
											
													Clone Shipment Unit
											</legend>
											<table>
												<tbody>
													<tr class="prop">
							                            <td valign="top" class="name"><label><g:message code="container.name.label" default="Shipment Unit ..." /></label></td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
															<g:select name="containerId" from="${shipmentInstance?.containers}" 
																optionKey="id" optionValue="optionValue" value="" noSelection="['0':'']" />	
						                                </td>
							                        </tr>  	          
													<tr class="prop">
							                            <td valign="top" class="name"><label><g:message code="container.copies.label" default="# of Copies" /></label></td>                            
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

										
								<br/>

								<fieldset>
									<legend>
										<img src="${createLinkTo(dir:'images/icons',file:'box_24.gif')}"
											alt="box" style="vertical-align: middle; width: 24px; height: 24px;" />	
										Add a Shipment Unit
									</legend>
									<div>
										<g:form action="addContainer">															
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />										
											<table>
												<tbody>
													<tr class="prop">
														<td class="name">Type</td>
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
														<td class="name">Unit #</td>
														<td class="value">
															<g:textField name="name" />													
														</td>
													</tr>											
													<tr class="prop">
														<td class="name">Weight</td>
														<td class="value">	
															<g:textField name="weight" size="15" /> kgs										
														</td>
													</tr>	
													<tr class="prop">
														<td class="name">Dimensions</td>
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
								<br/>
						</td>
					</tr>
				</tbody>
			</table>
							
		</div>

	</div>
</body>
</html>
