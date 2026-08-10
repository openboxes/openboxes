<nav class="navbar navbar-light bg-light">
    <g:if test="${session.warehouse}">
        <g:displayLogo location="${session?.warehouse?.id}" includeLink="${true}"/>
    </g:if>
    <g:else>
        %{-- Local asset: the logged-out navbar must not depend on the
             marketing site being reachable. Fixed height keeps the un-themed
             layout from rendering the image at natural size. --}%
        <a class="navbar-brand" href="#">
            <img src="${resource(dir: 'images/logo', file: 'logo-blue.png')}" alt="OpenBoxes" height="30"/>
        </a>
    </g:else>
</nav>
