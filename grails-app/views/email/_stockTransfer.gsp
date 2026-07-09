<%@ page contentType="text/html"%>
<g:applyLayout name="email">

    <div class="box">
        <h2>${warehouse.message(code:'email.stockTransfer.message.title', args: [uniqueBinNames?.join(', ')])}</h2>
        <div style="margin: 10px;">

            <table class="details">
                <thead>
                    <tr>
                        <th>
                            <g:message code="email.stockTransfer.message.transferNumber.label"/>
                        </th>
                        <th>
                            <g:message code="email.stockTransfer.message.originFacility.label"/>
                        </th>
                        <th>
                            <g:message code="email.stockTransfer.message.destinationFacility.label"/>
                        </th>
                        <th>
                            <g:message code="email.stockTransfer.message.transferedBy.label"/>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${stockTransferItems}" var="row">
                        <tr>
                            <td class="value">
                                ${stockTransfer?.stockTransferNumber}
                                <g:link controller="stockTransfer" action="show" id="${stockTransfer?.id }" absolute="true">
                                    ${warehouse.message(code: 'email.link.label', args: [stockTransfer?.stockTransferNumber])}
                                </g:link>
                            </td>
                            <td class="value">
                                <g:if test="${row?.originBinLocation?.supports('ENABLE_STOCK_TRANSFER_NOTIFICATIONS')}">
                                    <b>${row?.originBinLocation?.name}</b>
                                </g:if>
                                <g:else>
                                    ${row?.originBinLocation?.name}
                                </g:else>
                            </td>
                            <td class="value">
                                <g:if test="${row?.destinationBinLocation?.supports('ENABLE_STOCK_TRANSFER_NOTIFICATIONS')}">
                                    <b>${row?.destinationBinLocation?.name}</b>
                                </g:if>
                                <g:else>
                                    ${row?.destinationBinLocation?.name}
                                </g:else>
                            </td>
                            <td class="value">
                                ${stockTransfer?.orderedBy?.name}
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </div>

</g:applyLayout>
