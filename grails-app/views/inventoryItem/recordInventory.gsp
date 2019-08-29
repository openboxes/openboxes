<%@ page import="org.pih.warehouse.product.Product"%>
<g:applyLayout name="stockCard">	
	<content tag="title">	
		<format:product product="${commandInstance?.product}"/>
	</content>

	<content tag="content">
		<g:render template="recordInventory" />
	</content>
</g:applyLayout>

