
<%@ page import="org.pih.warehouse.shipping.ContainerType" %>
<%@ page import="org.pih.warehouse.shipping.Document" %>
<%@ page import="org.pih.warehouse.shipping.DocumentType" %>
<%@ page import="org.pih.warehouse.shipping.EventType" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.core.Organization" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<%@ page import="org.pih.warehouse.shipping.ReferenceNumberType" %>
<%@ page import="org.pih.warehouse.shipping.Shipment" %>
<%@ page import="org.pih.warehouse.user.User" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
			valign="top" style="vertical-align: middle;" /> 
			${shipmentInstance.name}
			<span style="color: #aaa; font-size: 0.8em;">
				Created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" /> |
				Updated: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />				
			</span>
	</content>
</head>

<body>    
	<div class="body">	
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<table>
			<tr>
				<td>
					<div id="details" class="section">
						<div style="">
							<table cellspacing="5" cellpadding="5">
								<tbody>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.shipmentNumber.label" default="Shipment #" /></label></td>
										<td valign="top" class="value">
											${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.shipmentType.label" default="Type" /></label></td>
										<td valign="top" class="value">
											${fieldValue(bean: shipmentInstance, field: "shipmentType.name")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.shipmentStatus.label" default="Name" /></label></td>
										<td valign="top" class="value">
											${fieldValue(bean: shipmentInstance, field: "name")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.currentStatus.label" default="Status" /></label></td>
										<td valign="top" class="value">
											${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventType.name")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.origin.label" default="From" /></label></td>
										<td valign="top" class="value">
											${fieldValue(bean: shipmentInstance, field: "origin.name")}<br/>										
											<g:if test="${shipmentInstance.expectedShippingDate}">
												<span style="font-size: 0.8em; color: #aaa">
													Expected to ship on <g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="dd MMM yyyy" />
												</span>											
											</g:if>
										
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.destination.label" default="To" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "destination.name")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.method.label" default="Shipper" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")} <img
											src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.png')}"
											valign="top" style="vertical-align: middle;" /></td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.trackingNumber.label" default="Tracking #" /></label></td>
										<td valign="top" class="value">										
											${fieldValue(bean: shipmentInstance, field: "trackingNumber")}											                            	
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"></td>
										<td valign="top" class="value">
										
											<span class="button">
												<g:link controller="shipment" action="editDetails" id="${shipmentInstance.id}"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
												alt="Edit Shipment" style="vertical-align: middle" /> edit details</g:link></span> 
																				
										</td>				
									
									</tr>									
									<tr class="prop">
										<td colspan="2">
											
										</td>
									</tr>									
									<tr class="prop">
										<td class="name"><label>Documents</label></td>
										<td class="value">
											<div style="padding: 5px;">
												<g:if test="${!shipmentInstance.documents}">
													<span class="fade">There are no documents for this
													shipment.</span>
												</g:if> 
												<g:else>
													<table>
														<tbody>
															<tr>
																<th>Date</th>
																<th>Document</th>
																<th style="text-align: center">Download</th>
															</tr>
														
															<g:each in="${shipmentInstance.documents}" var="document" status="i">
																<tr id="document-${document.id}"
																	class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td nowrap="nowrap">
																		<g:formatDate format="dd MMM yyyy" date="${document?.dateCreated}"/><br/>
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${document.dateCreated}"/>
																		</span>																							
																	</td>					
																	<td>
																		${document?.documentType?.name}
																	</td>
																	<td nowrap="nowrap" style="text-align: center">
																		<g:link controller="document" action="download" id="${document.id}">
																			<img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" alt="Download" style="vertical-align: middle"/>													
																		</g:link>
																	</td>
																</tr>
															</g:each>										
														</tbody>
													</table>
												</g:else>
											</div>
											<div>
												<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Document" style="vertical-align: middle"/> add document</a>
											</div>										
										</td>
									</tr>
									<tr class="prop">
										<td colspan="2">
											
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label>Tracking</label></td>
										<td class="value">

											<div style="padding: 5px;">
												<g:if test="${!shipmentInstance.events}">
													<span class="fade">There are no events for this
													shipment.</span>
												</g:if> 
												<g:else>
													<table >	
														<tbody>
															<tr>
																<th>Date</th>
																<th>Description</th>
															</tr>
															<g:each in="${shipmentInstance.events}" var="event" status="i">
																<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td nowrap="nowrap">
																		<g:formatDate format="dd MMM yy" date="${event.eventDate}"/><br/>
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${event.eventDate}"/>
																		</span>
																	</td>
																	<td nowrap="nowrap">
																		${event.eventType.name}<br/>
																		<span style="font-size: 0.8em; color: #aaa;">
																			${event.eventLocation.name}</span>
																	</td> 
																</tr>										
															</g:each>	
														</tbody>								
													</table>
												</g:else>
											</div>
											<div>
												<a href="${createLink(controller: "shipment", action: "addEvent", id: shipmentInstance.id)}"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Event" style="vertical-align: middle"/> add event</a>
											</div>										
										</td>
									</tr>									
									<tr class="prop">
										<td colspan="2">
											
										</td>
									</tr>
								</tbody>
							</table>
						</div>
					</div>
				</td>					
			
				<td width="60%">
					<div id="containers" class="section">
											
						<fieldset>
							<legend>Shipment Units</legend>
											
							<div style="padding: 5px;">
								<g:if test="${!shipmentInstance.containers}">
									<span class="fade" style="text-align: center">There are no shipping units in this
									shipment.</span>
								</g:if> 
								<g:else>
									<g:each in="${shipmentInstance.containersByType}" var="entry" status="i">									 
								
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
												alt="container"
												style="vertical-align: middle; width: 24px; height: 24px;" />
										</g:elseif>
										${entry?.key} Units						
								
										<g:each in="${entry.value}" var="container" status="j">
											<table class="container">
												<tbody>											
													<tr id="container-${container.id}"
														class="${(j % 2) == 0 ? 'odd' : 'even'}">
														<td>&nbsp;</td>
														<td valign="top" nowrap="nowrap">
															<div>
																<span style="color: #aaa;">${container?.name}</span>
																<span style="color: #aaa; font-size: .75em; padding-left: 10px;">
																	Weight: 
																	<g:if test="${container.weight}"><b>${container?.weight} ${container?.units}</b></g:if> 
																	<g:else><b>unknown</b></g:else> 
																</span> 
																<span style="color: #aaa; font-size: .75em; padding-left: 10px;">
																	Dimensions: 
																	<g:if test="${container.dimensions}">
																		<b>${container.dimensions}</b>
																	</g:if> 
																	<g:else>
																		<b>unknown</b>
																	</g:else> 
																</span> 
																<span style="color: #aaa; font-size: .75em; padding-left: 10px;">
																	Contains: 
																	<b><%= container.getShipmentItems().size() %> items</b> 
																</span>												 
															</div>	
														</td>
													</tr>
												</tbody>
											</table>										
										</g:each>
										<br/>										
									</g:each>									
								</g:else>
							</div>
							<div style="padding: 5px; text-align: left"> 								
								<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit Contents" style="vertical-align: middle"/> edit contents</g:link> &nbsp;
								<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="View Packing List" style="vertical-align: middle"/> packing list</g:link>
							</div>								
						</fieldset>											
					</div>
				</td>
			</tr>
		</table>
	</div>
</body>
</html>
