<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventoryLevel.label', default: 'Inventory Level')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /> &rsaquo; <span style="color: grey">
			${productInstance?.name }</span>        
        </title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${inventoryLevelInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
	            </div>
            </g:hasErrors>
			<div class="dialog">
				<g:form action="updateInventoryLevel">
					<g:hiddenField name="id" value="${inventoryLevelInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
					<g:hiddenField name="product.id" value="${productInstance?.id}"/>
					<fieldset>				
						<table>
							<tr class="prop">
								<td class="name"><label>Inventory</label></td>
								<td class="value">
									${inventoryInstance?.warehouse?.name }
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Product</label></td>
								<td class="value">
									${productInstance?.name }
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Supported</label></td>
								<td class="value">
									<g:checkBox name="supported" value="${inventoryLevelInstance?.supported }"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Minimum Quantity </label></td>
								<td class="value">
									<g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="3"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Reorder Quantity</label></td>
								<td class="value">
									<g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="3"/>
								</td>
							</tr>
							<tr class="prop">
								<td></td>
								<td>
								
									<button type="submit" class="positive"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
										alt="Save" /> ${message(code: 'default.button.save.label', default: 'Save')}
									</button>
									&nbsp;
									<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="negative">			
										${message(code: 'default.button.cancel.label', default: 'Cancel')}			
									</g:link>  
								
								</td>
							</tr>
						</table>			
					</fieldset>
				</g:form>			
			</div>
        </div>
    </body>
</html>
