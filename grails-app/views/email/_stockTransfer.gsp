<%@ page contentType="text/html"%>
<g:applyLayout name="email">

    <div class="box">
        <h2>${warehouse.message(code:'default.summary.label', default:'Summary') }</h2>
        <div style="margin: 10px;">

            <g:if test="${stockTransferItem?.destinationBinLocation?.supports('ENABLE_STOCK_TRANSFER_NOTIFICATIONS')}">
                ${warehouse.message(code: 'email.stockTransfer.message.toLocation', args: [stockTransferItem?.destinationBinLocation?.name])}
            </g:if>
            <g:else>
                ${warehouse.message(code: 'email.stockTransfer.message.fromLocation', args: [stockTransferItem?.originBinLocation?.name])}
            </g:else>
            <g:if test="${stockTransferItem?.reasonCode}">
                <span>, &quot;</span>
                ${warehouse.message(code: 'enum.ReasonCode.' + stockTransferItem?.reasonCode?.name())}
                <span>&quot;</span>
            </g:if>
            <g:link controller="stockTransfer" action="show" id="${stockTransfer?.id }" absolute="true">
                ${warehouse.message(code: 'email.link.label', args: [stockTransfer?.stockTransferNumber])}
            </g:link>

        </div>
    </div>

</g:applyLayout>
