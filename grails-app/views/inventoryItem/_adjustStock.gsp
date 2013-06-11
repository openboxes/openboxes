<div id="dlgAdjustStock-${itemInstance?.id}" title="${warehouse.message(code: 'inventory.adjustStock.label')}" style="padding: 10px; display: none;" >
    <table>
        <tr>
            <td>
                <g:form controller="inventoryItem" action="adjustStock">
                    <g:hiddenField name="id" value="${itemInstance?.id}"/>
                    <g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
                    <g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>

                    <table>
                        <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="product.label"/></label></td>
                            <td valign="top" class="value">
                                <format:product product="${commandInstance?.productInstance}"/>
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
                                ${commandInstance?.productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                            </td>
                        </tr>


                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message code="inventory.newQuantity.label" /></label></td>
                            <td valign="top" class="value">
                                <g:textField id="newQuantity" name="newQuantity" size="6" value="${itemQuantity }" class="text"/>
                                ${commandInstance?.productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}

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
                                <a href="javascript:void();" id="btnAdjustClose-${itemInstance?.id }" class="middle">
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
        $("#dlgAdjustStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '500px' });
        $("#btnAdjustStock-${itemInstance?.id}").click(function() { $("#dlgAdjustStock-${itemInstance?.id}").dialog('open'); });
        $("#btnAdjustClose-${itemInstance?.id}").click(function() { $("#dlgAdjustStock-${itemInstance?.id}").dialog('close'); });
    });
</script>

