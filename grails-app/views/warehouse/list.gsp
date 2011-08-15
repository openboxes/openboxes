<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="warehouse.warehouses.label" /></title>
    </head>
    <body>        
        <div class="body">
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
              
            <div class="dialog">
				<span class="menuButton">
           			<a href="/warehouse/warehouse/edit" class="new"><warehouse:message code="warehouse.addWarehouse.label"/></a>
	           	</span>
           	</div>
            <div class="list">
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />
                            <th><warehouse:message code="warehouse.manager.label" /></th>
                            <th class="center"><warehouse:message code="warehouse.active.label" /></th>
                            <th class="center"><warehouse:message code="warehouse.local.label" /></th>
                            <th class="center"><warehouse:message code="default.button.edit.label" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${warehouseInstanceList}" status="i" var="warehouseInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:link action="edit" id="${warehouseInstance.id}">${fieldValue(bean: warehouseInstance, field: "name")}</g:link>
							</td>
                            <td>${fieldValue(bean: warehouseInstance, field: "manager")}</td>                            
                            <td class="center tenth">${warehouseInstance.active ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label')}</td>                            
                            <td class="center tenth">${warehouseInstance.local ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label')}</td>                            
							<td class="center tenth"><g:link class="edit" action="edit" id="${warehouseInstance?.id}" >${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}</g:link></td>
						</tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${warehouseInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
