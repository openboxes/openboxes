<div class="box">
    <h2>${warehouse.message(code:'requisitionItem.cancel.label', default:'Cancel requisition item')}</h2>


    <g:hasErrors bean="${flash.errors}">
        <div class="errors">
            <g:renderErrors bean="${flash.errors}" as="list" />
        </div>
    </g:hasErrors>


    <g:form controller="requisitionItem" action="cancelQuantity" fragment="${selectedRequisitionItem?.id}">
        <g:hiddenField name="id" value="${selectedRequisitionItem?.id }"/>
        <g:hiddenField name="redirectAction" value="${actionName}"/>
        <g:hiddenField name="actionType" value="${params.actionType }"/>


        <table>

            <tr>
                <td class="right top">
                    <label><warehouse:message code="requisitionItem.product.label" default="Product"/></label>
                </td>
                <td>
                    ${selectedRequisitionItem?.product?.productCode}
                    <format:product product="${selectedRequisitionItem?.product}"/>

                </td>
            </tr>
            <tr>
                <td class="right top">
                    <label><warehouse:message code="requisitionItem.cancelQuantity.label" default="Cancel quantity"/></label>
                </td>
                <td>
                    ${selectedRequisitionItem?.quantity}
                    ${selectedRequisitionItem?.productPackage?:"EA/1"}
                </td>
            </tr>
            <tr>
                <td class="right top">
                    <label><warehouse:message code="requisitionItem.reasonCode.label" default="Reason code"/></label>
                </td>
                <td>

                    <g:selectCancelReasonCode
                            name="reasonCode"
                            class="chzn-select"
                            style="width:450px;"
                            data-placeholder="Choose a reason code ..."
                            noSelection="['':'']"
                            value="${selectedRequisitionItem?.cancelReasonCode }"/>

                </td>
            </tr>
            <tr>
                <td class="right top">
                    <label><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>

                </td>
                <td>
                    <g:textField name="comments" value="${selectedRequisitionItem.cancelComments}"
                                 size="60" class="text" placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>
                </td>

            </tr>
            <tr>
                <td></td>
                <td class="left"">
                    <button class="button">
                        ${warehouse.message(code:'default.button.save.label') }
                    </button>
                    &nbsp;
                    <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}">
                        ${warehouse.message(code:'default.button.cancel.label') }
                    </g:link>
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