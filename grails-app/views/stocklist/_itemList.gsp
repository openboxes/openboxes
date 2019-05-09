<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode"%>
<div class="page-content">
    <table id="requisition-items" class="fs-repeat-header w100">
        <thead>
        <tr>
            <th class="center b-r0">${warehouse.message(code: 'report.pihCode.label')}</th>
            <th class="center b-r0">${warehouse.message(code: 'report.productDescription.label')}</th>
            <th class="center b-r0">${warehouse.message(code: 'import.unit.label')}</th>
            <th class="center b-r0">${warehouse.message(code: 'import.maxQuantity.label')}</th>
            <th class="center b-r0">${warehouse.message(code: 'requisition.quantityOnHand.label')}</th>
            <th class="center b-r0">${warehouse.message(code: 'report.quantityRequested.label')}</th>
            <th class="center b-r0 gray-background">${warehouse.message(code: 'report.quantityApproved.label')}</th>
            <th class="center gray-background">${warehouse.message(code: 'comments.label')}</th>
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
            <g:set var="pageTitle" value="" />
            <g:set var="sortByCategory" value="${stocklist?.requisition?.sortByCode == RequisitionItemSortByCode.CATEGORY}" />
            <g:each in="${requisitionItems}" status="i" var="requisitionItem">
                <g:if test="${(!pageTitle || (pageTitle && pageTitle != requisitionItem?.product?.category)) && sortByCategory}">
                    <g:set var="pageTitle" value="${requisitionItem?.product?.category}" />
                    <tr>
                        <td colspan="8" class="gray-background b-t0">${pageTitle.encodeAsHTML()}</td>
                    </tr>
                </g:if>
                <tr>
                    <td class="b-t0 b-r0">${requisitionItem?.product?.productCode}</td>
                    <td class="b-t0 b-r0">${requisitionItem?.product?.name}</td>
                    <td class="b-t0 b-r0">${requisitionItem?.productPackage ? requisitionItem.productPackage.uom?.code + "/" + requisitionItem.productPackage.quantity + " -- " + requisitionItem.productPackage.uom?.name : 'EA/1'}</td>
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
