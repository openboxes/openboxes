<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<div class="logo">
    <g:if test="${attrs.includeLink}">
        <g:if test="${session?.user?.hasHighestRoleAuthenticated()}">
            <a href="${createLink(uri: '/stockMovement/list?direction=INBOUND')}">
                <img src="${attrs?.logoUrl}" />
            </a>
        </g:if>
        <g:else>
            <a href="${createLink(uri: '/')}">
                <img src="${attrs?.logoUrl}" />
            </a>
        </g:else>
    </g:if>
    <g:else>
        <img src="${attrs?.logoUrl}" />
    </g:else>
    <g:if test="${attrs?.logoLabel && attrs.showLabel}">
        <span class="logo-label">${attrs?.logoLabel}</span>
    </g:if>
</div>
