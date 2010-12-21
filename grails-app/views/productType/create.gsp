
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'productType.label', default: 'ProductType')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
			<div class="nav">            	
				<g:render template="nav"/>
           	</div>
        
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productTypeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productTypeInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><g:message code="productType.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${productTypeInstance?.name}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="productClass"><g:message code="productType.productClass.label" default="Product Class" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'productClass', 'errors')}">
	                                    <g:select name="productClass" from="${org.pih.warehouse.product.ProductClass?.values()}" value="${productTypeInstance?.productClass}" noSelection="['': '']" />
	                                </td>
	                            </tr>


<%-- 	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="code"><g:message code="productType.code.label" default="Code" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'code', 'errors')}">
	                                    <g:textField name="code" value="${productTypeInstance?.code}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><g:message code="productType.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${productTypeInstance?.description}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="sortOrder"><g:message code="productType.sortOrder.label" default="Sort Order" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'sortOrder', 'errors')}">
	                                    <g:textField name="sortOrder" value="${fieldValue(bean: productTypeInstance, field: 'sortOrder')}" />
	                                </td>
	                            </tr>
--%>	                            
	                        
	                        
	                        
		                        <tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">
						                <div class="buttons">
						                   <g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
						                   
						                   <g:link action="list">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
						                   
						                </div>                        	
		                        	</td>
		                        </tr>
		                        
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
