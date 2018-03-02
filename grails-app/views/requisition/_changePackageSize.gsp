<div class="box">
    <h2>${warehouse.message(code:'requisitionItem.changePackageSize.label', default:'Change package size')}</h2>
    <g:form controller="requisition" action="changeQuantity" fragment="${selectedRequisitionItem?.id}">
        <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
        <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
        <g:hiddenField name="actionType" value="${params.actionType }"/>

        <table>
            <tr>
                <td>
                    ${selectedRequisitionItem?.product?.productCode}
                    <format:product product="${selectedRequisitionItem?.product}"/>
                </td>
                <td>
                    <g:selectProductPackage product="${selectedRequisitionItem?.product}"/>
                </td>
                <td>
                    <g:textField name="quantityChange" value="${selectedRequisitionItem?.quantity}" class="text center" size="5"/>
                </td>
                <td>
                    <g:select name="cancelReasonCode"
                              from="['Package size',
                                      'Stock out',
                                      'Substituted',
                                      'Damaged','Expired',
                                      'Reserved',
                                      'Cancelled by requestor',
                                      'Clinical adjustment',
                                      'Other']"

                              noSelection="['null':'']" value="${selectedRequisitionItem.cancelReasonCode?:'Package size' }"/>
                </td>
                <td>
                    <button class="button">
                        ${warehouse.message(code:'default.button.save.label') }
                    </button>
                </td>
                <td>
                    <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}">
                        ${warehouse.message(code:'default.button.cancel.label') }
                    </g:link>
                </td>
            </tr>
            <tr>
                <td colspan="8">
                    <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                    <g:textArea name="comments" cols="100" rows="5"></g:textArea>
                </td>

            </tr>
        </table>
    </g:form>
</div>
<script type="text/javascript">
    $(function() {
        $("#reasonCode").focus();
    });
</script>