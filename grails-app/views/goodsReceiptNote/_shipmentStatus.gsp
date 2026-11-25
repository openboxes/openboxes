<g:if test="${goodsReceiptNote.statusCode}">
    <div class="header-title">
        ${g.message(code: 'goodsReceiptNote.shipmentStatus.label')}:
        ${g.message(code: "goodsReceiptNote.shipmentStatus.${goodsReceiptNote.statusCode.name()}")}
    </div>
</g:if>
