
<script type="text/javascript">
	$(document).ready(function(){
		$("#dlgAddToShipment-${dialogId}").dialog({ autoOpen: false, modal: true, width: 800, height: 500 });
		$("#btnAddToShipment-${dialogId}").click(function() { $("#dlgAddToShipment-${dialogId}").dialog('open'); });
		$("#btnAddClose-${dialogId}").click(function() { $("#dlgAddToShipment-${dialogId}").dialog('close'); });
	});
</script>	   

<div id="dlgAddToShipment-${dialogId}" title="${warehouse.message(code:'shipping.addToShipment.label')}" style="padding: 10px; display: none; vertical-align: middle;" >

	<div class="dialog">

		<g:if test="${commandInstance?.pendingShipmentList }">
			<table>
				<tr>
					<td>
						<g:form controller="inventoryItem" action="addToShipment">
							<g:hiddenField name="product.id" value="${commandInstance?.product?.id}"/>
                            <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
							<g:hiddenField name="inventory.id" value="${commandInstance?.inventory?.id}"/>
							<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
							<table>
								<tbody>
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="inventory.label"/></label></td>
										<td valign="top" class="value">
												${commandInstance?.inventory?.warehouse?.name }
										</td>
									</tr>


                                    <tr class="prop">
                                        <td valign="top" class="name"><label><warehouse:message code="product.label"/></label></td>
                                        <td valign="top" class="value">
                                            <format:product product="${commandInstance?.product}"/>
                                        </td>
                                    </tr>

                                    <tr class="prop">
                                        <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label"/></label></td>
                                        <td valign="top" class="value">
                                            <g:if test="${binLocation}">
                                                ${binLocation?.locationNumber}

                                            </g:if>
                                            <g:else>
                                                ${g.message(code:'default.label')}
                                            </g:else>
                                        </td>
                                    </tr>
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="inventoryItem.lotNumber.label"/></label></td>
										<td valign="top" class="value">
                                            <g:if test="${itemInstance}">
    											${itemInstance?.lotNumber }
                                            </g:if>
                                            <g:else>
                                                ${g.message(code:'default.empty.label')}
                                            </g:else>
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message code="shipping.addToShipment.label"/></label></td>
										<td valign="top" class="value">

											<select id="shipmentContainer" name="shipmentContainer" class="chzn-select-deselect">
												<option value="null"></option>
												<g:each var="shipmentInstance" in="${commandInstance?.pendingShipmentList }">
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
											<g:autoSuggestEditable id="recipient-${dialogId}" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName"
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
											<a href="javascript:void(-1);" id="btnAddClose-${dialogId }">
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
		</g:if>
		<g:else>
            <div class="empty fade center">
                <warehouse:message code="shipping.thereAreNoPendingShipmentsAvailable.message"/>

            </div>
		</g:else>
	</div>

</div>		
		     

