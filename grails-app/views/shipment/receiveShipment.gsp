
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
		Edit Shipment
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
			<table>
				<tr>
					<td>
						<div style="padding-bottom: 10px;">
							<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
							&raquo; <span style="font-size: 90%">Edit Details</span>
						</div>										
					</td>
				</tr>
			</table>		
		</td>	
	</tr>
	<tr>
		<td>
			<g:form action="update" method="post">
				<fieldset>			
					<table>
						<tbody>
							<tr>
								<td width="24px;">
									<%-- 
									<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
										valign="top" style="vertical-align: middle;" /> 
									--%>
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
					
				
				
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<g:hiddenField name="version" value="${shipmentInstance?.version}" />
					<table>
						<tbody>
							<tr >
								<td>&nbsp;</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
									nowrap="nowrap">
										<g:jqueryDatePicker name="expectedShippingDate" 
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy" />
								</td>
							</tr>		
							<tr class="prop">
								<td class="name"></td>
								<td>
									<div class="buttons">
										<button type="submit" class="positive"><img 
											src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
										<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
											alt="Cancel" /> Cancel</g:link>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</fieldset>
			</g:form>
			
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
								alt="Edit Contents" style="vertical-align: middle"/> &nbsp; edit contents</a></g:link>
							</td>
						</tr>
						<tr class="prop">
							<td>
								<g:link controller="shipment" action="editDetails" id="${shipmentInstance.id}"><img
								src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}"
								alt="Edit Shipment" style="vertical-align: middle" /> &nbsp; <b>edit details</b></g:link>
							
							</td>
						</tr>
						<tr class="prop">
							<td>
								<a href="${createLink(controller: "shipment", action: "addDocument", id: shipmentInstance.id)}"><img 
								src="${createLinkTo(dir:'images/icons/silk',file:'page_word.png')}" 
								alt="Attach Document" style="vertical-align: middle"/> &nbsp; attach document</a>										
							
							</td>
						</tr>
						<tr class="prop">
							<td>
								<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" ><img 
								src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
								alt="View Packing List" style="vertical-align: middle"/> &nbsp; view packing list</g:link>		
							</td>
						</tr>					
					</table>
				</fieldset>
			</div>								
		</td>				
	</tr>
</table>
			
			
	</div>
</body>
</html>
