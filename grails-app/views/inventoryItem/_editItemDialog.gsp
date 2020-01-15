<div class="dialog">

	<g:form name="editInventoryItem" controller="inventoryItem" action="update">
		<g:hiddenField name="id" value="${inventoryItem?.id}"/>
		<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
		<g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
		<g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>

		<table>
			<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.label" /></label></td>
					<td valign="top" class="value">
						<format:product product="${inventoryItem?.product}"/>
					</td>
				</tr>
				<g:if test="${binLocation}">
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
						<td valign="top" class="value">
							${binLocation?.name}
						</td>
					</tr>
				</g:if>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.lotNumber.label"/></label></td>
					<td valign="top" class="value">
						<g:textField name="lotNumber" value="${inventoryItem?.lotNumber}" class="text lotNumber"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.expirationDate.label"/></label></td>
					<td valign="top" class="">
						<g:set var="currentYear" value="${new Date()[Calendar.YEAR]}"/>
						<g:set var="minimumYear" value="${grailsApplication.config.openboxes.expirationDate.minValue[Calendar.YEAR]}"/>
						<g:datePicker name="expirationDate" precision="day" default="none" years="${minimumYear..currentYear + 20}"
									  value="${inventoryItem?.expirationDate }" noSelection="['':'']"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="default.comments.label"/></label></td>
					<td valign="top" class="value">
						<g:textArea name="comments" value="${inventoryItem?.comments}" class="text medium" rows="5"/>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2" class="center">
						<button type="submit" name="addItem" class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="default.button.save.label"/>
						</button>
					</td>
				</tr>
			</tfoot>
		</table>
	</g:form>

</div>

