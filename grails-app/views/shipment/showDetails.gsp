
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipping.shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.show.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
	<style>
		th { width: 200px; } 
		
		.newContainer { border-top: 1px solid lightgrey }
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
								<td valign="top" class="name"><label><warehouse:message
									code="shipping.details.label"/></label>
								</td>
								<td valign="top" class="value">
									<table style="display:inline">
										<tbody>								
											<tr style="height: 30px;">
												<td valign="top">
													<label><warehouse:message code="default.status.label" /></label>
												</td>
												<td valign="top">
													<format:metadata obj="${shipmentInstance?.status.code}"/>
													<span class="fade">
														<g:if test="${shipmentInstance?.status?.location}">
															<warehouse:message code="default.by.label"/> ${shipmentInstance?.status.location}
														</g:if>
														<g:if test="${shipmentInstance?.status?.date}">
															<warehouse:message code="default.on.label"/> <format:date obj="${shipmentInstance?.status.date}"/>
														</g:if>
													</span>
												</td>
											</tr>
											<tr style="height: 30px;">
												<td valign="top">
													<g:if test="${shipmentInstance?.expectedShippingDate && new Date().after(shipmentInstance?.expectedShippingDate) }">												
														<label><warehouse:message code="shipping.departed.label" /></label>
													</g:if>
													<g:else>
														<label><warehouse:message code="shipping.departing.label "/></label>
													</g:else>
												</td>
												<td valign="top" >
													<span>
														${fieldValue(bean: shipmentInstance, field: "origin.name")}									
													</span>
													<span class="fade">
														<g:if test="${shipmentInstance.expectedShippingDate && !shipmentInstance.hasShipped()}">
															<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
														</g:if>
													</span>											
												</td>
											</tr>
											<tr style="height: 30px;">
												<td valign="top">
													<g:if test="${shipmentInstance?.expectedDeliveryDate && new Date().after(shipmentInstance?.expectedDeliveryDate) }">
														<label><warehouse:message code="shipping.arrived.label" /></label>
													</g:if>
													<g:else>
														<label><warehouse:message code="shipping.arriving.label" /></label>
													</g:else>
												</td>
												<td>
													<span>
														${fieldValue(bean: shipmentInstance, field: "destination.name")}
													</span>											
													<span class="fade">
														<g:if test="${shipmentInstance.expectedDeliveryDate && !shipmentInstance?.wasReceived()}">
															<format:date obj="${shipmentInstance?.expectedDeliveryDate}"/>
														</g:if>
													</span>											
												</td>
											</tr>
											<tr>
												
												<td valign="top"><label>
													<warehouse:message
														code="shipping.totalWeight.label" /></label>
												</td>
												<td valign="top" class="value">
													<g:formatNumber format="#,##0.00" number="${shipmentInstance?.totalWeightInPounds() ? shipmentInstance?.totalWeightInPounds() : 0.00 }" />
													<warehouse:message code="default.lbs.label"/>
												</td>												
											</tr>
										</tbody>
									</table>
								</td>
							</tr>
										
							<tr class="prop">
								<td valign="top" class="name"><label>
									<img src="${createLinkTo(dir:'images/icons/silk',file:'tag.png')}" alt="tag" style="vertical-align: middle"/>
									<warehouse:message code="shipping.referenceNumbers.label"/></label>
								</td>
								<td valign="top" class="value">
									<table style="display:inline">
										<tbody>								
											<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}">
												<tr style="height: 30px;">								
												<!-- list all the reference numbers valid for this workflow -->
													<td valign="top" ><label><format:metadata obj="${referenceNumberType}"/></label></td>
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
										<label><warehouse:message code="shipping.value.label"/></label>
									</td>
									<td valign="top" class="value">
										<table style="display:inline">
											<tbody>																	
												<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')}">
													<tr>
														<td valign="top">
															<label><warehouse:message code="shipping.totalValue.label" /></label><br/>
														</td>
														<td valign="top" >
															<g:if test="${shipmentInstance.totalValue}">
																$<g:formatNumber format="#,##0.00" number="${shipmentInstance.totalValue}" /><br/>
															</g:if>
															<g:else>
																<span class="fade"><warehouse:message code="default.na.label"/></span>
															</g:else>
														</td>
													</tr>	
												</g:if>
												
												<g:if test="${!shipmentWorkflow?.isExcluded('statedValue')}">
													<tr>
														<td valign="top">
															<label><warehouse:message code="shipping.statedValue.label" /></label><br/>
														</td>
														<td valign="top">
															<g:if test="${shipmentInstance.statedValue}">
																$<g:formatNumber format="#,##0.00" number="${shipmentInstance.statedValue}" /><br/>
															</g:if>
															<g:else>
																<span class="fade"><warehouse:message code="default.na.label"/></span>
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
									<td valign="top" class="name"><label><warehouse:message
									code="shipping.freightForwarder.label" default="Freight Forwarder" /></label>
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
										<label><warehouse:message code="shipping.recipient.label" default="Recipient" /></label>
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
									<td valign="top" class="name"><label><warehouse:message
										code="shipping.traveler.label" /></label></td>
									<td valign="top" >
										<g:if test="${shipmentInstance?.carrier}">
											${fieldValue(bean: shipmentInstance, field: "carrier.firstName")}
											${fieldValue(bean: shipmentInstance, field: "carrier.lastName")}
										</g:if>												
									</td>
								</tr>
							</g:if>
											
	
							<g:if test="${!shipmentWorkflow?.isExcluded('additionalInformation') && shipmentInstance?.additionalInformation}">
								<tr class="prop">
									<td valign="top" class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" alt="comments" style="vertical-align: middle"/>
										<label><warehouse:message code="default.comments.label" /></label><br/>
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
										<label><warehouse:message code="shipping.templates.label" /></label><br/>
									</td>
									<td valign="top" class="">
											<a href="${createLink(controller: "shipment", action: "generateDocuments", id: shipmentInstance.id)}">
											<button><warehouse:message code="shipping.generateDocuments.label" args="[format.metadata(obj:shipmentWorkflow.shipmentType)]"/></button></a>
									</td>
									<td>
										&nbsp;
									</td>
								</tr>
							</g:if>
							<g:if test="${shipmentInstance.documents}">
								<tr class="prop">
									<td class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="document" style="vertical-align: middle"/>
										<label><warehouse:message code="document.documents.label"/></label>
									</td>
									<td class="value" colspan="3">										
										<div>
											<table>
												<tbody>
													<tr>
														<th><warehouse:message code="document.label"/></th>
														<th><warehouse:message code="document.uploaded.label"/></th>
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
																														
																<g:link class="remove" action="deleteDocument" id="${document?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteDocument.message')}')">
																	<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																</g:link>																		
																
															</td>
														</tr>
													</g:each>	
												</tbody>
											</table>
										</div>
									</td>					
								</tr>
							</g:if>
							<g:if test="${shipmentInstance.comments}">
								<tr class="prop">
									<td class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'note.png')}" alt="note" style="vertical-align: middle"/>
										<label>Notes</label>
									</td>
									<td class="value" colspan="3">
										<div>
											<table>
												<tbody>
													<tr>
														<th width="40%"><warehouse:message code="shipping.note.label"/></th>
														<th width="15%"><warehouse:message code="default.date.label"/></th>																	
														<th width="10%"></th>
													</tr>
													<g:each in="${shipmentInstance.comments}" var="comment" status="i">
														<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
															<td><i>"${comment?.comment}"</i> -<b>${comment?.sender?.username}</b></td>																	
															<td>
																<format:date obj="${comment?.dateCreated}"/> &nbsp; 
															</td>																		
															<td style="text-align: right">
																<g:link class="remove" action="deleteComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteNote.message')}')">
																	<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
																</g:link>	
															</td>
														</tr>
													</g:each>	
												</tbody>
											</table>												
										</div>
									</td>
								</tr>		
							</g:if> 
								
							<g:if test="${shipmentInstance.events}">
								<tr class="prop">
									<td class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'calendar.png')}" alt="events" style="vertical-align: middle"/>
										<label>Events</label>
									</td>
									<td class="value" colspan="3">
										<div>
											<table>	
												<tbody>
													<tr>
														<th width="10%"><warehouse:message code="default.event.label"/></th>
														<th width="20%"><warehouse:message code="location.label"/></th>
														<th width="15%"><warehouse:message code="default.date.label"/></th>
														<th width="10%"></th>
													</tr>
													<g:each in="${shipmentInstance.events}" var="event" status="i">
														<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
															<td>																
																<format:metadata obj="${event?.eventType}"/>
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
										</div>
									</td>
								</tr>				
							</g:if>
							
							<g:if test="${shipmentInstance.shipmentItems}">
								<tr class="prop">																									
									<td class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="package" style="vertical-align: middle"/>
										<label><warehouse:message code="shipping.contents.label"/></label>
									</td>										
									<td class="value" colspan="3">
										
										<div id="items" class="section">											
											<table border="0">		
												<tr>
													<th style="white-space:nowrap;"><warehouse:message code="shipping.package.label"/></th>
													<th style="white-space:nowrap;"><warehouse:message code="product.label"/></th>
													<th style="white-space:nowrap;"><warehouse:message code="default.lotSerialNo.label"/></th>
													<th style="white-space:nowrap;"><warehouse:message code="default.expires.label"/></th>
													<th style="white-space:nowrap;"><warehouse:message code="default.qty.label"/></th>
													<g:if test="${shipmentInstance?.wasReceived()}">
														<th style="white-space:nowrap;"><warehouse:message code="shipping.received.label"/></th>
													</g:if>
													<th style="white-space:nowrap;"><warehouse:message code="shipping.recipient.label"/></th>
												</tr>
												<g:set var="count" value="${0 }"/>
												<g:set var="previousContainer"/>
												
												<g:set var="shipmentItems" value="${shipmentInstance.shipmentItems.sort{(it?.container?.parentContainer) ? it?.container?.parentContainer?.sortOrder : it?.container?.sortOrder} }"/>
												<g:each in="${shipmentItems}" var="item" status="i">
													<g:set var="newContainer" value="${previousContainer != item?.container }"/>
													<tr class="${(count++ % 2 == 0)?'odd':'even'}" >
														<g:if test="${newContainer}">
															<td nowrap class="newContainer">
																<%-- <img src="${createLinkTo(dir: 'images/icons/silk', file: 'package.png')}" style="vertical-align: middle"/>&nbsp;--%>
																<g:if test="${item?.container?.parentContainer}">${item?.container?.parentContainer?.name } &rsaquo;</g:if>
																<g:if test="${item?.container?.name }">${item?.container?.name }</g:if>
																<g:else><warehouse:message code="shipping.unpacked.label"/></g:else>
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
															</td>
														</g:if>												
														<g:else>
															<td></td>
														</g:else>														
														<td class="${newContainer?'newContainer':''}" width="100%">
															<g:link controller="inventoryItem" action="showStockCard" id="${item?.product?.id}">
																<format:product product="${item?.product}"/>
															</g:link>
														</td>
														<td class="${newContainer?'newContainer':''}" style="white-space:nowrap;">
															${item?.lotNumber}
														</td>
														<td class="${newContainer?'newContainer':''}" style="white-space:nowrap;">
															<g:formatDate date="${item?.expirationDate}" format="MMM yyyy"/>
														</td>
														<td class="${newContainer?'newContainer':''}" style="white-space:nowrap;">
															${item?.quantity}
														</td>
														<g:if test="${shipmentInstance?.wasReceived()}">
															<g:set var="qtyReceived" value="${item?.quantityReceived()}"/>
															<td class="${newContainer?'newContainer':''}" style="white-space:nowrap;${qtyReceived != item?.quantity ? ' color:red;' : ''}">
																${qtyReceived}
															</td>
														</g:if>														
														<td class="${newContainer?'newContainer':''}" style="white-space:nowrap;">
															${item?.recipient?.name}
														</td>
													</tr>
													<g:set var="previousContainer" value="${item.container }"/>
													
												</g:each>
											</table>							
										</div>
									</td>
								</tr>					
							</g:if>	
						</tbody>
					</table>
				</div>
			</fieldset>
		</div>
	</div>
</body>
</html>
