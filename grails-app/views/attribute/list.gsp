
<%@ page import="org.pih.warehouse.product.Attribute" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'attribute.label', default: 'Attribute')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div>            	
	            	<span class="menuButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[entityName]"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'attribute.id.label', default: 'Id')}" />
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'attribute.name.label', default: 'Name')}" />
                            <g:sortableColumn property="options" title="${warehouse.message(code: 'attribute.options.label', default: 'Options')}" />
                            <g:sortableColumn property="allowOther" title="${warehouse.message(code: 'attribute.allowOther.label', default: 'Allow Other')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${attributeInstanceList}" status="i" var="attributeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="edit" id="${attributeInstance.id}">${fieldValue(bean: attributeInstance, field: "id")}</g:link></td>
                            <td><format:metadata obj="${attributeInstance}"/></td>
                            <td>${attributeInstance.options.size()} <warehouse:message code="attribute.options.label" default="Options"/></td>
                            <td><g:formatBoolean boolean="${attributeInstance.allowOther}" /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${attributeInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
