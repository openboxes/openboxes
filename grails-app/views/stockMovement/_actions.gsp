<%@ page import="org.pih.warehouse.requisition.RequisitionStatus"%>

<g:if test="${stockMovement?.id }">
    <span id="stockmovement-action-menu" class="action-menu">
        <button class="action-btn ">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'cog.png')}" />
        </button>
        <div class="actions" >
            <g:if test="${!request.request.requestURL.toString().contains('stockMovement/list')}">
                <div class="action-menu-item">
                    <g:link controller="stockMovement" action="list">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" style="vertical-align: middle" />
                        &nbsp;${warehouse.message(code: 'stockMovement.list.label')}
                    </g:link>
                </div>
            </g:if>
            <div class="action-menu-item">
                <g:link controller="stockMovement" action="show" id="${stockMovement?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />
                    &nbsp;${warehouse.message(code: 'stockMovement.show.label')}
                </g:link>
            </div>
            <div class="action-menu-item">
                <g:link controller="stockMovement" action="index" id="${stockMovement?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />
                    &nbsp;${warehouse.message(code: 'stockMovement.edit.label')}
                </g:link>
            </div>
        </div>
    </span>
</g:if>