<%@ page import="org.pih.warehouse.requisition.RequisitionStatus"%>

<g:if test="${requisition?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<%-- 
		
		<g:if test="${requisition?.isPending() }">
		</g:if>
		<g:else>
			<div class="actions" style="min-width: 300px;">
				<div class="action-menu-item center">
					<a href="#">No actions available for ${requisition?.status }</a>
				</div>
			</div>
		</g:else>
		--%>
			<div class="actions" >
				<g:if test="${!request.request.requestURL.toString().contains('requisitionTemplate/list')}">
					<div class="action-menu-item">
						<g:link controller="requisitionTemplate" action="list">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
							&nbsp;${warehouse.message(code: 'requisitionTemplate.list.label', default: 'List requisition templates')}
						</g:link>
					</div>
					<div class="action-menu-item">
						<hr/>
					</div>
				</g:if>
                <%--
				<div class="action-menu-item">
					<g:link controller="requisitionTemplate" action="show" id="${requisition?.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
						&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Preview requisition')}
					</g:link>		
				</div>
                --%>
                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="edit" id="${requisition?.id}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.edit.label', default: 'Edit requisition template')}
                    </g:link>
                </div>
                <g:if test="${!requisition.isPublished}">
                    <div class="action-menu-item">
                        <g:link controller="requisitionTemplate" action="publish" id="${requisition?.id}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_world.png')}" />
                            &nbsp;${warehouse.message(code: 'requisitionTemplate.publish.label', default: 'Publish requisition template')}
                        </g:link>
                    </div>
                </g:if>
                <g:else>
                    <div class="action-menu-item">
                        <g:link controller="requisitionTemplate" action="unpublish" id="${requisition?.id}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_world.png')}" />
                            &nbsp;${warehouse.message(code: 'requisitionTemplate.unpublish.label', default: 'Unpublish requisition template')}
                        </g:link>
                    </div>
                </g:else>
                <div class="action-menu-item">
                    <hr/>
                </div>

                <div class="action-menu-item">
                    <g:link controller="requisitionTemplate" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
                        &nbsp;${warehouse.message(code: 'requisitionTemplate.delete.label', default: 'Delete requisition template')}
                    </g:link>
                </div>

			</div>
	</span>
</g:if>