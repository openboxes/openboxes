
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipping.shipment.label', default: 'Shipment').toLowerCase()}" />
	<title><warehouse:message code="default.view.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
	<style>
		.container { border-right: 2px solid lightgrey; background-color: f7f7f7; }
		.newContainer { border-top: 2px solid lightgrey }
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
		
			<div id="summary">
				<g:render template="summary"/>						
			</div>
			
			<div class="tabs">
				<ul>
					<li>
						<a href="#tabs-details">
							<warehouse:message code="shipping.details.label"/>
						</a>
					</li>
					<li>
						<a href="#tabs-contents">
							<!-- <img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="package" style="vertical-align: middle"/> -->
							<warehouse:message code="shipping.contents.label"/>
						</a>
					</li>
					<li>
						<a href="#tabs-transactions">
							<warehouse:message code="shipping.transactions.label"/>
						</a>
					</li>
					<li>
						<a href="#tabs-documents">
							<warehouse:message code="shipping.documents.label"/>
						</a>
					</li>
					<li>
						<a href="#tabs-comments">
							<warehouse:message code="shipping.notes.label"/>																	
						</a>
					</li>
					<li>
						<a href="#tabs-events">
							<warehouse:message code="shipping.events.label"/>
						</a>
					</li>
					
				</ul>		
				
						
				
				<div id="tabs-details">
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message code="default.status.label" /></label>
								</td>
								<td valign="top">
									<format:metadata obj="${shipmentInstance?.status.code}"/>
									<%-- 
									<span>
										<g:if test="${shipmentInstance?.status?.location}">
											<span>-</span>
											<span class="fade"><warehouse:message code="default.from.label"/></span> 
											<span>${shipmentInstance?.status.location}</span>
										</g:if>
										<g:if test="${shipmentInstance?.status?.date}">
											<span class="fade"><warehouse:message code="default.on.label"/></span> 
											<span><format:date obj="${shipmentInstance?.status.date}"/></span>
										</g:if>
									</span>
									--%>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<g:if test="${shipmentInstance?.status.code in [org.pih.warehouse.shipping.ShipmentStatusCode.SHIPPED, org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED]}">												
										<label><warehouse:message code="shipping.departed.label" /></label>
									</g:if>
									<g:else>
										<label><warehouse:message code="shipping.departing.label"/></label>
									</g:else>
								</td>
								<td valign="top" >
									<span class="location">
										${fieldValue(bean: shipmentInstance, field: "origin.name")}									
									</span>
									<%-- 
									<span class="fade">
										<g:if test="${shipmentInstance.expectedShippingDate && !shipmentInstance.hasShipped()}">
											- <format:date obj="${shipmentInstance?.expectedShippingDate}"/>
										</g:if>
									</span>
									--%>											
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<g:if test="${shipmentInstance?.status.code == org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED}">
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
									<%-- 						
									<span class="fade">
										<g:if test="${shipmentInstance.expectedDeliveryDate && !shipmentInstance?.wasReceived()}">
											- <format:date obj="${shipmentInstance?.expectedDeliveryDate}"/>
										</g:if>
									</span>
									--%>											
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
	
									<label><warehouse:message
										code="shipping.totalWeight.label" /></label>
								</td>
								<td valign="top" class="value">
									<g:formatNumber format="#,##0.00" number="${shipmentInstance?.totalWeightInPounds() ? shipmentInstance?.totalWeightInPounds() : 0.00 }" />
									<warehouse:message code="default.lbs.label"/>
								</td>												
							</tr>
							<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')||!shipmentWorkflow?.isExcluded('statedValue')}">
								<g:if test="${!shipmentWorkflow?.isExcluded('totalValue')}">
									<tr class="prop">
										<td valign="top" class="name">
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
									<tr class="prop">
										<td valign="top" class="name">
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
							</g:if>										
						
	
							<g:if test="${!shipmentWorkflow?.isExcluded('carrier')}">  
								<tr class="prop">
									<td valign="top" class="name">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'user.png')}" alt="comments" style="vertical-align: middle"/>
										<label><warehouse:message code="shipping.traveler.label" /></label></td>
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
							<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}" status="i">
								<tr class="prop">								
									<!-- list all the reference numbers valid for this workflow -->
									<td valign="top" class="name">
										<label>
											<format:metadata obj="${referenceNumberType}"/>
										</label>
									</td>
									<td valign="top" class="value">
										<g:findAll in="${shipmentInstance?.referenceNumbers}" expr="it.referenceNumberType.id == referenceNumberType.id">
											${it.identifier }
										</g:findAll>	
									</td>
								</tr>
							</g:each>
	
							<%--
							<tr class="prop">
								<td valign="top" class="name"><label>
									<img src="${createLinkTo(dir:'images/icons/silk',file:'tag_blue.png')}" alt="tag" style="vertical-align: middle"/>
									<warehouse:message code="shipping.referenceNumbers.label"/></label>
								</td>
								<td valign="top" class="value">
									<table>
										<tbody>								
											<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}" status="i">
												<tr class="${i % 2 ? 'even' : 'odd' }">								
													<!-- list all the reference numbers valid for this workflow -->
													<td valign="top" width="25%">
														<label>
															<format:metadata obj="${referenceNumberType}"/>
														</label>
													</td>
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
							 --%>							
	
						
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
						</tbody>
					</table>				
				</div>
				<div id="tabs-contents">
					<g:if test="${shipmentInstance.shipmentItems}">
						<g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
						<div id="items" class="section">											
							<table border="0">		
								<tr>
									<th><warehouse:message code="shipping.container.label"/></th>
									<th><warehouse:message code="product.label"/></th>
									<th class="center"><warehouse:message code="default.lotSerialNo.label"/></th>
									<th class="center"><warehouse:message code="default.expires.label"/></th>
									<th class="center"><warehouse:message code="shipping.shipped.label"/></th>
									<th class="center"><warehouse:message code="shipping.totalShipped.label"/></th>
									<g:if test="${shipmentInstance?.wasReceived()}">
										<th class="center"><warehouse:message code="shipping.received.label"/></th>
										<th class="center"><warehouse:message code="shipping.totalReceived.label"/></th>
									</g:if>
									<th class="center"><warehouse:message code="shipping.recipient.label"/></th>
									<th class="left"><warehouse:message code="default.comment.label"/></th>
								</tr>
								<g:set var="count" value="${0 }"/>
								<g:set var="previousContainer"/>
								<g:set var="shipmentItems" value="${shipmentInstance.shipmentItems.sort()}"/>
								<g:each var="shipmentItem" in="${shipmentItems}" status="i">
									<g:set var="rowspan" value="${shipmentItemsByContainer[shipmentItem?.container]?.size() }"/>												
									<g:set var="newContainer" value="${previousContainer != shipmentItem?.container }"/>
									<tr class="${(count++ % 2 == 0)?'odd':'even'} ${newContainer?'newContainer':''}">
										<g:if test="${newContainer }">
											<td class="container top left" style="width: 25%" rowspan="${rowspan }">
												<g:render template="container" model="[container:shipmentItem?.container]"/>
											</td>
										</g:if>												
																							
										<td>
											<g:link controller="inventoryItem" action="showStockCard" id="${shipmentItem?.product?.id}">
												<format:product product="${shipmentItem?.product}"/>
											</g:link>
										</td>
										<td class="center">
											${shipmentItem?.inventoryItem?.lotNumber }
										</td>
										<td class="center">
											<g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
										</td>
										<td class="center">
											<g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />
										</td>
										<g:set var="totalQtyShipped" value=""/>
										<td class="center">
											${shipmentItem?.totalQuantityShipped()}
										</td>
										<g:if test="${shipmentInstance?.wasReceived()}">
											<td class="center" style="white-space:nowrap;${shipmentItem?.receiptItem?.quantityReceived != shipmentItem?.quantity ? ' color:red;' : ''}">
												${shipmentItem?.receiptItem?.quantityReceived }
											</td>
											<td class="center">
												${shipmentItem?.totalQuantityReceived()}
											</td>
										</g:if>														
										<td class="center">
											${shipmentItem?.recipient?.name}
										</td>
										<td class="left">
											${shipmentItem?.receiptItem?.comment}
										</td>
									</tr>
									<g:set var="previousContainer" value="${shipmentItem.container }"/>
									
								</g:each>
							</table>							
						</div>
					</g:if>			
				</div>
				<div id="tabs-transactions">
					<g:if test="${shipmentInstance?.incomingTransactions || shipmentInstance?.outgoingTransactions }">
						<table>
							<thead>
								<tr>
									<th width="15%"><warehouse:message code="transaction.date.label"/></th>																	
									<th width="10%"><warehouse:message code="transaction.type.label"/></th>
								</tr>
							</thead>
							<tbody>	
								<g:set var="count" value="${0 }"/>
								<g:each var="transaction" in="${shipmentInstance?.incomingTransactions}" status="i">							
									<tr class="${count++ % 2 ? 'even' : 'odd' }">								
										<td>
											<format:datetime obj="${transaction?.transactionDate }"/>
										</td>
										<td>
											<g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
												<format:metadata obj="${transaction?.transactionType}"/>
											</g:link>
											...
											${transaction?.inventory }
										</td>
									</tr>
								</g:each>
								<g:each var="transaction" in="${shipmentInstance?.outgoingTransactions}" status="i">							
									<tr class="${count++ % 2 ? 'even' : 'odd' }">								
										<td>
											<format:datetime obj="${transaction?.transactionDate }"/>
										</td>
										<td>
											<g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
												<format:metadata obj="${transaction?.transactionType}"/>
											</g:link>
											...
											${transaction?.inventory }
										</td>
									</tr>
								</g:each>											
							</tbody>
						</table>
					</g:if>					
				</div>
				<div id="tabs-documents">
					<g:if test="${shipmentInstance.documents}">
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
													<format:metadata obj="${document?.documentType}"/>
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
					</g:if>					
				</div>
				<div id="tabs-comments">
					<g:if test="${shipmentInstance.comments}">
						<div>
							<table>
								<tbody>
									<tr>
																											
										<th><warehouse:message code="comment.sender.label"/></th>
										<th colspan="2"><warehouse:message code="comment.label"/></th>
										
									</tr>
									<g:each in="${shipmentInstance.comments}" var="comment" status="i">
										<tr class="${(i % 2) == 0 ? 'odd' : 'even'} newContainer" >
											<td width="25%" class="container">
												
												${comment?.sender?.name} wrote:
												
											</td>
											<td>
												<p>
												${comment?.comment}
												</p>
												<br/>																
												<format:datetime obj="${comment?.dateCreated}"/> 
											</td>
											<td>
												<g:link class="remove" action="deleteComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteNote.message')}')">
													<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" style="vertical-align: middle"/>
												</g:link>	
											</td>																	
										</tr>
									</g:each>	
								</tbody>
							</table>												
						</div>
					</g:if> 
				</div>
				<div id="tabs-events">
					<g:if test="${shipmentInstance.events}">										
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
					</g:if>								
				</div>

			</div>				
		</div>
	</div>
	
	<script type="text/javascript">
    	$(document).ready(function() {
	    	$(".tabs").tabs(); 
	    });
       </script>		
</body>
</html>
