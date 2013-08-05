
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
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
            <g:form method="post" >
            	<fieldset>
	                <g:hiddenField name="id" value="${requisitionItemInstance?.id}" />
	                <g:hiddenField name="version" value="${requisitionItemInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                        <tbody>


                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label for="requisition"><warehouse:message code="requisitionItem.requisition.label" default="Requisition" /></label>
                                    </td>
                                    <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'requisition', 'errors')}">
                                        <g:hiddenField name="requisition.id" value="${requisitionItemInstance?.requisition?.id}"  />
                                        ${requisitionItemInstance?.requisition?.name}
                                        ${requisitionItemInstance?.requisition?.id}
                                    </td>
                                </tr>

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="product"><warehouse:message code="requisitionItem.product.label" default="Product" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'product', 'errors')}">
                                        <g:hiddenField name="product.id" value="${requisitionItemInstance?.product?.id}"  />
                                        ${requisitionItemInstance?.product}
                                        ${requisitionItemInstance?.product?.id}
	                                </td>
	                            </tr>
	                        

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="requestedBy"><warehouse:message code="requisitionItem.requestedBy.label" default="Requested By" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: requisitionItemInstance, field: 'requestedBy', 'errors')}">
                                        <g:hiddenField name="requestedBy.id" value="${requisitionItemInstance?.requestedBy?.id}"  />
                                        ${requisitionItemInstance?.requestedBy?.id}
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
		                        	<td valign="top"></td>
		                        	<td valign="top">                        	
						                <div class="buttons">
						                    <g:actionSubmit class="button icon approve" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
						                    <g:actionSubmit class="button icon delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
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
