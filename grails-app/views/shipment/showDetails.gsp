
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Show Shipment</content>
	<style>
		th { width: 200px; } 
	</style>
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
			
		<div class="dialog">
			<fieldset>
				<g:render template="summary"/>						
				<div id="details" class="section">
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.details.label" default="Details" /></label>
								</td>
								<td valign="top" class="value">
									<table style="display:inline">
										<tbody>								
											<tr style="height: 30px;">
												<th valign="top"><label><g:message code="shipment.currentStatus.label" default="Status" /></label>
												</th>
												<td valign="top">
													${shipmentInstance?.status.name}<br/>
													<span class="fade">
														<g:if test="${shipmentInstance?.status?.location}">
															${shipmentInstance?.status.location}
														</g:if>
														<g:if test="${shipmentInstance?.status?.date}">
															on <format:date obj="${shipmentInstance?.status.date}"/>
														</g:if>
													</span>
												</td>
											</tr>
											<tr style="height: 30px;">
												<th valign="top"><label><g:message
													code="shipment.origin.label" default="Departing" /></label>
												</th>
												<td valign="top" >
													<span>
														${fieldValue(bean: shipmentInstance, field: "origin.name")}									
													</span>
													<span class="fade">
														<g:if test="${shipmentInstance.expectedShippingDate && !shipmentInstance.hasShipped()}">
															on <format:date obj="${shipmentInstance?.expectedShippingDate}"/>
														</g:if>
													</span>											
												</td>
											</tr>
											<tr style="height: 30px;">
												<th valign="top">
													<label><g:message code="shipment.destination.label" default="Arriving" /></label>
												</th>
												<td >
													<span>
														${fieldValue(bean: shipmentInstance, field: "destination.name")}
													</span>											
													<span class="fade">
														<g:if test="${shipmentInstance.expectedDeliveryDate && !shipmentInstance.wasReceived()}">
															on <format:date obj="${shipmentInstance?.expectedDeliveryDate}"/>
														</g:if>
													</span>											
												</td>
											</tr>
										</tbody>
									</table>
								</td>
							</tr>
										
							<tr class="prop">
								<td valign="top" class="name"><label>
									<img src="${createLinkTo(dir:'images/icons/silk',file:'tag.png')}" alt="tag" style="vertical-align: middle"/>
									<g:message
										code="shipment.referenceNumbers.label" default="Reference #s" /></label>
								</td>
								<td valign="top" class="value">
									<table style="display:inline">
										<tbody>								
											<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}">
												<tr style="height: 30px;">								
												<!-- list all the reference numbers valid for this workflow -->
													<th valign="top" ><label><g:message
														code="shipment.${referenceNumberType?.name}" default="${referenceNumberType?.name}" /></label></th>
													<td valign="top">
														<g:findAll in="${shipmentInstance?.referenceNumbers}" expr="it.referenceNumberType.id == referenceNumberType.id">
															${it.identifier }
														</g:findAll>	
													</td>
												</tr>
											</g:each>
										</tbody>
									</table>
								</td>
							</tr>	
										
										
							<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')||!shipmentWorkflow?.isExcluded('statedValue')}">
								<tr class="prop">
									<td valign="top" class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'money.png')}" alt="money" style="vertical-align: middle"/>
										<label><g:message code="shipment.value.label" default="Value" /></label>
									</td>
									<td valign="top" class="value">
										<table style="display:inline">
											<tbody>																	
												<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')}">
													<tr>
														<th valign="top">
															<label><g:message code="shipment.totalValue.label" default="Total Value" /></label><br/>
														</th>
														<td valign="top" >
															<g:if test="${shipmentInstance.totalValue}">
																$<g:formatNumber format="#,##0.00" number="${shipmentInstance.totalValue}" /><br/>
															</g:if>
															<g:else>
																<span class="fade">N/A</span>
															</g:else>
														</td>
													</tr>	
												</g:if>
												
												<g:if test="${!shipmentWorkflow?.isExcluded('statedValue')}">
													<tr>
														<th valign="top">
															<label><g:message code="shipment.statedValue.label" default="Stated Value" /></label><br/>
														</th>
														<td valign="top">
															<g:if test="${shipmentInstance.statedValue}">
																$<g:formatNumber format="#,##0.00" number="${shipmentInstance.statedValue}" /><br/>
															</g:if>
															<g:else>
																<span class="fade">N/A</span>
															</g:else>
														</td>
													</tr>
												</g:if>									
											</tbody>
										</table>
									</td>
								</tr>
							</g:if>
							
							<%-- 
							<g:if test="${!shipmentWorkflow?.isExcluded('shipmentMethod.shipper')}"> 
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
									code="shipment.freightForwarder.label" default="Freight Forwarder" /></label>
									</td>
									<td valign="top" >
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
									<td  style="width: 30%;">
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
							--%>
							<g:if test="${!shipmentWorkflow?.isExcluded('carrier')}">  
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.traveler.label" default="Traveler" /></label></td>
									<td valign="top" >
										<g:if test="${shipmentInstance?.carrier}">
											${fieldValue(bean: shipmentInstance, field: "carrier.firstName")}
											${fieldValue(bean: shipmentInstance, field: "carrier.lastName")}
										</g:if>												
									</td>
								</tr>
							</g:if>
											
	
							<g:if test="${!shipmentWorkflow?.isExcluded('additionalInformation')}">
								<tr class="prop">
									<td valign="top" class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" alt="comments" style="vertical-align: middle"/>
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
								<td class="value" colspan="3">										
									<div>
										<g:if test="${shipmentInstance.documents}">
											<table>
												<tbody>
													<tr>
														<th>Document</th>
														<th>Uploaded</th>
														<th style="text-align: center"></th>																
													</tr>															
													<g:each in="${shipmentInstance.documents}" var="document" status="i">
														<tr id="document-${document.id}"
															class="${(i % 2) == 0 ? 'odd' : 'even'}">
															<td>
																<g:set var="f" value="${document?.filename?.toLowerCase()}"/>
																<g:if test="${f.endsWith('.jpg')||f.endsWith('.png')||f.endsWith('.gif') }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'picture.png')}"/>
																</g:if>
																<g:elseif test="${f.endsWith('.pdf') }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_acrobat.png')}"/>
																</g:elseif>
																<g:elseif test="${f.endsWith('.doc')||f.endsWith('.docx') }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}"/>
																</g:elseif>
																<g:elseif test="${f.endsWith('.xls')||f.endsWith('.xlsx')||f.endsWith('.csv') }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"/>2
																</g:elseif>
																<g:elseif test="${f.endsWith('.gz')||f.endsWith('.jar')||f.endsWith('.zip')||f.endsWith('.tar') }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_compressed.png')}"/>
																</g:elseif>
																<g:else>
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white.png')}"/> 
																</g:else>
																<g:link controller="document" action="download" id="${document.id}">
																	${document?.filename}
																</g:link> 
																<span class="fade">
																${document?.documentType?.name}	
																</span>
															</td>
															<td>
																<format:date obj="${document?.dateCreated}"/> &nbsp; 																	
															</td>
															<td style="text-align: right">			
																<g:link action="editDocument" params="[documentId:document.id,shipmentId:shipmentInstance.id]">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Download" style="vertical-align: middle"/>													
																</g:link>
																&nbsp;
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
										
									</div>
									
								</td>					
							</tr>
							<tr class="prop">
								<td class="name">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'note.png')}" alt="note" style="vertical-align: middle"/>
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
																<format:date obj="${comment?.dateCreated}"/> &nbsp; 
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
																<format:date obj="${event.eventDate}"/> &nbsp; 																			
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
										<g:if test="${!shipmentInstance.shipmentItems}">
											<div class="fade">empty</div>											
										</g:if> 
										<g:else>
											<div id="items" class="section">											
												<table>		
													<tr>
														<th style="white-space:nowrap;">&nbsp;</th>
														<th style="white-space:nowrap;">Lot/Serial No</th>
														<th style="white-space:nowrap;">Qty</th>
														<g:if test="${shipmentInstance?.wasReceived()}">
															<th style="white-space:nowrap;">Received</th>
														</g:if>
														<th style="white-space:nowrap;">Recipient</th>
													</tr>
													<g:set var="count" value="${0 }"/>
													<g:set var="previousContainer"/>
													
													<g:set var="shipmentItems" value="${shipmentInstance.shipmentItems.sort{(it?.container?.parentContainer) ? it?.container?.parentContainer?.sortOrder : it?.container?.sortOrder} }"/>
													<g:each in="${shipmentItems}" var="item" status="i">
														<g:if test="${previousContainer != item?.container}">
															<tr class="${(count++ % 2 == 0)?'odd':'even'}">
																<th nowrap colspan="${shipmentInstance?.wasReceived() ? 5 : 4}">
																	<%-- <img src="${createLinkTo(dir: 'images/icons/silk', file: 'package.png')}" style="vertical-align: middle"/>&nbsp;--%>
																	<g:if test="${item?.container?.parentContainer}">${item?.container?.parentContainer?.name } &rsaquo;</g:if>
																	<g:if test="${item?.container?.name }">${item?.container?.name }</g:if>
																	<g:else>Unpacked</g:else>
																	<span class="fade">
														 				<g:if test="${item?.container?.weight || item?.container?.width || item?.container?.length || item?.container?.height}">
															 				( 
															 				<g:if test="${item?.container?.weight}">
															 					${item?.container?.weight} ${item?.container?.weightUnits}, 
															 				</g:if>
																			${item?.container.height ?: '?'} ${item?.container?.volumeUnits}
																			x
																			${item?.container.width ?: '?'} ${item?.container?.volumeUnits}
																			x
																			${item?.container.length ?: '?'} ${item?.container?.volumeUnits}
																			)
																		</g:if>
																	</span>	
																</th>
															</tr>													
														</g:if>												
														<tr class="${(count++ % 2 == 0)?'odd':'even'}">
															<td width="100%">
																<g:link controller="inventoryItem" action="showStockCard" id="${item?.product?.id}">
																	${item?.product?.name}
																</g:link>
															</td>
															<td style="white-space:nowrap;">
																${item?.lotNumber}
															</td>
															<td style="white-space:nowrap;">
																${item?.quantity}
															</td>
															<g:if test="${shipmentInstance?.wasReceived()}">
																<g:set var="qtyReceived" value="${item?.quantityReceived()}"/>
																<td style="white-space:nowrap;${qtyReceived != item?.quantity ? ' color:red;' : ''}">
																	${qtyReceived}
																</td>
															</g:if>														
															<td style="white-space:nowrap;">
																${item?.recipient?.name}
															</td>
														</tr>
														<g:set var="previousContainer" value="${item.container }"/>
														
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
		</div>
	</div>
</body>
</html>
