<g:if test="${attrs.date}">
    <div>
        <format:date obj="${attrs.date}"  format="MMM dd, yyyy"/>
        <g:if test="${new Date().after(attrs.date)}">
            <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${g.message(code:'default.expired.label')}: ${prettytime.display(date: attrs.date)}"/>
        </g:if>
        <g:elseif test="${(new Date()+90).after(attrs.date)}">
            <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${g.message(code:'default.expiring.label')}: ${prettytime.display(date: attrs.date)}"/>
        </g:elseif>

    </div>
</g:if>
<g:else>
    <span class="fade"><warehouse:message code="default.never.label"/></span>
</g:else>
