
<div class="dialog">

	<g:form controller="inventoryItem" action="addToShipment">
		<g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
		<g:hiddenField name="location.id" value="${location?.id}"/>
		<g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
		<g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>
		<table>
			<tbody>

                <tr class="prop">
                    <td valign="top" class="name"><label><g:message code="product.label"/></label></td>
                    <td valign="top" class="value">
                        <format:product product="${inventoryItem?.product}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name"><label><g:message code="inventoryItem.lotNumber.label"/></label></td>
                    <td valign="top" class="value">
                        ${inventoryItem?.lotNumber?:g.message(code:'default.empty.label')}
                    </td>
                </tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="default.source.label"/></label></td>
					<td valign="top" class="value">
						${session.warehouse?.name }
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="location.binLocation.label"/></label></td>
					<td valign="top" class="value">
						${binLocation?.name?:g.message(code:'default.label')}
					</td>
				</tr>
                <tr class="prop">
                    <td valign="top" class="name"><label><g:message code="default.quantityOnHand.label"/> </label></td>
                    <td valign="top" class="value">
                        ${quantityAvailable }
                        ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                    </td>
                </tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipping.shipment.label"/></label></td>
					<td valign="top" class="value">
                        <g:selectContainer id="shipmentContainer" name="shipmentContainer" class="chzn-select-deselect"/>
					</td>
				</tr>
                <tr class="prop">
                    <td valign="top" class="name"><label><g:message code="shipping.recipient.label"/></label></td>
                    <td valign="top" class="value">
                        <g:selectPerson name="recipient.id" value="${params.recipient}" class="chzn-select-deselect"/>
                    </td>
                </tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="default.quantity.label"/> </label></td>
					<td valign="top" class="value">
						 <g:textField id="quantity" name="quantity" size="15" value="" class="medium text" />
                         ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
					</td>
				</tr>

			</tbody>
			<tfoot>
				<tr>
					<td colspan="2" class="middle center">
						<button type="submit" name="addItem" class="button icon add">
							<warehouse:message code="shipping.addToShipment.label"/>
						</button>
					</td>
				</tr>
			</tfoot>
		</table>
	</g:form>
</div>


