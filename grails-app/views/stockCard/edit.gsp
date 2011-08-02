
<%@ page import="org.pih.warehouse.inventory.StockCard" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'stockCard.label', default: 'StockCard')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><warehouse:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><warehouse:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><warehouse:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${stockCardInstance}">
            <div class="errors">
                <g:renderErrors bean="${stockCardInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${stockCardInstance?.id}" />
                <g:hiddenField name="version" value="${stockCardInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="product"><warehouse:message code="stockCard.product.label" default="Product" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardInstance, field: 'product', 'errors')}">
                                    <g:select name="product.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${stockCardInstance?.product?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="entries"><warehouse:message code="stockCard.entries.label" default="Entries" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: stockCardInstance, field: 'entries', 'errors')}">
                                    
<ul>
<g:each in="${stockCardInstance?.entries?}" var="e">
    <li><g:link controller="stockCardEntry" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="stockCardEntry" action="create" params="['stockCard.id': stockCardInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'stockCardEntry.label', default: 'StockCardEntry')])}</g:link>

                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
