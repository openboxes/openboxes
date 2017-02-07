
<%@ page import="org.pih.warehouse.product.ProductGroup" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productGroup.label', default: 'ProductGroup')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            
            <div class="summary">
                <h1><warehouse:message code="productGroups.label"/></h1>

            </div>

            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                </ul>
            </div>


            <div class="yui-gf">
                <div class="yui-u first">
                    <div class="dialog box">
                        <h2><warehouse:message code="default.search.label" default="Search"/></h2>

                        <g:form controller="productGroup" action="list">

                            <div class="filter-list-item">
                                <g:textField name="q" value="${params.q}" class="medium text" size="60"/>

                            </div>
                            <div class="buttons">
                                <g:submitButton name="submit" value="Search" class="button icon search"/>
                                <g:link action="list" class="button icon reload">${warehouse.message(code: 'default.button.reset.label')}</g:link>
                            </div>
                        </g:form>

                    </div>
                </div>
                <div class="yui-u">

                    <div class="list box">
                        <h2><warehouse:message code="productGroups.label"/> (${productGroupInstanceTotal})</h2>

                        <table>
                            <thead>
                                <tr>

                                    <g:sortableColumn property="description" title="${warehouse.message(code: 'productGroup.name.label', default: 'Name')}" />

                                    <g:sortableColumn property="category" title="${warehouse.message(code: 'productGroup.category.label', default: 'Category')}" />

                                    <g:sortableColumn property="products" title="${warehouse.message(code: 'productGroup.products.label', default: 'Products')}" />

                                    <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'productGroup.dateCreated.label', default: 'Date Created')}" />

                                </tr>
                            </thead>
                            <tbody>
                            <g:each in="${productGroupInstanceList}" status="i" var="productGroupInstance">
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <td>
                                        <g:link action="edit" id="${productGroupInstance.id}">
                                            ${fieldValue(bean: productGroupInstance, field: "description")?:productGroupInstance?.id}
                                        </g:link>
                                    </td>
                                    <td>${productGroupInstance.category}</td>
                                    <td>${productGroupInstance.products.size()}</td>

                                    <td><format:date obj="${productGroupInstance.dateCreated}" /></td>

                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                        <div class="paginateButtons">
                            <g:paginate total="${productGroupInstanceTotal}" params="${params}" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
    </body>
</html>
