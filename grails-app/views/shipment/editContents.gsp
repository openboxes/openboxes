
<%@ page import="org.pih.warehouse.shipping.ContainerType"%>
<%@ page import="org.pih.warehouse.shipping.Document"%>
<%@ page import="org.pih.warehouse.shipping.DocumentType"%>
<%@ page import="org.pih.warehouse.shipping.EventType"%>
<%@ page import="org.pih.warehouse.core.Location"%>
<%@ page import="org.pih.warehouse.core.Organization"%>
<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.shipping.ReferenceNumberType"%>
<%@ page import="org.pih.warehouse.shipping.Shipment"%>
<%@ page import="org.pih.warehouse.user.User"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		${shipmentInstance?.name}
		<span style="color: #aaa; font-size: 0.8em; padding-left: 20px;">
			Created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" /> |
			Updated: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />
		</span>
	</content>
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	
	
		<div id="containers" class="section" >
		
			<table>
				<tbody>
					<tr>
						<td style="width: 60%" valign="top">
						
							<g:each in="${shipmentInstance.containersByType}" var="entry" status="i">									 
								<h2>				
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
											alt="container" style="vertical-align: middle; width: 24px; height: 24px;" />
									</g:elseif>
								
									${entry?.key} Units						
								</h2>
										
								<table>		
									<g:each in="${entry.value}" var="container" status="j">				
								
										<tr>
											<td>
														
												
												<div style="background-color: #F8F7EF; padding: 10px; border-top: 1px solid #aaa;">
													<span style="color: #666">${container?.name}</span>
													<span style="color: #666; font-size: .75em; padding-left: 10px;">
														Weight: 
														<g:if test="${container.weight}"><b>${container?.weight} ${container?.units}</b></g:if> 
														<g:else><b>unknown</b></g:else> 
													</span> 
													<span style="color: #666; font-size: .75em; padding-left: 10px;">
														Dimensions: 
														<g:if test="${container.dimensions}">
															<b>${container.dimensions}</b>
														</g:if> 
														<g:else>
															<b>unknown</b>
														</g:else> 
													</span> 
													<span style="color: #666; font-size: .75em; padding-left: 10px;">
														Contains: 
														<b><%= container.getShipmentItems().size() %> items</b> 
													</span>												 
												</div>													
																						
												<fieldset class="container">
													<table>
														<tbody>
															<g:if test="${container?.shipmentItems}">
																<tr>
																	<th style="text-align: center;">Quantity</th>
																	<th>Item</th>
																</tr>
																<g:each var="item" in="${container.shipmentItems}" status="k">
																	<tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
																		<td style="text-align: center; width: 20%;">${item.quantity}</td>
																		<td>${item?.product?.name}</td>
																	</tr>							
																</g:each>
															</g:if>
															<g:else>
																<tr>
																	<td style="text-align: center">
																		<span class="fade">(empty)</span>
																	</td>
																</tr>													
															</g:else>
														</tbody>
													</table>											
												</fieldset>
											</td>						
										</tr>
									</g:each>		
								</table>
							</g:each>
						</td>
						<td width="1%"></td>
						<td valign="top">
						
							<h2>
								<span style="height: 24px">
									<img src="${createLinkTo(dir:'images/icons/silk/',file:'page_add.png')}"
												alt="suitcase"
												style="vertical-align: middle; " />
								
									Actions
								</span>
							</h2>		
							<fieldset>
								<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	

									<h2>Add a product</h2>
									<table>
										<tr class="prop">
											<td class="name"><label>Item</label></td>
											<td class="value">
												<gui:autoComplete size="20" width="100" 
													id="selectedItem" name="selectedItem" 
													controller="shipment" action="availableItems"/>												
											
											</td>												
										</tr>								
										<tr class="prop">
											<td class="name"><label>Add To</label></td>
											<td class="value">
												<g:select name="container.id" from="${shipmentInstance?.containers}" 
													optionKey="id" optionValue="name" value="" noSelection="['0':'']" />										
											</td>												
										</tr>								
										<tr class="prop">
											<td class="name"></td>
											<td class="value">
											
												<div class="buttons">
													<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add</button></span>
												</div>
											</td>												
										</tr>									
									</table>									
								</g:form>			
							
						
						
							</fieldset>
						
						</td>
					</tr>
				</tbody>
			</table>
							
		</div>

	</div>
</body>
</html>
