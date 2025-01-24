
<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="default.add.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <div class="list">

                <div class="buttonBar">
                    <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></g:link>
                    <g:isUserAdmin>
                        <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'attribute.label').toLowerCase()]"/></g:link>
                    </g:isUserAdmin>
                </div>


                <div class="yui-gf">
                    <div class="yui-u first">
                        <div class="box">
                            <h2><warehouse:message code="default.search.label" default="Search"/></h2>
                            <g:form action="list" method="get">
                                <g:hiddenField name="sort" value="${params.sort?:'name'}"/>
                                <g:hiddenField name="order" value="${params.order?:'asc'}"/>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="default.name.label"/></label>
                                    <g:textField name="q" class="text large" value="${params.q}"></g:textField>
                                </div>
                                <div class="filter-list-item center middle">
                                    <button type="submit" class="button block">
                                        ${warehouse.message(code: 'default.button.search.label')}
                                    </button>
                                </div>


                            </g:form>


                        </div>
                    </div>
                    <div class="yui-u">

                        <div class="box">
                            <h2><warehouse:message code="attributes.label" default="Attributes"/></h2>
                            <table>
                                <thead>
                                <tr>
                                    <g:sortableColumn property="code" title="${warehouse.message(code: 'default.code.label')}" />
                                    <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />
                                    <g:sortableColumn property="entityTypeCode" title="${warehouse.message(code: 'attribute.entityTypeCode.label', default: 'Entity Type')}" />
                                    <g:sortableColumn property="options" title="${warehouse.message(code: 'attribute.options.label')}" />
                                    <g:sortableColumn property="active" title="${warehouse.message(code: 'default.active.label')}" />
                                    <g:sortableColumn property="required" title="${warehouse.message(code: 'default.required.label')}" />
                                    <g:sortableColumn property="allowOther" title="${warehouse.message(code: 'attribute.allowOther.label')}" />
                                </tr>
                                </thead>
                                <tbody>
                                <g:each in="${attributeInstanceList}" status="i" var="attributeInstance">
                                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                        <td><g:link action="edit" id="${attributeInstance.id}">${fieldValue(bean: attributeInstance, field: "code")}</g:link></td>
                                        <td><g:link action="edit" id="${attributeInstance.id}"><format:metadata obj="${attributeInstance}"/></g:link></td>
                                        <td><format:metadata obj="${attributeInstance?.entityTypeCode}"/></td>
                                        <td>${attributeInstance.options.size()} <warehouse:message code="attribute.options.label" default="Options"/></td>
                                        <td><g:formatBoolean boolean="${attributeInstance.active}" /></td>
                                        <td><g:formatBoolean boolean="${attributeInstance.required}" /></td>
                                        <td><g:formatBoolean boolean="${attributeInstance.allowOther}" /></td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                            <div class="paginateButtons">
                                <g:paginate total="${attributeInstanceTotal}" />
                            </div>

                        </div>

                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
