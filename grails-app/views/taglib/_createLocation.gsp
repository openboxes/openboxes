<%@ page import="org.pih.warehouse.core.Location" %>
<div>
    ${warehouse.message(code: 'location.noWardOrPharmacy.message', default:'Currently, there are no wards or pharmacies associated with ' + location.name)}
    <%--
    <a href="javascript:void(-1);" class="open-dialog button">
        ${warehouse.message(code:'default.button.create.label')}
    </a>

    --%>
</div>

<%--
<div id="create-location-dialog" class="dialog-box" title="Add new location">
    <g:render template="/location/form" model="[location:new Location()]"></g:render>
</div>
<script type="text/javascript">

    $(function() {
        //$(".dialog").hide();
        $(".dialog-box").dialog({ autoOpen: false, modal: true, width: '600px' });
        $(".open-dialog").click(function() {
            $("#create-location-dialog").dialog('open');
            event.preventDefault();
        });
        $(".close-dialog").click(function() {
            $("#create-location-dialog").dialog('close');
            event.preventDefault();
        });

    });
</script>
--%>
