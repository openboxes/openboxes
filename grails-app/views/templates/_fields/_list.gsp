<table>
    <tbody>
        <g:each in="${domainProperties}" var="p">
            <tr class="prop">
                <td valign="top" class="name">
                    <label id="${p.name}-label"><g:message code="${domainClass.decapitalizedName}.${p.name}.label" default="${p.defaultLabel}" /></label>
                </td>
                <td valign="top" class="value" aria-labelledby="${p.name}-label">${body(p)}</td>
            </tr>
        </g:each>
    </tbody>
</table>
