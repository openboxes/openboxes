
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'productType.label', default: 'ProductType')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
        
			<div class="nav">            	
				<g:render template="nav"/>
           	</div>
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'productType.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'productType.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="code" title="${message(code: 'productType.code.label', default: 'Code')}" />
                        
                            <g:sortableColumn property="description" title="${message(code: 'productType.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="sortOrder" title="${message(code: 'productType.sortOrder.label', default: 'Sort Order')}" />
                        
                            <g:sortableColumn property="productClass" title="${message(code: 'productType.productClass.label', default: 'Product Class')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productTypeInstanceList}" status="i" var="productTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${productTypeInstance.id}">${fieldValue(bean: productTypeInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: productTypeInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: productTypeInstance, field: "code")}</td>
                        
                            <td>${fieldValue(bean: productTypeInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: productTypeInstance, field: "sortOrder")}</td>
                        
                            <td>${fieldValue(bean: productTypeInstance, field: "productClass")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productTypeInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
