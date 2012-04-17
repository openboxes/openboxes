<g:applyLayout name="stockCard">

	<content tag="title">	
		<warehouse:message code="inventory.showConsumption.label"/>
	</content>

	<content tag="heading">	
		<warehouse:message code="inventory.showConsumption.label"/>
		&rsaquo;
		<span class="fade"><format:product product="${commandInstance?.productInstance}"/></span>
	</content>

	<content tag="content">
		<div class="middle center">
			<g:render template="showGraph"/>
		</div>
		<div class="middle center" style="padding: 10px">
			<warehouse:message code='default.notImplementedYet.message'/>								
		</div>
	</content>
</g:applyLayout>

