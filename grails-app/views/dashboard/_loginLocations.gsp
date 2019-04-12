<g:if test="${loginLocationsMap && !loginLocationsMap.isEmpty() }">
    <div style="max-height: 400px; overflow: auto">
        <table>
            <tbody>
            <g:if test="${savedLocations}">
                <tr class="prop">
                    <td>
                        <h4><g:message code="user.savedLocations.label"/></h4>
                    </td>
                    <td class="middle">
                        <g:each var="location" in="${savedLocations}">
                            <g:if test="${location}">
                                <a href='${createLink(action:"chooseLocation", id: location?.id)}' class="button big">
                                    <format:metadata obj="${location}"/>
                                </a>
                            </g:if>
                        </g:each>
                    </td>
                </tr>
            </g:if>
            <g:set var="count" value="${0 }"/>
            <g:each var="entry" in="${loginLocationsMap}" status="i">
                <tr class="prop">
                    <td class="top left" width="25%">
                        <h4 class="left">${entry.key?:warehouse.message(code:'locationGroup.empty.label') }</h4>
                    </td>
                    <td class="top left" >
                        <g:set var="locationGroup" value="${entry.key }"/>
                        <g:each var="location" in="${entry.value.sort() }" status="status">
                            <div class="left" style="margin: 1px;">
                                <a href='${createLink(action:"chooseLocation", id: location.id, params:['targetUri':params.targetUri])}' class="button big">
                                    ${location.name}
                                </a>
                            </div>
                        </g:each>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</g:if>
<g:unless test="${loginLocationsMap }">
    <div class="error center">
        <warehouse:message code="dashboard.noWarehouse.message"/>
    </div>
</g:unless>
