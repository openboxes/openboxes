
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
				
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${warehouseInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${warehouseInstance}" as="list" />
	            </div>
            </g:hasErrors>
                        
            <g:if test="${!warehouseInstance?.inventory }">            
            	<div class="notice">
					Click the button below to create a new inventory for ${warehouseInstance?.name }.
            	</div>
            
            	<g:form>            
	                <div class="buttons">
	                	<g:hiddenField name="warehouse.id" value="${warehouseInstance?.id }"/>
	                    <g:actionSubmit class="save" action="save" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" /></span>
	                </div>                            	
            	</g:form>
            </g:if>
            <g:else>
            	Warehouse inventory exists ... click here to view ${warehouseInstance?.inventory?.id }
            
            </g:else>
            
            
		</div>	
    </body>
</html>
