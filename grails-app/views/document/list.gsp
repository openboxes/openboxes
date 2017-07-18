
<%@ page import="org.pih.warehouse.core.Document" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'document.label', default: 'Document')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['documents']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['document']"/></g:link>
            	</div>


                <div class="yui-gf">
                    <div class="yui-u first">
                        <div class="box">
                            <h2><g:message code="default.filters.label"/></h2>
                            <g:form action="list" method="get">
                                <g:hiddenField name="sort" value="${params.sort}"/>
                                <g:hiddenField name="order" value="${params.order}"/>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="default.name.label"/></label>
                                    <g:textField name="q" value="${params.q }" class="text" style="width:100%;"/>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="documentType.label"/></label>
                                    <g:select class="chzn-select-deselect" name="documentType.id" from="${org.pih.warehouse.core.DocumentType.list()}"
                                              optionKey="id" value="${params?.documentType?.id}" noSelection="['null': '']" />
                                </div>
                                <hr/>
                                <div class="filter-list-item">
                                    <button type="submit" class="button icon search">
                                        ${warehouse.message(code: 'default.button.find.label')}
                                    </button>
                                </div>

                            </g:form>
                        </div>

                    </div>
                    <div class="yui-u">

                        <div class="box">
                            <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                            <table>
                                <thead>
                                <tr>

                                    <g:sortableColumn property="id" title="${warehouse.message(code: 'document.id.label', default: 'Id')}" />

                                    <g:sortableColumn property="name" title="${warehouse.message(code: 'document.name.label', default: 'Name')}" />

                                    <g:sortableColumn property="name" title="${warehouse.message(code: 'document.documentType.label', default: 'Document Type')}" />

                                    <g:sortableColumn property="filename" title="${warehouse.message(code: 'document.filename.label', default: 'Filename')}" />

                                    <g:sortableColumn property="extension" title="${warehouse.message(code: 'document.extension.label', default: 'Extension')}" />

                                    <g:sortableColumn property="contentType" title="${warehouse.message(code: 'document.contentType.label', default: 'Content Type')}" />

                                </tr>
                                </thead>
                                <tbody>
                                <g:each in="${documentInstanceList}" status="i" var="documentInstance">
                                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                        <td><g:link action="edit" id="${documentInstance.id}">${fieldValue(bean: documentInstance, field: "id")}</g:link></td>

                                        <td><g:link action="edit" id="${documentInstance.id}">${fieldValue(bean: documentInstance, field: "name")}</g:link></td>

                                        <td>${fieldValue(bean: documentInstance?.documentType, field: "name")}</td>

                                        <td><g:link action="edit" id="${documentInstance.id}">${fieldValue(bean: documentInstance, field: "filename")}</g:link></td>

                                        <td>${fieldValue(bean: documentInstance, field: "extension")}</td>

                                        <td>${fieldValue(bean: documentInstance, field: "contentType")}</td>

                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                            <div class="paginateButtons">
                                <g:paginate total="${documentInstanceTotal}" />
                            </div>
                        </div>
                    </div>
                </div>




            </div>

        </div>
    </body>
</html>
