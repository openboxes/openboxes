<g:if test="${requisition?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
			<div class="action-menu-item">
				<g:link controller="requisition" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'request.view.label', default: 'View requisitions')}
				</g:link>
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="requisition" action="show" id="${requisition?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Show requisition details')}
			</g:link>		
		</div>
        <div class="action-menu-item">
            <g:link controller="requisition" action="printDraft" id="${requisition?.id}" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                &nbsp;${warehouse.message(code: 'picklist.print.label', default: 'Print picklist')}
            </g:link>
        </div>
        <div class="action-menu-item">
            <hr/>
        </div>
        <div class="action-menu-item">
            <g:link controller="requisition" action="addComment" id="${requisition?.id}">
                <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
                &nbsp;${warehouse.message(code: 'request.addComment.label', default: 'Add comment')}
            </g:link>
        </div>
        <div class="action-menu-item">
            <g:link controller="requisition" action="addDocument" id="${requisition?.id}">
                <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
                &nbsp;${warehouse.message(code: 'request.addDocument.label', default: 'Add document')}
            </g:link>
        </div>
        <div class="action-menu-item">
            <hr/>
        </div>
		<g:if test="${session?.warehouse?.id == requisition?.destination?.id }">
			<div class="action-menu-item">
				<g:link controller="requisition" action="withdraw" id="${requisition?.id}" onclick="alert('${warehouse.message(code: 'default.button.notSupported.message', default: 'This feature is not currently supported.')}'); return false;">
					<img src="${resource(dir: 'images/icons/silk', file: 'cart_delete.png')}" />
					&nbsp;${warehouse.message(code: 'request.cancel.label', default: 'Cancel requisition')}
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<g:link controller="requisition" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
					&nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete requisition')}
				</g:link>				
			</div>		
		</g:if>
	</div>
</g:if>