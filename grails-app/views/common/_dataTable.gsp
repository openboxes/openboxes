<table class="dataTable">
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
    <tfoot>

    </tfoot>
</table>
<g:javascript>
  $(document).ready(function() {
        $(".dataTable").dataTable();
    });
</g:javascript>
