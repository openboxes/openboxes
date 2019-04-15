<a href="${createLink(uri: '/dashboard/index')}">
    <g:if test="${attrs?.location?.logo }">
        <img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:attrs?.location?.id)}" class="middle" />
    </g:if>
    <g:elseif test="${attrs?.logo && attrs?.logo?.url}">
        <img class="logo" src="${attrs?.logo.url}" class="middle" />
    </g:elseif>
    <g:else>
        <span class="middle"><warehouse:message code="default.openboxes.label"/></span></a>
    </g:else>
</a>
<g:if test="${attrs?.logo?.label && attrs.showLabel}">
    ${attrs?.logo.label}
</g:if>