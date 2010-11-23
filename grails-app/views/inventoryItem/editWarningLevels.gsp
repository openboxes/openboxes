
<%@ page import="org.pih.warehouse.inventory.StockCardItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventoryItem.label', default: 'Inventory Item')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${itemInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${itemInstance}" as="list" />
	            </div>
            </g:hasErrors>
			<div class="dialog">
			
				<h1>${itemInstance?.product?.name }</h1>
			
				<g:form action="update">
					<g:hiddenField name="id" value="${itemInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${itemInstance?.inventory?.id}"/>
					<fieldset>				
						<legend>Threshholds</legend>
						<table>
							<tr class="prop">
								<td class="name"><label>Minimum Quantity </label></td>
								<td class="value">
									<g:textField name="minQuantity" value="${itemInstance?.minQuantity }" size="3"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Reorder Quantity</label></td>
								<td class="value">
									<g:textField name="reorderQuantity" value="${itemInstance?.reorderQuantity }" size="3"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Maximum Quantity</label></td>
								<td class="value">
									<g:textField name="maxQuantity" value="${itemInstance?.maxQuantity }" size="3"/>
								</td>
							</tr>
							<tr class="prop">
								<td colspan="2">
									<div class="buttons" style="text-align: right;">
					                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				                    </div>
								</td>
							</tr>
						</table>			
					</fieldset>
				</g:form>			
			</div>
        </div>
    </body>
</html>
