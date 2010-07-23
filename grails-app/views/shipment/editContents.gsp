
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
		${shipmentInstance?.name}
		<span style="color: #aaa; font-size: 0.8em; padding-left: 20px;">
			Created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" /> |
			Updated: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />
		</span>
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
						<td style="width: 60%" valign="top">
							<g:if test="${containerInstance}">							

								<div>
									<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}">
										&laquo; back to shipment units
									</g:link>
								</div>


								<h2>${containerInstance?.name}</h2>
								<table>
									<tbody>
										<g:if test="${containerInstance?.shipmentItems}">
											<tr>
												<th style="text-align: center;">Quantity</th>
												<th>Item</th>
											</tr>
											<g:each var="item" in="${containerInstance.shipmentItems}" status="k">
												<tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
													<td style="text-align: center; width: 20%;">${item.quantity}</td>
													<td>${item?.product?.name}</td>
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
							<g:else>
							
								<table>		
									<tr>
										<th>Name</th>
										<th>Description</th>
										<th>Contains</th>
										<th align="center">Actions</th>
									</tr>
									<g:each in="${shipmentInstance.containersByType}" var="entry" status="i">									 
										<tr>
											<td colspan="4">
												<g:if test="${entry?.key=='Box'}">
													<img src="${createLinkTo(dir:'images/icons',file:'box_24.gif')}"
														alt="box" style="vertical-align: middle; width: 24px; height: 24px;" />
												</g:if>
												<g:elseif test="${entry?.key=='Pallet'}">
													<img src="${createLinkTo(dir:'images/icons',file:'pallet.jpg')}"
														alt="pallet"
														style="vertical-align: middle; width: 24px; height: 24px;" />
												</g:elseif>
												<g:elseif test="${entry?.key=='Suitcase'}">
													<img src="${createLinkTo(dir:'images/icons',file:'suitcase.jpg')}"
														alt="suitcase"
														style="vertical-align: middle; width: 24px; height: 24px;" />
												</g:elseif>
												<g:elseif test="${entry?.key=='Container'}">
													<img src="${createLinkTo(dir:'images/icons',file:'container.jpg')}"
														alt="container" style="vertical-align: middle; width: 24px; height: 24px;" />
												</g:elseif>								
												${entry?.key} Units						
											</td>
										</tr>								
										<g:each in="${entry.value}" var="container" status="j">									
											<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">										
												<td width="20%">
													<div style="color: #666">
														${container?.name}
													</div>												
												</td>
												<td>
													<div>		
														<div style="color: #666; font-size: .75em; padding-left: 10px;">
															Weight: 
															<g:if test="${container.weight}"><b><span style="font-size: 1.25em;">${container?.weight}</span> ${container?.units}</b></g:if> 
															<g:else><b>unknown</b></g:else> 
														</div> 
														<div style="color: #666; font-size: .75em; padding-left: 10px;">
															Dimensions: 
															<g:if test="${container.dimensions}">
																<b>${container.dimensions}</b>
															</g:if> 
															<g:else>
																<b>unknown</b>
															</g:else> 
														</div> 
													</div>			
												</td>
												<td>	
													<div style="color: #666; font-size: .75em; padding-left: 10px;">
														<b><span style="font-size: 1.25em;"><%= container.getShipmentItems().size() %></span> items</b> 
													</div>												 
													<%--
													<fieldset class="container">
														<table>
															<tbody>
																<g:if test="${container?.shipmentItems}">
																	<tr>
																		<th style="text-align: center;">Quantity</th>
																		<th>Item</th>
																	</tr>
																	<g:each var="item" in="${container.shipmentItems}" status="k">
																		<tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
																			<td style="text-align: center; width: 20%;">${item.quantity}</td>
																			<td>${item?.product?.name}</td>
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
													</fieldset>
													--%>
												</td>						
												<td width="20%">
													<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add" style="vertical-align: middle"/>
													</g:link>											
													<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" style="vertical-align: middle"/>
													</g:link>											
													<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="Edit Contents" style="vertical-align: middle"/>
													</g:link>											
													<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page_delete.png')}" alt="Edit Contents" style="vertical-align: middle"/>
													</g:link>											
												</td>
											</tr>
										</g:each>		
									</g:each>
								</table>
							</g:else>
						</td>
						<td width="1%"></td>
						<td valign="top">						
							<fieldset>
									
								
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
														</div>
													</td>												
												</tr>									
											</table>									
										</g:form>			
									</div>						
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
										
								</g:if>
								<g:else>
								
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
								
								
								</g:else>
								
						
							</fieldset>
						
						</td>
					</tr>
				</tbody>
			</table>
							
		</div>

	</div>
</body>
</html>
