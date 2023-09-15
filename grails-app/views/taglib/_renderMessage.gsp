<g:if test="${messages}">
    <div class="${style}">
        <g:each in="${messages}" var="msg">
            <div>
                <g:if test="${msg instanceof java.lang.String}">
                    ${msg}
                </g:if>
                <g:else>
                   <g:message code="${msg?.code}" default="${msg?.default}" args="${msg?.args}" />
                </g:else>
            </div>
        </g:each>
    </div>
</g:if>
