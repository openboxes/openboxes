<div id="dlgAdjustStock-${dialogId}" title="${warehouse.message(code: 'inventory.adjustStock.label')}" style="display: none;">
    <div class="dialog">
        <table>
            <tr>
                <td>
                    <g:form controller="inventoryItem" action="adjustStock" autocomplete="off">
                        <g:hiddenField name="id" value="${itemInstance?.id}"/>
                        <g:hiddenField name="product.id" value="${itemInstance?.product?.id}"/>
                        <g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>

                        <table>
                            <tbody>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="product.label"/></label></td>
                                <td valign="top" class="value">
                                    <format:product product="${itemInstance?.product}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:selectBinLocation name="binLocation.id" value="${binLocation?.id}" class="binLocation" noSelection="['':'']"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="product.lotNumber.label"/></label></td>
                                <td valign="top" class="value">
                                    <g:if test="${itemInstance?.lotNumber }">
                                        <span class="lotNumber">${itemInstance?.lotNumber }</span>
                                    </g:if>
                                    <g:else><span class="fade"><warehouse:message code="default.none.label"/></span></g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="product.expirationDate.label"/></label></td>
                                <td valign="top" class="value">
                                    <g:if test="${itemInstance?.expirationDate }">
                                        <format:expirationDate obj="${itemInstance?.expirationDate}"/>
                                    </g:if>
                                    <g:else>
                                        <span class="fade"><warehouse:message code="default.never.label"/></span>
                                    </g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.previousQuantity.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:hiddenField id="oldQuantity" name="oldQuantity" value="${itemQuantity }"/>
                                    ${itemQuantity }
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="inventory.newQuantity.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:textField id="newQuantity" name="newQuantity" size="6" value="${itemQuantity }" class="text"/>

                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="product.unitOfMeasure.label" /></label></td>
                                <td valign="top" class="value">
                                    ${commandInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                                </td>
                            </tr>


                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="default.comments.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:textArea name="comment" cols="60" rows="5" value="${params.comment }" class="text"/>
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
                                    <a href="javascript:void(-1);" id="btnAdjustClose-${dialogId }" class="middle">
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
        $("#dlgAdjustStock-${dialogId}").dialog({ autoOpen: false, modal: true, width: 600 });
        $("#btnAdjustStock-${dialogId}").click(function() { $("#dlgAdjustStock-${dialogId}").dialog('open'); });
        $("#btnAdjustClose-${dialogId}").click(function() { $("#dlgAdjustStock-${dialogId}").dialog('close'); });
        $(".binLocation").livequery(function() {
            $(this).chosen({ allow_single_deselect:true, width: '100%' });
        });
    });
</script>

