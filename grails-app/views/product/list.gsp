
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom"/>
        <g:set var="entityName" value="${warehouse.message(code: 'product.label')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>    
    <body>
        <div class="body">

            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                </ul>
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
                                    <label for="includeCategoryChildren">${warehouse.message(code:'search.includeCategoryChildren.label', default: 'Include all subcategories')}</label>
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


                            <div class="buttons">
                                <button type="submit" class="button icon search">${warehouse.message(code: 'default.button.find.label')}</button>
                                <g:link action="list" class="button icon reload">${message(code: 'default.button.reset.label')}</g:link>
                            </div>

                        </g:form>
                    </div>
                </div>


                <div class="yui-u">


                    <div class="dialog box">
                        <h2>
                            <div class="right">
                                <g:link controller="product" action="exportProducts" class="button" style="margin: 5px;"
                                        params="['product.id': flash.productIds]">${warehouse.message(code:'default.downloadAsCsv.label', default: "Download as CSV")}</g:link>
                            </div>
                            <g:message code="default.list.label" args="[entityName]"/>
                            <small><g:message code="default.showing.message" args="[params.max]"/></small>
                        </h2>

                        <g:if test="${flash.message}">
                            <div class="message">${flash.message}</div>
                        </g:if>

                        <table>
                            <thead>
                                <tr>
                                    <th>${warehouse.message(code:'product.productCode.label')}</th>
                                    <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" params="${params}"/>
                                    <g:sortableColumn property="category" title="${warehouse.message(code: 'category.label')}" params="${params}"/>
                                    <g:sortableColumn property="manufacturer" title="${warehouse.message(code: 'product.manufacturer.label')}" params="${params}"/>
                                    <g:sortableColumn property="manufacturerCode" title="${warehouse.message(code: 'product.manufacturerCode.label')}" params="${params}" />
                                    <g:sortableColumn property="vendor" title="${warehouse.message(code: 'product.vendor.label')}" params="${params}"/>
                                    <g:sortableColumn property="vendorCode" title="${warehouse.message(code: 'product.vendorCode.label')}" params="${params}"/>
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
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                        <div class="paginateButtons">
                            <g:paginate total="${productInstanceTotal}" params="${params }" />
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </body>
</html>
