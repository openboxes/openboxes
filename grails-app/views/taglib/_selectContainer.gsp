<select id="shipmentContainer" name="shipmentContainer" class="chzn-select-deselect">
	<option value="null"></option>
	<g:each var="shipmentInstance" in="${attrs?.from }">
		<g:set var="expectedShippingDate" value="${prettyDateFormat(date: shipmentInstance?.expectedShippingDate)}"/>
		<g:set var="label" value="${shipmentInstance?.shipmentNumber + ' - ' + shipmentInstance?.name + ' to ' + shipmentInstance?.destination?.name + ', departing ' + expectedShippingDate}"/>
		<optgroup label="${label }">
			<option value="${shipmentInstance?.id }:0">
				<g:set var="looseItems" value="${shipmentInstance?.shipmentItems?.findAll { it.container == null }}"/>
				&nbsp; <warehouse:message code="inventory.looseItems.label"/> &rsaquo; ${looseItems.size() } <warehouse:message code="default.items.label"/>
			</option>
			<g:each var="containerInstance" in="${shipmentInstance?.containers }">
				<g:set var="containerItems" value="${shipmentInstance?.shipmentItems?.findAll { it?.container?.id == containerInstance?.id }}"/>
				<option value="${shipmentInstance?.id }:${containerInstance?.id }">
					&nbsp; ${containerInstance?.name } &rsaquo; ${containerItems.size() } <warehouse:message code="default.items.label"/>
				</option>
			</g:each>
		</optgroup>
	</g:each>
</select>
