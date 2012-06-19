<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="inventory.showLotNumbers.label"/>
	</content>

	<content tag="heading">	
		<format:product product="${commandInstance?.productInstance}"/>
	</content>

	<content tag="content">
		<g:render template="showLotNumbers"/>
	</content>
</g:applyLayout>