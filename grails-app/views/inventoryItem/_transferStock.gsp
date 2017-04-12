<div id="dlgTransferStock-${itemInstance?.id}" title="${warehouse.message(code: 'inventory.transferStock.label')}" style="display: none;" >

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
                                    ${binLocation?.locationNumber}


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
                            </tbody>
                            <tfoot>
                            <tr>
                                <td colspan="2" class="center">
                                    <button>
                                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="inventory.transferStock.label"/>
                                    </button>
                                    &nbsp;
                                    <a href="javascript:void(-1);" id="btnTransferClose-${itemInstance?.id }" class="middle">
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
        $("#dlgTransferStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: 800 });
        $("#btnTransferStock-${itemInstance?.id}").click(function() { $("#dlgTransferStock-${itemInstance?.id}").dialog('open'); });
        $("#btnTransferClose-${itemInstance?.id}").click(function() { $("#dlgTransferStock-${itemInstance?.id}").dialog('close'); });
    });
</script>

