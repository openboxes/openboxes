<%@ page import="org.pih.warehouse.requisition.RequisitionSourceType; org.pih.warehouse.shipping.ShipmentStatusCode" %>
<%@ page import="org.pih.warehouse.api.StockMovementDirection" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<g:if test="${stockMovement?.id }">
    <span id="stockmovement-action-menu" class="action-menu">
        <button class="action-btn ">
            <img src="${resource(dir:'images/icons/silk',file:'bullet_arrow_down.png')}" />
        </button>
        <g:set var="isSameOrigin" value="${stockMovement?.origin?.id==session.warehouse.id}"/>
        <g:set var="isSameDestination" value="${stockMovement?.destination?.id==session.warehouse.id}"/>
        <div class="actions" >
            <g:if test="${!request.request.requestURL.toString().contains('stockMovement/list')}">
                <div class="action-menu-item">
                    <g:link controller="stockMovement" action="list">
                        <img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" style="vertical-align: middle" />
                        &nbsp;${warehouse.message(code: 'stockMovement.list.label', args:[warehouse.message(code:'stockMovement.label')])}
                    </g:link>
                </div>
            </g:if>
            <div class="action-menu-item">
                <g:link
                    controller="stockMovement"
                    action="show"
                    id="${stockMovement?.order?.id ?: stockMovement?.id}"
                >
                    <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />
                    &nbsp;${warehouse.message(code: 'default.show.label', args:[warehouse.message(code:'stockMovement.label')])}
                </g:link>
            </div>
            <div class="action-menu-item">
                <g:if test="${stockMovement?.order}">
                    <g:link controller="stockTransfer" action="edit" id="${stockMovement?.order?.id}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />
                        &nbsp;${warehouse.message(code: 'default.edit.label', args:[warehouse.message(code:'stockMovement.label')])}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="stockMovement" action="edit" id="${stockMovement?.id}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />
                        &nbsp;${warehouse.message(code: 'default.edit.label', args:[warehouse.message(code:'stockMovement.label')])}
                    </g:link>
                </g:else>
            </div>
            <g:set var="isPending" value="${stockMovement?.isPending() || !stockMovement?.shipment?.currentStatus}" />
            <g:set var="originIsDepot" value="${stockMovement?.origin?.isDepot()}" />
            <g:if test="${isPending && (isSameOrigin || !originIsDepot) && !stockMovement?.electronicType}">
                <hr/>
                <div class="action-menu-item">
                    <g:if test="${stockMovement?.order}">
                        <g:isUserAdmin>
                            <g:link class="button" controller="stockTransfer" action="remove" id="${stockMovement?.id}" params="[orderId: stockMovement?.order?.id]"
                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
                                &nbsp;${warehouse.message(code: 'default.delete.label', args:[warehouse.message(code:'stockMovement.label')])}
                            </g:link>
                        </g:isUserAdmin>
                    </g:if>
                    <g:else>
                        <g:link controller="stockMovement" action="remove" id="${stockMovement?.id}"
                                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                disabledMessage="You do not have minimum required role to delete stock movement"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
                            &nbsp;${warehouse.message(code: 'default.delete.label', args:[warehouse.message(code:'stockMovement.label')])}
                        </g:link>
                    </g:else>
                </div>
            </g:if>
            <g:if test="${isPending && (isSameOrigin || isSameDestination || !originIsDepot) && stockMovement?.electronicType}">
                <hr/>
                <div class="action-menu-item">
                    <g:link controller="stockRequest" action="remove" id="${stockMovement?.id}"
                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                            disabledMessage="You do not have minimum required role to delete stock request"
                    >
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
                        &nbsp;${warehouse.message(code: 'default.delete.label', args:[warehouse.message(code:'stockMovement.label')])}
                    </g:link>
                </div>
            </g:if>
        </div>
    </span>
</g:if>
