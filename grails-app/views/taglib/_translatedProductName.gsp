<span title="${product?.translatedName ? product?.name : null}">
    <g:if test="${showProductCode}">
        ${product?.productCode}
    </g:if>
    ${product?.translatedName ?: product?.name}
</span>
