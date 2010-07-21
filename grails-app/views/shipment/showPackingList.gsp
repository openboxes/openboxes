
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
	
	
		<div class="list">	
			<table>
				<tbody>
					<g:each var="item" in="${shipmentInstance.allShipmentItems}" status="i">
				
						<tr>
							<td>
								${i}
							
							</td>
						</tr>
					</g:each>
		
				</tbody>
			</table>
			
		</div>
	</div>
</body>
</html>
