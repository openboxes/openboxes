<g:set var="tableId" value="${UUID.randomUUID()}"/>
<div class="box">
    <g:if test="${!data}">
        <div class="center fade padded">
            No data
        </div>
    </g:if>
    <g:if test="${data}">
        <table id="${tableId}" class="dataTable">
            <thead>
                <tr>
                    <g:each var="entry" in="${data[0].keySet()}">
                        <td>${entry}</td>
                    </g:each>
                </tr>
            </thead>
            <tbody>
                <g:each var="row" in="${data}">
                    <tr>
                        <g:each var="entry" in="${row}">
                            <td>${entry.value}</td>
                        </g:each>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
</div>
<g:javascript>
  $(document).ready(function() {
    $("#${tableId}").dataTable({
                "iDisplayLength": 10,
                "bSearch": true,
                "bJQueryUI": true,
                "bAutoWidth": true,
                "sPaginationType": "full_numbers"
    });
  });
</g:javascript>
