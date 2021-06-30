<nav class="navbar navbar-light bg-light">
    <g:if test="${session.warehouse}">
        <g:displayLogo location="${session?.warehouse?.id}" includeLink="${true}"/>
    </g:if>
    <g:else>
        <a class="navbar-brand" href="#">
            <img src="https://openboxes.com/img/logo_30.png"/>
        </a>
    </g:else>
</nav>
