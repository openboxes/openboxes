<div class="logo">
    <g:if test="${attrs.includeLink}">
        <a href="${createLink(uri: '/')}">
            <img src="${attrs?.logo?.url}" />
        </a>
    </g:if>
    <g:else>
        <img src="${attrs?.logo?.url}" />
    </g:else>
    <g:if test="${attrs?.logo?.label && attrs.showLabel}">
        ${attrs?.logo.label}
    </g:if>
</div>

