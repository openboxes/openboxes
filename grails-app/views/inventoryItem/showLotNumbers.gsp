<g:applyLayout name="stockCard">

	<content tag="title">
		<warehouse:message code="inventory.showLotNumbers.label"/>
	</content>

	<content tag="heading">	
		<warehouse:message code="inventory.showLotNumbers.label"/>
		&rsaquo;
		<span class="fade"><format:product product="${commandInstance?.productInstance}"/></span>
	</content>

	<content tag="content">
		<g:render template="showLotNumbers"/>
	</content>
</g:applyLayout>