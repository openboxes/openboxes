<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="shipping.addDocument.label" /></title>
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
	<g:render template="summary" />
	<div class="box">
		<h2><warehouse:message code="shipping.addDocument.label" /></h2>
	<!-- process an upload or save depending on whether we are adding a new doc or modifying a previous one -->
		<g:uploadForm controller="document" action="uploadDocument">
			<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
			<g:hiddenField name="documentId" value="${documentInstance?.id}" />
			<table>
				<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="default.name.label" /></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: documentInstance, field: 'name', 'errors')}">
						<g:textField name="name" value="${documentInstance?.name}" class="text" size="80"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="document.type.label" /></label></td>
					<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">
						<g:select name="typeId" from="${org.pih.warehouse.core.DocumentType.list().sort { it.name }}" noSelection="['':'']"
								  class="chzn-select-deselect" value="${documentInstance?.documentType?.id}" optionKey="id" optionValue="${{format.metadata(obj:it)}}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
							code="document.file.label" default="File"/></label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
						<input name="fileContents" type="file" />
					</td>
				</tr>
				</tbody>
				<tfoot>
				<tr>
					<td>
					</td>
					<td>
						<div class="buttons left">
							<button type="submit" class="button">${warehouse.message(code:'default.button.upload.label')}</button>
							<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}">
								<warehouse:message code="default.button.cancel.label" />
							</g:link>
						</div>
					</td>
				</tr>
				</tfoot>
			</table>
		</g:uploadForm>
	</div>
</div>
</body>
</html>
