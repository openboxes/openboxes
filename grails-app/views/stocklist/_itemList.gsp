<div class="page-content">
    <table id="requisition-items" class="fs-repeat-header w100">
        <thead>
        <tr>
            <th colspan="8" class="gray-background">${pageTitle}</th>
        </tr>
        <tr>
            <th class="center b-t0 b-r0">${warehouse.message(code: 'report.pihCode.label')}</th>
            <th class="center b-t0 b-r0">${warehouse.message(code: 'report.productDescription.label')}</th>
            <th class="center b-t0 b-r0">${warehouse.message(code: 'import.unit.label')}</th>
            <th class="center b-t0 b-r0">${warehouse.message(code: 'import.maxQuantity.label')}</th>
            <th class="center b-t0 b-r0">${warehouse.message(code: 'requisition.quantityOnHand.label')}</th>
            <th class="center b-t0 b-r0">${warehouse.message(code: 'report.quantityRequested.label')}</th>
            <th class="center b-t0 b-r0 gray-background">${warehouse.message(code: 'report.quantityApproved.label')}</th>
            <th class="center b-t0 gray-background">${warehouse.message(code: 'comments.label')}</th>
        </tr>
        </thead>
        <tbody>
            <g:unless test="${requisitionItems}">
                <tr>
                    <td colspan="10" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${requisitionItems}" status="i" var="requisitionItem">
                <tr>
                    <td class="b-t0 b-r0">${requisitionItem?.product?.productCode}</td>
                    <td class="b-t0 b-r0">${requisitionItem?.product?.name}</td>
                    <td class="b-t0 b-r0">${requisitionItem?.product?.defaultUom}</td>
                    <td class="b-t0 b-r0">${requisitionItem.quantity}</td>
                    <td class="b-t0 b-r0"></td>
                    <td class="b-t0 b-r0"></td>
                    <td class="b-t0 b-r0 gray-background"></td>
                    <td class="b-t0 gray-background"></td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>
