
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${requisitionItemInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${requisitionItemInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><warehouse:message code="requisitionItem.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${requisitionItemInstance?.description}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="category"><warehouse:message code="requisitionItem.category.label" default="Category" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'category', 'errors')}">
	                                    <g:select name="category.id" from="${org.pih.warehouse.product.Category.list()}" optionKey="id" value="${requisitionItemInstance?.category?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="product"><warehouse:message code="requisitionItem.product.label" default="Product" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'product', 'errors')}">
	                                    <g:select name="product.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${requisitionItemInstance?.product?.id}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="productGroup"><warehouse:message code="requisitionItem.productGroup.label" default="Product Group" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'productGroup', 'errors')}">
	                                    <g:select name="productGroup.id" from="${org.pih.warehouse.product.ProductGroup.list()}" optionKey="id" value="${requisitionItemInstance?.productGroup?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="inventoryItem"><warehouse:message code="requisitionItem.inventoryItem.label" default="Inventory Item" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'inventoryItem', 'errors')}">
	                                    <g:select name="inventoryItem.id" from="${org.pih.warehouse.inventory.InventoryItem.list()}" optionKey="id" value="${requisitionItemInstance?.inventoryItem?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="requestedBy"><warehouse:message code="requisitionItem.requestedBy.label" default="Requested By" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'requestedBy', 'errors')}">
	                                    <g:select name="requestedBy.id" from="${org.pih.warehouse.core.Person.list()}" optionKey="id" value="${requisitionItemInstance?.requestedBy?.id}" noSelection="['null': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="quantity"><warehouse:message code="requisitionItem.quantity.label" default="Quantity" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'quantity', 'errors')}">
	                                    <g:textField name="quantity" value="${fieldValue(bean: requisitionItemInstance, field: 'quantity')}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="unitPrice"><warehouse:message code="requisitionItem.unitPrice.label" default="Unit Price" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'unitPrice', 'errors')}">
	                                    <g:textField name="unitPrice" value="${fieldValue(bean: requisitionItemInstance, field: 'unitPrice')}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="substitutable"><warehouse:message code="requisitionItem.substitutable.label" default="Substitutable" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'substitutable', 'errors')}">
	                                    <g:checkBox name="substitutable" value="${requisitionItemInstance?.substitutable}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="comment"><warehouse:message code="requisitionItem.comment.label" default="Comment" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'comment', 'errors')}">
	                                    <g:textField name="comment" value="${requisitionItemInstance?.comment}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="recipient"><warehouse:message code="requisitionItem.recipient.label" default="Recipient" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'recipient', 'errors')}">
	                                    <g:textField name="recipient" value="${requisitionItemInstance?.recipient}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="orderIndex"><warehouse:message code="requisitionItem.orderIndex.label" default="Order Index" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'orderIndex', 'errors')}">
	                                    <g:textField name="orderIndex" value="${fieldValue(bean: requisitionItemInstance, field: 'orderIndex')}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="dateCreated"><warehouse:message code="requisitionItem.dateCreated.label" default="Date Created" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'dateCreated', 'errors')}">
	                                    <g:datePicker name="dateCreated" precision="minute" value="${requisitionItemInstance?.dateCreated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="lastUpdated"><warehouse:message code="requisitionItem.lastUpdated.label" default="Last Updated" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'lastUpdated', 'errors')}">
	                                    <g:datePicker name="lastUpdated" precision="minute" value="${requisitionItemInstance?.lastUpdated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="requisition"><warehouse:message code="requisitionItem.requisition.label" default="Requisition" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'requisition', 'errors')}">
	                                    <g:select name="requisition.id" from="${org.pih.warehouse.requisition.Requisition.list()}" optionKey="id" value="${requisitionItemInstance?.requisition?.id}"  />
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
