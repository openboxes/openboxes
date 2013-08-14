<g:if test="${requisition?.id}">
<div class="wizard-box">
	<div class="wizard-steps">
		<div class="${actionName.contains('create')||actionName.equals('show')?'active-step':''}">
			<g:link controller="requisition" action="show" id="${requisition?.id}">
                <span class="step">1</span>

                <g:if test="${requisition?.id}">
                    <warehouse:message code="requisition.wizard.show.label" default="View"/>
                </g:if>
                <g:else>
                    <warehouse:message code="requisition.wizard.create.label" default="Create"/>
                </g:else>

			</g:link>
		</div>
        <div class="${actionName.equals('edit')||actionName.equals('editHeader')?'active-step':''}">
            <g:link controller="requisition" action="edit"  id="${requisition?.id}">
                <span class="step">2</span>
                <warehouse:message code="requisition.wizard.edit.label" default="Edit"/>
            </g:link>
        </div>
        <div class="${actionName.equals('review')||actionName.equals('change')?'active-step':''}">
            <g:link controller="requisition" action="review" id="${requisition?.id}">
                <span class="step">3</span>
                <warehouse:message code="requisition.wizard.verify.label" default="Verify"/>
            </g:link>
        </div>
		<div class="${actionName.equals('pick')?'active-step':''}">
			<g:link controller="requisition" action="pick" id="${requisition?.id}">
                <span class="step">4</span>
				<warehouse:message code="requisition.wizard.pick.label" default="Pick"/>
			</g:link>
		</div>
		<div class="${actionName.equals('confirm')?'active-step':''}">
			<g:link controller="requisition" action="confirm" id="${requisition?.id}">
                <span class="step">5</span>
				<warehouse:message code="requisition.wizard.check.label" default="Check"/>
			</g:link>
		</div>
		<div class="${actionName.equals('transfer')?'active-step':''}">
			<g:link controller="requisition" action="transfer" id="${requisition?.id}">
                <span class="step">6</span>
				<warehouse:message code="requisition.wizard.issue.label" default="Issue"/>
			</g:link>
		</div>
	</div>

    <div class="right button-group">
        <g:link controller="picklist" action="renderPdf" id="${requisition?.id}" target="_blank" class="button">
            <img src="${resource(dir: 'images/icons', file: 'pdf.png')}" />&nbsp;
            ${warehouse.message(code: 'picklist.button.print.label', default: 'Download pick list')}
        </g:link>
        <g:link controller="picklist" action="print" id="${requisition?.id}" target="_blank" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
            ${warehouse.message(code: 'picklist.button.print.label', default: 'Print pick list')}
        </g:link>
        <g:link controller="deliveryNote" action="print" id="${requisition?.id}" target="_blank" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
            ${warehouse.message(code: 'deliveryNote.button.print.label', default: 'Print delivery note')}
        </g:link>
    </div>

</div>
</g:if>