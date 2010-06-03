
<%@ page import="org.pih.warehouse.Product" %>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>		
		<g:javascript library="prototype" />
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
						    <th><g:message code="product.details.label" default="Details" /></th>
                            <g:sortableColumn property="ean" title="${message(code: 'product.ean.label', default: 'EAN/UPC')}" />
                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productInstanceList}" status="i" var="productInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
							<td align="center"><g:link action="show" id="${productInstance.id}">view</g:link></td>
                            <td>${fieldValue(bean: productInstance, field: "ean")}</td>
                            <td>${fieldValue(bean: productInstance, field: "name")}</td>
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
