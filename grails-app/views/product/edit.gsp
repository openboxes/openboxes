
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>		
    </head>    
    <body>
    	<h1>Edit Product</h1>
    
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${productInstance?.id}" />
                <g:hiddenField name="version" value="${productInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="product.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${productInstance?.name}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="ean"><g:message code="product.ean.label" default="UPC" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'ean', 'errors')}">
                                    <g:textField name="ean" value="${productInstance?.ean}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="product.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${productInstance?.description}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="type.id"><g:message code="product.productType.label" default="Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'type', 'errors')}">
                                    <g:select name="type.id" from="${org.pih.warehouse.product.ProductType.list()}" optionKey="id" value="${productInstance?.type?.id}"  />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="subType.id"><g:message code="product.productType.label" default="Sub Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'type', 'errors')}">
                                    <g:select name="subType.id" from="${org.pih.warehouse.product.ProductType.list()}" optionKey="id" value="${productInstance?.type?.id}"  />
                                </td>
                            </tr>
                        	<%-- 
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="user"><g:message code="product.user.label" default="User" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'user', 'errors')}">
                                    <g:select name="user.id" from="${org.pih.warehouse.user.User.list()}" optionKey="id" value="${productInstance?.user?.id}"  />
                                </td>
                            </tr>
                        	--%>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.save.label', default: 'Save')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
