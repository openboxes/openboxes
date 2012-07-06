
<%@ page import="org.pih.warehouse.product.ProductGroup" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productGroup.label', default: 'ProductGroup')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            
            
			<div class="buttonBar">            	
            	<span class="linkButton">
            		<g:link class="list" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'productGroup.label').toLowerCase()]"/></g:link>
            	</span>
            	<span class="linkButton">
            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'productGroup.label').toLowerCase()]"/></g:link>
            	</span>
           	</div>
            
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="description" title="${warehouse.message(code: 'productGroup.description.label', default: 'Description')}" />

                            <g:sortableColumn property="category" title="${warehouse.message(code: 'productGroup.category.label', default: 'Category')}" />

                            <g:sortableColumn property="products" title="${warehouse.message(code: 'productGroup.products.label', default: 'Products')}" />
                        
                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'productGroup.dateCreated.label', default: 'Date Created')}" />
                        
                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'productGroup.lastUpdated.label', default: 'Last Updated')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productGroupInstanceList}" status="i" var="productGroupInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>
	                            <g:link action="edit" id="${productGroupInstance.id}">
    	                        	${fieldValue(bean: productGroupInstance, field: "description")}
    	                  		</g:link>
    	                  	</td>
                            <td>${productGroupInstance.category}</td>
                            <td>${productGroupInstance.products.size()}</td>
                        
                            <td><format:date obj="${productGroupInstance.dateCreated}" /></td>
                        
                            <td><format:date obj="${productGroupInstance.lastUpdated}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productGroupInstanceTotal}" />
            </div>
        
        </div>
        
    </body>
</html>
