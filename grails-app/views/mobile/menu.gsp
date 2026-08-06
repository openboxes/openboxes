<nav class="navbar navbar-light bg-light">
    <g:if test="${session.warehouse}">
        <g:displayLogo location="${session?.warehouse?.id}" includeLink="${true}"/>
    </g:if>
    <g:else>
        %{-- Was https://openboxes.com/img/logo_30.png, which now 404s — and
             this branch is what the logged-out pages render, so the login
             screen led with a broken image. Serve the same local asset the
             desktop login uses instead of depending on the marketing site. --}%
        <a class="navbar-brand" href="#">
            %{-- height matches the 30px logo this replaces, so the un-themed
                 layout doesn't render the asset at natural size --}%
            <img src="${resource(dir: 'images/logo', file: 'logo-blue.png')}" alt="OpenBoxes" height="30"/>
        </a>
    </g:else>
</nav>
