<div class="logo">
    <g:if test="${attrs.includeLink}">
        <a href="${createLink(uri: '/')}">
            <img src="${attrs?.logo?.url}" width="${attrs.logo.width}" height="${attrs.logo.height}"/>
        </a>
    </g:if>
    <g:else>
        <img src="${attrs?.logo?.url}" width="${attrs.logo.width}" height="${attrs.logo.height}"/>
    </g:else>
    <g:if test="${attrs?.logo?.label && attrs.showLabel}">
        ${attrs?.logo.label}
    </g:if>
</div>

