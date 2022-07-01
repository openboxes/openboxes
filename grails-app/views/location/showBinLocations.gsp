<div class="buttonBar">
    <div class="button-container">
        <button id="btnAddBinLocation" class="button">
            <g:message code="default.add.label" args="[g.message(code:'location.internalLocation.label', default: 'Internal Location')]"/>
        </button>
        <button id="btnImportBinLocations" class="button">
            <g:message code="default.import.label" args="[g.message(code:'location.internalLocations.label', default: 'Internal Locations')]"/>
        </button>
        <button id="btnExportBinLocations" class="button" data-href="${g.createLink(controller: 'location', action: 'exportBinLocations', id: params.id)}">
            <g:message code="default.export.label" args="[g.message(code:'location.internalLocations.label', default: 'Internal Location')]"/>
        </button>
    </div>
</div>

<div class="box">
    <h2><warehouse:message code="locations.internalLocations.label" default="Internal Locations" /></h2>
    <div class="dialog">

        <table id="internalLocationsTable" class="dataTable">
            <thead>
                <tr>
                    <th>Active</th>
                    <th>Location Name</th>
                    <th>Location Number</th>
                    <th>Location Type</th>
                    <th>Location Type Code</th>
                    <th>Zone</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
</div>

<script id="rowTemplate" type="text/x-jquery-tmpl">
    <tr>
        <td>
            {{if active }}
            <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />
            {{else}}
            <img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}" />
            {{/if}}
        </td>
        <td>
            <a href="${request.contextPath}/location/edit/{{= id }}" fragment="location-details-tab">
                {{= name }}
            </a>
        </td>
        <td>
            {{= locationNumber }}
        </td>
        <td>
            {{= locationType }}
        </td>
        <td>
            {{= locationTypeCode }}
        </td>
        <td>
            {{= zoneName}}
        </td>
        <td>
            <a href="javascript:void(-1)" class="btnShowContents button" data-id="{{= id }}" fragment="location-details-tab">
                ${g.message(code: 'default.button.show.label')}
            </a>

            <a href="${request.contextPath}/location/edit/{{= id }}" fragment="location-details-tab" class="button">
                ${g.message(code: 'default.button.edit.label')}
            </a>

            <a href="${request.contextPath}/location/delete/{{= id }}" fragment="location-details-tab" class="button">
                ${g.message(code: 'default.button.delete.label')}
            </a>
        </td>
    </tr>

</script>

<script>
    $(document).ready(function() {
      $('#internalLocationsTable').dataTable({
        "bProcessing": true,
        "sServerMethod": "GET",
        "iDisplayLength": 100,
        "bSort": false,
        "bSearch": false,
        "bScrollInfinite": true,
        "bScrollCollapse": true,
        "sScrollY": 300,
        "bJQueryUI": true,
        "bAutoWidth": true,
        "sAjaxSource": "${request.contextPath}/api/internalLocations",
        "sAjaxDataProp": "data",
        "fnServerParams": function (data) {
          data.push({ name: "includeInactive", value: "true"})
          data.push({ name: "location.id", value: "${params.id}"})
        },
        "fnServerData": function (url, params, callback) {
          $.ajax({
            "dataType": 'json',
            "type": "GET",
            "url": url,
            "data": params,
            "success": callback,
            "timeout": 15000,   // optional if you want to handle timeouts (which you should)
            "error": handleAjaxError // this sets up jQuery to give me errors
          });
        },
        "oLanguage": {
          "sZeroRecords": "No records found",
          "sProcessing": "<span style='z-index: 999'>Loading ... <img alt='spinner' src='${request.contextPath}/images/spinner.gif' /></span>"
        },
        // rendered via template in fnCallback
        "aoColumns": [
          { "mDataProp": "id", "bSortable": false },
          { "mDataProp": "name", "bSortable": true },
          { "mDataProp": "locationNumber", "bSortable": true },
          { "mDataProp": "locationType", "bSortable": true },
          { "mDataProp": "locationTypeCode", "bSortable": true },
          { "mDataProp": "zoneName", "bSortable": true },
          { "mDataProp": "id", "bSortable": false, "sWidth": '25%' }
        ],
        "bUseRendered": false,
        "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
            var content = $("#rowTemplate").tmpl(aData).html();
            $(nRow).html(content);
        }


      });
    });


    function handleAjaxError( xhr, status, error ) {
        if ( status === 'timeout' ) {
            alert( 'The server took too long to send the data.' );
        }
        else {
            // User probably refreshed page or clicked on a link, so this isn't really an error
            if(xhr.readyState == 0 || xhr.status == 0) {
                return;
            }

            var errorMessage = "<p class='error'>An unexpected error has occurred on the server.  Please contact your system administrator.</p>";

            if (xhr.responseText) {
                var error = JSON.parse(xhr.responseText);
                errorMessage = errorMessage += "<code>" + error.errorMessage + "</code>"
            }
        }
    }

</script>
