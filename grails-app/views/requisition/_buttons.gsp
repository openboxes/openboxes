<g:if test="${requisition?.id }">		
    <div class="buttons center">
        <g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
            <div class="button-group">
                <g:link class="button" controller="requisition" action="list">
                    <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="Back to requisitions" style="vertical-align: middle" />
                    &nbsp;${warehouse.message(code: 'requisition.back.label', default: 'Back')}
                </g:link>
            </div>
        </g:if>

        <div class="button-group">
            <g:link class="button ${'show'.equals(actionName)?'active':'' }" controller="requisition" action="show" id="${requisition?.id}">
                <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
                1.&nbsp;${warehouse.message(code: 'default.button.show.label', default: 'View')}
            </g:link>
            <g:link class="button ${('editHeader'.equals(actionName)||'edit'.equals(actionName))?'active':'' }" controller="requisition" action="edit" id="${requisition?.id}">
                <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
                2.&nbsp;${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}
            </g:link>
            <g:link class="button ${'change'.equals(actionName)?'active':''||'review'.equals(actionName)?'active':'' }" controller="requisition" action="review" id="${requisition?.id}">
                <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" />
                3.&nbsp;${warehouse.message(code: 'default.button.review.label', default: 'Review')}
            </g:link>
            <g:if test="${session?.warehouse?.id == requisition?.origin?.id }">
                <g:link class="button ${'pick'.equals(actionName)?'active':'' }" controller="requisition" action="pick" id="${requisition?.id}" name="processRequisition">
                    <img src="${resource(dir: 'images/icons/silk', file: 'reload.png')}" />
                    4.&nbsp;${warehouse.message(code: 'default.button.pick.label', default: 'Pick')}
                </g:link>
                <g:link class="button ${'confirm'.equals(actionName)?'active':'' }" controller="requisition" action="confirm" id="${requisition?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'check_error.png')}" />
                    5.&nbsp;${warehouse.message(code: 'default.button.confirm.label', default: 'Confirm')}
                </g:link>
                <g:link class="button ${'transfer'.equals(actionName)?'active':'' }" controller="requisition" action="transfer" id="${requisition?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />
                    6.&nbsp;${warehouse.message(code: 'default.button.transfer.label', default: 'Transfer')}
                </g:link>
            </g:if>
        </div>
        <div class="button-group">

            <g:link class="button" controller="picklist" action="print" id="${requisition?.id}" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                &nbsp;${warehouse.message(code: 'picklist.button.print.label', default: 'Print picklist')}
            </g:link>

            <g:link class="button" controller="deliveryNote" action="print" id="${requisition?.id}" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                &nbsp;${warehouse.message(code: 'deliveryNote.button.print.label', default: 'Print delivery note')}
            </g:link>

        </div>


        <g:if test="${session?.warehouse?.id == requisition?.origin?.id }">
            <g:isUserAdmin>
                <div class="button-group">
                    <g:link class="button" controller="requisition" action="cancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}" />
                        &nbsp;${warehouse.message(code: 'requisition.cancel.label', default: 'Cancel requisition')}
                    </g:link>
                    <g:link class="button" controller="requisition" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
                        &nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete requisition')}
                    </g:link>
                </div>
            </g:isUserAdmin>
        </g:if>
    </div>
</g:if>

