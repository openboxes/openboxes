

<select id="${attrs.id}" name="${attrs.name }"> 
	<option value="">
		${attrs.noSelection.entrySet().iterator().next().value }
	</option>
	<g:each var="shipment" in="${attrs.from}">		
		<optgroup label="${shipment.name }">
			<option value="${shipment?.id }:0"><warehouse:message code="shipping.unpackedItems.label"/></option>
			<g:each var="container" in="${shipment.containers }">
				<option value="${shipment?.id }:${container.id }">${container?.name }</option>
			</g:each>
		</optgroup>
	</g:each>
</select>