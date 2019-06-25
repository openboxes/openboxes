<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitions.label', default: 'Stock Requisitions').toLowerCase()}" />
        <title>
	        <warehouse:message code="requisition.label"/>
		</title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>

            <div class="buttonBar">
                <g:link class="button" controller="requisitionTemplate" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[warehouse.message(code:'requisitionTemplates.label').toLowerCase()]"/>
                </g:link>
                <g:isUserAdmin>
                    <g:link class="button" controller="requisitionTemplate" action="create" params="[type:'STOCK']">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[g.message(code:'requisitionTemplate.label')]"/>
                    </g:link>
                </g:isUserAdmin>
                <g:link controller="requisitionTemplate" action="exportPage" class="button">
                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}"/>
                    ${warehouse.message(code: 'stockList.export.label')}
                </g:link>
            </div>
            <div>
                <div>
                    <div class="box">
                        <h2><g:message code="report.parameters.label"/></h2>
                        <g:form controller="requisitionTemplate" action="exportAsCsv" method="GET">
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.origin.label" default="Origin" /></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:selectLocation name="origin" multiple="true" class="chzn-select-deselect" noSelection="['null':'']" data-placeholder=" "/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.destination.label" default="Destination" /></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:selectLocation name="destination" multiple="true" class="chzn-select-deselect" noSelection="['null':'']" data-placeholder=" "/>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                            <div class="prop">
                                <div class="center">
                                    <g:submitButton name="Download"/>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
