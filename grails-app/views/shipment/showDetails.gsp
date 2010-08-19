
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
		Show Shipment
	</content>
</head>

<body>    
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
				
		<table>
			<tr>
				<td colspan="2">
					<div style="padding-bottom: 10px;">
						<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
						&raquo; <span style="font-size: 90%">Show Details</span>
					</div>										
				</td>			
			</tr>
			<tr>
				<td width="75%">				
					<fieldset>
						<div>
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
						</div>				
				
						<div id="details" class="section">
							<table cellspacing="5" cellpadding="5">
								<tbody>

									<tr>
										<td>&nbsp;</td>
									</tr>
								
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.currentStatus.label" default="Status" /></label></td>
										<td valign="top" class="">
											${fieldValue(bean: shipmentInstance, field: "mostRecentStatus")}<br/> 
											<span style="font-size: 0.8em; color: #aaa">
												${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventType.name")} <br/>
												${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventLocation.name")} &nbsp;
												<g:formatDate format="dd MMM yyyy" date="${shipmentInstance?.mostRecentEvent?.eventDate}"/>
											</span>
										</td>
										<td valign="top" class="name"><label><g:message
											code="shipment.method.label" default="Shipper" /></label></td>
										<td valign="top" class="">
											<table>
												<tbody>
													<tr>
														<td>
															 ${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")} <br/> 
															<span class="fade" style="font-size: 0.8em;">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</span>
														
														</td>
														<td style="width:16px;">
															<img src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.png')}"
															valign="top" style="vertical-align: middle;" />
														</td>
													</tr>
												</tbody>
											
											</table>
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.origin.label" default="From" /></label></td>
										<td valign="top" style="width: 30%;">
											<span>
												${fieldValue(bean: shipmentInstance, field: "origin.name")}<br/>										
												<g:if test="${shipmentInstance.expectedShippingDate}">
													<span style="font-size: 0.8em; color: #aaa">
														Expected to ship on <g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="dd MMM yyyy" />
													</span>											
												</g:if>
											</span>
										</td>
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.destination.label" default="To" /></label>
										</td>
										<td class="value" style="width: 30%;">
											<span>
												${fieldValue(bean: shipmentInstance, field: "destination.name")}<br/>
												<g:if test="${shipmentInstance.expectedDeliveryDate}">
													<span style="font-size: 0.8em; color: #aaa">
														Expected to arrive on <g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" format="dd MMM yyyy" />
													</span>											
												</g:if>
											</span>											
										</td>
									</tr>
									
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.carrier.label" default="Carrier" /></label></td>
										<td valign="top" style="width: 30%;">
											<span>${fieldValue(bean: shipmentInstance, field: "carrier")}</span>
										</td>
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.destination.label" default="Recipient" /></label>
										</td>
										<td class="value" style="width: 30%;">
											<span>${fieldValue(bean: shipmentInstance, field: "recipient")}</span>
										</td>
									</tr>



									<tr class="prop">
										<td>&nbsp;</td>
									</tr>
									
									<tr class="prop">
										<td valign="top" class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'money.png')}" alt="money" style="vertical-align: middle"/>
										<label><g:message
											code="shipment.totalValue.label" default="Total Value" /></label><br/></td>
										<td valign="top" class="">
											<g:if test="${shipmentInstance.totalValue}">
												$<g:formatNumber format="#,###.##" number="${shipmentInstance.totalValue}" /><br/>
											</g:if>
											<g:else>
												<span class="fade">(empty)</span>
											</g:else>
										</td>
									</tr>
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>
						

									<tr class="prop">																									
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="package" style="vertical-align: middle"/>
											<label>Packages</label>
										</td>										
										<td class="value" colspan="3">
											<div>
												<g:if test="${!shipmentInstance.containers}">
													<div class="fade">(empty)</div>											
												</g:if> 
												<g:else>
													<div id="containers" class="section">											
														<table>		
															<tr>
																<th width="5%"></th>
																<th width="15%">Shipment Units</th>
																<th width="15%" style="text-align: center"># Items</th>
																<th width="10%" style="text-align: center"></th>															
															</tr>
															<g:each in="${shipmentInstance.containers}" var="container" status="i">		
																<tr>										
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
																		<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">
																			<span>${container?.containerType?.name} #${container?.name}</span> 
																		</g:link>
																		&nbsp;
																	</td>
																	<td style="text-align: center;">
																		<span class="fade">${container.shipmentItems.size()} items</span>
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
																		
																		<%-- 
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
																		--%>														
																	</td>
																	<td></td>
																	
																	<td style="text-align: center">
																		<!-- 
																		<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" 
																		params="['container.id':container.id]"><img 
																		src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" 
																		alt="Add" style="vertical-align: middle"/></g:link>																	
																		 -->
																	</td>
																	
																</tr>
															</g:each>		
														</table>							
													</div>
												</g:else>
											</div>
										</td>
									</tr>
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>
									<tr class="prop">
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="document" style="vertical-align: middle"/>
											<label>Documents</label>
										</td>
										<td class="value"  colspan="3">										
											<div>
												<g:if test="${!shipmentInstance.documents}">
													<div class="fade">(empty)</div>											
												</g:if> 
												<g:else>													
													<table>
														<tbody>
															<tr>
																<th width="5%"></th>
																<th width="40%">Document</th>
																<th width="15%"></th>
																<th width="10%" style="text-align: center">Download</th>																
															</tr>															
															<g:each in="${shipmentInstance.documents}" var="document" status="i">
																<tr id="document-${document.id}"
																	class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}.</td>
																	<td>
																		${document?.documentType?.name}
																	</td>
																	<td>
																		<%-- 
																		<g:formatDate format="dd MMM yyyy" date="${document?.dateCreated}"/> &nbsp; 
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${document.dateCreated}"/>
																		</span>
																		--%>																							
																	</td>					
																	<td style="text-align: center">
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
										</td>					
									</tr>
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>
									
									<tr class="prop">
																							
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" alt="comment" style="vertical-align: middle"/>
											<label>Comments</label>
										</td>
										<td class="value" colspan="3">
										
											<div>
													<table>
														<tbody>
															<g:if test="${!shipmentInstance.comments}">
																<div class="fade">(empty)</div>
															</g:if> 
															<g:else>
																<tr>
																	<th width="5%"></th>
																	<th width="15%">Date</th>																	
																	<th width="40%">Comment</th>
																	<th width="15%"></th>
																	<th width="10%"></th>
																</tr>
															
																<g:each in="${shipmentInstance.comments}" var="comment" status="i">
																	<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																		<td>${i+1}</td>
																		<td>
																			<g:formatDate format="dd MMM yy" date="${comment.dateCreated}"/> &nbsp; 
																			<span style="font-size: 0.8em; color: #aaa;">
																				<g:formatDate type="time" date="${comment?.dateCreated}"/>
																			</span>	
																		
																		</td>																		
																		<td>${comment?.comment}	</td>																	
																		<td>	
																			<%--																	
																			 --%>			
																		</td>
																		<td></td> 
																	</tr>
																</g:each>	
															</g:else>									
														</tbody>
													</table>	
											</div>
										</td>
									</tr>			
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>																		
									<tr class="prop">
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'calendar.png')}" alt="activity" style="vertical-align: middle"/>
											<label>Activity</label>
										</td>
										<td class="" colspan="3">
											<div>
													<g:if test="${!shipmentInstance.events}">
														<span class="fade">(empty)</span>
													</g:if> 
													<g:else>
														<table>	
															<tbody>
																<tr>
																	<th width="5%"></th>
																	<th width="15%">Date</th>
																	<th width="40%">Description</th>
																	<th width="15%"></th>
																	<th width="10%"></th>
																</tr>
																<g:each in="${shipmentInstance.events}" var="event" status="i">
																	<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																		<td>${i+1}.</td>
																		<td>																			
																			<g:formatDate format="dd MMM yy" date="${event.eventDate}"/> &nbsp; 
																			<span style="font-size: 0.8em; color: #aaa;">
																				<g:formatDate type="time" date="${event.eventDate}"/>
																			</span>																			
																		</td>
																		<td>
																		
																			${event.eventType.name} @ ${event.eventLocation.name}
																		</td>
																		<td>
																			<%--
																			<g:formatDate format="dd MMM yy" date="${event.eventDate}"/> &nbsp; 
																			<span style="font-size: 0.8em; color: #aaa;">
																				<g:formatDate type="time" date="${event.eventDate}"/>
																			</span> --%>
																		</td>
																		<td></td> 
																	</tr>
																</g:each>
																<!-- 
																<tr>
																	<td colspan="3">
																		<span class="fade" style="font-size: 0.8em; padding-left: 10px;">Showing last 3 out of ${shipmentInstance.events.size()} events</span>
																	</td>
																</tr>
																 -->																
															</tbody>								
														</table>
													</g:else>
											</div>
										</td>
									</tr>									

								</tbody>
							</table>
						</div>
					</fieldset>
				</td>		
				<td width="30%">
					<div style="width: 300px">
						<fieldset>
							<legend>Actions</legend>
							
							<h3>Prepare Shipment</h3>
							<table>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
										alt="Show Shipment" style="vertical-align: middle" /> &nbsp; <b>show details</b></g:link>
									
									</td>
								</tr>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}"><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
										alt="Add Document" style="vertical-align: middle"/> &nbsp; edit contents</a></g:link>
									</td>
								</tr>
								
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="editDetails" id="${shipmentInstance.id}"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
										alt="Edit Shipment" style="vertical-align: middle" /> &nbsp; edit details</g:link>
									
									</td>
								</tr>
								<tr class="prop">
									<td>
										<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}"><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'page_word.png')}" 
										alt="Add Document" style="vertical-align: middle"/> &nbsp; attach document</a>										
									
									</td>
								</tr>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
										alt="View Packing List" style="vertical-align: middle"/> &nbsp; view packing list</g:link>		
									</td>
								</tr>					
							</table>
							<br/>
							<h3>Send Shipment</h3>
							<table>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="sendShipment" id="${shipmentInstance.id}"><img
										src="${createLinkTo(dir:'images/icons',file:'truck.png')}"
										alt="Send Shipment" style="vertical-align: middle" /> &nbsp; send shipment</g:link>
									
									</td>
								</tr>
							</table>							
							<br/>
							<h3>Receive Shipment</h3>
							<table>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="receiveShipment" id="${shipmentInstance.id}"><img
										src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}"
										alt="Send Shipment" style="vertical-align: middle" /> &nbsp; receive shipment</g:link>
									
									</td>
								</tr>
							</table>							
							
						</fieldset>
					</div>
					<br/>
					
					<div style="width: 300px">
						<fieldset>
							<legend>Tracking</legend>
							<table>
								<g:each in="${shipmentInstance.events}" var="event" status="i">
									
									<tr class="">
										<td>
											<g:if test="${i == 0}"><b>${event?.eventType?.name}</b></g:if>
											<g:else><span style="font-size: 0.8em; color: #aaa;">${event?.eventType?.name}</span></g:else>
										</td>
										<td>
											
											<g:if test="${i == 0}">
												<b><g:formatDate format="dd MMM yy" date="${event.eventDate}"/>
												&nbsp; <g:formatDate type="time" date="${event.eventDate}"/>
												</b>
											
											</g:if>
											<g:else>
												<span style="font-size: 0.8em; color: #aaa;">
													<g:formatDate format="dd MMM yy" date="${event.eventDate}"/> &nbsp;
													<g:formatDate type="time" date="${event.eventDate}"/>
												</span>
											</g:else>
										</td>
									</tr>
								</g:each>
							</table>
						</fieldset>
					</div>
				</td>	
			</tr>
		</table>
	</div>
</body>
</html>
