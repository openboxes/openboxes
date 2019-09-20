<form role="form" controller="inventoryBrowser" action="list">

    <div class="form-group">
        <label><i class="glyphicon glyphicon-tag"></i> <warehouse:message code="product.status.label"/></label>
        <span id="status-spinner" class="spinner"><i class="icon-spin icon-refresh"></i></span>
    </div>
    <div class="form-group">

        <ul class="list-group">
            <g:each in="${['ALL','OVERSTOCK','IN_STOCK','IDEAL_STOCK','REORDER','LOW_STOCK','STOCK_OUT','NOT_STOCKED','INVALID']}" var="status" status="i">
                <li class="list-group-item justify-content-between">
                        <label for="status-${i}" title="${status}">
                            <g:checkBox id="status-${i}" name="status[]" value="${status}" checked="${false}" class="status-filter"/>
                            <span class=""><warehouse:message code="enum.InventoryLevelStatus.${status}"/></span>
                        </label>
                        <span class="badge badge-default badge-pill" id="badge-status-${status}"></span>
                </li>
            </g:each>
        </ul>
    </div>
    <button id="refresh-btn" class="btn btn-primary">Refresh</button>
    <button id="cancel-btn" class="btn btn-default">Cancel</button>
</form>
