
<%@ page import="org.pih.warehouse.shipping.ShipmentItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${shipmentItemInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${shipmentItemInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="container"><warehouse:message code="shipmentItem.container.label" default="Container" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'container', 'errors')}">
	                                    <g:select name="container.id" from="${org.pih.warehouse.shipping.Container.list()}" optionKey="id" value="${shipmentItemInstance?.container?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="product"><warehouse:message code="shipmentItem.product.label" default="Product" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'product', 'errors')}">
	                                    <g:select name="product.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${shipmentItemInstance?.product?.id}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="lotNumber"><warehouse:message code="shipmentItem.lotNumber.label" default="Lot Number" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'lotNumber', 'errors')}">
	                                    <g:textArea name="lotNumber" cols="40" rows="5" value="${shipmentItemInstance?.lotNumber}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="expirationDate"><warehouse:message code="shipmentItem.expirationDate.label" default="Expiration Date" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'expirationDate', 'errors')}">
	                                    <g:datePicker name="expirationDate" precision="minute" value="${shipmentItemInstance?.expirationDate}" noSelection="['': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="quantity"><warehouse:message code="shipmentItem.quantity.label" default="Quantity" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'quantity', 'errors')}">
	                                    <g:textField name="quantity" value="${shipmentItemInstance?.quantity }" size="10" class="text"/>	                                    
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="recipient"><warehouse:message code="shipmentItem.recipient.label" default="Recipient" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'recipient', 'errors')}">
	                                    <g:select name="recipient.id" from="${org.pih.warehouse.core.Person.list()}" optionKey="id" value="${shipmentItemInstance?.recipient?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="inventoryItem"><warehouse:message code="shipmentItem.inventoryItem.label" default="Inventory Item" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'inventoryItem', 'errors')}">
	                                    <g:select name="inventoryItem.id" from="${org.pih.warehouse.inventory.InventoryItem.list()}" optionKey="id" value="${shipmentItemInstance?.inventoryItem?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="donor"><warehouse:message code="shipmentItem.donor.label" default="Donor" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'donor', 'errors')}">
	                                    <g:select name="donor.id" from="${org.pih.warehouse.donation.Donor.list()}" optionKey="id" value="${shipmentItemInstance?.donor?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="dateCreated"><warehouse:message code="shipmentItem.dateCreated.label" default="Date Created" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'dateCreated', 'errors')}">
	                                    <g:datePicker name="dateCreated" precision="minute" value="${shipmentItemInstance?.dateCreated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="lastUpdated"><warehouse:message code="shipmentItem.lastUpdated.label" default="Last Updated" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'lastUpdated', 'errors')}">
	                                    <g:datePicker name="lastUpdated" precision="minute" value="${shipmentItemInstance?.lastUpdated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="shipment"><warehouse:message code="shipmentItem.shipment.label" default="Shipment" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: shipmentItemInstance, field: 'shipment', 'errors')}">
	                                    <g:select name="shipment.id" from="${org.pih.warehouse.shipping.Shipment.list()}" optionKey="id" value="${shipmentItemInstance?.shipment?.id}"  />
	                                </td>
	                            </tr>
	                        
	                        
		                        <tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">
						                <div class="buttons">
						                   <g:submitButton name="create" class="save" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
						                   
						                   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
						                   
						                </div>                        	
		                        	</td>
		                        </tr>
		                        
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
