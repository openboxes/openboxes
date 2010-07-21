
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
<g:set var="entityName"
	value="${message(code: 'shipment.label', default: 'Shipment')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<!-- Specify content to overload like global navigation links, page titles, etc. -->
<content tag="pageTitle">
Shipment Details
</content>
<content tag="menuTitle">
${entityName}</content>
<content tag="globalLinksMode">
append
</content>
<content tag="localLinksMode">
override
</content>
<content tag="globalLinks">
<g:render template="global" model="[entityName:entityName]" />
</content>
<content tag="localLinks">
<g:render template="local" model="[entityName:entityName]" />
</content>
</head>

<body>

<div class="body"><g:if test="${flash.message}">
	<div class="message">
	${flash.message}
	</div>
</g:if>



<table>
	<tbody>
		<tr class="prop">
			<td>			
				<g:form action="update" method="post">
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<g:hiddenField name="version" value="${shipmentInstance?.version}" />
					<table>
						<tbody>
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
									name="shipmentType"
									from="${org.pih.warehouse.shipping.ShipmentType.list()}"
									optionKey="id" value="${shipmentInstance?.shipmentType?.id}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name"><label><g:message
									code="shipment.shipmentStatus.label" default="Status" /></label></td>
								<td valign="middle" class="value" nowrap="nowrap"><g:select
									name="shipmentStatus.id"
									from="${org.pih.warehouse.shipping.ShipmentStatus.list()}"
									optionKey="id" value="${shipmentInstance?.shipmentStatus?.id}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedShippingDate.label" default="Ship date" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
									nowrap="nowrap"><g:datePicker name="expectedShippingDate"
									precision="day"
									value="${shipmentInstance?.expectedShippingDate}" /></td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedDeliveryDate.label"
									default="Delivery date" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}"
									nowrap="nowrap"><g:datePicker name="expectedDeliveryDate"
									precision="day"
									value="${shipmentInstance?.expectedDeliveryDate}" /></td>
							</tr>
							<tr>
								<td>&nbsp;</td>
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
									style="color: #aaa">format: ${shipmentInstance?.shipmentMethod?.trackingFormat}
								</span></td>
							</tr>
							<tr>
								<td>&nbsp;</td>
							</tr>
	
							<tr class="prop">
								<td></td>
								<td>
								<div class="buttons">
								<button type="submit" class="positive"><img
									src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
									alt="Save" /> Save</button>
								<a href="#" id="edit-details-link" class="negative"> <img
									src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
									alt="Cancel" /> Cancel </a></div>
								</td>
							</tr>
						</tbody>
					</table>
				</g:form>
			</td>
		</tr>
	</tbody>
</table>
</div>
</body>
</html>
