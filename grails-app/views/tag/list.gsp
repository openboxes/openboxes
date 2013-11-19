
<%@ page import="org.pih.warehouse.core.Tag" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'tag.label', default: 'Tag')}" />
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
				<div class="buttonBar">
                    <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="['tag']"/></g:link>

            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'tag.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="tag" title="${warehouse.message(code: 'tag.tag.label', default: 'Tag')}" />

                            <th><warehouse:message code="tag.products.label" default="Products"/></th>

                            <th><warehouse:message code="tag.isActive.label" default="Is active?"/></th>

                            <th><warehouse:message code="tag.updatedBy.label" default="Updated By" /></th>
                   	    
                            <th><warehouse:message code="tag.createdBy.label" default="Created By" /></th>
                   	    
                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'tag.dateCreated.label', default: 'Date Created')}" />
                        
                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'tag.lastUpdated.label', default: 'Last Updated')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${tagInstanceList}" status="i" var="tagInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${tagInstance.id}">${fieldValue(bean: tagInstance, field: "id")}</g:link></td>
                        
                            <td><g:link action="edit" id="${tagInstance.id}">${fieldValue(bean: tagInstance, field: "tag")}</g:link></td>

                            <td>${tagInstance?.products?.size()} </td>

                            <td>${fieldValue(bean: tagInstance, field: "isActive")}</td>

                            <td>${fieldValue(bean: tagInstance, field: "updatedBy")}</td>
                        
                            <td>${fieldValue(bean: tagInstance, field: "createdBy")}</td>
                        
                            <td><format:date obj="${tagInstance.dateCreated}" /></td>
                        
                            <td><format:date obj="${tagInstance.lastUpdated}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${tagInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
