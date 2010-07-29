
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
												<td width="10%">${container?.containerType?.name} #${container?.name}</td>																
												<td width="5%">${container?.dimensions}</td>
												<td width="5%">${container?.weight} ${container?.units}</td>
												<td width="5%" style="border: 1px ">${item?.quantity}</td>
												<td width="20%" style="border:1px solid black">${item?.product.name}</td>
												<td width="15%" style="border:1px solid black">${item?.serialNumber}</td>
											</tr>																			
										</g:each>										
									</g:each>		
								</tbody>
							</table>
						</div>
								
					</td>
				</tr>
			</table>


		</fieldset>		
		

	</div>
</body>
</html>
