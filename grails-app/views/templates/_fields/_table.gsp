<table>
    <thead>
        <tr>
            <g:each in="${domainProperties}" var="p">
                <g:sortableColumn property="${p.property}" title="${p.label}" params="${params}" />
            </g:each>
        </tr>
    </thead>
    <tbody>
        <g:each in="${collection}" var="bean" status="i">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <g:each in="${domainProperties}" var="p" status="j">
                    <td>
                        <g:if test="${j == 0}">
                            <g:link method="GET" resource="${bean}"><f:display bean="${bean}" property="${p.property}" displayStyle="${displayStyle ?: 'table'}" theme="${theme}"/></g:link>
                        </g:if>
                        <g:else>
                            <f:display bean="${bean}" property="${p.property}" displayStyle="${displayStyle ?: 'table'}" theme="${theme}"/>
                        </g:else>
                    </td>
                </g:each>
            </tr>
        </g:each>
    </tbody>
</table>
