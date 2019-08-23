<form role="form" controlle="inventorySnapshot" action="index" class="form-search">
    <input type="hidden" id="userid" name="user.id" value="${session.user.id}"/>
    <div class="form-group">
        <label><i class="icon icon-map-marker"></i> <warehouse:message code="default.location.label" default="Location"/></label>
        <div>
            ${session?.warehouse?.name}
            <input id="locationid" name="location.id" type="hidden" value="${session.warehouse.id}"/>
        </div>
    </div>
    <div class="form-group">
        <label><i class="icon icon-calendar"></i> <warehouse:message code="default.date.label" default="Date"/></label>
        <div>
            <input id="date" name="date" class="datepicker" value=""/>
        </div>
    </div>
    <hr/>
    <div class="form-actions">
        <button id="refresh-btn" class="btn btn-primary">Reload</button>
    </div>
</form>

<hr/>
<h2>Re-indexing</h2>
<p>If data is stale or does not exist, you can run a background process that re-indexes the quantity on hand values for the current location and selected date.</p>
<div class="form-group">
    <label><i class="icon icon-map-marker"></i> <warehouse:message code="default.location.label" default="Location"/></label>
    <div>

        <div class="btn-group" data-toggle="buttons">
            <label class="btn btn-primary">
                <input type="radio" name="location" id="allLocations" autocomplete="off"> All locations
            </label>
            <label class="btn btn-primary active">
                <input type="radio" name="location" id="currentLocation" autocomplete="off" checked> ${session?.warehouse?.name}
            </label>
        </div>
    </div>
</div>
<div class="form-group">
    <a id="trigger-button" href="#" data-link="${g.createLink(controller:'inventorySnapshot', action:'refresh')}" class="btn btn-default">Run Background Re-indexer</a>
</div>
