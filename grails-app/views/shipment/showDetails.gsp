<%@ page import="groovy.time.TimeCategory" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipping.shipment.label', default: 'Shipment').toLowerCase()}" />
	<title><warehouse:message code="default.view.label" args="[entityName]" /></title>        
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
</head>

<body>    
	<div class="body">

        <g:render template="summary"/>

        <g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	
			
		<div class="dialog">

			
			<div class="yui-gd">
	
				<!-- the first child of a Grid needs the "first" class -->

                <div class="yui-u first">

					<div id="details" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="Details" style="vertical-align: middle"/>
							<label><warehouse:message code="shipping.details.label"/></label>
						</h2>
						<table>
							<tbody>
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message code="default.status.label" /></label>
								</td>
								<td valign="top" id="shipmentStatus" class="value">
									<format:metadata obj="${shipmentInstance?.status?.code}"/>
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
									<g:if test="${shipmentInstance?.status?.code == org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED}">
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
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message
											code="shipping.timeToProcess.label" default="Time to process" /></label>
								</td>
								<td valign="top" class="value">
									${shipmentInstance?.timeToProcess()?:"<span class='fade'>N/A</span>"}
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message
											code="shipping.timeInCustoms.label" default="Time in customs"/></label>
								</td>
								<td valign="top" class="value">
									${shipmentInstance?.timeInCustoms()?:"<span class='fade'>N/A</span>"}
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message
											code="shipping.timeInTransit.label" default="Time in transit"/></label>
								</td>
								<td valign="top" class="value">
									<g:if test="${shipmentInstance?.isPending()}">~</g:if>${shipmentInstance?.timeInTransit()?:"<span class='fade'>N/A</span>"}
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
							<%--
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
							--%>
							</g:if>

							<g:if test="${!shipmentWorkflow?.isExcluded('carrier') && shipmentInstance?.carrier}">
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
										<label><warehouse:message code="shipping.additionalInformation.label" default="Additional information" /></label><br/>
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
							<g:if test="${shipmentWorkflow?.referenceNumberTypes && shipmentInstance?.referenceNumbers}">
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

							</tbody>
						</table>
					</div>

                    <div id="events" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'date.png')}" alt="event" style="vertical-align: middle"/>
							<label><warehouse:message code="shipping.tracking.label"/></label>
						</h2>
						<g:set var="i" value="${0 }"/>
						<table>
							<thead>
								<tr>
									<th><warehouse:message code="default.event.label"/></th>
									<th><warehouse:message code="default.date.label"/></th>
									<th><warehouse:message code="default.time.label"/></th>
                                    <th><warehouse:message code="location.label"/></th>
									<th></th>
								</tr>
							</thead>
							<tbody>
								<tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
									<td>
										<warehouse:message code="default.created.label"/>
									</td>
									<td>
										<g:formatDate date="${shipmentInstance?.dateCreated}" format="MMM d, yyyy"/>
									</td>
									<td>
										<g:formatDate date="${shipmentInstance?.dateCreated}" format="hh:mma"/>
									</td>
									<td>
										${shipmentInstance?.origin?.name}
									</td>
									<td style="text-align: right">

									</td>
								</tr>
								<g:each in="${shipmentInstance?.events?.sort { it?.eventDate}}" var="event">
									<tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
										<td>
											<g:link action="editEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
												<format:metadata obj="${event?.eventType}"/>
											</g:link>

										</td>
										<td>
											<g:formatDate date="${event.eventDate}" format="MMM d, yyyy"/>
										</td>
										<td>
											<g:formatDate date="${event.eventDate}" format="hh:mma"/>
										</td>											
										
										<td>
											${event?.eventLocation?.name}
										</td>
										<td style="text-align: right">
											<g:if test="${event?.eventType?.eventCode == org.pih.warehouse.core.EventCode.SHIPPED }">
											
											</g:if>
											<g:if test="${event?.eventType?.eventCode == org.pih.warehouse.core.EventCode.RECEIVED }">
											
											</g:if>
											<%--
											<g:link action="editEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" style="vertical-align: middle"/>
											</g:link>
											<g:link action="deleteEvent" id="${event?.id}" params="[shipmentId:shipmentInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" style="vertical-align: middle"/>
											</g:link>
											--%>
										</td>
									</tr>
								</g:each>
							</tbody>
							<tfoot>
								<tr class="prop">
									<td colspan="5">
										<g:form controller="shipment" action="saveEvent" method="POST">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="eventId" value="${eventInstance?.id}" />
											<table>
												<tbody>

													<tr class="prop">
														<td valign="top" class="name"><label><warehouse:message code="shipping.eventType.label"/></label></td>
														<td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventType', 'errors')}">
															<g:if test="${!eventInstance?.eventType}">
																<g:select id="eventType.id" name='eventType.id' noSelection="['':warehouse.message(code:'default.selectOne.label')]"
																		  from='${org.pih.warehouse.core.EventType.list()}' optionKey="id"
																		  optionValue="name" value="${eventInstance?.eventType}" class="chzn-select-deselect">
																</g:select>
															</g:if>
															<g:else>
																<g:hiddenField name="eventType.id" value="${eventInstance?.eventType?.id}"/>
																<format:metadata obj="${eventInstance?.eventType}"/>
															</g:else>
														</td>
													</tr>
													<tr class="prop">
														<td valign="top" class="name"><label><warehouse:message code="location.label" /></label></td>
														<td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
															<g:select id="eventLocation.id" name='eventLocation.id' noSelection="['':warehouse.message(code:'default.selectOne.label')]"
																	  from='${org.pih.warehouse.core.Location.list()}' optionKey="id" optionValue="name"
																	  value="${eventInstance?.eventLocation?.id}" class="chzn-select-deselect">
															</g:select>
														</td>
													</tr>
													<tr class="prop">
														<td valign="top" class="name"><label><warehouse:message code="shipping.eventDate.label" /></label></td>
														<td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
															<g:jqueryDatePicker name="eventDate" value="${eventInstance?.eventDate}" format="MM/dd/yyyy" />

															<button type="submit" class="button icon add">
																<warehouse:message code="default.button.add.label"/>
															</button>

														</td>
													</tr>
												</tbody>
											</table>
										</g:form>
									</td>
								</tr>
							</tfoot>
						</table>
					</div>

                    <g:if test="${shipmentInstance?.outgoingTransactions || shipmentInstance?.incomingTransactions }">
                        <div id="transactions" class="box">
                            <h2>
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_switch_bluegreen.png')}" style="vertical-align: middle"/>
                                <label><warehouse:message code="shipping.transactions.label"/></label>
                            </h2>
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
                                            <g:formatDate date="${transaction?.transactionDate}" format="MMM d, yyyy"/>
                                        </td>
                                        <td>
                                            <g:formatDate date="${transaction?.transactionDate}" format="hh:mma"/>
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
                                            <g:formatDate date="${transaction?.transactionDate}" format="MMM d, yyyy"/>
                                        </td>
                                        <td>
                                            <g:formatDate date="${transaction?.transactionDate}" format="hh:mma"/>
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


					<div id="documents" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="document" style="vertical-align: middle"/>
							<label><warehouse:message code="document.documents.label"/></label>
						</h2>
						<table>
							<tbody>
								<tr>
									<th width="1%"></th>
									<th><warehouse:message code="document.label"/></th>
									<th class="right"></th>
								</tr>															
								<g:each in="${shipmentInstance.documents}" var="document" status="i">
									<tr id="document-${document.id}"
										>
										<td style="width:1%" class="middle">
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
											<div>
												<g:link controller="document" action="download" id="${document.id}" title="${document?.filename}">
													<label><format:metadata obj="${document?.documentType}"/></label></g:link>

												<span class="fade">
													<g:link controller="document" action="download" id="${document.id}">
														<warehouse:message code="default.button.download.label"/></g:link>
													·
													<g:link action="editDocument" params="[documentId:document.id,shipmentId:shipmentInstance.id]">
														<warehouse:message code="default.button.edit.label"/></g:link>
													·
													<g:link class="remove" action="deleteDocument" id="${document?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteDocument.message')}')" style="color: #666">
														<warehouse:message code="default.button.delete.label"/></g:link>
												</span>

											</div>
											<%--
											<span class="fade"><format:date obj="${document?.dateCreated}"/></span>
											--%>
										</td>
										<td class="right">

											<g:link controller="document" action="download" id="${document.id}" class="button">
												<warehouse:message code="default.button.download.label"/>
											</g:link>
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
								<tr class=" ">
									<td>
										<img src="${createLinkTo(dir:'images/icons',file:'pdf.png')}" class="middle"/>
									</td>																				
									<td>
										<div>
								   			<g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]">
												<label>Packing List</label></g:link>
										</div>

							   		</td>
							   		<td class="right">
										<g:link target="_blank" controller="report" action="printShippingReport" params="['shipment.id':shipmentInstance?.id]" class="button">
											<warehouse:message code="default.button.download.label"/>
										</g:link>
							   		</td>
							   	</tr>									   		
								<tr class=" ">
									<td>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_break.png')}" class="middle"/>										
									</td>
									<td>
										<div>
								   			<g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]">
												<label>Packing List (Pallet)</label>
								   			</g:link>
							   			</div>

									</td>
							   		<td class="right">
										<g:link target="_blank" controller="report" action="printPaginatedPackingListReport" params="['shipment.id':shipmentInstance?.id]" class="button">
											<warehouse:message code="default.button.download.label"/>
										</g:link>
							   		</td>
							   	</tr>									   		
								<tr class=" ">
									<td>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" class="middle"/>											
									</td>
									<td>
										<div>
											<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">
												<label>Goods Receipt Note (Excel)</label>
											</g:link>
										</div>

									</td>
							   		<td class="right">
										<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }" class="button">
											<warehouse:message code="default.button.download.label"/>
										</g:link>

							   		</td>
							   	</tr>									   		
								<tr class="">
									<td>
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}" class="middle"/>
									</td>
									<td>
										<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">
											<label>Certificate of Donation</label>
										</g:link>
									</td>
							   		<td class="right">
										<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }" class="button">
											<warehouse:message code="default.button.download.label"/>
										</g:link>
							   		</td>
								</tr>
							</tbody>
							<tfoot>
								<tr >
									<td colspan="3">
										<g:uploadForm controller="document" action="${documentInstance?.id ? 'save' : 'upload'}">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="documentId" value="${documentInstance?.id}" />

                                            <table>
                                                <tr>
                                                    <td width="1%">
                                                        <label><warehouse:message code="document.documentType.label" default="Document type"/></label>
                                                    </td>
                                                    <td>
                                                        <g:select name="typeId" from="${org.pih.warehouse.core.DocumentType.list()}"
                                                                  noSelection="['null':'Choose a document type']"
                                                                  value="${documentInstance?.documentType?.id}"
                                                                  optionKey="id" optionValue="${{format.metadata(obj:it)}}"
                                                                  class="chzn-select-deselect"/>

                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <label><warehouse:message code="document.label" default="Document"/></label>
                                                    </td>
                                                    <td>
                                                        <input name="fileContents" type="file" />
														<button type="submit" class="button icon arrowup right">${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
                                                    </td>

                                                </tr>
                                            </table>
											<%--
											<g:textField name="name" value="${documentInstance?.name}" />
											<g:textField name="documentNumber" value="${documentInstance?.documentNumber}" />
											--%>

										</g:uploadForm>
									</td>
								</tr>
							</tfoot>
						</table>
					</div>
				</div>


				<div class="yui-u">



					<g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
					<div id="items" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="contents" style="vertical-align: middle"/>
							<label><warehouse:message code="shipping.contents.label"/></label>
						</h2>
						<table>
							<tr>
								<th><warehouse:message code="shipping.container.label"/></th>
								<th><warehouse:message code="product.productCode.label"/></th>
								<th><warehouse:message code="product.label"/></th>
								<th><warehouse:message code="product.uom.label"/></th>
								<th class="left"><warehouse:message code="default.lotSerialNo.label"/></th>
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
							<g:if test="${shipmentInstance.shipmentItems}">
								<g:set var="count" value="${0 }"/>
								<g:set var="previousContainer"/>
								<g:set var="shipmentItems" value="${shipmentInstance.shipmentItems.sort()}"/>
								<g:each var="shipmentItem" in="${shipmentItems}" status="i">
									<g:set var="rowspan" value="${shipmentItemsByContainer[shipmentItem?.container]?.size() }"/>
									<g:set var="newContainer" value="${previousContainer != shipmentItem?.container }"/>
									<tr class="${(count++ % 2 == 0)?'odd':'even'} ${newContainer?'newContainer':''} shipmentItem">
										<g:if test="${newContainer }">
											<td class="top left" >
												<g:render template="container" model="[container:shipmentItem?.container]"/>
											</td>
										</g:if>
										<g:else>
											<td></td>
										</g:else>
										<td>
											${shipmentItem?.inventoryItem?.product?.productCode}
										</td>
										<td class="product">
											<g:link controller="inventoryItem" action="showStockCard" id="${shipmentItem?.inventoryItem?.product?.id}">
												<format:product product="${shipmentItem?.inventoryItem?.product}"/>
											</g:link>
										</td>
										<td>
											${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
										</td>
										<td class="lotNumber">
											${shipmentItem?.inventoryItem?.lotNumber}
										</td>
										<td class="center expirationDate">

											<g:if test="${shipmentItem?.inventoryItem?.expirationDate}">
												<span class="expirationDate">
													<g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
												</span>
											</g:if>
											<g:else>
												<span class="fade">
													${warehouse.message(code: 'default.never.label')}
												</span>
											</g:else>

										</td>
										<td class="center quantity">
											<g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />

										</td>
										<g:if test="${shipmentInstance?.wasReceived()}">
											<g:set var="totalQtyReceived" value="${shipmentItem?.totalQuantityReceived()}"/>
											<td class="center" style="white-space:nowrap;${shipmentItem?.receiptItem?.quantityReceived != shipmentItem?.quantity ? ' color:red;' : ''}">
												<g:formatNumber number="${shipmentItem?.receiptItem?.quantityReceived }"/>
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
							</g:if>
							<g:else>
								<tr>
									<td colspan="9" class="middle center fade empty">
										<warehouse:message code="shipment.noShipmentItems.message"/>
									</td>
								</tr>
							</g:else>

						</table>
					</div>
					<hr/>

					<div id="comments" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'comment.png')}" alt="note" style="vertical-align: middle"/>
							<label>${warehouse.message(code: 'comments.label') }</label>
						</h2>

						<table>
							<tbody>

							<g:unless test="${shipmentInstance.comments}">

								<tr>
									<td colspan="2">
										<div class="fade center empty"><warehouse:message code="default.noComments.label" /></div>
									</td>
								</tr>
							</g:unless>

							<g:each in="${shipmentInstance.comments}" var="comment" status="i">
								<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}" >
									<td width="1%">
										<g:if test="${comment?.sender?.photo}">
											<g:link action="viewPhoto" id="${comment?.sender?.id }">
												<img src="${createLink(controller:'user', action:'viewThumb', id:comment?.sender?.id)}"
													 style="vertical-align: middle" />
											</g:link>
										</g:if>
										<g:else>
											<img class="photo" src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg') }"
												 style="vertical-align: bottom;" />
										</g:else>

									</td>
									<td>
										<b>${comment?.sender?.name}</b> ·
										<span class="fade">
											<g:prettyDateFormat date="${comment?.dateCreated }"/>
										</span>

										<blockquote>
											<g:if test="${comment?.recipient}">
												<span class="fade" title="${comment?.recipient?.name}">@${comment?.recipient?.username}</span>
											</g:if>
											${comment?.comment}
										</blockquote>

									</td>
									<td>
										<p class="fade right">
											<%--
											<g:link class="fade" action="editComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" style="vertical-align: middle"/>
											</g:link>
											--%>
											<%--
											<g:link class="fade" action="deleteComment" id="${comment?.id}" params="[shipmentId:shipmentInstance.id]" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteNote.message')}')">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" style="vertical-align: middle"/>
											</g:link>
											--%>
										</p>
									</td>
								</tr>

							</g:each>

							<tr class="">
								<td colspan="3" class="left">
									<g:form action="saveComment">
										<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
										<div>
											<label><warehouse:message code="comment.label" default="Comments"/></label>
										</div>
										<div style="padding:1px;">
											<g:textArea name="comment" rows="5" style="width:100%" placeholder="${warehouse.message(code:'default.comment.message')}" class="text"/>
										</div>

										<div class="buttons">
											<button type="submit" class="button icon add"><warehouse:message code="default.button.add.label"/></button>
										</div>
									</g:form>
								</td>
							</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>



</body>
</html>
