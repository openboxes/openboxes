<%@ page import="org.pih.warehouse.requisition.RequisitionStatus"%>

<g:if test="${requisition?.id }">
	<span id="requisition-action-menu" class="action-menu">
		<button class="action-btn ">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'cog.png')}" />
		</button>
			<div class="actions" >
				<g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
					<div class="action-menu-item">
						<g:link controller="requisition" action="list">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
							&nbsp;${warehouse.message(code: 'requisition.view.label', default: 'View requisitions')}
						</g:link>
					</div>
					<div class="action-menu-item">
						<hr/>
					</div>
				</g:if>
				<div class="action-menu-item">
					<g:link controller="requisition" action="show" id="${requisition?.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
						&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Preview requisition')}
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="requisition" action="editHeader" id="${requisition?.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" />
						&nbsp;${warehouse.message(code: 'requisition.editHeader.label', default: 'Edit requisition')}
					</g:link>
				</div>
                <div class="action-menu-item">
                    <g:link controller="requisition" action="edit" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
                        &nbsp;${warehouse.message(code: 'requisition.edit.label', default: 'Edit requisition items')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisition" action="review" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom_in.png')}" />
                        &nbsp;${warehouse.message(code: 'requisition.review.label', default: 'Review requisition')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisition" action="pick" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'basket_add.png')}" />
                        &nbsp;${warehouse.message(code: 'requisition.pick.label', default: 'Pick requisition items')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisition" action="confirm" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'basket_edit.png')}" />
                        &nbsp;${warehouse.message(code: 'requisition.confirm.label', default: 'Confirm requisition')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="requisition" action="issue" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'basket_go.png')}" />
                        &nbsp;${warehouse.message(code: 'requisition.issue.label', default: 'Issue requisition')}
                    </g:link>
                </div>
                <hr/>
                <div class="action-menu-item">
                    <g:link controller="stockMovement" action="edit" id="${requisition?.id}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />
                        &nbsp;${warehouse.message(code: 'stockMovement.edit.label', default: 'Edit Stock Movement')}
                    </g:link>
                </div>
                <hr/>
                <div class="action-menu-item">
                    <g:link controller="picklist" action="renderPdf" id="${requisition?.id}" target="_blank">
                        <img src="${resource(dir: 'images/icons', file: 'pdf.png')}" />
                        &nbsp;${warehouse.message(code: 'picklist.button.print.label', default: 'Download picklist PDF')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="picklist" action="print" id="${requisition?.id}" target="_blank">
                        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                        &nbsp;${warehouse.message(code: 'picklist.button.print.label', default: 'Print picklist')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="deliveryNote" action="print" id="${requisition?.id}" target="_blank">
                        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                        &nbsp;${warehouse.message(code: 'deliveryNote.button.print.label', default: 'Print delivery note')}
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <hr/>
                </div>
				<g:if test="${session?.warehouse?.id == requisition?.origin?.id }">
					<g:isUserManager>
						<g:if test="${requisition.status == RequisitionStatus.CANCELED }">
							<div class="action-menu-item">
								<g:link controller="requisition" action="undoCancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
									<img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />
									&nbsp;${warehouse.message(code: 'requisition.undoCancel.label', default: 'Undo cancel requisition')}
								</g:link>
							</div>
						</g:if>
						<g:else>
							<div class="action-menu-item">
								<g:link controller="requisition" action="cancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
									<img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}" />
									&nbsp;${warehouse.message(code: 'requisition.cancel.label', default: 'Cancel requisition')}
								</g:link>
							</div>

						</g:else>
					</g:isUserManager>
					<g:isUserAdmin>
                        <div class="action-menu-item">
                            <g:link controller="requisition" action="normalize" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${resource(dir: 'images/icons/silk', file: 'reload.png')}" />
                                &nbsp;${warehouse.message(code: 'request.normalize.label', default: 'Normalize requisition')}
                            </g:link>
                        </div>
			            <div class="action-menu-item">
			                <g:link controller="requisition" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
			                    <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
			                    &nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete requisition')}
			                </g:link>
			            </div>
                        <g:if test="${requisition?.status == RequisitionStatus.ISSUED}">
                            <div class="action-menu-item">
                                <g:link controller="requisition" action="rollback" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />
                                    &nbsp;${warehouse.message(code: 'request.rollback.label', default: 'Rollback requisition')}
                                </g:link>
                            </div>
                        </g:if>
                        <hr/>
                        <div class="action-menu-item">
                            <g:link controller="requisition" action="generatePicklist" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${resource(dir: 'images/icons/silk', file: 'text_list_numbers.png')}" />
                                &nbsp;${warehouse.message(code: 'picklist.generate.label', default: 'Generate picklist')}
                            </g:link>
                        </div>
                        <div class="action-menu-item">
                            <g:link controller="requisition" action="clearPicklist" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
                                &nbsp;${warehouse.message(code: 'picklist.clear.label', default: 'Clear picklist')}
                            </g:link>
                        </div>

                    </g:isUserAdmin>
				</g:if>
			</div>
	</span>
</g:if>
