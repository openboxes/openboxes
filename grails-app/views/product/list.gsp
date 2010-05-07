
<%@ page import="org.pih.warehouse.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
	<!--
	    Specify content to overload like global navigation links,
	    page titles, etc.
	-->
	<content tag="globalLinks">
	    <span class="menuButton">
		<g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link>
	    </span>
	</content>
	<content tag="pageTitle">
	    <g:message code="default.list.label" args="[entityName]" />
	</content>
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
                            <g:sortableColumn property="ean" title="${message(code: 'product.ean.label', default: 'EAN/UPC')}" />
                            <g:sortableColumn property="category" title="${message(code: 'product.category.label', default: 'Category')}" />
                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
                            <th><g:message code="product.user.label" default="User" /></th>
			    <th><g:message code="product.details.label" default="Show Details" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productInstanceList}" status="i" var="productInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean: productInstance, field: "ean")}</td>
                            <td>${fieldValue(bean: productInstance, field: "category")}</td>
                            <td>${fieldValue(bean: productInstance, field: "name")}</td>
                            <td>${fieldValue(bean: productInstance, field: "user")}</td>
                            <td>
			      <g:link action="show" id="${productInstance.id}">Show Details</g:link>
			    </td>
<!--
			    <td>
			      <g:if test="${productInstance.stockCard}">
				<g:link controller="stockCard" action="manage" id="${productInstance.id}">Manage Stock Card</g:link>
			      </g:if>
			      <g:else>
				<g:link controller="stockCard" action="create" params="[product:productInstance.id]">Create Stock Card</g:link>
			      </g:else>
			    </td>
-->
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
