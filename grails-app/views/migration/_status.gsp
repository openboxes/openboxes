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
<g:hasErrors bean="${command}">
    <div class="errors">
        <g:renderErrors bean="${command}" as="list" />
    </div>
</g:hasErrors>