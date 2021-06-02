
<div class="buttonBar">
    <div class="button-container">
        <button id="btnAddZoneLocation" class="button">
            <g:message code="default.add.label" args="[g.message(code:'location.zoneLocations.label')]"/>
        </button>
    </div>
</div>

<div class="box">
    <h2><warehouse:message code="location.zoneLocations.label" default="Zone Locations" /></h2>
    <div class="dialog">
        <g:if test="${zoneLocations}">
            <table id="zoneLocationsTable" class="dataTable">
                <thead>
                <tr class="prop">
                    <th width="1%"><g:message code="warehouse.active.label" default="Active"/></th>
                    <th><g:message code="location.zoneLocations.label" default="Zone Location"/></th>
                    <th><g:message code="location.locationType.label"/></th>
                    <th><g:message code="default.actions.label"></g:message></th>
                </tr>
                </thead>

                <g:each in="${zoneLocations}" var="zoneLocation" status="status">
                    <tr>
                        <td>
                            <g:if test="${zoneLocation.active}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />
                            </g:if>
                            <g:else>
                                <img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}" />
                            </g:else>
                        </td>
                        <td>
                            <a href="${request.contextPath}/location/edit/${zoneLocation?.id}" fragment="location-details-tab">
                                ${zoneLocation.name}
                            </a>
                        </td>
                        <td>
                            ${zoneLocation?.locationType?.name}
                        </td>

                        <td>
                            <a href="${request.contextPath}/location/edit/${zoneLocation?.id}" fragment="location-details-tab" class="button">
                                ${g.message(code: 'default.button.edit.label')}
                            </a>

                            <a href="${request.contextPath}/location/delete/${zoneLocation?.id}" fragment="location-details-tab" class="button">
                                ${g.message(code: 'default.button.delete.label')}
                            </a>
                        </td>
                    </tr>
                </g:each>

            </table>
        </g:if>
        <g:unless test="${zoneLocations}">
            <div class="empty center fade">
                <g:message code="location.noZoneLocations.label" default="No zone locations"/>
            </div>
        </g:unless>
    </div>
</div>
<script>
    $(document).ready(function() {
        $('#zoneLocationsTable').dataTable({
            "bJQueryUI": true,
            "bDestroy": true,
            "sPaginationType": "full_numbers"
        });
    });
</script>
