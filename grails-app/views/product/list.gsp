
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom"/>
        <g:set var="entityName" value="${warehouse.message(code: 'products.label')}" />
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

				<div class="button-bar">
                    <g:link class="button" action="list">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                        <warehouse:message code="default.list.label" args="[warehouse.message(code:'products.label')]"/>
                    </g:link>
	            	<g:isUserAdmin>
                        <g:link class="button" action="create">
                            <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                            <warehouse:message code="default.add.label" args="[warehouse.message(code:'product.label')]"/>
                        </g:link>
	                </g:isUserAdmin>
                    <g:link controller="product" action="exportAsCsv" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />&nbsp;
                        <warehouse:message code="default.export.label" args="[g.message(code: 'products.label')]"/>
                    </g:link>
                    <g:link controller="product" action="exportAsCsv" class="button" params="[includeAttributes:true]">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />&nbsp;
                        <warehouse:message code="default.export.label" args="[g.message(code: 'productAttributes.label')]"/>
                    </g:link>
                    <g:isUserAdmin>
                        <g:link controller="product" action="importAsCsv" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'database_refresh.png')}" />&nbsp;
                            <warehouse:message code="default.import.label" args="[g.message(code: 'products.label')]"/>
                        </g:link>
                    </g:isUserAdmin>
                </div>

                <div class="yui-gf">
                    <div class="yui-u first">
                        <div class="box">
                            <h2>${warehouse.message(code:'default.filters.label')}</h2>
                            <g:form action="list" method="get">
                                <g:hiddenField name="sort" value="${params.sort}"/>
                                <g:hiddenField name="order" value="${params.order}"/>
                                <g:hiddenField name="offset" value="${params.offset}"/>

                                <div class="filter-list-item">
                                    <label><warehouse:message code="product.name.label"/></label>
                                    <p>
                                        <g:textField name="q" value="${params.q }" class="text" style="width:100%;"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="category.label"/></label>
                                    <p>
                                        <g:selectCategory id="categoryId"
                                                          name="categoryId"
                                                          multiple="false"
                                                          class="chzn-select-deselect"
                                                          noSelection="['null':'']"
                                                          style="width:100%;"
                                                          value="${params.list('categoryId')}"/>

                                    </p>
                                    <p>
                                        <g:checkBox name="includeCategoryChildren" value="${params.includeCategoryChildren}"/>
                                        <label>${warehouse.message(code:'search.includeCategoryChildren.label', default: 'Include all products in all subcategories.')}</label>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="product.formulary.label"/></label>
                                    <p>
                                        <g:selectCatalogs name="catalogId" noSelection="['null':'']"
                                                          value="${params.list('catalogId')}"
                                                          style="width:100%;"
                                                          class="chzn-select-deselect"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="product.tags.label"/></label>
                                    <p>
                                        <g:selectTags id="tagId"
                                                          name="tagId"
                                                          class="chzn-select-deselect"
                                                          noSelection="['null':'']"
                                                          style="width:100%;"
                                                          value="${params.list('tagId')}"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label for="includeInactive">
                                        <g:checkBox name="includeInactive" value="true" checked="${params.includeInactive}"/>
                                        <warehouse:message code="default.includeInactive.label" default="Include inactive"/>
                                    </label>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="default.limit.label" default="Limit"/></label>
                                    <p>
                                        <g:select id="max"
                                                      name="max"
                                                      class="chzn-select-deselect"
                                                      noSelection="['null':'']"
                                                      style="width:100%;"
                                                      optionKey="key"
                                                      optionValue="value"
                                                      from="[10:10, 25:25, 50:50, 100:100, 250:250, 500:500, 1000:1000]"
                                                      value="${params.max}"/>
                                    </p>
                                </div>

                                <div class="filter-list-item center middle">
                                    <button type="submit" class="button">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'find.png')}" />&nbsp;
                                        ${warehouse.message(code: 'default.button.search.label')}
                                    </button>

                                    <button type="submit" name="format" value="csv" class="button">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}"/>&nbsp;
                                        ${warehouse.message(code: 'default.button.downloadFiltered.label', default: 'Download results')}
                                    </button>
                                </div>

                            </g:form>
                        </div>
                    </div>


                    <div class="yui-u">

                        <div class="box">
                            <h2>
                                Showing ${productInstanceTotal > params.max ? params.max : productInstanceTotal} of ${productInstanceTotal} ${warehouse.message(code:'products.label')}
                            </h2>

                            <div class="dialog">

                                <table>
                                    <thead>
                                        <tr>
                                            <th>${warehouse.message(code:'product.active.label')}</th>
                                            <th>${warehouse.message(code:'product.productCode.label')}</th>
                                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" params="${params}"/>
                                            <g:sortableColumn property="category" title="${warehouse.message(code: 'category.label')}" params="${params}"/>
                                            <g:sortableColumn property="updatedBy" title="${warehouse.message(code: 'default.updatedBy.label')}" params="${params}"/>
                                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'default.lastUpdated.label')}" params="${params}"/>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:each in="${productInstanceList}" status="i" var="productInstance">
                                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                                <td align="center">
                                                    ${(productInstance.active) ? g.message(code:"default.active.label") : g.message(code:"default.inactive.label")}
                                                </td>
                                                <td align="center">
                                                    <g:link action="edit" id="${productInstance.id}">
                                                        ${productInstance.productCode}
                                                    </g:link>
                                                </td>
                                                <td align="center">
                                                    <g:link action="edit" id="${productInstance.id}">
                                                        <format:product product="${productInstance}"/>
                                                    </g:link>
                                                </td>
                                                <td align="center">
                                                    <format:category category="${productInstance?.category }"/>
                                                </td>
                                                <td align="center">
                                                    ${productInstance?.updatedBy }
                                                </td>
                                                <td align="center">
                                                    <div title="<g:formatDate date="${productInstance?.lastUpdated }"/>">
                                                        <g:prettyDateFormat date="${productInstance?.lastUpdated}"/>
                                                    </div>

                                                </td>
                                            </tr>
                                        </g:each>
                                    </tbody>
                                </table>
                            </div>
                            <div class="paginateButtons">
                                <g:paginate total="${productInstanceTotal}" params="${params }" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
