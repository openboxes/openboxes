<div class="box">
    <h2>
        <warehouse:message code="inventory.expiring.label" default="Expiration Summary"/>
        <img id="expiration-summary-spinner" class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
    </h2>
    <div class="widget-content" style="padding:0; margin:0">
        <div id="expiration-summary-details">
            <table id="expiration-summary-table" class="table table-striped">
                <thead>
                <tr>
                    <th></th>
                    <th>Status</th>
                    <th class="right" width="10%">Count</th>
                    <th class="right" width="10%">Amount (USD)</th>
                </tr>
                </thead>
                <tbody>

                </tbody>
            </table>
        </div>
    </div>
</div>
<script>
    $(document).ready(function(){
        $.ajax({
            dataType: "json",
            url: "${request.contextPath}/dashboard/expirationSummary?location.id=${session.warehouse.id}",
            //data: data,
            success: function (data) {
                console.log("expiration summary: ", data);
                if (!data.error) {
                    renderExpirationSummary(data);
                }
                else {
                    renderExpirationSummaryError(data);
                }
            },
            error: function(xhr, status, error) {
                console.log("xhr: ", xhr);
                console.log("status: ", status);
                console.log("error: " , error);
                //$("#expiration-summary-details").html("There was an error.")
                var data = { message: message };
                renderExpirationSummaryError(data);
                // Expiration
            }
        });
    });

    function renderExpirationSummary(data) {
        console.log(data);
        if (data.length == 0) {
            //showNoActivity();
        }
        else {
            $("#expiration-summary-spinner").show();

            $.each(data, function () {
                var expirationSummary = {
                    label: $(this).attr("label"),
                    code: $(this).attr("code"),
                    status: $(this).attr("status"),
                    subStatus: $(this).attr("subStatus"),
                    count: $(this).attr("count"),
                    cost: $(this).attr("cost"),
                    styleClass: $(this).attr("styleClass"),
                    url: $(this).attr("url")
                };
                addExpirationSummary(expirationSummary);
            });

            $("#expiration-summary-spinner").hide();
        }
    }

    function renderExpirationSummaryError(data) {
        $("#expiration-summary-error-template").tmpl(data).appendTo('#expiration-summary-table tbody');
        $("#expiration-summary-spinner").hide();
    }

    function addExpirationSummary(data) {
        console.log($("#expiration-summary-table"))
        $("#expiration-summary-template").tmpl(data).appendTo('#expiration-summary-table tbody');
    }


</script>

<script id="expiration-summary-template" type="x-jquery-tmpl">
    <tr class="prop {{= styleClass}} {{= code}} {{= status}}">
        <td class="center" style="width: 1%">
            <img src="{{= imgUrl}}" class="middle" title=""{{= title}}" />
        </td>
        <td>
            <a href="{{= url}}">{{= label}}</a>
        </td>
        <td class="right">
            <a href="{{= url}}">{{= count}}</a>
        </td>
        <td class="right">
            <a href="{{= url}}">{{= cost}}</a>
        </td>
    </tr>
</script>

<script id="expiration-summary-error-template" type="x-jquery-tmpl">
    <tr class="prop error">
        <td colspan="4">
            {{= message}}
        </td>
    </tr>
</script>

