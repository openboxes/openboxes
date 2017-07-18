<g:if test="${attrs?.location?.logo }">
    <a href="${createLink(uri: '/dashboard/index')}">
        <img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:attrs?.location?.id)}" class="middle" />
    </a>
</g:if>
<g:elseif test="${attrs?.logo}">
    <a href="${createLink(uri: '/dashboard/index')}">
        <g:if test="${attrs?.logo.url}">
            <img class="logo" src="${attrs?.logo.url}" class="middle" />
        </g:if>
        <g:if test="${attrs?.logo?.label && attrs.showLabel}">
            <span class="middle">${attrs?.logo.label}</span>
        </g:if>
    </a>
</g:elseif>
<g:else>
    <a href="${createLink(uri: '/dashboard/index')}">
        <span class="middle"><warehouse:message code="default.openboxes.label"/></span></a>
</g:else>