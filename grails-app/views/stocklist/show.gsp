<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'warehouse.label', default: 'Stock list')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>		
			
	<div class="yui-gf">
		<div class="yui-u first">
			<table>
				<tbody>
					<tr class="prop">
						<td class="name">
							${warehouse.message(code: 'location.name.label') }
						</td>
						<td class="value">
							${location?.name }
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<div class="yui-u">
			<g:autoSuggest id="product-search" name="product" jsonUrl="${request.contextPath }/json/findProductByName" />
			
			${inventoryLevels?.size() } items
			<table>
				<thead>
					<tr>
						<td>
						
						</td>
					</tr>
				</thead>
				<tbody>
					<g:each in="${inventoryLevels }" var="inventoryLevel">
						<tr>
							<td>									
								${inventoryLevel?.product }
							</td>
							<td>									
								${inventoryLevel?.minimumQuantity }
							</td>
							<td>									
								${inventoryLevel?.maximumQuantity }
							</td>
							<td>									
								${inventoryLevel?.reorderQuantity }
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
</div>                        
<script type="text/javascript">

$(document).ready(function() {


	$("#product-search").autocomplete({
		select: function(event, ui) {
			console.log(ui.item);
		}
	});

});


</script>        


    </body>
</html>
