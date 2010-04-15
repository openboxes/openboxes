
<%@ page import="org.pih.warehouse.StockCard" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'stockCard.label', default: 'Manage Stock Cards')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                        
                            <th><g:message code="stockCard.product.label" default="Product" /></th>
                            <th><g:message code="stockCard.action.label" default="Actions" /></th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${stockCardInstanceList}" status="i" var="stockCardInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">                                                
                            <td>${fieldValue(bean: stockCardInstance, field: "product")}</td>
                            <td>
			      <g:link action="manage" id="${stockCardInstance.id}">show stock card</g:link>

			    </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${stockCardInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
