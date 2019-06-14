
<%@ page import="org.pih.warehouse.product.ProductAssociation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productAssociations.label', default: 'Product Associations')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div>
            
				<div class="button-bar">
                    <g:link class="button" action="list">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                        <warehouse:message code="default.list.label" args="[g.message(code:'productAssociations.label')]"/>
                    </g:link>
                    <g:link class="button" action="create">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[g.message(code:'productAssociation.label')]"/>
                    </g:link>
                    <g:link class="button" action="export">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                        <warehouse:message code="default.export.label" args="[g.message(code:'productAssociations.label')]"/>
                    </g:link>
	        	</div>


                <div class="yui-gf">
                    <div class="yui-u first">

                        <div class="box">
                            <h2><warehouse:message code="filters.label" default="Filters"/></h2>
                            <g:form action="list" method="get">

                                <div class="filters">
                                    <div class="filter-list-item">
                                        <label>${g.message(code:'product.label')}</label>
                                        <g:textField name="q" value="${params.q}" class="large text"/>
                                    </div>
                                    <div class="filter-list-item">
                                        <label>
                                            ${g.message(code:'productAssociation.productAssociationTypeCode.label')}
                                        </label>
                                        <g:selectProductAssociationTypeCode name="code"
                                                                            value="${selectedTypes}"
                                                                            multiple="true"
                                                                            noSelection="['':'']"
                                                                            class="chzn-select-deselect"/>
                                    </div>
                                </div>
                                <div class="buttons">
                                    <button class="button">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />&nbsp;
                                    ${g.message(code: 'default.button.search.label')}
                                    </button>
                                    <button name="format" value="csv" class="button">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                    <warehouse:message code="default.button.download.label" default="Download"/>
                                    </button>
                                </div>
                            </g:form>
                        </div>
                    </div>
                    <div class="yui-u">

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
                            <div class="paginateButtons">
                                <g:paginate total="${productAssociationInstanceTotal}" />
                            </div>
                        </div>

                    </div>
                </div>


            </div>
        </div>
    </body>
</html>
