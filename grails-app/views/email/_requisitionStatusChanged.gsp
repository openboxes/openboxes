<p>
${warehouse.message(code: 'email.requisitionStatusChanged.message', args: [requisition.requestNumber, requisition?.status])}
</p>
<p>
<g:link controller="mobile" action="outboundDetails" id="${requisition?.id}" absolute="${true}">Click for more details</g:link>
</p>

<g:if test="${requisition.status >= org.pih.warehouse.requisition.RequisitionStatus.PICKED}">
<div class="box">
    <h2>
        <g:message code="shipping.summary.label" args="[requisition?.requestNumber]"/>
    </h2>
    <table>
        <tr class="prop">
            <th class="name">
            </th>
            <th class="center">
                Estimated
            </th>
            <th class="center">
                Actual
            </th>
        </tr>
        <tr class="prop">
            <td class="name">
                Items
            </td>
            <td class="center">
                ${requisition?.requisitionItems?.size()?:0}
            </td>
            <td class="center">
                ${requisition?.shipment?.shipmentItems?.size()?:0}
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                Containers
            </td>
            <td class="center">
                Not available
            </td>
            <td class="center">
                ${requisition?.shipment?.containers?.size()?:0}
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                Volume
            </td>
            <td class="center">
                <g:set var="estimatedVolume" value="${requisition?.totalVolume}"/>
                <g:formatNumber number="${estimatedVolume?.value?:0}"/>
                ${estimatedVolume?.unitOfMeasure}
            </td>
            <td class="center">
                <g:set var="actualVolume" value="${requisition?.shipment?.totalVolume}"/>
                <g:formatNumber number="${actualVolume?.value?:0}"/>
                ${actualVolume?.unitOfMeasure}
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                Weight
            </td>
            <td class="center">
                <g:set var="estimatedWeight" value="${requisition?.totalWeight}"/>
                <g:formatNumber number="${estimatedWeight?.value?:0}"/>
                ${estimatedWeight?.unitOfMeasure}
            </td>
            <td class="center">
                <g:set var="actualWeight" value="${requisition?.shipment?.totalWeight}"/>
                <g:formatNumber number="${actualWeight?.value?:0}"/>
                ${actualWeight?.unitOfMeasure}
            </td>
        </tr>
    </table>
</div>
</g:if>
