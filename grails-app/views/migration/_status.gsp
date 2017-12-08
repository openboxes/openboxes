<g:if test="${message}">
    <div id="message" class="message">
        ${message}
    </div>
</g:if>
<g:if test="${errors}">
    <div id="error" class="error">
        ${errors}
    </div>
</g:if>
<g:hasErrors bean="${target}">
    <div class="errors">
        <g:renderErrors bean="${target}" as="list" />
    </div>
</g:hasErrors>