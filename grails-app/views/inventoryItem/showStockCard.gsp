
<g:applyLayout name="stockCard">

	<content tag="title">
		<%--
		<warehouse:message code="inventory.currentAndPendingStock.label"/>
		 --%>
		<format:product product="${commandInstance?.productInstance}"/>
	</content>

	<content tag="heading">
		<format:product product="${commandInstance?.productInstance}"/>
	</content>

	<content tag="content">
		<g:render template="showStockCard"/>
	</content>

</g:applyLayout>