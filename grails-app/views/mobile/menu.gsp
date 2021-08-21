<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <g:link controller="mobile" action="index" class="navbar-brand">
        <g:if test="${session.warehouse}">
            <g:displayLogo location="${session?.warehouse?.id}" includeLink="${false}"/>
        </g:if>
        <g:else>
            <a class="navbar-brand" href="#">
                <img src="https://openboxes.com/img/logo_30.png"/>
            </a>
        </g:else>
    </g:link>
</nav>
