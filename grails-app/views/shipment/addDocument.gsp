<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Attach Document</content>
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
				<td width="75%">
					<fieldset>
						<g:render template="summary" />
						<div>
							<g:uploadForm controller="document" action="upload">
								<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
										<tr class="prop">
											<td valign="top" class="name"><label><g:message
												code="document.documentType.label" default="Document Type" /></label></td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">
												<g:select name="typeId" from="${org.pih.warehouse.core.DocumentType.list()}" optionKey="id" optionValue="name"/>
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label class="optional"><g:message
												code="document.name.label" default="Description" /></label>
											</td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'name', 'errors')}">
												<g:textField name="name" value="${documentInstance?.name}" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label class="optional"><g:message
												code="document.number.label" default="Document Number" /></label>
											</td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'documentNumber', 'errors')}">
												<g:textField name="documentNumber" value="${documentInstance?.documentNumber}" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label><g:message
												code="document.file.label" default="Select a file" /></label>
											</td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
												<input name="fileContents" type="file" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
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
						</div>
					</fieldset>
				</td>
				<td width="20%">
					<g:render template="sidebar" />						
				</td>				
			</tr>
		</table>
	</div>
</body>
</html>
