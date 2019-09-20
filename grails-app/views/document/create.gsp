
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
