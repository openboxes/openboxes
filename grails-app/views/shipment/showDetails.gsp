
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
</head>
	
<body>
    
	<div class="body">
	     	
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
			<table>
				<tr>
					<td>
						<div id="details" class="section">
							<h2>Shipment Details</h2>
							<table cellspacing="5" cellpadding="5">
								<tbody>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.shipmentType.label" default="Type" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "shipmentType.name")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.shipmentStatus.label" default="Name" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "name")}
										</td>
									</tr>
									
								
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.destination.label" default="To" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "destination.name")}
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.origin.label" default="From" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "origin.name")}
										</td>
									</tr>
																

									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.method.label" default="Ship By" /></label></td>
										<td valign="top" class="value">
										${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")} <img
											src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.png')}"
											valign="top" style="vertical-align: middle;" /></td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.trackingNumber.label" default="Tracking" /></label></td>
										<td valign="top" class="value"><span style="color: #aaa">
												<g:if test="${shipmentInstance?.trackingNumber}">
													${fieldValue(bean: shipmentInstance, field: "trackingNumber")}											                            	
												</g:if> 
												<g:else>(none)</g:else> 
											</span></td>
									</tr>
									<%-- 
									<tr class="prop">
										<td valign="middle" class="name"><label><g:message
											code="shipment.shipmentNumber.label" default="Last Modified on" /></label>
										</td>
										<td valign="top" class="value" nowrap="nowrap"><span
											style="color: #aaa"><g:formatDate
											date="${shipmentInstance?.lastUpdated}"
											format="dd MMM yyyy hh:mm:ss" /></span></td>
									</tr>
									<tr class="prop">
										<td valign="middle" class="name"><label><g:message
											code="shipment.shipmentNumber.label" default="Created on" /></label></td>
										<td valign="top" class="value" nowrap="nowrap"><span
											style="color: #aaa"><g:formatDate
											date="${shipmentInstance?.dateCreated}"
											format="dd MMM yyyy hh:mm:ss" /></span></td>
									</tr>
									--%>
									
								</tbody>
							</table>
						</div>
						<div id="documents" class="section">
							<h2>Documents</h2>
							<g:if test="${!shipmentInstance.containers}">
								<div>There are currently no documents for this
								shipment.</div>
							</g:if> 
							<g:else>
								<table>
									<tbody>
										<g:each in="${shipmentInstance.documents}" var="document" status="i">
											<tr id="document-${document.id}"
												class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td nowrap="nowrap">
													<g:formatDate format="dd MMM yyyy" date="${document?.dateCreated}"/>					
												</td>					
												<td>
													<label>${document?.documentType?.name}</label>
												</td>
												<td>
													<g:link controller="document" action="download" id="${document.id}">download</g:link>
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</g:else>
						</div>
						<div id="events" class="section">
							<h2>Events</h2>
							<g:if test="${!shipmentInstance.events}">
								<div>There are currently no events for this
								shipment.</div>
							</g:if> 
							<g:else>
								<table>	
									<g:each in="${shipmentInstance.events}" var="event" status="i">
										<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
											<td nowrap="nowrap">
												<g:formatDate format="dd MMM yy" date="${event.eventDate}"/>
											</td>
											<td nowrap="nowrap">${event.eventLocation.name}</td> 
											<td>${event.eventType.name}</td> 
										</tr>										
									</g:each>
								</table>
							</g:else>
						</div>					
					</td>					
					</td>
				
					<td width="60%">
						<div id="containers" class="section">
							<h2>Shipment Units</h2>
							<g:if test="${!shipmentInstance.containers}">
								<div>There are currently no shipping units in this
								shipment.</div>
							</g:if> 
							<g:else>
								<g:each in="${shipmentInstance.containersByType}" var="entry" status="i">									 
									
									<fieldset>
										<legend>
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
											${entry?.key}								
										</legend>
									
										<g:each in="${entry.value}" var="container" status="j">
											<table class="container">
												<tbody>											
													<tr id="container-${container.id}"
														class="${(j % 2) == 0 ? 'odd' : 'even'}">
														<td>&nbsp;</td>
														<td valign="top">
															<div>
																<span> <g:link controller="container" action="show" id="${container.id}">${container?.name}</g:link></span> 
																<span style="color: #aaa; font-size: .75em; padding-left: 15px;">
																	Weight: 
																	<g:if test="${container.weight}"><b>${container?.weight} ${container?.units}</b></g:if> 
																	<g:else><b>unknown</b></g:else> 
																</span> 
																<span style="color: #aaa; font-size: .75em; padding-left: 15px;">
																	Dimensions: 
																	<g:if test="${container.dimensions}">
																		<b>${container.dimensions}</b>
																	</g:if> 
																	<g:else>
																		<b>unknown</b>
																	</g:else> 
																</span> 
																<span style="color: #aaa; font-size: .75em; padding-left: 15px;">
																	Contains: 
																	<b><%= container.getShipmentItems().size() %> items</b> </span>
															</div>	
														</td>
													</tr>
												</tbody>
											</table>										
										</g:each>
										
									</fieldset>
																	

								</g:each>
								<!-- iterate over each container -->
							</g:else>
						</div>						
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
				</tr>				
				<tr style="border-top: 1px solid #aaa;">
					<td>
						<div id="buttons" class="buttons"><span class="button"><a
							href="${createLink(controller: "shipment", action="editDetails")}"><img
							src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
							alt="Edit Shipment" /> Edit Shipment</a></span> <span class="button"><a
							href="${createLink(controller: "shipment", action="changeState")}"><img
							src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}"
							alt="Send Shipment" /> Send Shipment</a></span> 
							<span class="button"><a
							href="${createLink(controller: "shipment", action="addDocument")}"><img
							src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}"
							alt="Add Reference Number" /> Add Document</a></span>
						</div>
							
					</td>
				</tr>
			</table>
		</div>

    </body>
</html>
