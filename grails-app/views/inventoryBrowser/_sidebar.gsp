<form role="form" controller="inventoryBrowser" action="list">

    <div class="form-group">
        <label><i class="glyphicon glyphicon-tag"></i> <warehouse:message code="product.status.label"/></label>
        <span id="status-spinner" class="spinner"><i class="icon-spin icon-refresh"></i></span>
    </div>
    <div class="form-group">
        <g:each in="${['ALL','OVERSTOCK','IN_STOCK','IDEAL_STOCK','REORDER','LOW_STOCK','STOCK_OUT','NOT_STOCKED','INVALID']}" var="status" status="i">
            <div class="checkbox">
                <label for="status-${i}" title="${status}">
                    <span class=""><warehouse:message code="enum.InventoryLevelStatus.${status}"/></span>
                </label>
                <g:checkBox id="status-${i}" name="status[]" value="${status}" checked="${false}" class="status-filter"/>
                <span class="badge" id="badge-status-${status}"></span>
                <span class="" id="badge-percentage-${status}"></span>
            </div>
        </g:each>
    </div>



    <button id="refresh-btn" class="btn btn-primary">Refresh</button>
    <button id="cancel-btn" class="btn btn-default">Cancel</button>
    <%--
    <button id="export-btn" class="btn btn-default" target="_blank">Export</button>
    --%>
</form>
