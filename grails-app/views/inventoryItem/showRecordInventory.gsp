<%@ page import="org.pih.warehouse.product.Product"%>
<g:applyLayout name="stockCard">
	
	<content tag="title">
		<warehouse:message code="inventory.record.label"/>
	</content>
	
	<content tag="heading">	
		<span class="fade">
			<format:product product="${commandInstance?.productInstance}"/>
		</span>
		&rsaquo;
		<warehouse:message code="inventory.record.label"/>
	</content>

	<content tag="content">
		<g:render template="showRecordInventory" mode="[commandInstance:commandInstance]" />
	</content>
</g:applyLayout>

