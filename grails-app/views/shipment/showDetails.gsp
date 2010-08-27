
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

						<g:render template="summary"/>
						
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
											${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventType.name")}
											<span style="font-size: 0.8em; color: #aaa">
												${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventLocation.name")} &nbsp;
												<g:formatDate format="dd MMM yyyy" date="${shipmentInstance?.mostRecentEvent?.eventDate}"/>
											</span>
										</td>
										<td valign="top" class="name"><label><g:message
											code="shipment.method.label" default="Shipper" /></label>
										</td>
										<td valign="top" class="">
											<g:if test="${shipmentInstance.shipmentMethod}">
												<table>
													<tbody>
														<tr>
															<td>
																${fieldValue(bean: shipmentInstance, field: "shipmentMethod.shipper.name")} <br/> 
																<span class="fade" style="font-size: 0.8em;">${fieldValue(bean: shipmentInstance, field: "shipmentMethod.trackingNumber")}</span>														
															</td>
															<%-- 
															<td style="width:16px;">
																<img src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.shipperService?.name + '.png')}"
																valign="top" style="vertical-align: middle;" />
															</td>
															--%>
														</tr>
													</tbody>											
												</table>
											</g:if>
											<g:else>
												<span class="fade">(none)</span>
											</g:else>										
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
											<span>
												${fieldValue(bean: shipmentInstance, field: "carrier.firstName")}
												${fieldValue(bean: shipmentInstance, field: "carrier.lastName")}
											</span>
											<span class="fade" style="font-size: 0.8em;">${fieldValue(bean: shipmentInstance, field: "carrier.email")}</span>
										</td>
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.destination.label" default="Recipient" /></label>
										</td>
										<td class="value" style="width: 30%;">
											<span>
												${fieldValue(bean: shipmentInstance, field: "recipient.firstName")}
												${fieldValue(bean: shipmentInstance, field: "recipient.lastName")}
											</span>
											<span class="fade" style="font-size: 0.8em;">${fieldValue(bean: shipmentInstance, field: "recipient.email")}</span>
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
																<th width="15%">Created</th>
																<th width="40%">Package</th>
																<th width="10%"></th>
															</tr>
															<g:each in="${shipmentInstance.containers}" var="container" status="i">		
																<tr id="container-${container.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}.</td>
																	<td>
																		<g:formatDate format="dd MMM yyyy" date="${container?.dateCreated}"/> <br/>
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${container.dateCreated}"/>
																		</span>																		
																	</td>
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
																			<span>${container?.containerType?.name} #${container?.name}</span> &nbsp;
																			<span class="fade">${container.shipmentItems.size()} items</span>
																		</g:link>
																	</td>
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
																<th width="15%">Uploaded</th>
																<th width="40%">Document</th>
																<th width="10%" style="text-align: center">Download</th>																
															</tr>															
															<g:each in="${shipmentInstance.documents}" var="document" status="i">
																<tr id="document-${document.id}"
																	class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>${i+1}.</td>
																	<td>
																		<g:formatDate format="dd MMM yyyy" date="${document?.dateCreated}"/> &nbsp; 
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${document.dateCreated}"/>
																		</span>																		
																	</td>
																	<td>
																		${document?.documentType?.name} <br/>
																		<span class="fade">
																		${document?.name} 
																		</span>
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
																	<td>${comment.sender.username}</td> 
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
																<th width="40%">Location</th>
																<th width="10%">Status</th>
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
																		${event.eventLocation.name}
																	</td>
																	<td>																
																		${event.eventType.name}
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
