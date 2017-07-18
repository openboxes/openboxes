
<%@ page import="org.pih.warehouse.core.Document" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'document.label', default: 'Document')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>

            <div class="button-bar">
                <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['documents']"/></g:link>
                <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['document']"/></g:link>
            </div>

            <div class="box">
                <h2><warehouse:message code="default.show.label" args="[entityName]" /></h2>
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.filename.label" default="Filename" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "filename")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.fileContents.label" default="File Contents" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "fileContents")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.extension.label" default="Extension" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "extension")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.contentType.label" default="Content Type" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "contentType")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.fileUri.label" default="File Uri" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "fileUri")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.documentNumber.label" default="Document Number" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "documentNumber")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.documentType.label" default="Document Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="documentType" action="show" id="${documentInstance?.documentType?.id}">${documentInstance?.documentType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${documentInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${documentInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.image.label" default="Image" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${documentInstance?.image}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="document.size.label" default="Size" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: documentInstance, field: "size")}</td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons">
					                <g:form>
					                    <g:hiddenField name="id" value="${documentInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
