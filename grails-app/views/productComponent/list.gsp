
<%@ page import="org.pih.warehouse.product.ProductComponent" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productComponent.label', default: 'ProductComponent')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productComponent']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productComponent']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'productComponent.id.label', default: 'Id')}" />
                            
                                <th><warehouse:message code="productComponent.componentProduct.label" default="Component Product" /></th>
                            
                                <g:sortableColumn property="quantity" title="${warehouse.message(code: 'productComponent.quantity.label', default: 'Quantity')}" />
                            
                                <th><warehouse:message code="productComponent.unitOfMeasure.label" default="Unit Of Measure" /></th>
                            
                                <th><warehouse:message code="productComponent.assemblyProduct.label" default="Assembly Product" /></th>
                            
                                <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'productComponent.dateCreated.label', default: 'Date Created')}" />
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${productComponentInstanceList}" status="i" var="productComponentInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${productComponentInstance.id}">${fieldValue(bean: productComponentInstance, field: "id")}</g:link></td>
                            
                                <td>${fieldValue(bean: productComponentInstance, field: "componentProduct")}</td>
                            
                                <td>${fieldValue(bean: productComponentInstance, field: "quantity")}</td>
                            
                                <td>${fieldValue(bean: productComponentInstance, field: "unitOfMeasure")}</td>
                            
                                <td>${fieldValue(bean: productComponentInstance, field: "assemblyProduct")}</td>
                            
                                <td><format:date obj="${productComponentInstance.dateCreated}" /></td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productComponentInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
