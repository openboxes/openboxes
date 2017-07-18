<div id="dlgTransferStock-${dialogId}" title="${warehouse.message(code: 'inventory.transferStock.label')}" style="display: none;" >
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
                                <td valign="top" class="name"><label><warehouse:message code="inventory.transferTo.label" default="Transfer to" /></label></td>
                                <td valign="top" class="value">
                                    <g:selectLocation id="transferTo-${itemInstance?.id}"
                                                      name="destination.id"
                                                      class="chzn-select-deselect"
                                                      noSelection="['null':'']"
                                                      data-placeholder="Choose where stock is being transferred to ..."
                                                      value=""/>


                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.transferFrom.label" default="Transfer from" /></label></td>
                                <td valign="top" class="value">
                                    ${session.warehouse.name}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
                                <td valign="middle" class="value">
                                    <g:if test="${binLocation}">
                                        ${binLocation?.name}
                                    </g:if>
                                    <g:else>
                                        <g:message code="default.label"/>
                                    </g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.quantity.label" /></label></td>
                                <td valign="top" class="value">
                                    ${itemQuantity}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.quantityToTransfer.label" /></label></td>
                                <td valign="middle" class="value">
                                    <g:textField name="quantity" size="10" value="" class="text medium"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="product.unitOfMeasure.label" /></label></td>
                                <td valign="top" class="value">
                                    ${commandInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
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
                                        <warehouse:message code="inventory.transferStock.label"/>
                                    </button>
                                    &nbsp;
                                    <a href="javascript:void(-1);" id="btnTransferClose-${dialogId }" class="middle">
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
        $("#dlgTransferStock-${dialogId}").dialog({ autoOpen: false, modal: true, width: 800, height: 400 });
        $("#btnTransferStock-${dialogId}").click(function() { $("#dlgTransferStock-${dialogId}").dialog('open'); });
        $("#btnTransferClose-${dialogId}").click(function() { $("#dlgTransferStock-${dialogId}").dialog('close'); });
    });
</script>

