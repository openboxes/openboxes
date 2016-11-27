<div id="dlgTransferStock-${itemInstance?.id}" title="${warehouse.message(code: 'inventory.transferStock.label')}" style="display: none;" >

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

    <table>
        <tr>
            <td>
                <g:form controller="inventoryItem" action="transferStock">
                    <g:hiddenField name="id" value="${itemInstance?.id}"/>
                    <g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
                    <g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
                    <table>
                        <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="inventory.transferFrom.label" default="Transfer from" /></label></td>
                            <td valign="top" class="value">
                                <g:select name="transferFromReadOnly" from="${[session.warehouse.name]}" class="chzn-select-deselect" readonly="readonly"></g:select>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="inventory.transferTo.label" default="Transfer to" /></label></td>
                            <td valign="top" class="value">
                                <g:selectLocation id="transferTo-${itemInstance?.id}"
                                                  name="destination.id"
                                                  class="chzn-select"
                                                  noSelection="['null':'']"
                                                  data-placeholder="Choose where stock is being transferred to ..."
                                                  value=""
                                                  style="width: 350px" />


                            </td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="default.quantity.label" /></label></td>
                            <td valign="middle" class="value">
                                <g:textField name="quantity" size="20" value="" class="text middle right"/>
                                ${commandInstance?.productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}

                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="transfer.transferredBy.label" default="Transferred By"/></label></td>
                            <td valign="top" class="value">
                                <g:textField id="adjustedByName" name="adjustedByName" size="60" value="${session?.user?.name}" class="text middle" readonly="readonly"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="transfer.transferredOn.label" default="transferred On" /></label></td>
                            <td valign="top" class="value">
                                <g:textField id="transferredOn" name="transferredOn" size="60"
                                             value="${g.formatDate(date:new Date())}" class="text middle" readonly="readonly"/>

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
<script type="text/javascript">
    $(document).ready(function(){
        $("#dlgTransferStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: 600, height: 500 });
        $("#btnTransferStock-${itemInstance?.id}").click(function() { $("#dlgTransferStock-${itemInstance?.id}").dialog('open'); });
        $("#btnTransferClose-${itemInstance?.id}").click(function() { $("#dlgTransferStock-${itemInstance?.id}").dialog('close'); });
    });
</script>

