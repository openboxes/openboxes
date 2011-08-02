
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
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
            
            
            
            <g:form method="post" >
            	<fieldset>
	                <g:hiddenField name="id" value="${warehouseInstance?.id}" />
	                <g:hiddenField name="version" value="${warehouseInstance?.version}" />
	                            
				<script type="text/javascript">
					$(function() {
						$("#tabs").tabs();
					});
					</script>
					<style type="text/css">						
					</style>
					<div id="tabs">
						<ul>
							<g:each in="${productInstanceMap.keySet()}" var="productType">
								<li><a href="#tab-${productType?.id}">${productType?.name?:'Other'}</a></li>
					        </g:each>        
						</ul>
						
						<g:set var="rowStatus" value="${0 }"/>
						<g:each in="${productInstanceMap.keySet()}" var="productType">
							<div id="tab-${productType?.id}">
								<table>
									<thead>
										<tr class="${rowStatus++%2==0?'odd':'even'}">
											<th></th>
											<th>Item</th>
											<th></th>
										</tr>
									</thead>
									<tbody>
										<g:each var="productInstance" in="${productInstanceMap.get(productType) }">
											<tr class="${rowStatus++%2==0?'odd':'even'}">
												<td><g:checkBox name="product.id" value="${productInstance?.id }"/></td>
												<td>${productInstance?.name }</td>
												<td>
													
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>                
							</div>
						</g:each>						
					</div>
		            <div class="buttons">
	                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
	                </div> 
                </fieldset>
            </g:form>
        </div>
    </body>
</html>