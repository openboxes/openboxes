<div id="dlgAdjustStock-${itemInstance?.id}" class="dligAdjustStock" title="${warehouse.message(code: 'inventory.adjustStock.label')}" style="display: none;" >


    <g:formRemote name="adjustStock" url="[controller: 'inventoryItem', action: 'adjustStock']" onLoading="showLoading()"
                  onComplete="hideLoading();"
                  onSuccess="onSuccess('#dlgAdjustStock-${itemInstance?.id}', data);"
                  onFailure="onFailure('#dlgAdjustStock-${itemInstance?.id}', XMLHttpRequest,textStatus,errorThrown);">

        <g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
        <g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>

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

        <div class="errorMessage"></div>

        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="adjustment.oldQuantity.label" /></label></td>
                <td valign="top" class="value">
                    <g:textField id="oldQuantity" name="oldQuantity" size="10" value="${itemQuantity }" readonly="readonly" class="text middle right"/>
                    ${commandInstance?.productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>


            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="adjustment.newQuantity.label" /></label></td>
                <td valign="top" class="value">
                    <g:textField id="newQuantity" name="newQuantity" size="10" value="" class="text middle right" autocomplete="off"/>
                    ${commandInstance?.productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}

                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="adjustment.reasonCode.label" /></label></td>
                <td valign="top" class="value">
                    <g:selectAdjustmentReasonCode name="reasonCode" noSelection="['':'']" class="chzn-select-deselect"
                             value="${command?.reasonCode}"/>

                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="adjustment.comment.label" /></label></td>
                <td valign="top" class="value">
                    <g:textArea name="comments" value="${command?.comments}" cols="60" rows="3" class="middle text"/>

                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="adjustment.adjustedBy.label" /></label></td>
                <td valign="top" class="value">
                    <g:textField id="adjustedBy" name="adjustedByName" size="60" value="${session?.user?.name}" class="text middle" readonly="readonly"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="adjustment.adjustedOn.label" /></label></td>
                <td valign="top" class="value">
                    <g:textField id="adjustedOnReadOnly" name="adjustedOnReadOnly" size="60"
                                 value="${g.formatDate(date:new Date())}" class="text middle" readonly="readonly"/>

                </td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="2" class="center">
                    <button>
                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="inventory.adjustStock.label"/>
                    </button>
                    &nbsp;
                    <a href="javascript:void(0);" id="btnAdjustClose-${itemInstance?.id }" class="middle">
                        <warehouse:message code="default.button.cancel.label"/>
                    </a>
                </td>
            </tr>
            </tfoot>
        </table>
    </g:formRemote>
</div>
<script type="text/javascript">
    $(document).ready(function(){
        $("#dlgAdjustStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: 700, height: 500 });
        $("#btnAdjustStock-${itemInstance?.id}").click(function() {
            clearMessage("#dlgAdjustStock-${itemInstance?.id}");
            $("#dlgAdjustStock-${itemInstance?.id}").dialog('open');
        });
        $("#btnAdjustClose-${itemInstance?.id}").click(function() {
            $("#dlgAdjustStock-${itemInstance?.id}").dialog('close');
        });
    });
</script>

