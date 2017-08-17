<div class="buttonBar">
    <button class="btnAddBinLocation button">
        <g:message code="default.add.label" args="[g.message(code:'location.binLocation.label')]"/>
    </button>
    <button class="btnImportBinLocations button">
        <g:message code="default.import.label" args="[g.message(code:'location.binLocations.label')]"/>
    </button>
</div>

<div class="box">
    <h2><warehouse:message code="binLocations.label" default="Bin Locations" /></h2>
    <div class="dialog">
        <g:if test="${binLocations}">
            <table class="dataTable">
                <thead>
                <tr class="prop">
                    <th><g:message code="location.binLocation.label" default="Bin Location"/></th>
                    <th><g:message code="location.locationType.label"/></th>
                    <th width="1%"><g:message code="warehouse.active.label" default="Active"/></th>
                    <th><g:message code="default.actions.label"></g:message></th>
                </tr>
                </thead>

                <g:each in="${binLocations}" var="binLocation" status="status">
                    <tr>
                        <td>
                            <a href="${request.contextPath}/location/edit/${binLocation?.id}" fragment="location-details-tab">
                                ${binLocation.name}
                            </a>
                        </td>
                        <td>
                            ${binLocation?.locationType?.name}
                        </td>

                        <td>
                            ${binLocation.active}
                        </td>
                        <td>
                            <a href="javascript:void(-1)" class="btnShowContents button" data-id="${binLocation?.id}" fragment="location-details-tab">
                                ${g.message(code: 'default.button.show.label')}
                            </a>

                            <a href="${request.contextPath}/location/edit/${binLocation?.id}" fragment="location-details-tab" class="button">
                                ${g.message(code: 'default.button.edit.label')}
                            </a>

                            <a href="${request.contextPath}/location/delete/${binLocation?.id}" fragment="location-details-tab" class="button">
                                ${g.message(code: 'default.button.delete.label')}
                            </a>
                        </td>
                    </tr>
                </g:each>

            </table>
        </g:if>
        <g:unless test="${binLocations}">
            <div class="empty center fade">
                <g:message code="location.noBinLocations.label" default="No bin locations"/>
            </div>
        </g:unless>
    </div>
</div>
<script>
    $(document).ready(function() {
        $('.dataTable').dataTable({
            "bJQueryUI": true,
            "sPaginationType": "full_numbers"
        });
    });
</script>