
<%@ page import="org.pih.warehouse.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.id.label" default="ID" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "id")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.ean.label" default="EAN/UPC" /></td>                            
                            <td valign="top" class="value">
                            	${fieldValue(bean: productInstance, field: "ean")}                            
                            	<a href="http://www.upcdatabase.com/item/${fieldValue(bean: productInstance, field: "ean")}">UPC Lookup</a>
                            </td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.description.label" default="Description" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "description")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.name.label" default="Name" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "name")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.user.label" default="User" /></td>                            
                            <td valign="top" class="value"><g:link controller="user" action="show" id="${productInstance?.user?.id}">${productInstance?.user?.encodeAsHTML()}</g:link></td>                            
                        </tr>                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${productInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
