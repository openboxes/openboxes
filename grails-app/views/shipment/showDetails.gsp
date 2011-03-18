
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
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	
				
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
											code="shipment.currentStatus.label" default="Status" /></label>
										</td>
										<td valign="top" class="value">
											${shipmentInstance?.status.name}<br/>
										</td>
										<td>
											<span class="fade">
												<g:if test="${shipmentInstance?.status?.location}">
													${shipmentInstance?.status.location}
												</g:if>
												<g:if test="${shipmentInstance?.status?.date}">
													on <g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.status.date}"/>
												</g:if>
											</span>
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name" ><label><g:message
											code="shipment.origin.label" default="From" /></label></td>
										<td valign="top" class="value" style="width: 30%;">
											${fieldValue(bean: shipmentInstance, field: "origin.name")}<br/>										
										</td>
										<td>
											<span class="fade">
												<g:if test="${shipmentInstance.expectedShippingDate && !shipmentInstance.hasShipped()}">
													Expected to ship on <g:formatDate date="${shipmentInstance?.expectedShippingDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" />
												</g:if>
											</span>											
										</td>
									</tr>
									<tr class="prop">
										<td class="name"  >
											<label><g:message code="shipment.destination.label" default="To" /></label>
										</td>
										<td class="value" style="width: 30%;">
											<span>
												${fieldValue(bean: shipmentInstance, field: "destination.name")}<br/>
											</span>											
										</td>
										<td>
											<span class="fade">
												<g:if test="${shipmentInstance.expectedDeliveryDate && !shipmentInstance.wasReceived()}">
													Expected to arrive on <g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" />
												</g:if>
											</span>											
										</td>
									</tr>
									<g:if test="${!shipmentWorkflow?.isExcluded('carrier')}">  
										<tr class="prop">
											<td valign="top" class="name" ><label><g:message
												code="shipment.traveler.label" default="Traveler" /></label></td>
											<td valign="top" class="value" style="width: 30%;">
												<g:if test="${shipmentInstance?.carrier}">
													${fieldValue(bean: shipmentInstance, field: "carrier.firstName")}
													${fieldValue(bean: shipmentInstance, field: "carrier.lastName")}
												</g:if>												
											</td>
										</tr>
									</g:if>
									
									<g:if test="${!shipmentWorkflow?.isExcluded('shipmentMethod.shipper')}"> 
										<tr class="prop">
											<td valign="top" class="name"><label><g:message
											code="shipment.freightForwarder.label" default="Freight Forwarder" /></label>
											</td>
											<td valign="top" class="value">
												<g:if test="${shipmentInstance?.shipmentMethod?.shipper}">
													${fieldValue(bean: shipmentInstance, field: "shipmentMethod.shipper.name")} 
													${fieldValue(bean: shipmentInstance, field: "shipmentMethod.shipperService.name")}																														
												</g:if>									
											</td>
											<td>
												<g:if test="${shipmentInstance?.shipmentMethod?.trackingNumber}">
													<span class="fade">
														Tracking # <b>${fieldValue(bean: shipmentInstance, field: "shipmentMethod.trackingNumber")}</b>
													</span>
												</g:if>
											</td>										
										</tr>
									</g:if>
								
									<g:if test="${!shipmentWorkflow?.isExcluded('recipient')}"> 
										<tr class="prop">
											<td class="name"  >
												<label><g:message code="shipment.recipient.label" default="Recipient" /></label>
											</td>
											<td class="value" style="width: 30%;">
												<g:if test="${shipmentInstance?.recipient}">
													<span>
														${fieldValue(bean: shipmentInstance, field: "recipient.firstName")}
														${fieldValue(bean: shipmentInstance, field: "recipient.lastName")}
													</span>
												</g:if>
											</td>
											<td>
												<span class="fade">${fieldValue(bean: shipmentInstance, field: "recipient.email")}</span>
											</td>
										</tr>
									</g:if>
								
									<%-- 
									<tr class="prop">
										<td valign="top" class="name">
											<label><g:message code="shipment.donor.label" default="Donated by" /></label><br/>
										</td>
										<td valign="top" class="">
											<g:if test="${shipmentInstance.donor}">
												${shipmentInstance.donor.name}
											</g:if>
										</td>
										<td>
											&nbsp;
										</td>
									</tr>
									--%>
									
									<!-- list all the reference numbers valid for this workflow -->
									<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}">
										<tr class="prop">
											<td valign="top" class="name" style="width: 10%;"><label><g:message
												code="shipment.${referenceNumberType?.name}" default="${referenceNumberType?.name}" /></label></td>
											<td valign="top" style="width: 30%;">
												<g:findAll in="${shipmentInstance?.referenceNumbers}" expr="it.referenceNumberType.id == referenceNumberType.id">
													${it.identifier}
												</g:findAll>	
											</td>
										</tr>
									</g:each>
									
									<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')}">
										<tr class="prop">
											<td valign="top" class="name">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'money.png')}" alt="money" style="vertical-align: middle"/>
												<label><g:message code="shipment.totalValue.label" default="Total Value" /></label><br/>
											</td>
											<td valign="top" class="">
												<g:if test="${shipmentInstance.totalValue}">
													$ <g:formatNumber format="#,##0.00" number="${shipmentInstance.totalValue}" /><br/>
												</g:if>
											</td>
											<td>
												&nbsp;
											</td>
										</tr>
									</g:if>
									
									<g:if test="${!shipmentWorkflow?.isExcluded('additionalInformation')}">
										<tr class="prop">
											<td valign="top" class="name">
												<label><g:message code="shipment.comments.label" default="Comments" /></label><br/>
											</td>
											<td valign="top" class="">
												<g:if test="${shipmentInstance.additionalInformation}">
													${shipmentInstance.additionalInformation}<br/>
												</g:if>
											</td>
											<td>
												&nbsp;
											</td>
										</tr>
									</g:if>
									
									<g:if test="${shipmentWorkflow?.documentTemplate}">
										<tr class="prop">
											<td valign="top" class="name">
												<label><g:message code="shipment.templates.label" default="Templates" /></label><br/>
											</td>
											<td valign="top" class="">
													<a href="${createLink(controller: "shipment", action: "generateDocuments", id: shipmentInstance.id)}">
													<button>Generate ${shipmentWorkflow.shipmentType?.name} Documents</button></a>
											</td>
											<td>
												&nbsp;
											</td>
										</tr>
									</g:if>
									
									<tr class="prop">
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="document" style="vertical-align: middle"/>
											<label>Documents</label>
										</td>
										<td class="value"  colspan="3">										
											<div>
												<g:if test="${shipmentInstance.documents}">
													<table>
														<tbody>
															<tr>
																<th width="40%">Document</th>
																<th width="15%">Uploaded</th>
																<th width="10%" style="text-align: center"></th>																
															</tr>															
															<g:each in="${shipmentInstance.documents}" var="document" status="i">
																<tr id="document-${document.id}"
																	class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>
																		${document?.documentType?.name} <br/>
																		<span class="fade">
																		${document?.name} 
																		</span>
																	</td>
																	<td>
																		<g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${document?.dateCreated}"/> &nbsp; 
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
																		<g:link class="remove" action="deleteDocument" id="${document?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('Are you sure you want to delete this document?')">
																			<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																		</g:link>																		
																		
																	</td>
																</tr>
															</g:each>	
														</tbody>
													</table>
												</g:if>												
												<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}">
													<button><img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" 
																alt="Upload an Existing Document" style="vertical-align: middle"/> Upload an Existing Document</button></a>													
												
											</div>
										</td>					
									</tr>
									<tr class="prop">
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" alt="comment" style="vertical-align: middle"/>
											<label>Notes</label>
										</td>
										<td class="value" colspan="3">
											<div>
												<g:if test="${shipmentInstance.comments}">
													<table>
														<tbody>
															<tr>
																<th width="40%">Note</th>
																<th width="15%">Date</th>																	
																<th width="10%"></th>
															</tr>
															<g:each in="${shipmentInstance.comments}" var="comment" status="i">
																<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td><i>"${comment?.comment}"</i> -<b>${comment?.sender?.username}</b></td>																	
																	<td>
																		<g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${comment?.dateCreated}"/> &nbsp; 
																		<%-- 
																		<span style="font-size: 0.8em; color: #aaa;">
																			<g:formatDate type="time" date="${comment?.dateCreated}"/>
																		</span>
																		--%>	
																	
																	</td>																		
																	<td style="text-align: right">
																		<g:link class="remove" action="deleteComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('Are you sure you want to delete this note?')">
																			<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																		</g:link>	
																	</td>
																</tr>
															</g:each>	
														</tbody>
													</table>												
												</g:if> 
												<a href="${createLink(controller: "shipment", action: "addComment", id: shipmentInstance.id)}">
													<button><img src="${createLinkTo(dir:'images/icons/silk',file:'comment_add.png')}" 
																alt="Add Notes" style="vertical-align: middle"/> Add Note</button></a>													
																									
											</div>
										</td>
									</tr>		
										
									<tr class="prop">
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'calendar.png')}" alt="events" style="vertical-align: middle"/>
											<label>Events</label>
										</td>
										<td class="value" colspan="3">
											<div>
												<g:if test="${shipmentInstance.events}">
													<table>	
														<tbody>
															<tr>
																<th width="10%">Event</th>
																<th width="20%">Location</th>
																<th width="15%">Date</th>
																<th width="10%"></th>
															</tr>
															<g:each in="${shipmentInstance.events}" var="event" status="i">
																<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
																	<td>																
																		${event?.eventType?.name}
																	</td>
																	<td>
																		${event?.eventLocation?.name}
																	</td>
																	<td>																			
																		<g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${event.eventDate}"/> &nbsp; 																			
																	</td>
																	<td style="text-align: right">
																		<g:link action="editEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
																			<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" style="vertical-align: middle"/>
																		</g:link>
																	</td>
																</tr>
															</g:each>
														</tbody>								
													</table>
												</g:if>
											</div>
										</td>
									</tr>				
									
									<tr class="prop">																									
										<td class="name">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="package" style="vertical-align: middle"/>
											<label>Contents</label>
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
																<th>Package</th>
																<th>Qty</th>
																<th>Lot/Serial No</th>
																<th>Recipient</th>
																<th width="10%"></th>
															</tr>
															<g:set var="count" value="${0 }"/>
															<g:each in="${shipmentInstance.containers}" var="container" status="i">		
																<g:if test="${!container?.parentContainer}">
																	<tr id="container-${container.id}" class="${(count++ % 2 == 0)?'odd':'even'}">
																		<td>	
																			<span style="font-weight: bold;">${container?.containerType?.name}</span> ${container?.name}&nbsp;
																		</td>
																		<td>	
																			${container?.description}
																		</td>
																		<td>
																			<%-- 
																			<span class="fade">${container.shipmentItems.size()}</span>
																			--%>
																		</td>
																		<td>
																			<%-- 
																			${container?.containers?.size()}
																			--%>
																		</td>
																		<td style="text-align: right" nowrap="true">																		 
																			<%-- 
																			<g:link controller="shipment" action="addPackage" id="${shipmentInstance.id}" 
																				params="['parentContainer.id':container.id]"><img 
																				src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" 
																				alt="Add" style="vertical-align: middle"/></g:link>
	
																			<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" 
																				params="['container.id':container.id]"><img 
																				src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}" 
																				alt="Add" style="vertical-align: middle"/></g:link>
	
																			<g:link class="copy" action="copyContainer" id="${container?.id}" params="[shipmentId:shipmentInstance.id]">
																				<img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="Copy" style="vertical-align: middle"/>
																			</g:link>																		
																			<g:link class="remove" action="deleteContainer" id="${container?.id}" params="[shipmentId:shipmentInstance.id]">
																				<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}" alt="Delete" style="vertical-align: middle"/>
																			</g:link>																		
																			--%>
																		</td>		
																	</tr>
																	<g:if test="${container?.shipmentItems }">
																		<g:each in="${container?.shipmentItems}" var="itemInstance">
																			<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																				<td>
																					<span style="padding-left: 30px;">
																						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" style="vertical-align: middle"/>
																						&nbsp; ${itemInstance?.product?.name}
																					</span>
																				</td>
																				<td>${itemInstance?.quantity}</td>																		
																				<td>${itemInstance?.lotNumber}</td>																		
																				<td>${itemInstance?.recipient?.name}</td>																		
																				<td></td>																		
																			</tr>
																		</g:each>
																	</g:if>
																	<g:else>
																		<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																			<td>				
																				<span style="padding-left: 30px">
																					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" style="vertical-align: middle"/>
																					&nbsp; No items					
																				</span>	
																			</td>
																		</tr>													
																	</g:else>

																	
																	<g:each in="${container?.containers}" var="childContainer">
																		<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																			<td>
																				<span style="padding-left: 30px">
																					<img src="${createLinkTo(dir: 'images/icons', file: 'box.png')}" style="vertical-align: middle"/>
																					&nbsp; ${childContainer?.containerType?.name} ${childContainer?.name}
																				</span>
																			</td>																		
																			<td></td>																		
																			<td></td>																		
																			<td></td>		
																			<td style="text-align: right;">																			
																				<%-- 
																				<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" 
																					params="['container.id':container.id]"><img 
																					src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}" 
																					alt="Edit" style="vertical-align: middle"/>
																				</g:link>
		
																				<g:link class="copy" action="copyContainer" id="${container?.id}" params="[shipmentId:shipmentInstance.id]">
																					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="Copy" style="vertical-align: middle"/>
																				</g:link>																		
																				<g:link class="remove" action="deleteContainer" id="${container?.id}" params="[shipmentId:shipmentInstance.id]">
																					<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}" alt="Delete" style="vertical-align: middle"/>
																				</g:link>																		
																				--%>
																			</td>																		
																		</tr>
																		<g:if test="${childContainer?.shipmentItems }">
																			<g:each var="childItem" in="${childContainer?.shipmentItems}">
																				<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																					<td>
																						<span style="padding-left: 60px">
																							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" style="vertical-align: middle"/>
																							&nbsp; ${childItem?.product?.name }
																						</span>
																					</td>
																					<td>${childItem?.quantity}</td>																		
																					<td>${childItem?.lotNumber}</td>																		
																					<td>${childItem?.recipient?.name}</td>																		
																					<td></td>																		
																				</tr>
																			</g:each>
																		</g:if>
																		<g:else>
																			<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																				<td>				
																					<span style="padding-left: 60px">
																						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" style="vertical-align: middle"/>
																						&nbsp; ${childContainer?.name } contains no items 																				
																					</span>
																				</td>
																			</tr>													
																		</g:else>
																	</g:each>
																</g:if>
															</g:each>		
															<g:each in="${shipmentInstance.shipmentItems.findAll { !it.container } }" var="item" status="i">	
																<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																	<td>
																		<span style="padding-left: 0px">
																			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" style="vertical-align: middle"/>
																			&nbsp;${item?.product?.name}
																		</span>
																	</td>
																	<td>
																		${item?.quantity}
																	</td>
																	<td>
																		${item?.lotNumber}
																	</td>
																	<td>
																		${item?.recipient?.name}
																	</td>
																	<td></td>																		
																</tr>
															</g:each>
														</table>							
													</div>
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
