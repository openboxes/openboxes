
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Show Shipment</content>
</head>

<body>    
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
				
		<table>
			<tr>
				<td width="75%">				
					<fieldset>
						<g:render template="summary"/>						
						<div id="details" class="section">
							<table cellspacing="5" cellpadding="5">
								<tbody>

									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.currentStatus.label" default="Status" /></label></td>
										<td valign="top" class="">
											${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventType.name")}<br/>
										</td>
										<td>
											<span class="fade">
												${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventLocation.name")}
												on <g:formatDate format="dd MMM yyyy" date="${shipmentInstance?.mostRecentEvent?.eventDate}"/>
											</span>
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.origin.label" default="From" /></label></td>
										<td valign="top" style="width: 30%;">
											<span>
												${fieldValue(bean: shipmentInstance, field: "origin.name")}<br/>										
											</span>
										</td>
										<td>
											<span class="fade">
												<g:if test="${shipmentInstance.expectedShippingDate}">
													Expected to ship on <g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="MMM dd yyyy" />
												</g:if>
												<g:else>
													No expected shipping date yet.
												</g:else>
											</span>											
										</td>
									</tr>
									<tr class="prop">
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.destination.label" default="To" /></label>
										</td>
										<td class="" style="width: 30%;">
											<span>
												${fieldValue(bean: shipmentInstance, field: "destination.name")}<br/>
											</span>											
										</td>
										<td>
											<span class="fade">
												<g:if test="${shipmentInstance.expectedDeliveryDate}">
													Expected to arrive on <g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" format="MMM dd yyyy" />
												</g:if>
												<g:else>
													No expected delivery date yet.
												</g:else>
											</span>											
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.method.label" default="Shipper" /></label>
										</td>
										<td valign="top" class="">
											${fieldValue(bean: shipmentInstance, field: "shipmentMethod.shipper.name")} 
											${fieldValue(bean: shipmentInstance, field: "shipmentMethod.shipperService.name")} 
																														
										</td>
										<%-- 
										<td style="width:16px;">
											<img src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.shipperService?.name + '.png')}"
											valign="top" style="vertical-align: middle;" />
										</td>
										--%>
										<td>
											<span class="fade">Tracking Number <b>${fieldValue(bean: shipmentInstance, field: "shipmentMethod.trackingNumber")}</b></span>										
										</td>										
									</tr>
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.carrier.label" default="Carrier" /></label></td>
										<td valign="top" style="width: 30%;">
											<g:if test="${shipmentInstance?.carrier}">
												<span>
													${fieldValue(bean: shipmentInstance, field: "carrier.firstName")}
													${fieldValue(bean: shipmentInstance, field: "carrier.lastName")}
												</span>
											</g:if>
											<g:else>
												<span class="fade">(empty)</span>
											</g:else>												
										</td>
										<td>
											<span class="fade">${fieldValue(bean: shipmentInstance, field: "carrier.email")}</span>
										</td>
									</tr>
									<%-- 
									<tr class="prop">
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.recipient.label" default="Recipient" /></label>
										</td>
										<td class="value" style="width: 30%;">
											<g:if test="${shipmentInstance?.recipient}">
												<span>
													${fieldValue(bean: shipmentInstance, field: "recipient.firstName")}
													${fieldValue(bean: shipmentInstance, field: "recipient.lastName")}
												</span>
											</g:if>
											<g:else>
												<span class="fade">(empty)</span>
											</g:else>												
											
										</td>
										<td>
											<span class="fade">${fieldValue(bean: shipmentInstance, field: "recipient.email")}</span>
										</td>
									</tr>
									--%>
									<%-- 
									<tr class="prop">
										<td valign="top" class="name">
											<label><g:message code="shipment.donor.label" default="Donated by" /></label><br/>
										</td>
										<td valign="top" class="">
											<g:if test="${shipmentInstance.donor}">
												${shipmentInstance.donor.name}
											</g:if>
											<g:else>
												<span class="fade">(empty)</span>
											</g:else>										
										
										</td>
										<td>
											&nbsp;
										</td>
									</tr>
									--%>
									<tr class="prop">
										<td valign="top" class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'money.png')}" alt="money" style="vertical-align: middle"/>
											<label><g:message code="shipment.totalValue.label" default="Total Value" /></label><br/>
										</td>
										<td valign="top" class="">
											<g:if test="${shipmentInstance.totalValue}">
												$<g:formatNumber format="#,###.##" number="${shipmentInstance.totalValue}" /><br/>
											</g:if>
											<g:else>
												<span class="fade">(empty)</span>
											</g:else>
										</td>
										<td>
											&nbsp;
										</td>
									</tr>
<!-- 									
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>																		
 -->									
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
																<th>Package</th>
																<th>Description</th>
																<th>Items</th>
																<th>Status</th>
																<th width="10%"></th>
															</tr>
															<g:each in="${shipmentInstance.containers}" var="container" status="i">		
																<tr id="container-${container.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}.</td>
																	<td>	
																		<span>${container?.containerType?.name}-${container?.name}</span> &nbsp;
																	</td>
																	<td>	
																		${container?.description}
																	</td>
																	<td>
																		<span class="fade">${container.shipmentItems.size()}</span>
																	</td>
																	<td>	
																		<g:if test="${container?.containerStatus}">
																			<span>${container?.containerStatus?.name}</span>
																		</g:if>
																		<g:else>
																			<span class="fade">(none)</span>
																		</g:else>
																	</td>
																	<td style="text-align: right" nowrap="true">																		 
																		<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" 
																			params="['container.id':container.id]"><img 
																			src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" 
																			alt="Add" style="vertical-align: middle"/></g:link>

																		&nbsp;
																		
																		<g:link class="copy" action="copyContainer" id="${container?.id}" params="[shipmentId:shipmentInstance.id]">
																			<img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="Copy" style="vertical-align: middle"/>
																		</g:link>																		

																		&nbsp;
																		<%-- 
																		<g:if test="${container}">
																			<g:link controller="shipment" action="openContainer" id="${shipmentInstance.id}" params="['container.id':container.id]">
																				<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Pack" style="vertical-align: middle"/>														
																			</g:link>
																		</g:if>
																		<g:else>
																			<g:link controller="shipment" action="closeContainer" id="${shipmentInstance.id}" params="['container.id':container.id]">
																				<img src="${createLinkTo(dir:'images/icons/',file:'pack-shipment.png')}" alt="Pack" style="vertical-align: middle"/>														
																			</g:link>
																		</g:else>
																		&nbsp;
																		--%>
																		<g:link class="remove" action="deleteContainer" id="${container?.id}" params="[shipmentId:shipmentInstance.id]">
																			<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																		</g:link>																		
																	</td>																	
																</tr>
															</g:each>		
															
															
														</table>							
													</div>
												</g:else>
											</div>
										</td>
									</tr>
<!-- 									
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>																		
 -->									
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
																<th width="15%">Uploaded</th>
																<th width="10%" style="text-align: center"></th>																
															</tr>															
															<g:each in="${shipmentInstance.documents}" var="document" status="i">
																<tr id="document-${document.id}"
																	class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}.</td>
																	<td>
																		${document?.documentType?.name} <br/>
																		<span class="fade">
																		${document?.name} 
																		</span>
																	</td>
																	<td>
																		<g:formatDate format="MMM dd yyyy" date="${document?.dateCreated}"/> &nbsp; 
																		<%--
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${document.dateCreated}"/>
																		</span>
																		 --%>																		
																	</td>
																	<td style="text-align: right">
																		<g:link controller="document" action="download" id="${document.id}">
																			<img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" alt="Download" style="vertical-align: middle"/>													
																		</g:link>
																		&nbsp;
																		<g:link class="remove" action="deleteDocument" id="${document?.id}" params="[shipmentId:shipmentInstance.id]">
																			<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
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
<!-- 									
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>																		
 -->									
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
																<th width="40%">Comment</th>
																<th width="15%">Date</th>																	
																<th width="10%"></th>
															</tr>
														
															<g:each in="${shipmentInstance.comments}" var="comment" status="i">
																<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}</td>
																	<td><i>"${comment?.comment}"</i> --<b>${comment?.sender?.username}</b></td>																	
																	<td>
																		<g:formatDate format="MMM dd yyyy" date="${comment?.dateCreated}"/> &nbsp; 
																		<%-- 
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${comment?.dateCreated}"/>
																		</span>
																		--%>	
																	
																	</td>																		
																	<td style="text-align: right">
																		<g:link class="remove" action="deleteComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]">
																			<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																		</g:link>	
																	</td>
																</tr>
															</g:each>	
														</g:else>									
													</tbody>
												</table>	
											</div>
										</td>
									</tr>			
<!-- 									
									<tr class="prop">
										<td>&nbsp;</td>
									</tr>																		
 -->									
									<tr class="prop">
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'calendar.png')}" alt="events" style="vertical-align: middle"/>
											<label>Events</label>
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
																<th width="20%">Location</th>
																<th width="10%">Status</th>
																<th width="15%">Date</th>
																<th width="10%"></th>
															</tr>
															<g:each in="${shipmentInstance.events}" var="event" status="i">
																<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}.</td>
																	<td>
																		${event.eventLocation.name}
																	</td>
																	<td>																
																		${event.eventType.name}
																	</td>
																	<td>																			
																		<g:formatDate format="MMM dd yyyy" date="${event.eventDate}"/> &nbsp; 
																		<%-- 
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${event.eventDate}"/>
																		</span>
																		--%>																			
																	</td>
																	<td style="text-align: right">
																		<g:link class="remove" action="deleteEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
																			<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																		</g:link>	
																	</td>
																	
																	
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
				<td width="20%">				
					<g:render template="sidebar" />
				</td>	
			</tr>
		</table>
	</div>
</body>
</html>
