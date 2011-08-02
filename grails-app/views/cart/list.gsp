
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'catalog.label', default: 'Cart')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>            
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

			<div class="actions-menu clearfix" style="float: left;">					
				<ul>
					<li>
						<g:link controller="inventory" action="browse" >
							<button>		
								<img src="${resource(dir: 'images/icons/silk', file: 'arrow_left.png')}"/>
								Back to <b>Inventory</b>
							</button>
						</g:link>
					</li>
				</ul>
			</div>	
			<br clear="all">


			<div class="dialog">
				<h2><img src="${resource(dir: 'images/icons/silk', file: 'cart.png')}"/>&nbsp;<label>Your Cart Items</label></h2>
				<div class="info">
					This is a partial implementation of the shopping cart feature.  Please check back again soon.
				</div>
	            <table style="display: inline">
	           		<tr>
	           			<th>Product</th>
	           			<th>Qty</th>
	           			<th></th>
	           		</tr>
	            	<g:each var="mapEntry" in="${productInstanceMap }" status="status">
						<tr class="${(status%2==0)?'odd':'even' }">
							<td>	
								${mapEntry?.key?.name}
							</td>
							<td>
								${productInstanceMap?.get(mapEntry.key) }
							</td>
							<td>
							
							</td>
		   				</tr>        
	   				</g:each>
	            </table>
            </div>
		</div>
    </body>
</html>
