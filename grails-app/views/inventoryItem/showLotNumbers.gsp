<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="inventory.showLotNumbers.label"/>
	</content>

	<content tag="heading">	
		<span class="fade"><format:product product="${commandInstance?.productInstance}"/></span>
		&rsaquo;
		<warehouse:message code="inventory.showLotNumbers.label"/>
	</content>

	<content tag="content">
		<g:render template="showLotNumbers"/>
	</content>
</g:applyLayout>