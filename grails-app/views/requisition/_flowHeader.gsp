<div class="wizard-box">
	<div class="wizard-steps "> 
		<div class="${actionName.contains('create')||actionName.equals('show')?'active-step':''}">
			<g:link controller="requisition" action="show" id="${requisition?.id}">
                <span class="step">1</span>

                <g:if test="${requisition?.id}">
                    <warehouse:message code="requisition.show.label" args="[warehouse.message(code:'enum.RequisitionType.' + requisition.type).toLowerCase()]"/>
                </g:if>
                <g:else>
                    <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'enum.RequisitionType.' + requisition.type).toLowerCase()]"/>
                </g:else>

			</g:link>
		</div>
        <div class="${actionName.equals('edit')||actionName.equals('editHeader')?'active-step':''}">
            <g:link controller="requisition" action="edit"  id="${requisition?.id}">
                <span class="step">2</span>
                <warehouse:message code="requisition.edit.label"/>
            </g:link>
        </div>
        <div class="${actionName.equals('review')||actionName.equals('change')?'active-step':''}">
            <g:link controller="requisition" action="review" id="${requisition?.id}">
                <span class="step">3</span>
                <warehouse:message code="requisition.verify.label"/>
            </g:link>
        </div>
		<div class="${actionName.equals('pick')?'active-step':''}">
			<g:link controller="requisition" action="pick" id="${requisition?.id}">
                <span class="step">4</span>
				<warehouse:message code="requisition.pick.label"/>
			</g:link>
		</div>
		<div class="${actionName.equals('confirm')?'active-step':''}">
			<g:link controller="requisition" action="confirm" id="${requisition?.id}">
                <span class="step">5</span>
				<warehouse:message code="requisition.check.label"/>
			</g:link>
		</div>
		<div class="${actionName.equals('transfer')?'active-step':''}">
			<g:link controller="requisition" action="transfer" id="${requisition?.id}">
                <span class="step">6</span>
				<warehouse:message code="requisition.issue.label"/>
			</g:link>
		</div>
	</div>

    <div class="right">
        <g:link controller="picklist" action="print" id="${requisition?.id}" target="_blank" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
            ${warehouse.message(code: 'picklist.button.print.label', default: 'Print picklist')}
        </g:link>
        <g:link controller="deliveryNote" action="print" id="${requisition?.id}" target="_blank" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
            ${warehouse.message(code: 'deliveryNote.button.print.label', default: 'Print delivery note')}
        </g:link>


    </div>

</div>