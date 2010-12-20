
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'catalog.label', default: 'Catalog')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>            
    </head>    

    <body>
        <div class="body">
        
			<div class="nav">
				<g:render template="nav"/>
			</div>
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${inventoryInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryInstance}" as="list" />
	            </div>
            </g:hasErrors>			

			<div class="dialog">
            <table style="border: 1px solid black">
           		<tr>
           			<th>Product</th>
           			<th>Type</th>
           			<th>Qty</th>
           		</tr>
            	<g:each var="mapEntry" in="${productInstanceMap }" status="status">
					<tr class="${(status%2==0)?'odd':'even' }">
						<td>	
							${mapEntry?.key?.name}
						</td>
						<td>
							${mapEntry?.key?.productType?.name }
						</td>
						<td>
							${productInstanceMap?.get(mapEntry.key) }
						</td>
	   				</tr>        
   				</g:each>
            </table>
            </div>
		</div>
    </body>
</html>
