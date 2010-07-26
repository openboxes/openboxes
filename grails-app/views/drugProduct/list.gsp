
<%@ page import="org.pih.warehouse.product.DrugProduct" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'drugProduct.label', default: 'DrugProduct')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>          
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'drugProduct.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'drugProduct.name.label', default: 'Name')}" />
                        
                            <th><g:message code="drugProduct.category.label" default="Category" /></th>
                   	    
                            <th><g:message code="drugProduct.genericType.label" default="Generic Type" /></th>
                   	    
                            <th><g:message code="drugProduct.productType.label" default="Product Type" /></th>
                   	    
                            <g:sortableColumn property="tags" title="${message(code: 'drugProduct.tags.label', default: 'Tags')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${drugProductInstanceList}" status="i" var="drugProductInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${drugProductInstance.id}">${fieldValue(bean: drugProductInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: drugProductInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: drugProductInstance, field: "category")}</td>
                        
                            <td>${fieldValue(bean: drugProductInstance, field: "genericType")}</td>
                        
                            <td>${fieldValue(bean: drugProductInstance, field: "productType")}</td>
                        
                            <td>${fieldValue(bean: drugProductInstance, field: "tags")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${drugProductInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
