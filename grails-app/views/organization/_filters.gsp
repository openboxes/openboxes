<%@ page import="org.pih.warehouse.core.ActivityCode" %>
<section class="box" aria-label="filters">
    <h2><warehouse:message code="default.filters.label"/></h2>
    <g:form id="searchForm" action="search" method="GET">
        <g:hiddenField name="type" value="${params.type}"/>
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="filter-list">
            <div class="filter-list-item">
                <label for="q">${warehouse.message(code: 'default.search.label')}</label>
                <g:textField class="text" id="q" name="q" value="${params.q}" style="width:100%"/>
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'role.roleType.label')}</label>
                <div data-testid="role-type-select" >
                    <g:select class="chzn-select-deselect" multiple="true"
                              name="roleType"
                              from="${org.pih.warehouse.core.RoleType?.listOrganizationRoleTypes()}"
                              value="${params?.roleType}"  />
                </div>
            </div>
            <div class="buttons center">
                <button type="submit" class="button" name="search" value="true">
                    <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />&nbsp;
                    <warehouse:message code="default.search.label"/>
                </button>
                <g:link controller="organization" action="download" params="${params}" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                    <warehouse:message code="default.button.download.label" default="Download"/>
                </g:link>
            </div>
        </div>
    </g:form>
</section>
