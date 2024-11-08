<g:set var="productSourceName" value="${productSupplier?.name ? ' (source: ' + productSupplier?.name + ')' : ''}"/>
<g:set var="displayNameLabel" value="${displayName ? product?.name : ''}"/>

<span title="${displayNameLabel + productSourceName}">
    <g:if test="${showProductCode}">
        ${product?.productCode}
    </g:if>
    ${displayName ?: product?.name}
</span>
