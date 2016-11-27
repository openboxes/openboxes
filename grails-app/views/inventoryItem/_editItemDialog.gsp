<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditItem-${itemInstance?.id}").click(function() { $("#dlgEditItem-${itemInstance?.id}").dialog('open'); });									
		$("#dlgEditItem-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: 600, height: 300 });
		$("#btnEditClose-${itemInstance?.id}").click( function() { $("#dlgEditItem-${itemInstance?.id}").dialog('close'); });	
	});
</script>	   
<div id="dlgEditItem-${itemInstance?.id}" title="${warehouse.message(code: 'inventory.editItem.label')}" style="display: none;" >
	<g:form name="editInventoryItem" controller="inventoryItem" action="update">
		<g:hiddenField name="id" value="${itemInstance?.id}"/>
		<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
		<g:hiddenField name="product.id" value="${itemInstance?.product?.id}"/>
		<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>


		<div class="summary">

			<h1 class="title">${commandInstance?.productInstance.productCode} <format:product product="${commandInstance?.productInstance}"/></h1>
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

		<table>
			<tbody>

				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.lotNumber.label"/></label></td>
					<td valign="top" class="value">
						<g:textField name="lotNumber" value="${itemInstance?.lotNumber}" class="text lotNumber"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.expirationDate.label"/></label></td>
					<td valign="top" class="">
						<g:datePicker name="expirationDate" precision="day" default="none" value="${itemInstance?.expirationDate }" noSelection="['':'']"/>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2" class="center">
						<button type="submit" name="addItem">
							<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="default.button.save.label"/>
						</button>
						&nbsp;
						<a href="javascript:void();" id="btnEditClose-${itemInstance?.id }">
							<warehouse:message code="default.button.cancel.label"/>
						</a>
					</td>
				</tr>
			</tfoot>
		</table>
	</g:form>


</div>

