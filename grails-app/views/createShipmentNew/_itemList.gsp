<table border="0" width="100%">
	<thead>
		<tr>
			<th>Container</th>
			<th>Product</th>
			<th>Quantity</th>
			<th>Serial/Lot Number</th>
			<th>Recipient</th>
		</tr>
	</thead>
	<tbody>
		<g:set var="counter" value="${0 }" />
		<g:each var="containerInstance"
			in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort()}">
			<g:each var="itemInstance"
				in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == containerInstance?.id})?.sort()}">
				<tr class="${counter++ % 2 == 0 ? 'odd':'even'}">
					<td>
						<g:if test="${itemInstance?.container?.parentContainer}">
							${itemInstance?.container?.parentContainer?.name}
							/
																	</g:if>
						${itemInstance?.container?.name}

					</td>
					<td>
						${itemInstance?.product?.name}
																</td>
					<td>
						${itemInstance?.quantity}
																</td>
					<td>
						<g:if test="${itemInstance?.lotNumber}">
							${itemInstance?.lotNumber}
																	</g:if>
						<g:else>
							${itemInstance?.serialNumber}
																	</g:else>
					</td>
					<td>
						${itemInstance?.recipient?.name}
																</td>
				</tr>
			</g:each>

			<g:each var="boxInstance" in="${containerInstance?.containers?.sort()}">
				<g:each var="itemInstance"
					in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == boxInstance?.id})?.sort()}">

					<tr class="${counter++ % 2 == 0 ? 'odd':'even'}">
						<td>
							<g:if test="${itemInstance?.container?.parentContainer}">
								${itemInstance?.container?.parentContainer?.name}
								/
																	</g:if>
							${itemInstance?.container?.name}

						</td>
						<td>
							${itemInstance?.product?.name}
																</td>
						<td>
							${itemInstance?.quantity}
																</td>
						<td>
							<g:if test="${itemInstance?.lotNumber}">
								${itemInstance?.lotNumber}
																	</g:if>
							<g:else>
								${itemInstance?.serialNumber}
																	</g:else>
						</td>
						<td>
							${itemInstance?.recipient?.name}
																</td>
					</tr>
				</g:each>
			</g:each>
		</g:each>
	</tbody>
</table>		