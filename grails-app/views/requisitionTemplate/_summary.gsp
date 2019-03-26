<div id="requisition-template-header" class="summary">
	<g:if test="${requisition?.id}">
		<table>
			<tbody>
				<tr>
					<g:isUserAdmin>
						<td class="middle" width="1%">
							<g:render template="actions" model="[requisition:requisition]" />
						</td>
					</g:isUserAdmin>
					<td class="left">
						<div class="title" id="description">
                            ${requisition.name}
						</div>
						<div>
							<span id="origin">
								<warehouse:message code="requisition.origin.label"/>:
								<label>${requisition?.origin?.name?.encodeAsHTML()?:warehouse.message(code: 'default.none.label')}</label>
							</span>
							<span id="destination">
								<warehouse:message code="requisition.destination.label"/>:
	                            <label>${requisition?.destination?.name?.encodeAsHTML()?:session?.warehouse?.name}</label>
							</span>
							<span class="request-items">
								<warehouse:message code="requisition.requisitionItems.label"/>:
								<label>${requisition?.requisitionItems?.size()?:0}</label>
							</span>
							<span id="recipientProgram">
								<warehouse:message code="requisition.commodityClass.label"/>:
	                            <label><format:metadata obj="${requisition?.commodityClass?:warehouse.message(code: 'default.none.label') }"/></label>
							</span>
						</div>
					</td>
					<td>
						<div class="right">
							<g:if test="${requisition.isPublished}">
								<div class="tag tag-alert">
									<warehouse:message code="default.published.label" default="Published"/>
								</div>
							</g:if>
							<g:else>
								<div class="tag tag-danger">
									<warehouse:message code="default.draft.label" default="Draft"/>
								</div>
							</g:else>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:if>
	<g:else>
        <table>
			<tbody>
				<tr>
					<td class="left">
						<div id="new-requisition-template" class="title">
                            ${warehouse.message(code: 'requisitionTemplate.new.label') }
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
	</g:else>
</div>

<div class="buttonBar">
	<g:link class="button" controller="requisitionTemplate" action="list">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" />&nbsp;
		<warehouse:message code="default.list.label" args="[warehouse.message(code:'requisitionTemplates.label').toLowerCase()]"/>
	</g:link>
	<g:isUserAdmin>
		<g:link class="button" controller="requisitionTemplate" action="create" params="[type:'STOCK']">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" />&nbsp;
			<warehouse:message code="default.create.label" args="[warehouse.message(code:'requisitionTemplate.label').toLowerCase()]"/>
		</g:link>
	</g:isUserAdmin>

	<div class="right">
		<g:isUserAdmin>
			<g:link controller="requisitionTemplate" action="show" id="${requisition?.id}" class="button">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'application_side_boxes.png')}" />&nbsp;
				${warehouse.message(code: 'default.show.label', args: [warehouse.message(code:'requisitionTemplate.label')])}
			</g:link>
			<g:link controller="requisitionTemplate" action="editHeader" id="${requisition?.id}" class="button">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />&nbsp;
				${warehouse.message(code: 'requisitionTemplate.editHeader.label', default: 'Edit stock list')}
			</g:link>
			<g:link controller="requisitionTemplate" action="edit" id="${requisition?.id}" class="button">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'application_add.png')}" />&nbsp;
				${warehouse.message(code: 'requisitionTemplate.edit.label', default: 'Edit stock list items')}
			</g:link>
			<g:link controller="requisitionTemplate" action="batch" id="${requisition?.id}" class="button">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'page_go.png')}" />
				&nbsp;${warehouse.message(code: 'requisitionTemplate.import.label', default: 'Import stock list items')}
			</g:link>
			<g:link controller="requisitionTemplate" action="export" id="${requisition?.id}" class="button">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
				&nbsp;${warehouse.message(code: 'requisitionTemplate.export.label', default: 'Export stock list items')}
			</g:link>

			<g:if test="${!requisition.isPublished}">
				<g:link controller="requisitionTemplate" action="publish" id="${requisition?.id}" class="button">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_world.png')}" />
					&nbsp;${warehouse.message(code: 'requisitionTemplate.publish.label', default: 'Publish stock list')}
				</g:link>
			</g:if>
			<g:else>
				<g:link controller="requisitionTemplate" action="unpublish" id="${requisition?.id}" class="button">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_world.png')}" />
					&nbsp;${warehouse.message(code: 'requisitionTemplate.unpublish.label', default: 'Unpublish stock list')}
				</g:link>
			</g:else>
			<g:link
					controller="stocklist"
					action="sendMail"
					params="['id':requisition.id,'subject':'STOCK LIST UPDATE','body':'STOCK LIST UPDATE','recipients':requisition.requestedBy?.email]"
					class="button"
			>
				<img src="${createLinkTo(dir:'images/icons/silk',file:'email.png')}" />&nbsp;
				${warehouse.message(code: 'default.button.email.label')}
			</g:link>
		</g:isUserAdmin>
		<g:link controller="stocklist" action="renderPdf" id="${requisition?.id}" class="button">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'disk_download.png')}" />&nbsp;
			${warehouse.message(code: 'default.button.download.label')}
		</g:link>
	</div>
</div>
