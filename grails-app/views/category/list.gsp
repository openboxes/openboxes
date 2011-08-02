
<%@ page import="org.pih.warehouse.product.Category" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'category.label', default: 'Category')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'category.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'category.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="description" title="${message(code: 'category.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="sortOrder" title="${message(code: 'category.sortOrder.label', default: 'Sort Order')}" />
                        
                            <th><warehouse:message code="category.parentCategory.label" default="Parent Category" /></th>
                   	    
                            <g:sortableColumn property="dateCreated" title="${message(code: 'category.dateCreated.label', default: 'Date Created')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${categoryInstanceList}" status="i" var="categoryInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${categoryInstance.id}">${fieldValue(bean: categoryInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: categoryInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: categoryInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: categoryInstance, field: "sortOrder")}</td>
                        
                            <td>${fieldValue(bean: categoryInstance, field: "parentCategory")}</td>
                        
                            <td><format:datetime obj="${categoryInstance.dateCreated}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${categoryInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
