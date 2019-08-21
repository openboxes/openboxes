<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.level.label', default: 'Inventory Level')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /> &rsaquo; <span style="color: grey">
			<format:product product="${productInstance}"/></span>        
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
							<tbody>			
								<tr>
									<td style="width: 50px; vertical-align: middle;">
										<div class="fade" style="font-size: 0.9em;">
											<span class="action-menu">
												<button class="action-btn">
													<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
												</button>
												<div class="actions">
													<g:render template="showStockCardMenuItems" model="[product: productInstance, inventory: inventoryInstance]"/>																				
												</div>
											</span>				
										</div>			
									</td>
									<td style="vertical-align: middle;">
										<h1><format:product product="${productInstance}"/></h1>
									</td>
								</tr>
							</tbody>
						</table>					
						
						<table>
							<tr class="prop">
								<td class="name"><label><warehouse:message code="inventory.label"/></label></td>
								<td class="value">
									${inventoryInstance?.warehouse?.name }
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label><warehouse:message code="product.label"/></label></td>
								<td class="value">
									<format:product product="${productInstance}" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label><warehouse:message code="inventoryLevel.status.label"/></label></td>
								<td class="value">
						           	<g:select name="status" 
		           					   from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
		           					   optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance.status}" 
		           					   noSelection="['':warehouse.message(code:'inventoryLevel.chooseStatus.label')]" />&nbsp;&nbsp;	
								
								

								</td>
							</tr>
							<tr class="prop">
								<td></td>
								<td>
								
									<button type="submit" class="positive"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
										alt="Save" /> ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
									</button>
									&nbsp;
									<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="negative">			
										${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}			
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
