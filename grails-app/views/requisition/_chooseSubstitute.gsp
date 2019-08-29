<div class="box">
    <h2>${warehouse.message(code:'requisitionItem.chooseSubstitute.label', default: 'Choose substitute') }</h2>
    <div class="dialog">
        <g:hasErrors bean="${flash.errors}">
            <div class="errors">
                <g:renderErrors bean="${flash.errors}" as="list" />
            </div>
        </g:hasErrors>


        <g:form controller="requisitionItem" action="chooseSubstitute" fragment="${selectedRequisitionItem?.id}">
            <g:hiddenField name="id" value="${selectedRequisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>
            <table>
                <tr class="">
                    <td class="middle right">
                        <label><warehouse:message code="requisitionItem.product.label"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: selectedRequisitionItem, field: 'product', 'errors')} ${hasErrors(bean: selectedRequisitionItem, field: 'productPackage', 'errors')}">



                        <g:chooseSubstitute id="substitutionId"
                                       name="substitutionId"
                                       data-placeholder="Choose a product package ..."
                                       jsonUrl="${request.contextPath }/json/searchProductPackages"
                                       width="400"
                                       styleClass="text"/>
                    </td>
                </tr>
                <tr class="">
                    <td class="name middle right">
                        <label><warehouse:message code="requisitionItem.quantity.label" default="Quantity"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: selectedRequisitionItem, field: 'quantity', 'errors')}">
                        <g:textField id="quantity" name="quantity" value="" class="text"
                                     placeholder="${warehouse.message(code:'default.quantity.label') }"/> EA/1
                    </td>
                </tr>
                <tr>
                    <td class="name middle right">
                        <label><warehouse:message code="requisitionItem.totalQuantity.label" default="Total quantity"/></label>
                    </td>
                    <td>
                        <span id="totalQuantity">0</span>
                        <span id="unitOfMeasure">EA</span>
                    </td>
                </tr>
                <tr>
                    <td class="middle right">
                        <label><warehouse:message code="requisitionItem.reasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: selectedRequisitionItem, field: 'cancelReasonCode', 'errors')}">

                        <g:selectSubstitutionReasonCode
                                name="reasonCode"
                                class="chzn-select"
                                style="width:450px;"
                                data-placeholder="Choose a reason code ..."
                                noSelection="['':'']"
                                value="${selectedRequisitionItem?.cancelReasonCode }"/>
                    </td>
                </tr>
                <tr>
                    <td class="top right">
                        <label><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                    </td>
                    <td class="${hasErrors(bean: selectedRequisitionItem, field: 'comments', 'errors')}">
                        <g:textField name="comments" value="${selectedRequisitionItem.cancelComments}"
                                     size="60" class="text" placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>


                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="center">
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
</div>
<script language="javascript">
	$(document).ready(function() {

        $("#productPackageQty,#quantity").change(function() {
            var totalQuantity = 0;
            var productPackageQty = $("#productPackageQty").val();
            productPackageQty = productPackageQty ? productPackageQty : 1;
            var quantity = $("#quantity").val();
            console.log(productPackageQty);
            console.log(quantity);
            if(productPackageQty && quantity) {
                totalQuantity = addCommas(productPackageQty * quantity);
            }
            else {
                totalQuantity = 0
            }
            $("#totalQuantity").html(totalQuantity);
        });

        /**
         * Borrowed from
         * http://www.mredkj.com/javascript/numberFormat.html
         *
         * @param nStr
         * @return {*}
         */
        function addCommas(nStr) {
            nStr += '';
            x = nStr.split('.');
            x1 = x[0];
            x2 = x.length > 1 ? '.' + x[1] : '';
            var rgx = /(\d+)(\d{3})/;
            while (rgx.test(x1)) {
                x1 = x1.replace(rgx, '$1' + ',' + '$2');
            }
            return x1 + x2;
        }

    });
</script>
