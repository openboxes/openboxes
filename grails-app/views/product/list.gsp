
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
            
				<div class="buttonBar">
                    <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'product.label').toLowerCase()]"/></g:link>
	            	<g:isUserAdmin>
                        <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'product.label').toLowerCase()]"/></g:link>
	                </g:isUserAdmin>
            	</div>



                <div class="yui-gf">
                    <div class="yui-u first">
                        <div class="box">
                            <h2>${warehouse.message(code:'default.filters.label')}</h2>
                            <g:form action="list" method="get">
                                <g:hiddenField name="sort" value="${params.sort}"/>
                                <g:hiddenField name="order" value="${params.order}"/>
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
                                    <button type="submit" class="button icon search">
                                        ${warehouse.message(code: 'default.button.find.label')}
                                    </button>



                                </div>

                            </g:form>
                        </div>
                    </div>


                    <div class="yui-u">

                        <div class="box">
                            <h2>
                                Showing ${productInstanceTotal > params.max ? params.max : productInstanceTotal} of ${productInstanceTotal} ${warehouse.message(code:'products.label')}
                                <g:link controller="product" action="exportProducts" params="['product.id': flash.productIds]" class="button icon arrowdown">${warehouse.message(code:'default.downloadAsCsv.label', default: "Download as CSV")}</g:link>

                            </h2>


                            <table>
                                <thead>
                                    <tr>
                                        <%--
                                        <th></th>
                                        --%>
                                        <th>${warehouse.message(code:'product.productCode.label')}</th>
                                        <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" params="${params}"/>
                                        <g:sortableColumn property="category" title="${warehouse.message(code: 'category.label')}" params="${params}"/>
                                        <g:sortableColumn property="manufacturer" title="${warehouse.message(code: 'product.manufacturer.label')}" params="${params}"/>
                                        <g:sortableColumn property="manufacturerCode" title="${warehouse.message(code: 'product.manufacturerCode.label')}" params="${params}" />
                                        <g:sortableColumn property="vendor" title="${warehouse.message(code: 'product.vendor.label')}" params="${params}"/>
                                        <g:sortableColumn property="vendorCode" title="${warehouse.message(code: 'product.vendorCode.label')}" params="${params}"/>
                                        <g:sortableColumn property="createdBy" title="${warehouse.message(code: 'default.createdBy.label')}" params="${params}"/>
                                        <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'default.dateCreated.label')}" params="${params}"/>
                                        <g:sortableColumn property="updatedBy" title="${warehouse.message(code: 'default.updatedBy.label')}" params="${params}"/>
                                        <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'default.lastUpdated.label')}" params="${params}"/>
                                    </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${productInstanceList}" status="i" var="productInstance">
                                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                            <%--
                                            <td align="center">
                                                <span title="${productInstance?.description }">
                                                    <img src="${resource(dir:'images/icons/silk',file:'information.png')}" class="middle" alt="Information" />
                                                </span>
                                            </td>
                                            --%>
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
                                                ${productInstance?.manufacturer }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.manufacturerCode }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.vendor }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.vendorCode }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.createdBy }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.dateCreated }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.updatedBy }
                                            </td>
                                            <td align="center">
                                                ${productInstance?.lastUpdated }
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
    </body>
</html>
