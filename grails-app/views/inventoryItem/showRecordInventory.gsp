<%@ page import="org.pih.warehouse.product.Product"%>
<g:applyLayout name="stockCard">
	
	<content tag="title">
		<warehouse:message code="inventory.record.label"/>
	</content>
	
	<content tag="heading">	
		<warehouse:message code="inventory.record.label"/>
		&rsaquo;
		<span class="fade">
			<format:product product="${commandInstance?.productInstance}"/>
		</span>
	</content>

	<content tag="content">
		<g:render template="showRecordInventory" mode="[commandInstance:commandInstance]" />
	</content>
</g:applyLayout>

