
<%@ page import="org.pih.warehouse.product.ProductAssociation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productAssociation']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productAssociation']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'productAssociation.id.label', default: 'Id')}" />
                            
                                <g:sortableColumn property="code" title="${warehouse.message(code: 'productAssociation.code.label', default: 'Code')}" />

                                <th><warehouse:message code="productAssociation.product.label" default="Product" /></th>

                                <th><warehouse:message code="productAssociation.associatedProduct.label" default="Associated Product" /></th>
                            
                                <g:sortableColumn property="quantity" title="${warehouse.message(code: 'productAssociation.quantity.label', default: 'Quantity')}" />
                            
                                <g:sortableColumn property="comments" title="${warehouse.message(code: 'productAssociation.comments.label', default: 'Comments')}" />
                            
                                <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'productAssociation.dateCreated.label', default: 'Date Created')}" />
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${productAssociationInstanceList}" status="i" var="productAssociationInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${productAssociationInstance.id}">${fieldValue(bean: productAssociationInstance, field: "id")}</g:link></td>
                            
                                <td>${fieldValue(bean: productAssociationInstance, field: "code")}</td>

                                <td>
                                    <g:link controller="product" action="edit" id="${productAssociationInstance?.product?.id}">
                                        ${fieldValue(bean: productAssociationInstance?.product, field: "productCode")}
                                        ${fieldValue(bean: productAssociationInstance?.product, field: "name")}
                                    </g:link>

                                </td>

                                <td>
                                    <g:link controller="product" action="edit" id="${productAssociationInstance?.product?.id}">
                                        ${fieldValue(bean: productAssociationInstance?.associatedProduct, field: "productCode")}
                                        ${fieldValue(bean: productAssociationInstance?.associatedProduct, field: "name")}
                                    </g:link>
                                </td>
                            
                                <td>${fieldValue(bean: productAssociationInstance, field: "quantity")}</td>
                            
                                <td>${fieldValue(bean: productAssociationInstance, field: "comments")}</td>
                            
                                <td><format:date obj="${productAssociationInstance.dateCreated}" /></td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productAssociationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
