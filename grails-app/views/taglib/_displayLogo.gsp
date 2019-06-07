<div class="logo">
    <g:if test="${attrs.includeLink}">
        <a href="${createLink(uri: '/')}">
            <img src="${attrs?.logoUrl}" />
        </a>
    </g:if>
    <g:else>
        <img src="${attrs?.logoUrl}" />
    </g:else>
    <g:if test="${attrs?.logo?.label && attrs.showLabel}">
        ${attrs?.logo.label}
    </g:if>
</div>

