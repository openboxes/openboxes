<%@ page import="org.pih.warehouse.product.Product"%>
<g:applyLayout name="stockCard">
	
	<content tag="title">
		<warehouse:message code="inventory.record.label"/>
	</content>
	
	<content tag="heading">	
		<format:product product="${commandInstance?.productInstance}"/>
	</content>

	<content tag="content">
		<g:render template="showRecordInventory" mode="[commandInstance:commandInstance]" />
	</content>
</g:applyLayout>

