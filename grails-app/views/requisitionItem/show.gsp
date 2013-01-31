
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.category.label" default="Category" /></td>
                            
                            <td valign="top" class="value"><g:link controller="category" action="show" id="${requisitionItemInstance?.category?.id}">${requisitionItemInstance?.category?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.product.label" default="Product" /></td>
                            
                            <td valign="top" class="value"><g:link controller="product" action="show" id="${requisitionItemInstance?.product?.id}">${requisitionItemInstance?.product?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.productGroup.label" default="Product Group" /></td>
                            
                            <td valign="top" class="value"><g:link controller="productGroup" action="show" id="${requisitionItemInstance?.productGroup?.id}">${requisitionItemInstance?.productGroup?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.inventoryItem.label" default="Inventory Item" /></td>
                            
                            <td valign="top" class="value"><g:link controller="inventoryItem" action="show" id="${requisitionItemInstance?.inventoryItem?.id}">${requisitionItemInstance?.inventoryItem?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.requestedBy.label" default="Requested By" /></td>
                            
                            <td valign="top" class="value"><g:link controller="person" action="show" id="${requisitionItemInstance?.requestedBy?.id}">${requisitionItemInstance?.requestedBy?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.quantity.label" default="Quantity" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "quantity")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.unitPrice.label" default="Unit Price" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "unitPrice")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.substitutable.label" default="Substitutable" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${requisitionItemInstance?.substitutable}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.comment.label" default="Comment" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "comment")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.recipient.label" default="Recipient" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "recipient")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.orderIndex.label" default="Order Index" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: requisitionItemInstance, field: "orderIndex")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${requisitionItemInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${requisitionItemInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="requisitionItem.requisition.label" default="Requisition" /></td>
                            
                            <td valign="top" class="value"><g:link controller="requisition" action="show" id="${requisitionItemInstance?.requisition?.id}">${requisitionItemInstance?.requisition?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons">
					                <g:form>
					                    <g:hiddenField name="id" value="${requisitionItemInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
