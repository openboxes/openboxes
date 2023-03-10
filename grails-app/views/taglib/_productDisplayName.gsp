<span title="${displayName ? product?.name : null}">
    <g:if test="${showProductCode}">
        ${product?.productCode}
    </g:if>
    ${displayName ?: product?.name}
</span>
