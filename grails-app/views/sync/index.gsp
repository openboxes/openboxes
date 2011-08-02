
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'sync.label', default: 'Sync')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
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
			<div class="dialog">

				<table>
					<tr>
						<td>
      		
			      			<h2>Remote Products</h2>
			      			<table>
			      				<tr>
				      				<th>ID</th>
			      					<th>Product</th>
			      					<th>Created</th>
			      					<th>Modified</th>      	
			      				</tr>		
				      			<g:each in="${remoteProducts}" var="product" status="i">
				      				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					      				<td>${product.id}</td>
					      				<td>${product.name}</td>
					      				<td>${product.dateCreated}</td>
					      				<td>${product.lastUpdated}</td>
				      				</tr>
				      			</g:each>
			      			</table>
	      			      		
						</td>
						
						<td><h2>Local Products</h2>
			      			<table>
			      				<tr>
				      				<th>ID</th>
			      					<th>Product</th>
			      					<th>Created</th>
			      					<th>Modified</th>      	
			      				</tr>		
				      			<g:each in="${localProducts}" var="product" status="i">
				      				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					      				<td>${product.id}</td>
					      				<td>${product.name}</td>
					      				<td>${product.dateCreated}</td>
					      				<td>${product.lastUpdated}</td>
				      				</tr>
				      			</g:each>
			      			</table>
	      			      		
						
						
						
						
						</td>      			      		
      			</table>	
      				 
      			
      			
      			
      		</div>
            
        </div>
    </body>
</html>
