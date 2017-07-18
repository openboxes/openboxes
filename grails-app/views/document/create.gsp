
<%@ page import="org.pih.warehouse.core.Document" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'document.label', default: 'Document')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${documentInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${documentInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['documents']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['document']"/></g:link>
			</div>

			<g:form action="save" method="post" enctype="multipart/form-data">
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="documentType.id"><warehouse:message code="document.documentType.label" default="Document Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">
                                    <g:select class="chzn-select-deselect" name="documentType.id" from="${org.pih.warehouse.core.DocumentType.list()}" optionKey="id" value="${documentInstance?.documentType?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="fileContents"><warehouse:message code="document.fileContents.label" default="File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
                                    <input type="file" id="fileContents" name="fileContents" />
                                </td>
                            </tr>

                        <%--
                            <tr class="prop">
								<td valign="top" class="name">
									<label for="name"><warehouse:message code="document.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'name', 'errors')}">
									<g:textField class="text" size="100" name="name" cols="40" rows="5" value="${documentInstance?.name}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="filename"><warehouse:message code="document.filename.label" default="Filename" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'filename', 'errors')}">
									<g:textField class="text" size="100" name="filename" cols="40" rows="5" value="${documentInstance?.filename}" />
								</td>
							</tr>



							<tr class="prop">
								<td valign="top" class="name">
									<label for="extension"><warehouse:message code="document.extension.label" default="Extension" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'extension', 'errors')}">
									<g:textField class="text" size="100" name="extension" value="${documentInstance?.extension}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="contentType"><warehouse:message code="document.contentType.label" default="Content Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'contentType', 'errors')}">
									<g:textField class="text" size="100" name="contentType" value="${documentInstance?.contentType}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="fileUri"><warehouse:message code="document.fileUri.label" default="File Uri" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'fileUri', 'errors')}">
                                    <g:textField class="text" size="100" name="fileUri" value="${documentInstance?.fileUri}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="documentNumber"><warehouse:message code="document.documentNumber.label" default="Document Number" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'documentNumber', 'errors')}">
									<g:textField class="text" size="100" name="documentNumber" cols="40" rows="5" value="${documentInstance?.documentNumber}" />
								</td>
							</tr>
                            --%>


                            <%--
							<tr class="prop">
								<td valign="top" class="name">
									<label for="image"><warehouse:message code="document.image.label" default="Image" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'image', 'errors')}">
									<g:checkBox name="image" value="${documentInstance?.image}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="size"><warehouse:message code="document.size.label" default="Size" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'size', 'errors')}">
									<g:textField class="text" size="100" name="size" value="${fieldValue(bean: documentInstance, field: 'size')}" />
								</td>
							</tr>
                            --%>

							<tr class="prop">
								<td valign="top"></td>
								<td valign="top">
									<div class="buttons left">
									   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

									   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

									</div>
								</td>
							</tr>

						</tbody>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
