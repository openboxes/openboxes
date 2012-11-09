<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
    <content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${requisition}">
        <div class="errors">
            <g:renderErrors bean="${requisition}" as="list" />
        </div>
    </g:hasErrors>

    <div id="requisition-header">
        <div class="title" id="description">${requisition.name ?: warehouse.message(code: 'requisition.label', default: 'Requisition')}</div>
        <g:if test="${requisition.lastUpdated}">
            <div class="time-stamp fade"><g:formatDate date="${requisition.lastUpdated }" format="dd/MMM/yyyy hh:mm a"/></div>
        </g:if>
        <div class="status fade">${requisition.status.toString()}</div>

        ${requisition?.requisitionItems?.size() }
    </div>

    <g:form name="requisitionForm" method="post" action="save">
        <g:hiddenField name="id" value="${requisition?.id}" />
        <g:hiddenField name="version" value="${requisition?.version}" />
        <input type="hidden" id="name" name="name" size="80" value="${requisition.name}"/>

        <div class="dialog">
            <table id="requisition">
                <tbody>

                <tr>
                    <td>
                        <table >
                            <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                <tr class="requisitionItem ${i%2?'even':'odd' }" style="border-top-width: 1px; border-top-style: solid; border-bottom-width: 1px; border-bottom-style: solid">
                                    <g:render template="listItem" model="[requisition: requisition, requisitionItem:requisitionItem, i:i]"/>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                    <td>
                        %{--key--}%
                    </td>
                </tr>

                <tr>

                    <td valign="top">
                    </td>
                    <td colspan="5">
                        <div class="buttons right">
                            <button type="submit">
                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
                                <g:link action="save" id="${requisition.id}">
                                    <warehouse:message code="default.button.save.label"/>
                                </g:link>
                            </button>
                            &nbsp;
                            <g:link action="list">
                                ${warehouse.message(code: 'default.button.cancel.label')}
                            </g:link>
                        </div>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>


    </g:form>
</div>


</body>
</html>
