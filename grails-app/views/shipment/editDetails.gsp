
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
	
	
		<g:form action="update" method="post">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
			<table>
				<tbody>
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
							value="${shipmentInstance?.trackingNumber}" /> <span
							style="color: #aaa">
						</span></td>
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
						<td class="name"></td>
						<td>
							<div class="buttons">
								<button type="submit" class="positive"><img
								src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
								alt="Save" /> Save</button>
								<g:link controller="dashboard" action="index" class="negative"> <img
								src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
								alt="Cancel" /> Cancel </g:link>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
</body>
</html>
