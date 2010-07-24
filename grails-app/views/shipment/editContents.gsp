
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
	
		<div id="containers" class="section">		
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<g:if test="${containerInstance}">			
								<div style="padding-bottom: 10px;">
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
									 &nbsp; &raquo; &nbsp;
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}">Show all boxes</g:link>
									 &nbsp; &raquo; &nbsp; 
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':containerInstance.id]">${containerInstance?.name}</g:link>
									&nbsp; &raquo; &nbsp;
									<span style="font-size: 90%">Included Items</span>
								</div>					
							</g:if>
							<g:else>
								<div style="padding-bottom: 10px;">
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
									&raquo; <span style="font-size: 90%">Packing List</span>
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
								
								<h2>Shipping Packages</h2>									
								<table>		
									<tr>
										<th width="5%"></th>
										<th>Name</th>
										<th width="70%">Contents</th>
									</tr>
									<g:each in="${shipmentInstance.containers}" var="container" status="i">	
									
									
										<g:set var="cssStyle"></g:set>
										<g:if test="${containerInstance?.id == container?.id}">
											<g:set var="cssStyle">background-color: white</g:set>											
										</g:if>
										<tr style="${cssStyle}" class="${(i % 2) == 0 ? 'odd' : 'even'}">										
											<td>${i+1}.</td>
											<%--
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
											</td>
											 --%>
											<td>			
												
												<div>													
													<g:if test="${containerInstance?.id == container?.id}">
														<span style="font-size:1.25em">${container?.name}</span>
													</g:if>
													<g:else>
														<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
															<span style="font-size:1.25em">${container?.name}</span>
														</g:link>
													</g:else>
													&nbsp; 
													<span class="fade">
														<g:if test="${container.shipmentItems}">
															(${container.shipmentItems.size()} items)
														</g:if>
														<g:else>
															(empty)
														</g:else>
													</span>
												</div>												
												
												<%-- 
												<div style="color: #666; font-size: .75em;">
													Weight: &nbsp;
														<g:if test="${container.weight}">
															${container?.weight} ${container?.units}
														</g:if> 
														<g:else><b>unknown</b></g:else> 
													
													<div>
													Dimensions: &nbsp;
														<g:if test="${container.dimensions}">
															${container.dimensions}
														</g:if> 
														<g:else>
															<b>unknown</b>
														</g:else> 													
													</div>
													<div>
														# Items: &nbsp;<%= container.getShipmentItems().size() %></span>
													</div>
												</div>
												--%>
											</td>
											<%-- 
											<td>
												<div style="color: #666; font-size: .75em; padding-left: 10px;">
													<g:if test="${container.shipmentItems}">
														<ul>
															<g:each var="item" in="${container.shipmentItems}" 
																status="k">
																<li>
																	<span><g:if test="${!(k-1 == container.shipmentItems.size())}">&nbsp;&bull;&nbsp;</g:if></span>
																	<span>${item.quantity} ${item?.product?.name}</span>
																</li>
															</g:each>
														</ul>															
													</g:if>
												</div>														
											</td>
											--%>
											
											<%-- 
											<td width="20%">
												<div style="text-align: left">
													<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
					 									<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Items" style="vertical-align: middle"/> add items
					 								</g:link>												
													<!--
													<ul>
						 								<li> 						
						 									<g:link controller="shipment" action="editContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="" style="vertical-align: middle"/> edit box</a></g:link> &nbsp;
						 								</li>
						 								<li>
						 									<g:link controller="shipment" action="copyContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="" style="vertical-align: middle"/> copy box</a></g:link> &nbsp;
						 								</li>
						 								<li>
						 									<g:link controller="shipment" action="deleteContainer" id="${container.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="" style="vertical-align: middle"/> delete box</a></g:link> &nbsp;
						 								</li>
					 								</ul>
					 								-->
												</div>								
											</td>
											
											--%>
											
											<td width="70%">
												<fieldset>
												<g:if test="${containerInstance.id == container.id}">
													<g:if test="${containerInstance}">																
													
														<table>
															<tbody>
																<g:if test="${containerInstance?.shipmentItems}">
																	<tr>
																		<th></th>
																		<th>Item</th>
																		<th style="text-align: center;" width="20%">Qty</th>
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
																			<td style="text-align: center;" width="20%;">${item.quantity}</td>
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
												</g:if>
											</fieldset>
											</td>
											
											
											
										</tr>
									</g:each>		
								</table>

							</fieldset>							
						</td>
						
						
						
						<td width="1%"></td>
						<td valign="top" width="25%">						
							<fieldset>
									<div>
										<g:form action="addContainer">															
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />										
											<h2>Add a shipment unit</h2>
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
														<td class="name">Name</td>
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
																<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
															</div>													
														</td>
													</tr>	
												</tbody>
											
											</table>
										</g:form>														
									</div>								
									<br/><br/>							
							
								<g:if test="${containerInstance}">
									<div>
										<h2>Add a product</h2>										
										<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	
											<table>
												<tr class="prop">
													<td class="name"><label>Add To</label></td>
													<td class="value">
														${containerInstance?.name}
														<%-- 														
														<g:select name="container.id" from="${shipmentInstance?.containers}" 
															optionKey="id" optionValue="name" value="" noSelection="['0':'']" />															
														--%>										
														<g:hiddenField name="container.id" value="${containerInstance?.id}"></g:hiddenField>
													</td>												
												</tr>								
												<tr class="prop">
													<td class="name"><label>Item</label></td>
													<td class="value">
														<gui:autoComplete size="20" 
															id="selectedItem" name="selectedItem" 
															controller="shipment" action="availableItems"/>												
													</td>												
												</tr>								
												<tr class="prop">
													<td class="name"></td>
													<td class="value">
													
														<div class="buttons">
															<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add</button></span>
															<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" alt="Done" /> Done</button></span>
														</div>
						
														
														
													</td>												
												</tr>									
											</table>									
										</g:form>			
									</div>				
									
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
										
								</g:if>
								
						
							</fieldset>
						
						</td>
					</tr>
				</tbody>
			</table>
							
		</div>

	</div>
</body>
</html>
