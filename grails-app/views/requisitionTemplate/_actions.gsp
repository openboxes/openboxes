<%@ page import="org.pih.warehouse.requisition.RequisitionStatus"%>

<g:if test="${requisition?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
			<div class="actions" >
				<g:if test="${!request.request.requestURL.toString().contains('requisitionTemplate/list')}">
					<div class="action-menu-item">
						<g:link controller="requisitionTemplate" action="list">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
							&nbsp;${warehouse.message(code: 'requisitionTemplate.list.label', default: 'List stock lists')}
						</g:link>
					</div>
					<div class="action-menu-item">
						<hr/>
					</div>
				</g:if>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="show" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.show.label', default: 'Show stock list')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="editHeader" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.editHeader.label', default: 'Edit stock list')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="edit" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_add.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.edit.label', default: 'Edit stock list items')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="batch" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_go.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.import.label', default: 'Import stock list items')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="export" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.export.label', default: 'Export stock list items')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="clone" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.clone.label', default: 'Clone stock list')}
                    </g:link>
                </div>
                <g:if test="${!requisition.isPublished}">
                    <div class="action-menu-item">
                        <g:link controller="requisitionTemplate" action="publish" id="${requisition?.id}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_world.png')}" />
                            &nbsp;${warehouse.message(code: 'requisitionTemplate.publish.label', default: 'Publish stock list')}
                        </g:link>
                    </div>
                </g:if>
                <g:else>
                    <div class="action-menu-item">
                        <g:link controller="requisitionTemplate" action="unpublish" id="${requisition?.id}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_world.png')}" />
                            &nbsp;${warehouse.message(code: 'requisitionTemplate.unpublish.label', default: 'Unpublish stock list')}
                        </g:link>
                    </div>
                </g:else>
                <hr/>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="clear" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'erase.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.clear.label', default: 'Clear stock list items')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.delete.label', default: 'Delete stock list')}
                    </g:link>
                </div>

			</div>
	</span>
</g:if>
