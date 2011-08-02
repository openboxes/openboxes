
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>    
    </head>    

	<body>
       <div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="list">
				<table>
                    <thead>
                        <tr>   
							<g:sortableColumn property="name" title="${message(code: 'warehouse.name.label', default: 'Name')}" />
                            <g:sortableColumn property="manager" title="${message(code: 'warehouse.manager.label', default: 'Manager')}" />								
							<th>Actions</th>
                        </tr>
                    </thead>
       	           	<tbody>
			
						<g:each var="warehouseInstance" in="${warehouses}" status="i">           
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
								<td>${warehouseInstance?.name }</td>				
								<td>${warehouseInstance?.manager?.name }</td>
								<td>
									<g:if test="${warehouseInstance?.inventory}">								
										<g:link controller="inventory" action="show" id="${warehouseInstance?.inventory?.id}">view</g:link>
										&nbsp;
										<g:link controller="inventory" action="edit" id="${warehouseInstance?.inventory?.id}">edit</g:link>
									</g:if>
									<g:else>
										<g:link controller="inventory" action="create" params="['warehouse.id':warehouseInstance?.id]">create inventory</g:link>
									</g:else>
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
	</body>

</html>
