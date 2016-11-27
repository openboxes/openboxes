
<script type="text/javascript">
	$(document).ready(function(){
		$("#dlgAddToShipment-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
		$("#btnAddToShipment-${itemInstance?.id}").click(function() { $("#dlgAddToShipment-${itemInstance?.id}").dialog('open'); });									
		$("#btnAddClose-${itemInstance?.id}").click(function() { $("#dlgAddToShipment-${itemInstance?.id}").dialog('close'); });									
	});
</script>	   

<div id="dlgAddToShipment-${itemInstance?.id}" title="${warehouse.message(code:'shipping.addToShipment.label')}" style="padding: 10px; display: none; vertical-align: middle;" >

	<div class="summary">
		<p class="title">
			<small>${commandInstance?.productInstance.productCode}</small>
			<format:product product="${commandInstance?.productInstance}"/>
		</p>
		<table>
			<tr>
				<td>
					<label><warehouse:message code="product.lotNumber.label"/></label>
					<g:if test="${itemInstance?.lotNumber }">
						<span class="lotNumber">${itemInstance?.lotNumber }</span>
					</g:if>
					<g:else><span class="fade"><warehouse:message code="default.none.label"/></span></g:else>
				</td>
				<td>
					<label><warehouse:message code="product.expirationDate.label"/></label>
					<g:if test="${itemInstance?.expirationDate }">
						<format:expirationDate obj="${itemInstance?.expirationDate}"/>
					</g:if>
					<g:else>
						<span class="fade"><warehouse:message code="default.never.label"/></span>
					</g:else>
				</td>
				<td>
					<label><warehouse:message code="inventoryItem.qtyAvailable.label" default="Qty Available"/></label>
					${itemQuantity} ${commandInstance?.productInstance.unitOfMeasure}
				</td>
			</tr>
		</table>
	</div>

	<g:if test="${commandInstance?.pendingShipmentList }">
		<g:form controller="inventoryItem" action="addToShipment">
			<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
			<g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
			<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="inventory.label"/></label></td>
						<td valign="top" class="value">
								${commandInstance?.inventoryInstance?.warehouse?.name }
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="item.label"/></label></td>
						<td valign="top" class="value">
							<format:product product="${commandInstance?.productInstance}"/>
							<g:if test="${itemInstance?.lotNumber }">&rsaquo; ${itemInstance?.lotNumber }</g:if>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="shipping.addToShipment.label"/></label></td>
						<td valign="top" class="value">

							<select id="shipmentContainer" name="shipmentContainer">
								<option value="null"></option>
								<g:each var="shipmentInstance" in="${commandInstance?.pendingShipmentList }">
									<g:set var="expectedShippingDate" value="${prettyDateFormat(date: shipmentInstance?.expectedShippingDate)}"/>
									<g:set var="label" value="${shipmentInstance?.name + ' to ' + shipmentInstance?.destination?.name + ', departing ' + expectedShippingDate}"/>
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
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="default.quantity.label"/> </label></td>
						<td valign="top" class="value">
							 <g:textField id="quantity" name="quantity" size="15" value="" class="medium text" /> &nbsp;
								<span class="fade"><warehouse:message code="product.remaining.label"/>: ${itemQuantity }</span>
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="shipping.recipient.label"/></label></td>
						<td valign="top" class="value">
							<g:autoSuggestEditable id="recipient-${itemInstance?.id}" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName"
								width="200" valueId="" valueName="" class="medium text"/>
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2" class="middle center">
							<button type="submit" name="addItem" class="button icon add">
								<warehouse:message code="shipping.addToShipment.label"/>
							</button>
							&nbsp;
							<a href="javascript:void(-1);" id="btnAddClose-${itemInstance?.id }">
								<warehouse:message code="default.button.cancel.label"/>
							</a>

						</td>
					</tr>
				</tfoot>
			</table>
		</g:form>
	</g:if>
	<g:else>
		<div class="empty fade center">
			<warehouse:message code="shipping.thereAreNoPendingShipmentsAvailable.message"/>

		</div>
	</g:else>
</div>		
		     

