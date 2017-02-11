<div id="dlgReturnStock-${itemInstance?.id}" title="${warehouse.message(code: 'inventory.returnStock.label', default: 'Return stock')}" style="padding: 10px; display: none;" >
	<table>
		<tr>
			<td>
				<g:form controller="inventoryItem" action="transferStock">

                    <%--
					<g:hiddenField name="id" value="${itemInstance?.id}"/>
					--%>
					<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
					<table>
						<tbody>

                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.transferFrom.label" default="Transfer from" /></label></td>
                                <td valign="top" class="value">
                                    <g:selectLocation id="transferFrom-${itemInstance?.id}"
                                                        name="source.id"
                                                      class="chzn-select"
                                                      noSelection="['null':'']"
                                                      data-placeholder="Choose where stock is being returned from ..."
                                                      value=""
                                                      style="width: 350px" />

                                </td>
                            </tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="inventory.transferTo.label" default="Transfer to" /></label></td>
								<td valign="top" class="value">
									<input type="text" disabled="disabled" class="text" value="${session.warehouse.name}"/>
								</td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.lotNumber.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:selectInventoryItem id="lotNumber-${itemInstance?.id}"
                                                       name="id"
                                                       value="${itemInstance?.id}"
                                                        class="chzn-select"
                                                        noSelection="['null':'']"
                                                        product="${commandInstance?.productInstance}"
                                                        data-placeholder="Choose lot number"/>


                                </td>
                            </tr>

                        	<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="default.quantity.label" /></label></td>
								<td valign="top" class="value">
									<g:textField name="quantity" size="6" value="" class="text" placeholder="${commandInstance?.productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}"/>

									
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="default.comment.label" /></label></td>
								<td valign="middle" class="value">
									<g:textArea name="comment"></g:textArea>

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
									<a href="javascript:void(-1);" id="btnReturnClose-${itemInstance?.id }" class="button">
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
<script type="text/javascript">
	$(document).ready(function(){
		$("#dlgReturnStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: 600 });
		$("#btnReturnStock-${itemInstance?.id}").click(function() { $("#dlgReturnStock-${itemInstance?.id}").dialog('open'); });
		$("#btnReturnClose-${itemInstance?.id}").click(function() { $("#dlgReturnStock-${itemInstance?.id}").dialog('close'); });
	});
</script>	

