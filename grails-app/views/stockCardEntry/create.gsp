
<%@ page import="org.pih.warehouse.inventory.StockCardEntry" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'stockCardEntry.label', default: 'StockCardEntry')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${stockCardEntryInstance}">
            <div class="errors">
                <g:renderErrors bean="${stockCardEntryInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="quantityOutgoing"><g:message code="stockCardEntry.quantityOutgoing.label" default="Quantity Outgoing" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardEntryInstance, field: 'quantityOutgoing', 'errors')}">
                                    <g:textField name="quantityOutgoing" value="${fieldValue(bean: stockCardEntryInstance, field: 'quantityOutgoing')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="stockCard"><g:message code="stockCardEntry.stockCard.label" default="Stock Card" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardEntryInstance, field: 'stockCard', 'errors')}">
                                    <g:select name="stockCard.id" from="${org.pih.warehouse.StockCard.list()}" optionKey="id" value="${stockCardEntryInstance?.stockCard?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="remainingBalance"><g:message code="stockCardEntry.remainingBalance.label" default="Remaining Balance" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardEntryInstance, field: 'remainingBalance', 'errors')}">
                                    <g:textField name="remainingBalance" value="${fieldValue(bean: stockCardEntryInstance, field: 'remainingBalance')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="quantityIncoming"><g:message code="stockCardEntry.quantityIncoming.label" default="Quantity Incoming" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardEntryInstance, field: 'quantityIncoming', 'errors')}">
                                    <g:textField name="quantityIncoming" value="${fieldValue(bean: stockCardEntryInstance, field: 'quantityIncoming')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="startingBalance"><g:message code="stockCardEntry.startingBalance.label" default="Starting Balance" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardEntryInstance, field: 'startingBalance', 'errors')}">
                                    <g:textField name="startingBalance" value="${fieldValue(bean: stockCardEntryInstance, field: 'startingBalance')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="entryDate"><g:message code="stockCardEntry.entryDate.label" default="Entry Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardEntryInstance, field: 'entryDate', 'errors')}">
                                    <g:datePicker name="entryDate" precision="day" value="${stockCardEntryInstance?.entryDate}"  />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
