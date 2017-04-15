<div id="dlgReturnStock-${dialogId}" title="${warehouse.message(code: 'inventory.returnStock.label', default: 'Return stock')}" style="display: none;" >
	<div class="dialog">
		<table>
			<tr>
				<td>
					<g:form controller="inventoryItem" action="transferStock">
                        <g:hiddenField name="id" value="${itemInstance?.id}"/>
						<g:hiddenField name="product.id" value="${itemInstance?.product?.id}"/>
						<g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
						<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
						<table>
							<tbody>

                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.transferFrom.label" default="Transfer from" /></label></td>
                                <td valign="top" class="value">
                                    <g:selectLocation id="transferFrom-${itemInstance?.id}"
                                                        name="source.id"
                                                      class="chzn-select-deselect"
                                                      noSelection="['null':'']"
                                                      data-placeholder="Choose where stock is being returned from ..."
                                                      value=""/>

                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.transferTo.label" default="Transfer to" /></label></td>
                                <td valign="top" class="value">
                                    ${session.warehouse.name}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
                                <td valign="top" class="value">
                                    ${binLocation?.name}

                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.lotNumber.label" /></label></td>
                                <td valign="top" class="value">
                                    ${itemInstance?.lotNumber}
                                </td>
                            </tr>
							<tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.quantityToReturn.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:textField name="quantity" size="6" value="" class="text"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="product.unitOfMeasure.label" /></label></td>
                                <td valign="top" class="value">
                                    ${commandInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                                </td>
                            </tr>
							</tbody>
							<tfoot>
								<tr>
									<td colspan="2" class="center">
										<button class="button icon approve">
											<warehouse:message code="inventory.returnStock.label" default="Return stock"/>
										</button>
										&nbsp;
										<a href="javascript:void(-1);" id="btnReturnClose-${dialogId }" class="middle">
											<warehouse:message code="default.button.cancel.label"/>
										</a>
									</td>
								</tr>
							</tfoot>
						</table>
					</g:form>
				</td>
			</tr>
		</table>
	</div>
</div>		
<script type="text/javascript">
	$(document).ready(function(){
		$("#dlgReturnStock-${dialogId}").dialog({ autoOpen: false, modal: true, width: 800 });
		$("#btnReturnStock-${dialogId}").click(function() { $("#dlgReturnStock-${dialogId}").dialog('open'); });
		$("#btnReturnClose-${dialogId}").click(function() { $("#dlgReturnStock-${dialogId}").dialog('close'); });
	});
</script>	

