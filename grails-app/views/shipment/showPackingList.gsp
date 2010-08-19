
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
		Packing List
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
	
		<table>

			<tr>
				<td colspan="2">
					<div style="padding-bottom: 10px;">
						<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
						 &nbsp; &raquo; &nbsp; 
						<span style="font-size: 90%">View Packing List</span>
					</div>					
				</td>
			</tr>
	
			<tr>
				<td>
					<fieldset>							
						<table>
							<tbody>
								<tr>
									<td width="24px;">
										<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
											alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />						
									</td>
									<td>
										<span style="font-size: 1.2em;">${shipmentInstance.name}</span> 
										&nbsp; 
										<br/>
										<span style="color: #aaa; font-size: 0.8em;">
											last modified: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />	&nbsp;							
											created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" />			
										</span>	
									</td>		
									<td style="text-align: right;">
										<span class="fade">[Shipment No. ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}]</span>
									</td>
								</tr>
							</tbody>
						</table>
						<br/><br/>
						<table>
							<tr>
								<td>
									<div id="containers" class="section">			
															
										<table border="1" style="padding: 0px; margin: 0px">
											<tbody>
												<tr>
													<th>Shipment Unit</th>
													<th>Dimensions</th>
													<th>Weight</th>
													<th>Qty</th>
													<th>Item</th>
													<th>Serial No.</th>										
												</tr>				
												<g:each var="container" in="${shipmentInstance.containers}" status="i">
													<g:each var="item" in="${container.shipmentItems}" status="j">
														<tr>
															<td width="10%">
																<g:if test="${j==0}">${container?.containerType?.name} #${container?.name}</g:if>
															</td>																
															<td width="5%">
																<g:if test="${j==0}">${container?.dimensions}</g:if>
															</td>
															<td width="5%">
																<g:if test="${j==0}">${container?.weight} ${container?.units}</g:if>
															</td>
															<td width="5%" style="border: 1px ">${item?.quantity}</td>
															<td width="20%" style="border:1px solid black">${item?.product.name}</td>
															<td width="15%" style="border:1px solid black">${item?.serialNumber}</td>
														</tr>																			
													</g:each>										
												</g:each>		
											</tbody>
										</table>
									</div>
									<div style="text-align: right; padding: 10px;">
										<g:link controller="shipment" action="downloadPackingList" id="${shipmentInstance.id}" ><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" 
										alt="Export Packing List" style="vertical-align: middle"/> Download</g:link>		
									</div>						
											
								</td>
							</tr>
						</table>
					</fieldset>		
		



				
				</td>
		
		
				<td width="30%">
					<div style="width: 300px">
						<fieldset>
							<legend>Actions</legend>
							<table>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
										alt="Show Shipment" style="vertical-align: middle" /> &nbsp; show details</g:link>
									
									</td>
								</tr>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}"><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
										alt="Add Document" style="vertical-align: middle"/> &nbsp; edit contents</a></g:link>
									</td>
								</tr>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="editDetails" id="${shipmentInstance.id}"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
										alt="Edit Shipment" style="vertical-align: middle" /> &nbsp; edit details</g:link>
									
									</td>
								</tr>
								<tr class="prop">
									<td>
										<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}"><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'page_word.png')}" 
										alt="Add Document" style="vertical-align: middle"/> &nbsp; attach document</a>										
									
									</td>
								</tr>
								<tr class="prop">
									<td>
										<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img 
										src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
										alt="View Packing List" style="vertical-align: middle"/> &nbsp; <b>view packing list</b></g:link>		
									</td>
								</tr>									
								
								
								<tr class="prop"></tr>
							</table>
						</fieldset>
					</div>
				</td>
			</tr>
		</table>

	</div>
</body>
</html>
