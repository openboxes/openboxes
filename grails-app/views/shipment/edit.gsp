
<%@ page import="org.pih.warehouse.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${shipmentInstance}">
            <div class="errors">
                <g:renderErrors bean="${shipmentInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="trackingNumber"><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'trackingNumber', 'errors')}">
                                    <g:textField name="trackingNumber" value="${shipmentInstance?.trackingNumber}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="expectedShippingDate"><g:message code="shipment.expectedShippingDate.label" default="Expected Shipping Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}">
                                    <g:datePicker name="expectedShippingDate" precision="day" value="${shipmentInstance?.expectedShippingDate}" noSelection="['': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="actualShippingDate"><g:message code="shipment.actualShippingDate.label" default="Actual Shipping Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'actualShippingDate', 'errors')}">
                                    <g:datePicker name="actualShippingDate" precision="day" value="${shipmentInstance?.actualShippingDate}" noSelection="['': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="source"><g:message code="shipment.source.label" default="Source" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'source', 'errors')}">
                                    <g:select name="source.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.source?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="target"><g:message code="shipment.target.label" default="Target" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'target', 'errors')}">
                                    <g:select name="target.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.target?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="products"><g:message code="shipment.products.label" default="Products" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'products', 'errors')}">
                                    <g:select name="products" from="${org.pih.warehouse.Product.list()}" multiple="yes" optionKey="id" size="5" value="${shipmentInstance?.products}" />
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
