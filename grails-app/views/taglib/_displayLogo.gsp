<g:if test="${location?.logo }">
    <a href="${createLink(uri: '/dashboard/index')}">
        <img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:location?.id)}" />
    </a>
</g:if>
<g:elseif test="${logo}">
    <a href="${createLink(uri: '/dashboard/index')}">
        <g:if test="${logo.url}">
            <img class="logo" src="${logo.url}" class="middle" />
        </g:if>
        <span class="middle">${logo.label}</span></a>
    </a>
</g:elseif>
<g:else>
    <a href="${createLink(uri: '/dashboard/index')}">
        <warehouse:message code="default.openboxes.label"/></a>
</g:else>