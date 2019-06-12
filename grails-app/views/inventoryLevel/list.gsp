
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div>
            
				<div class="buttonBar">
                    <g:link action="list" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                       <warehouse:message code="default.list.label" default="List" args="[g.message(code: 'inventoryLevels.label')]"/>
                    </g:link>
                    <g:link action="create" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" default="Add" args="[g.message(code: 'inventoryLevel.label')]"/>
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
                                            ${g.message(code:'inventory.label')}
                                        </label>
                                        <g:selectLocation name="location.id" class="chzn-select-deselect" noSelection="['':'']"
                                                          value="${params?.location?.id}"/>
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
                            <h2><warehouse:message code="results.label" default="Results"/></h2>

                            <table>
                                <thead>
                                    <tr>

                                        <g:sortableColumn property="status" title="${warehouse.message(code: 'inventoryLevel.status.label', default: 'Status')}" />

                                        <th><warehouse:message code="product.productCode.label" default="Product Code" /></th>

                                        <th><warehouse:message code="product.label" default="Product" /></th>

                                        <g:sortableColumn property="inventory" title="${warehouse.message(code: 'inventoryLevel.inventory.label', default: 'Inventory')}" />

                                        <g:sortableColumn property="minQuantity" title="${warehouse.message(code: 'inventoryLevel.minQuantity.label', default: 'Min Quantity')}" />

                                        <g:sortableColumn property="reorderQuantity" title="${warehouse.message(code: 'inventoryLevel.reorderQuantity.label', default: 'Reorder Quantity')}" />

                                        <g:sortableColumn property="maxQuantity" title="${warehouse.message(code: 'inventoryLevel.maxQuantity.label', default: 'Max Quantity')}" />

                                        <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'inventoryLevel.dateCreated.label', default: 'Date Created')}" />

                                    </tr>
                                </thead>
                                <tbody>
                                <g:each in="${inventoryLevelInstanceList}" status="i" var="inventoryLevelInstance">
                                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                        <td>${inventoryLevelInstance.status}</td>
                                        <td>
                                            <g:link action="edit" id="${inventoryLevelInstance.id}">
                                                ${fieldValue(bean: inventoryLevelInstance?.product, field: "productCode")}
                                            </g:link>
                                        </td>
                                        <td>
                                            <g:link action="edit" id="${inventoryLevelInstance.id}">
                                                ${fieldValue(bean: inventoryLevelInstance, field: "product")}
                                            </g:link>
                                        </td>

                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "inventory")}</td>


                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "minQuantity")}</td>

                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "reorderQuantity")}</td>
                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "maxQuantity")}</td>

                                        <td><format:date obj="${inventoryLevelInstance.dateCreated}" /></td>

                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                            <div class="paginateButtons">

                                <g:set var="pageParams"
                                       value="${['location.id': params?.location?.id, q: params.q].findAll {it.value}}"/>

                                <g:paginate total="${inventoryLevelInstanceTotal}" params="${pageParams}"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
