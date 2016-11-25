<div class="box">
    <h2>
        <warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/>
        <img class="spinner" id="recent-activity-spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
    </h2>

    <div class="widget-content clearfix" style="padding:0px; margin:0">
        <div id="recent-activity-details" style="max-height:250px;overflow:auto;">
            <div id="recent-activity-summary" class="prop fade" style="padding:10px">
                <!-- to be rendered by jquery -->
            </div>
            <table class="table table-striped" >
                <tbody>
                    <!-- to be rendered by jquery -->
                </tbody>
            </table>
        </div>
        <div id="recent-activity-empty" class="fade center empty">
            <warehouse:message code="dashboard.noActivityFound.message"/>
        </div>
    </div>
</div>
<script>
    $(window).load(function(){
        $.get("${request.contextPath}/dashboard/recentActivities", function(data) {
            console.log(data);
            renderRecentActivities(data);
        });
    });

    function init() {
        $("#recent-activity-summary").empty();
        $("#recent-activity-details").empty();
        $("#recent-activity-empty").hide();
    }

    function showNoActivity() {
        $("#recent-activity-empty").show();
    }

    function addActivity(recentActivity) {
        $("#recent-activity-template").tmpl(recentActivity).appendTo('#recent-activity-details table tbody');
    }

    function renderRecentActivities(data) {
        console.log(data);
        if (data.recentActivities.length == 0) {
            showNoActivity();
        }
        else {
            $("#recent-activity-empty").hide();
            $("#recent-activity-summary").html(data.message);

            $("#recent-activity-spinner").show();
            $("#recent-activity-summary").show();
            $.each(data.recentActivities, function() {
                //console.log($(this));
                var $this = $(this),
                    recentActivity = {
                        url: $(this).attr("url"),
                        label: $(this).attr("label"),
                        styleClass: $(this).attr("styleClass"),
                        activityType: $(this).attr("activityType"),
                        thumbnailUrl: $(this).attr("thumbnailUrl"),
                        date: $(this).attr("date"),
                        type: $(this).attr("type")
                    };

                //console.log(recentActivity);
                addActivity(recentActivity);
            });
            $("#recent-activity-spinner").hide();
        }
    }


</script>
<script id="recent-activity-template" type="x-jquery-tmpl">
<tr class="prop {{= styleClass}}">
    <td class="center top">
        <img src="{{= thumbnailUrl}}"/>
    </td>
    <td class="middle">
        <div>{{html label}}</div>
    </td>
    <td class="nowrap middle">
        <div class='fade'>{{= date}}</div>
    </td>
</tr>
</script>

