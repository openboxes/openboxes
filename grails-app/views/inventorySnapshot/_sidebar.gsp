<form role="form" controlle="inventorySnapshot" action="index" class="well form-search">

    <%--
    <div class="form-group">
        <label><i class="glyphicon glyphicon-tag"></i> <warehouse:message code="product.status.label"/></label>
    </div>
    <g:each in="${['OVERSTOCK','IN_STOCK','IDEAL_STOCK','REORDER','LOW_STOCK','STOCK_OUT','NOT_STOCKED','INVALID']}" var="status" status="i">
        <div class="checkbox">
            <label for="status-${i}" title="${status}">
                <g:checkBox id="status-${i}" name="status" value="${status}" checked="${false}" class="status-filter"/>
                <span class=""><warehouse:message code="enum.InventoryLevelStatus.${status}"/></span>
                <span class="badge" id="badge-status-${status}"></span>
                <span class="" id="badge-percentage-${status}"></span>
            </label>
        </div>

    </g:each>
    <div class="form-group">
        <label><i class="icon icon-calendar"></i> <warehouse:message code="default.startDate.label" default="Start date"/></label>
        <div>
            <input id="startDate" name="startDate" class="datepicker"/>
        </div>
    </div>

    <div class="form-group">
        <label><i class="icon icon-calendar"></i> <warehouse:message code="default.startDate.label" default="End date"/></label>
        <div>
            <input id="endDate" name="endDate" class="datepicker"/>
        </div>
    </div>
    --%>
    <div class="form-group">
        <label><i class="icon icon-calendar"></i> <warehouse:message code="default.date.label" default="Date"/></label>
        <div>
            <input id="date" name="date" class="datepicker" value=""/>
        </div>
    </div>

    <%--

    <div class="form-group">
        <label><i class="icon icon-map-marker"></i> <warehouse:message code="default.location.label" default="Location"/></label>
        <g:selectDepot name="location.id" value="${params?.location?.id}" data-placeholder="Select location"/>

    </div>
    --%>
    <hr/>
    <div class="form-actions">
        <button id="refresh-btn" class="btn btn-primary">Refresh</button>
        <button id="do-btn" class="btn btn-default">Do Something</button>
        <%--
        <button id="submit-btm" class="btn btn-default" target="_blank">Export</button>
        --%>
    </div>
</form>