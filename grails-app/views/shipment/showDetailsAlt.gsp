
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.show.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
			valign="top" style="vertical-align: middle;" /> 
			
			${shipmentInstance.name} &nbsp; ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}
			<br/>
			<span style="color: #aaa; font-size: 0.8em;">
				last modified: <format:datetime obj="${shipmentInstance?.lastUpdated}"/>	&nbsp;							
				created: <format:datetime obj="${shipmentInstance?.dateCreated}"/>			
			</span>										
	</content>
</head>

<body>    
	<div >	
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<table>
			<tr>
				<td style="padding:0px;"><!-- necessary for proper layout -->				
					<div id="details" class="section">
						<table>
							<tbody>
								<tr>
									<td style="padding:0px;"><!-- necessary for proper layout -->
										<table>
											<tbody>
												<tr class="prop">
													<td valign="top" class="name"><label><warehouse:message
														code="shipment.currentStatus.label" default="Status" /></label></td>
													<td valign="top" class="value">
														${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventType.name")}
													</td>
												</tr>
												<tr class="prop">
													<td valign="top" class="name"><label><warehouse:message
														code="shipment.origin.label" default="From" /></label></td>
													<td valign="top" class="value">
														${fieldValue(bean: shipmentInstance, field: "origin.name")}<br/>										
														<g:if test="${shipmentInstance.expectedShippingDate}">
															<span style="font-size: 0.8em; color: #aaa">
																Expected to ship on <format:date obj="${shipmentInstance?.expectedShippingDate}" />
															</span>											
														</g:if>
													
													</td>
												</tr>
												<tr class="prop">
													<td valign="top" class="name"><label><warehouse:message
														code="shipment.destination.label" default="To" /></label></td>
													<td valign="top" class="value">
													${fieldValue(bean: shipmentInstance, field: "destination.name")}
													</td>
												</tr>
												<tr class="prop">
													<td valign="top" class="name"><label><warehouse:message
														code="shipment.method.label" default="Shipper" /></label></td>
													<td valign="top" class="value">
													${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")} <img
														src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.png')}"
														valign="top" style="vertical-align: middle;" />
														&nbsp;<br/>
														
														<span class="fade">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</span>
														</td>
												</tr>
												<tr class="prop">
													<td class="name"><label>Documents</label></td>
													<td class="value">
														<div>
															<table>
																<tbody>
																	<g:if test="${!shipmentInstance.documents}">
																		<span class="fade">(empty)</span>
																	</g:if> 
																	<g:else>
																		<tr>
																			<th>Date</th>
																			<th>Document</th>
																			<th style="text-align: center">Download</th>
																		</tr>
																		<g:each in="${shipmentInstance.documents}" var="document" status="i">
																			<tr id="document-${document.id}"
																				class="${(i % 2) == 0 ? 'odd' : 'even'}">
																				<td nowrap="nowrap">
																					<format:date obj="${document?.dateCreated}"/><br/>
																					<span style="font-size: 0.8em; color: #aaa;">
																						<format:time obj="${document.dateCreated}"/>
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
																	</g:else>									
																</tbody>
															</table>														
														</div>
													</td>
												</tr>												
												<tr class="prop">
													<td class="name"><label>Comments</label></td>
													<td class="value">
														<div>
															<table>
																<tbody>
																	<g:if test="${!shipmentInstance.comments}">
																		<span class="fade">(empty)</span>
																	</g:if> 
																	<g:else>
																		<g:each in="${shipmentInstance.comments}" var="comment" status="i">
																			<div id="comment-${comment?.id}">
																				
																					<format:date obj="${comment?.dateCreated}"/><br/>
																					<span style="font-size: 0.8em; color: #aaa;">
																						<format:time obj="${comment?.dateCreated}"/>
																					</span>																					
																					${comment?.comment}

																			</div>
																		</g:each>	
																	</g:else>									
																</tbody>
															</table>														
														</div>
													</td>
												</tr>												
												
											</tbody>
										</table>
									</td>
									<td style="padding:0px;"><!-- necessary for proper layout -->
										<table>
											<tbody>
												<g:if test="${!shipmentInstance.events}">
													
													<tr class="prop">
														<td class="name"><label>Tracking</label></td>
														<td class="value"><span class="fade">(empty)</span></td>
													</tr>
												</g:if> 
												<g:else>
													<tr class="prop">
														<td class="name">
															<label>Tracking</label>													
														</td>
														<td class="value">
																														
														</td>
													</tr>
													<g:each in="${shipmentInstance.events}" var="event" status="i">																			
														<g:if test="${!(i > 2)}">
															<tr class="prop">
																<td class="name">
																	<format:date obj="${event.eventDate}"/><br/>
																	<span style="font-size: 0.8em; color: #aaa;">
																		<format:time obj="${event.eventDate}"/>
																	</span>
																</td>
																<td class="value">
																	${event.eventType.name}<br/>
																	<span style="font-size: 0.8em; color: #aaa;">${event.eventLocation.name}</span>
																</td> 
															</tr>					
														</g:if>					
													</g:each>	
													<tr>
														<td colspan="2">
															<span class="fade" style="font-size: 0.8em; padding-left: 10px;">Showing last 3 out of ${shipmentInstance.events.size()} events</span>
														</td>
													</tr>
												</g:else>
												
											</tbody>
										</table>
									</td>
								</tr>
							
							</tbody>
						
						</table>
							
						<table style="border: 1px solid #aaa;">	
							<tbody>						
								<tr class="prop">
									<td class="name"><label>Packing List</label></td>
								
									<td class="value">
										<div>
											This shipment consists of <b>${shipmentInstance.containers.size()} boxes </b> 
											containing a total of
											<b>${shipmentInstance.allShipmentItems.size()} items</b>
										</div>
									</td>
								</tr>								
							
								<tr class="prop">
									<td class="name"></td>
									<td class="value">
										<div id="containers" class="section">												
											<g:if test="${!shipmentInstance.containers}">
												<div class="fade" style="text-align: center; padding: 50px">(empty)</div>
											</g:if> 
											<g:else>
												<g:each in="${shipmentInstance.containersByType}" var="entry" status="i">									 
													<g:each in="${entry.value}" var="container" status="j">
														<table class="container">
															<tbody>											
																<tr id="container-${container.id}"
																	class="${(j % 2) == 0 ? 'odd' : 'even'}">
																	<td rowspan="3">
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
																		
																		<span style="color: #aaa;">${container?.name}</span>
																	</td>
																</tr>
																<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
																	<td></td>
																	<td valign="top">
																		<div>
																			
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
																<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
																	<td></td>
																	<td> 
			
																		<div style="color: #666; font-size: .75em; padding-left: 10px;">
																			<g:if test="${container.shipmentItems}">
																				<g:each var="item" in="${container.shipmentItems}" 
																					status="k">
																					<span><g:if test="${!(k-1 == container.shipmentItems.size())}">&nbsp;&bull;&nbsp;</g:if></span>
																					<span>${item.quantity} ${item?.product?.name}</span>																					
																				</g:each>															
																			</g:if>
																		</div>															
																	</td>
																</tr>
																
															</tbody>
														</table>										
													</g:each>
												</g:each>									
											</g:else>
										</div>													
									</td>
								</tr>
								<tr class="prop">
									<td class="name"></td>
								
									<td class="value">									
										<div>
			 								<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Document" style="vertical-align: middle"/> add boxes</g:link> &nbsp;
											<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="View Packing List" style="vertical-align: middle"/> packing list</g:link>
											<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Document" style="vertical-align: middle"/> add document
											</a>
											<a href="${createLink(controller: "shipment", action: "addEvent", id: shipmentInstance.id)}">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Event" style="vertical-align: middle"/> add event
											</a>
											<g:link controller="shipment" action="editDetails" id="${shipmentInstance.id}"><img
											src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
											alt="Edit Shipment" style="vertical-align: middle" /> edit details</g:link>											
										</div>								
									
									</td>
								
								</tr>
								
								
							</tbody>
						</table>							
					</div>		
				</td>								
			</tr>
		</table>
	</div>
</body>
</html>
