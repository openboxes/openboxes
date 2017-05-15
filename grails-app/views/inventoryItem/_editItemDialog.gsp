<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditItem-${dialogId}").click(function() { $("#dlgEditItem-${dialogId}").dialog('open'); });
		$("#dlgEditItem-${dialogId}").dialog({ autoOpen: false, modal: true, width: 800 });
		$("#btnEditClose-${dialogId}").click( function() { $("#dlgEditItem-${dialogId}").dialog('close'); });
	});
</script>	   
<div id="dlgEditItem-${dialogId}" title="${warehouse.message(code: 'inventory.editItem.label')}" style="padding: 10px; display: none;" >
	
	<div class="dialog">

		<g:form name="editInventoryItem" controller="inventoryItem" action="update">
			<g:hiddenField name="id" value="${itemInstance?.id}"/>
			<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
			<g:hiddenField name="product.id" value="${itemInstance?.product?.id}"/>
			<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
			
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="product.label" /></label></td>                            
						<td valign="top" class="value">
							<format:product product="${itemInstance?.product}"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
						<td valign="top" class="value">
							${binLocation?.name}
						</td>
					</tr>
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
							<a href="javascript:void(-1);" id="btnEditClose-${dialogId }">
								<warehouse:message code="default.button.cancel.label"/>
							</a>
						</td>
					</tr>	
				</tfoot>			
			</table>
		</g:form>				

	</div>		
</div>

