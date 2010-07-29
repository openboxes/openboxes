
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
		Attach Document
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
				<td>

					<g:uploadForm controller="document" action="upload">
						<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
						<table>
							<tbody>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="document.documentType.label" default="Document Type" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">
									<g:select name="typeId" from="${DocumentType.list()}" optionKey="id"/></td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="document.file.label" default="Select a file" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: documentInstance, field: 'contents', 'errors')}">
										<input name="contents" type="file" /></td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"></td>
									<td valign="top" class="value">
										<div class="buttons" style="">
											<button type="submit" class="positive"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
												alt="save" /> Upload</button>
											<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
												<img
													src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
													alt="Cancel" /> Cancel </g:link>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</g:uploadForm>


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
										alt="Add Document" style="vertical-align: middle"/> &nbsp; <b>attach document</b></a>										
									
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
