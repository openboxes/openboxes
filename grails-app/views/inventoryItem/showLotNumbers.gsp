<g:applyLayout name="stockCard">

	<content tag="title">
		<format:product product="${commandInstance?.product}"/>
	</content>

	<content tag="heading">	
		<warehouse:message code="inventory.showLotNumbers.label"/>
	</content>

	<content tag="content">
		<g:render template="showLotNumbers"/>
	</content>
</g:applyLayout>