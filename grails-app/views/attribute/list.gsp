
<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'attribute.label', default: 'Attribute')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div>

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

                            <g:form action="list" method="get">
                                <g:hiddenField name="sort" value="${params.sort?:'name'}"/>
                                <g:hiddenField name="order" value="${params.order?:'asc'}"/>

                                <table>
                                    <tr>
                                        <td>
                                            <label><warehouse:message code="default.name.label"/></label>
                                        </td>
                                        <td>
                                            <g:textField name="q" value="${params.q}" class="text"></g:textField>
                                        </td>
                                    </tr>
                                </table>
                                <hr/>
                                <div class="buttons center">
                                    <button type="submit" class="button icon search">
                                        ${warehouse.message(code: 'default.button.find.label')}
                                    </button>
                                </div>


                            </g:form>


                        </div>
                    </div>
                    <div class="yui-u">

                        <div class="dialog box">
                            <h2><warehouse:message code="attributes.label" default="Attributes"/></h2>
                            <table>
                                <thead>
                                <tr>
                                    <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />
                                    <g:sortableColumn property="options" title="${warehouse.message(code: 'attribute.options.label')}" />
                                    <g:sortableColumn property="allowOther" title="${warehouse.message(code: 'attribute.allowOther.label')}" />
                                </tr>
                                </thead>
                                <tbody>
                                <g:each in="${attributeInstanceList}" status="i" var="attributeInstance">
                                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                        <td><g:link action="edit" id="${attributeInstance.id}"><format:metadata obj="${attributeInstance}"/></g:link></td>
                                        <td>${attributeInstance.options.size()} <warehouse:message code="attribute.options.label" default="Options"/></td>
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
