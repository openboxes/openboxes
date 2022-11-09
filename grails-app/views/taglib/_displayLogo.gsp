<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<div id="logo-wrapper" class="d-flex align-items-center gap-8">
    <g:if test="${attrs.includeLink}">
        <g:hasHighestRoleAuthenticated>
            <div id="logo-square">
                <a href="${createLink(uri: '/stockMovement/list?direction=INBOUND')}">
                    <img src="${attrs?.logoUrl}" alt="Openboxes" width="40" height="40" />
                </a>
            </div>
        </g:hasHighestRoleAuthenticated>
        <g:hasHigherRoleThanAuthenticated>
            <div id="logo-square">
                <a href="${createLink(uri: '/')}">
                    <img src="${attrs?.logoUrl}" alt="Openboxes" width="40" height="40" />
                </a>
            </div>
        </g:hasHigherRoleThanAuthenticated>
    </g:if>
    <g:else>
        <div id="logo-square">
            <img src="${attrs?.logoUrl}" alt="Openboxes" width="40" height="40" />
        </div>
    </g:else>
</div>
