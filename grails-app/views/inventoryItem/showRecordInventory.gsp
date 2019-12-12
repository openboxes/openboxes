<%@ page import="org.pih.warehouse.product.Product"%>
<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="inventory.record.label"/>
	</content>

	<content tag="heading">
		<format:product product="${commandInstance?.product}"/>
	</content>

	<content tag="content">
		<g:render template="showRecordInventory" model="[commandInstance:commandInstance, product:product]"/>
	</content>
</g:applyLayout>

