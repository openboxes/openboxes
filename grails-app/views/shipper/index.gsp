
<%@ page import="org.pih.warehouse.shipping.Shipper" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipper.label', default: 'Shipper')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

                <div class="nav" role="navigation">
                    <ul>
                        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                        <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
                        <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
                    </ul>
                </div>

                <div class="">
                    <h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>

                                <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />

                                <g:sortableColumn property="description" title="${warehouse.message(code: 'default.description.label')}" />

                                <g:sortableColumn property="trackingUrl" title="${warehouse.message(code: 'shipper.trackingUrl.label')}" />

                                <g:sortableColumn property="trackingFormat" title="${warehouse.message(code: 'shipper.trackingFormat.label')}" />

                                <g:sortableColumn property="parameterName" title="${warehouse.message(code: 'shipper.parameterName.label')}" />

                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${shipperInstanceList}" status="i" var="shipperInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                <td><g:link action="show" id="${shipperInstance.id}">${fieldValue(bean: shipperInstance, field: "name")}</g:link></td>

                                <td>${fieldValue(bean: shipperInstance, field: "description")}</td>

                                <td>${fieldValue(bean: shipperInstance, field: "trackingUrl")}</td>

                                <td>${fieldValue(bean: shipperInstance, field: "trackingFormat")}</td>

                                <td>${fieldValue(bean: shipperInstance, field: "parameterName")}</td>

                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <div class="paginateButtons">
                        <g:paginate total="${shipperInstanceCount}" />
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
