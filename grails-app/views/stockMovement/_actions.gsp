<%@ page import="org.pih.warehouse.requisition.RequisitionSourceType; org.pih.warehouse.requisition.RequisitionStatus"%>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>

<g:if test="${stockMovement?.id }">
    <span id="stockmovement-action-menu" class="action-menu">
        <button class="action-btn ">
            <img src="${resource(dir:'images/icons/silk',file:'bullet_arrow_down.png')}" />
        </button>
        <g:set var="hasBeenReceived" value="${stockMovement?.shipment?.currentStatus >= ShipmentStatusCode.PARTIALLY_RECEIVED}"/>
        <g:set var="isPending" value="${stockMovement?.shipment?.currentStatus==ShipmentStatusCode.PENDING}"/>
        <g:set var="isSameOrigin" value="${stockMovement?.origin?.id==session.warehouse.id}"/>
        <g:set var="isElectronicType" value="${stockMovement?.requisition?.sourceType == RequisitionSourceType.ELECTRONIC}"/>
        <g:set var="disableEditButton"
               value="${hasBeenReceived || (!isSameOrigin && stockMovement?.origin?.isDepot() && isPending && !isElectronicType)}"/>
        <g:if test="${hasBeenReceived}">
            <g:set var="disabledEditMessage" value="${g.message(code:'stockMovement.cantEditReceived.message')}"/>
        </g:if>
        <g:elseif test="${!isSameOrigin && stockMovement?.origin?.isDepot() && isPending && !isElectronicType}">
            <g:set var="disabledEditMessage" value="${g.message(code:'stockMovement.isDifferentOrigin.message')}"/>
        </g:elseif>
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
                <g:link controller="stockMovement" action="show" id="${stockMovement?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />
                    &nbsp;${warehouse.message(code: 'default.show.label', args:[warehouse.message(code:'stockMovement.label')])}
                </g:link>
            </div>
            <div class="action-menu-item">
                <g:link controller="stockMovement" action="edit" id="${stockMovement?.id}"
                        disabled="${disableEditButton}"
                        disabledMessage="${disabledEditMessage}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />
                    &nbsp;${warehouse.message(code: 'default.edit.label', args:[warehouse.message(code:'stockMovement.label')])}
                </g:link>
            </div>
            <g:isUserAdmin>
                <g:if test="${(isPending || !stockMovement?.shipment?.currentStatus) && (isSameOrigin || !stockMovement?.origin?.isDepot())}">
                    <hr/>
                    <div class="action-menu-item">
                        <g:link controller="stockMovement" action="remove" id="${stockMovement?.id}"
                                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                            <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
                            &nbsp;${warehouse.message(code: 'default.delete.label', args:[warehouse.message(code:'stockMovement.label')])}
                        </g:link>
                    </div>
                </g:if>
            </g:isUserAdmin>
        </div>
    </span>
</g:if>
