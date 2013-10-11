<div id="consumption">
    <table class="box" id="data">
        <thead>
            <tr class="header odd">
                <th><warehouse:message code="requisition.monthRequested.label" default="Month requested"/></th>
                <th><warehouse:message code="product.label"/></th>
                <th><warehouse:message code="product.unitOfMeasure.label"/></th>
                <th>
                    <warehouse:message code="requisition.requestNumber.label"/> -
                    <warehouse:message code="requisition.status.label"/>
                </th>
                <th><warehouse:message code="requisitionItem.status.label"/></th>
                <th><warehouse:message code="requisitionItem.cancelReasonCode.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantity.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityCanceled.label"/></th>
                <th class="center middle"><warehouse:message code="requisitionItem.quantityIssued.label" default="Quantity issued"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each var="entry" in="${issuedRequisitionItems.groupBy { it.requisition.monthRequested } }" status="i">

                <g:set var="monthlyQuantityRequested" value="${entry?.value?.collect { it?.quantity?:0 }?.sum()?:0 }"/>
                <g:set var="monthlyQuantityCanceled" value="${entry?.value?.collect { it?.quantityCanceled?:0 }?.sum()?:0 }"/>
                <g:set var="monthlyQuantityIssued" value="${monthlyQuantityRequested - monthlyQuantityCanceled}"/>
                <tr class="prop header ${i%2?'even':'odd'}" style="cursor: pointer">
                    <td>
                        <b>${entry.key}</b>
                    </td>
                    <td>
                        <b>${commandInstance?.productInstance?.name}</b>
                    </td>
                    <td>
                        <b>${commandInstance?.productInstance?.unitOfMeasure}</b>
                    </td>
                    <td colspan="3"></td>
                    <td class="center middle">
                        <b>${monthlyQuantityRequested}</b>
                    </td>
                    <td class="center middle">
                        <b>${monthlyQuantityCanceled}</b>
                    </td>
                    <td class="center middle">
                        <b>${monthlyQuantityIssued}</b>
                    </td>

                </tr>
                    <g:each var="requisitionItem" in="${entry.value}" status="j">
                        <tr class="prop ${j%2?'odd':'even'}">
                            <td>
                                <%--
                                ${requisitionItem.requisition.monthRequested}
                                --%>
                            </td>
                            <td>
                                ${requisitionItem.product}
                            </td>
                            <td>
                                ${requisitionItem.product.unitOfMeasure}
                            </td>
                            <td>
                                ${requisitionItem.requisition.requestNumber} -
                                ${requisitionItem.requisition.status}
                            </td>
                            <td>
                                ${requisitionItem.status}
                            </td>
                            <td>
                                ${requisitionItem.cancelReasonCode}
                            </td>
                            <td class="center middle">
                                ${requisitionItem.quantity}
                            </td>
                            <td class="center middle">
                                ${requisitionItem.quantityCanceled}
                            </td>
                            <td class="center middle">
                                ${(requisitionItem.quantity?:0) - (requisitionItem.quantityCanceled?:0)}
                            </td>
                        </tr>
                    </g:each>
            </g:each>
        </tbody>
    </table>
</div>
<script type="text/javascript">
$(function () {
    $("#data tr:not(.header)").hide();
    $('#data tr.header').click(function(){
        $(this).nextUntil('tr.header').slideToggle(100);
    });
});
</script>
