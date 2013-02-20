
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
		tr.box { margin: 10px; }
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
			<g:render template="summary"/>						
			
			
			<div class="yui-gd">
	
				<!-- the first child of a Grid needs the "first" class -->
				<div class="yui-u first">
					<div id="details" class="box">
						<h4>										
							<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="Details" style="vertical-align: middle"/>
							<label><warehouse:message code="shipping.details.label"/></label>
						</h4>
						<table>
							<tbody>
								<tr class="prop">
									<td valign="top" class="name">
										<label><warehouse:message code="default.status.label" /></label>
									</td>
									<td valign="top" id="shipmentStatus" class="value">
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
									<td valign="top" class="value">
										<span class="location" id="shipmentOrigin">
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
									<td valign="top" class="value">
										<span id="shipmentDestination">
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
											<td valign="top" class="value">
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
											<td valign="top" class="value">
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
											<img src="${createLinkTo(dir:'images/icons/silk',file:'user.png')}" alt="carrier" style="vertical-align: middle"/>
											<label><warehouse:message code="shipping.traveler.label" /></label></td>
										<td valign="top" class="value">
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
											<label><warehouse:message code="default.comments.label" /></label><br/>
										</td>
										<td valign="top" class="value">
											<g:if test="${shipmentInstance.additionalInformation}">
												${shipmentInstance.additionalInformation}<br/>
											</g:if>
										</td>
									</tr>
								</g:if>
								<g:if test="${shipmentWorkflow?.documentTemplate}">
									<tr class="prop">
										<td valign="top" class="name">
											<label><warehouse:message code="shipping.templates.label" /></label><br/>
										</td>
										<td valign="top" class="value">
												<a href="${createLink(controller: "shipment", action: "generateDocuments", id: shipmentInstance.id)}">
												<button><warehouse:message code="shipping.generateDocuments.label" args="[format.metadata(obj:shipmentWorkflow.shipmentType)]"/></button></a>
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
					<div id="events" class="box">
						<h4>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'date.png')}" alt="event" style="vertical-align: middle"/>
							<label><warehouse:message code="shipping.tracking.label"/></label>
						</h4>									
						<table>	
							<tbody>
								<tr>
									<th><warehouse:message code="default.date.label"/></th>
									<th><warehouse:message code="default.time.label"/></th>
									<th><warehouse:message code="location.label"/></th>
									<th><warehouse:message code="default.event.label"/></th>
									<th></th>
								</tr>
								<g:set var="i" value="${0 }"/>
								<g:each in="${shipmentInstance.events}" var="event">
									<tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
										<td>																			
											<g:formatDate date="${event.eventDate}" format="MMMMM dd, yyyy"/> 																			
										</td>
										<td>
											<g:formatDate date="${event.eventDate}" format="hh:mm a"/>
										</td>											
										
										<td>																
											<format:metadata obj="${event?.eventType}"/>
										</td>
										<td>
											${event?.eventLocation?.name}
										</td>
										<td style="text-align: right">
											<g:if test="${event?.eventType?.eventCode == org.pih.warehouse.core.EventCode.SHIPPED }">
											
											</g:if>
											<g:if test="${event?.eventType?.eventCode == org.pih.warehouse.core.EventCode.RECEIVED }">
											
											</g:if>
											
											<g:link action="editEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" style="vertical-align: middle"/>
											</g:link>
										</td>
									</tr>
								</g:each>
								<tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
									<td>																			
										<g:formatDate date="${shipmentInstance?.dateCreated}" format="MMMMM dd, yyyy"/>																		
									</td>
									<td>
										<g:formatDate date="${shipmentInstance?.dateCreated}" format="hh:mm a"/>
									</td>
									<td>																
										<warehouse:message code="default.created.label"/>
									</td>
									<td>
										${shipmentInstance?.origin?.name}
									</td>
									<td style="text-align: right">
										
									</td>
								</tr>
								
							</tbody>								
						</table>
					</div>
					<div id="comments" class="box">	
						<h4>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'note.png')}" alt="note" style="vertical-align: middle"/>
							<label>${warehouse.message(code: 'comments.label') }</label>
						</h4>									

						<table>
							<tbody>
								<tr>
									<th>
									</th>																											
									<th>
									</th>
								</tr>
								<g:each in="${shipmentInstance.comments}" var="comment" status="i">
									<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}" >
										<td>
											
											<g:prettyDateFormat date="${comment?.dateCreated }"/>
											·
											${comment?.sender?.name} wrote:
											<blockquote>${comment?.comment}</blockquote>
										</td>
										<td>
											<p class="fade right">
												<g:link class="fade" action="deleteComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteNote.message')}')" style="color: #666;">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" style="vertical-align: middle"/>									
												</g:link>			
																									
											</p>
										</td>
									</tr>

								</g:each>	
								
								<tr class="prop">
									<td colspan="2" class="center">											
										<g:form action="saveComment">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
		                                    <g:textArea name="comment" rows="3" style="width:100%" placeholder="${warehouse.message(code:'default.comment.message')}"/>
		                                    <div class="buttons">
												<button type="submit" class="button"><warehouse:message code="default.button.add.label"/></button>
											</div>
										</g:form>											
									</td>
								</tr>
							</tbody>
						</table>												
					</div>
					<div id="documents" class="box">
						<h4>										
							<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="document" style="vertical-align: middle"/>
							<label><warehouse:message code="document.documents.label"/></label>
						</h4>
						<table>
							<tbody>
								<tr>
									<th></th>
									<th><warehouse:message code="document.label"/></th>
									<th class="right"><warehouse:message code="document.uploaded.label"/></th>
								</tr>															
								<g:each in="${shipmentInstance.documents}" var="document" status="i">
									<tr id="document-${document.id}"
										class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
										<td style="width:1%">
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
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"/>
											</g:elseif>
											<g:elseif test="${f.endsWith('.gz')||f.endsWith('.jar')||f.endsWith('.zip')||f.endsWith('.tar') }">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_compressed.png')}"/>
											</g:elseif>
											<g:else>
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white.png')}"/> 
											</g:else>
										</td>
										<td>
											<g:link controller="document" action="download" id="${document.id}" style="color: #666;" title="${document?.filename}">
												<format:metadata obj="${document?.documentType}"/>
												·
												<span class="fade">
													<g:link controller="document" action="download" id="${document.id}" style="color: #666">
														<warehouse:message code="default.button.download.label"/>													
													</g:link>
													·																													
													<g:link action="editDocument" params="[documentId:document.id,shipmentId:shipmentInstance.id]" style="color: #666">
														<warehouse:message code="default.button.edit.label"/>													
													</g:link>
													·																													
													<g:link class="remove" action="deleteDocument" id="${document?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteDocument.message')}')" style="color: #666">
														<warehouse:message code="default.button.delete.label"/>													
													</g:link>																		
												</span>
											</g:link> 
										</td>
										<td class="right">
											<span class="fade"><format:date obj="${document?.dateCreated}"/></span>
										</td>
									</tr>
									<%-- 
									<tr>
										<td colspan="2" class="center">
											<p class="fade">
												<g:link action="editDocument" params="[documentId:document.id,shipmentId:shipmentInstance.id]" style="color: #666">
													<warehouse:message code="default.button.edit.label"/>													
												</g:link>
												·
												<g:link controller="document" action="download" id="${document.id}" style="color: #666">
													<warehouse:message code="default.button.download.label"/>													
												</g:link>
												·																													
												<g:link class="remove" action="deleteDocument" id="${document?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteDocument.message')}')" style="color: #666">
													<warehouse:message code="default.button.delete.label"/>													
												</g:link>																		
											</p>											
										</td>
									</tr>
									--%>
								</g:each>	
								<tr class="prop odd">
									<td>
										<img src="${createLinkTo(dir:'images/icons',file:'pdf.png')}" class="middle"/>
									</td>																				
									<td>
							   			<g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]" style="color: #666;">
											Packing List
							   			</g:link>
							   			·
							   			<g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]" style="color: #666;">
							   				<warehouse:message code="default.button.download.label"/>	
							   			</g:link>
							   		</td>
							   		<td class="right">
										<span class="fade"><format:date obj="${shipmentInstance?.lastUpdated}"/></span>								   		
							   		</td>
							   	</tr>									   		
								<tr class="prop even">
									<td>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_break.png')}" class="middle"/>										
									</td>
									<td>
							   			<g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]" style="color: #666;">
											Packing List (Pallet)
							   			</g:link>
							   			·
							   			<g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]" style="color: #666;">
							   				<warehouse:message code="default.button.download.label"/>	
							   			</g:link>
									</td>
							   		<td class="right">
										<span class="fade"><format:date obj="${shipmentInstance?.lastUpdated}"/></span>								   		
							   		</td>
							   	</tr>									   		
								<tr class="prop odd">
									<td>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" class="middle"/>											
									</td>
									<td>
										<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }" style="color: #666;">
											Goods Receipt Note (Excel)
										</g:link> 
							   			·
										<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }" style="color: #666;">
							   				<warehouse:message code="default.button.download.label"/>	
							   			</g:link>
									</td>
							   		<td class="right">
										<span class="fade"><format:date obj="${shipmentInstance?.lastUpdated}"/></span>								   		
							   		</td>
							   	</tr>									   		
								<tr class="prop even">
									<td>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}" class="middle"/>
									</td>
									<td>
										<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }" style="color: #666;">
											Certificate of Donation
										</g:link> 
							   			·
										<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }" style="color: #666;">
							   				<warehouse:message code="default.button.download.label"/>	
							   			</g:link>
									</td>
							   		<td class="right">
										<span class="fade"><format:date obj="${shipmentInstance?.lastUpdated}"/></span>								   		
							   		</td>
								</tr>
								<tr class="prop">
									<td colspan="3">
									
										<g:uploadForm controller="document" action="${documentInstance?.id ? 'save' : 'upload'}">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="documentId" value="${documentInstance?.id}" />
											<g:select name="typeId" from="${org.pih.warehouse.core.DocumentType.list()}" value="${documentInstance?.documentType?.id}" optionKey="id" optionValue="${{format.metadata(obj:it)}}"/>
											<%-- 
											<g:textField name="name" value="${documentInstance?.name}" />
											<g:textField name="documentNumber" value="${documentInstance?.documentNumber}" />
											--%>
											<input name="fileContents" type="file" />
											<!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
											<button type="submit" class="button">${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
										</g:uploadForm>																							
									</td>
								</tr>
							</tbody>
						</table>
					</div>
					
					<g:if test="${shipmentInstance?.outgoingTransactions || shipmentInstance?.incomingTransactions }">
						<div id="transactions" class="box">
							<h4>										
								<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_switch_bluegreen.png')}" style="vertical-align: middle"/>
								<label><warehouse:message code="shipping.transactions.label"/></label>
							</h4>
							<table>
								<thead>
									<tr>
										<th><warehouse:message code="default.date.label"/></th>
										<th><warehouse:message code="default.time.label"/></th>																														
										<th><warehouse:message code="transaction.type.label"/></th>
										<th><warehouse:message code="transaction.transactionNumber.label"/></th>																	
									</tr>
								</thead>
								<tbody>	
									<g:set var="i" value="${0 }"/>
									<g:each var="transaction" in="${shipmentInstance?.incomingTransactions}">							
										<tr class="${i++ % 2 ? 'even' : 'odd' }">								
											<td>
												<g:formatDate date="${transaction?.transactionDate}" format="MMMMM dd, yyyy"/> 
											</td>
											<td>		
												<g:formatDate date="${transaction?.transactionDate}" format="hh:mm a"/> 		
												<%--<format:datetime obj="${transaction?.transactionDate }"/> --%>
											</td>
											<td>
												<g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
													<format:metadata obj="${transaction?.transactionType}"/>
												</g:link>
											</td>
											<td>
												<g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
													${transaction?.transactionNumber}
												</g:link>
											</td>
										</tr>
									</g:each>												
									<g:each var="transaction" in="${shipmentInstance?.outgoingTransactions}">							
										<tr class="${i++ % 2 ? 'even' : 'odd' }">								
											<td>
												<g:formatDate date="${transaction?.transactionDate}" format="MMMMM dd, yyyy"/> 		
											</td>
											<td>
												<g:formatDate date="${transaction?.transactionDate}" format="hh:mm a"/> 		
												<%--<format:datetime obj="${transaction?.transactionDate }"/>--%>
											</td>
											<td>
												<g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
													<format:metadata obj="${transaction?.transactionType}"/>
												</g:link>
											</td>
											<td>
												<g:link controller="inventory" action="showTransaction" id="${transaction?.id }">													
													${transaction?.transactionNumber }
												</g:link>
											</td>
										</tr>
									</g:each>
								</tbody>
							</table>
						</div>
					</g:if>							
				</div>
				
				
				<div class="yui-u">
					
					
					
					<g:if test="${shipmentInstance.shipmentItems}">
						<g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
						<div id="items" class="box">	
						
							<h4>
								<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="contents" style="vertical-align: middle"/>
								<label><warehouse:message code="shipping.contents.label"/></label>
							</h4>									
						
																
							<table>		
								<tr>
									<th><warehouse:message code="shipping.container.label"/></th>
									<th><warehouse:message code="product.label"/></th>
									<th class="center"><warehouse:message code="default.lotSerialNo.label"/></th>
									<th class="center"><warehouse:message code="default.expires.label"/></th>
									<th class="center"><warehouse:message code="shipping.shipped.label"/></th>
									<g:if test="${shipmentInstance?.wasReceived()}">
										<th class="center"><warehouse:message code="shipping.received.label"/></th>
										<%-- 
										<th class="center"><warehouse:message code="shipping.totalReceived.label"/></th>
										--%>
									</g:if>
									<th><warehouse:message code="shipping.recipient.label"/></th>
									<th class="left"><warehouse:message code="default.comment.label"/></th>										
								</tr>
								<g:set var="count" value="${0 }"/>
								<g:set var="previousContainer"/>
								<g:set var="shipmentItems" value="${shipmentInstance.shipmentItems.sort()}"/>
								<g:each var="shipmentItem" in="${shipmentItems}" status="i">
									<g:set var="rowspan" value="${shipmentItemsByContainer[shipmentItem?.container]?.size() }"/>												
									<g:set var="newContainer" value="${previousContainer != shipmentItem?.container }"/>
									<tr class="${(count++ % 2 == 0)?'odd':'even'} ${newContainer?'newContainer':''} shipmentItem">
										<g:if test="${newContainer }">
											<td class="container top left" rowspan="${rowspan }">
												<g:render template="container" model="[container:shipmentItem?.container]"/>
											</td>
										</g:if>												
										<td class="product">
											<g:link controller="inventoryItem" action="showStockCard" id="${shipmentItem?.inventoryItem?.product?.id}">
												<format:product product="${shipmentItem?.inventoryItem?.product}"/>
											</g:link>
										</td>
										<td class="center lotNumber">
											${shipmentItem?.inventoryItem?.lotNumber}
										</td>
										<td class="center expirationDate">
											<g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
										</td>
										<td class="center quantity">
											<g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />
											${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
											
										</td>
										<g:if test="${shipmentInstance?.wasReceived()}">
											<g:set var="totalQtyReceived" value="${shipmentItem?.totalQuantityReceived()}"/>
											<td class="center" style="white-space:nowrap;${shipmentItem?.receiptItem?.quantityReceived != shipmentItem?.quantity ? ' color:red;' : ''}">
												<g:formatNumber number="${shipmentItem?.receiptItem?.quantityReceived }"/>
												${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
											</td>
											<%-- 
											<td class="center">
												${shipmentItem?.totalQuantityReceived()}
											</td>
											--%>
										</g:if>														
										<td class="left">
											<g:if test="${shipmentItem?.recipient }">
												${shipmentItem?.recipient?.name}
												<span class="fade">${shipmentItem?.recipient?.email}</span>
											</g:if>
											<g:else>
												<warehouse:message code="default.none.label"/>															
											</g:else>
										</td>
										<td class="left" >
											<g:if test="${shipmentItem?.receiptItem?.comment }">
												${shipmentItem?.receiptItem?.comment}
											</g:if>
											<g:else>
												<warehouse:message code="default.none.label"/>															
											</g:else>
										</td>
										
									</tr>
									<g:set var="previousContainer" value="${shipmentItem.container }"/>
									
								</g:each>
							</table>							
						</div>
					</g:if>		
				</div>				
				
			</div>			
		</div>
	</div>					
</body>
</html>
