
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
			<g:form action="sendShipment" method="post">
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
									code="shipment.name.label" default="Shipment Number" /></label></td>
								<td colspan="3" valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								<span style="line-height: 1.5em">${shipmentInstance?.shipmentNumber}</span></td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.name.label" default="Nickname" /></label></td>
								<td colspan="3" valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
								<g:textField name="name" value="${shipmentInstance?.name}" /></td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name"><label><g:message
									code="shipment.shipmentType.label" default="Type" /></label></td>
								<td valign="middle" class="value" nowrap="nowrap"><g:select
									name="shipmentType.id"
									from="${org.pih.warehouse.shipping.ShipmentType.list()}"
									optionKey="id" value="${shipmentInstance?.shipmentType?.id}" />
								</td>
							</tr>							
							
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.method.label" default="Shipment method" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentMethod', 'errors')}">
								<g:select name="shipmentMethod.id"
									from="${org.pih.warehouse.shipping.ShipmentMethod.list()}"
									optionKey="id" optionValue="name"
									value="${shipmentInstance?.shipmentMethod?.id}" /></td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.trackingNumber.label" default="Tracking number" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'trackingNumber', 'errors')}">
									<g:textField name="trackingNumber"
										value="${shipmentInstance?.trackingNumber}" /> </td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name" style="width: 10%;"><label><g:message
									code="shipment.carrier.label" default="Carrier" /></label></td>
								<td valign="top" style="width: 30%;">
									<g:textField name="carrier"
										value="${shipmentInstance?.carrier}" /> 	
								</td>
							</tr>
							<tr class="prop">
								<td class="name"  style="width: 10%;">
									<label><g:message code="shipment.destination.label" default="Recipient" /></label>
								</td>
								<td class="value" style="width: 30%;">								
									<g:textField name="recipient"
										value="${shipmentInstance?.recipient}" /> 								
								</td>
							</tr>						
							
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
									nowrap="nowrap">
										<g:jqueryDatePicker name="expectedShippingDate"
									value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
								</td>
							</tr>		
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.totalValue.label" default="Total Value (USD)" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
									nowrap="nowrap">
										<g:textField name="totalValue" value="${shipmentInstance?.totalValue}" />
								</td>
							</tr>		
							<tr class="prop">
								<td class="name"></td>
								<td>
									<div class="buttons">
										<button type="submit" class="positive"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
										alt="Save" /> Save</button>
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
