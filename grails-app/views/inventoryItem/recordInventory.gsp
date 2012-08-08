<%@ page import="org.pih.warehouse.product.Product"%>
<g:applyLayout name="stockCard">
	
	<%-- 
	<content tag="head">
		<style>
			.selected-row { background-color: #ffffe0; } 
		</style>	
		<g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'inventory')}" />
		<title><warehouse:message code="inventory.record.label" args="[entityName]" default="Record Inventory"/></title>
	</content>
	--%>
	
	<content tag="title">	
		<format:product product="${commandInstance?.productInstance}"/>
	</content>

	<content tag="content">
		<g:render template="recordInventory" />
	</content>
</g:applyLayout>

