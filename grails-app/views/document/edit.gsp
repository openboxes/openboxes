<%@ page import="org.pih.warehouse.core.Document" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'document.label', default: 'Document')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
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


            <div id="document-tabs" class="tabs">
                <ul>
                    <li><a href="#document-metadata-tab"><warehouse:message code="document.label"/></a></li>
                    <li><a href="#document-file-tab"><warehouse:message code="document.file.label" default="File"/></a></li>
                    <li><a href="${request.contextPath}/document/preview/${documentInstance?.id}"><warehouse:message code="default.button.preview.label" default="Preview"/></a></li>
                </ul>
                <div id="document-metadata-tab">
                    <g:form method="post" enctype="multipart/form-data">
                        <g:hiddenField name="id" value="${documentInstance?.id}" />
                        <g:hiddenField name="version" value="${documentInstance?.version}" />
                        <div class="box">
                            <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                            <table>
                                <tbody>

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
                                            <label for="documentType.id"><warehouse:message code="document.documentType.label" default="Document Type" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">
                                            <g:select class="chzn-select-deselect" name="documentType.id" from="${org.pih.warehouse.core.DocumentType.list()}" optionKey="id" value="${documentInstance?.documentType?.id}" noSelection="['null': '']" />
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                          <label for="extension"><warehouse:message code="document.extension.label" default="Extension" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'extension', 'errors')}">
                                            <g:textField class="text" size="100" name="extension" cols="40" rows="5" value="${documentInstance?.extension}" />
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                          <label for="contentType"><warehouse:message code="document.contentType.label" default="Content Type" /></label>
                                        </td>
                                        <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'contentType', 'errors')}">
                                            <g:textField class="text" size="100" name="contentType" cols="40" rows="5" value="${documentInstance?.contentType}" />
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
                                    <tr class="prop">
                                        <td valign="top"></td>
                                        <td valign="top">
                                            <div class="buttons left">
                                                <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />

                                                <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </g:form>
                </div>
                <div id="document-file-tab">
                    <g:form method="post" enctype="multipart/form-data">

                        <g:hiddenField name="id" value="${documentInstance?.id}" />
                        <g:hiddenField name="version" value="${documentInstance?.version}" />
                        <div class="box">
                            <h2><warehouse:message code="default.upload.label" args="[entityName]" /></h2>
                            <table>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="filename"><warehouse:message code="document.filename.label" default="Filename" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'filename', 'errors')}">
                                        <div id="filename">${documentInstance?.filename}</div>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="image"><warehouse:message code="document.image.label" default="Image" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'image', 'errors')}">
                                        <div id="image">${documentInstance?.isImage()}</div>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="size"><warehouse:message code="document.size.label" default="Size" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'size', 'errors')}">
                                        <div id="size">${documentInstance.size} bytes</div>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="fileContents"><warehouse:message code="document.lastUpdated.label" default="Last Updated" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'lastUpdated', 'errors')}">
                                        <g:formatDate date="${documentInstance?.lastUpdated}"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="fileContents"><warehouse:message code="document.fileContents.label" default="File Contents" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
                                        <input type="file" id="fileContents" name="fileContents" />
                                        <g:actionSubmit class="button" action="upload" value="${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}" />
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top"></td>
                                    <td valign="top">
                                        <div class="buttons left">
                                            <g:link controller="document" action="download" class="button" id="${documentInstance?.id}">
                                                <g:message code="document.download.label"/>
                                            </g:link>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </g:form>
                </div>
            </div>
        </div>
        <script>
            $(document).ready(function() {

                $(".tabs").tabs({
                    cookie : {
                        expires : 1
                    }
                });
            });
        </script>

    </body>
</html>
