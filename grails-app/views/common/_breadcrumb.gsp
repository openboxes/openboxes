<ul class="breadcrumb">
    <li>
        <g:link controller="dashboard" action="index">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'house.png')}" class="home"/>
        </g:link>
    </li>
    <g:if test="${session?.user && session?.warehouse}">
        <li>
            <a href="javascript:void(0);" class="warehouse-switch">
                ${session?.warehouse?.name }
            </a>
        </li>
    </g:if>
    <g:if test="${controllerName }">
        <li>
            <g:link controller="${controllerName }" action="index">
                <warehouse:message code="${controllerName + '.label'}" />
            </g:link>
        </li>
    </g:if>
<%--
<g:if test="${actionName }">
    <li>
        <a href="">
            ${actionName.capitalize() }
        </a>
    </li>
</g:if>
--%>
    <g:if test="${g.layoutTitle() && !actionName.equals('index') && !actionName.equals('list') }">
        <li class="last">
            <a href="#">${g.layoutTitle()}</a>
        </li>
    </g:if>

</ul>
