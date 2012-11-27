<g:if test="${requisition?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<g:if test="${requisition?.isOpen() }">
			<g:render template="actionsRequested" model="[requisition:requisition]"/>
		</g:if>
		<g:elseif test="${requisition?.isCreated() }">
			<g:render template="actionsNotYetRequested" model="[requisition:requisition]"/>
		</g:elseif>
		<g:elseif test="${requisition.isPending() }">
			<g:render template="actionsRequested" model="[requisition:requisition]"/>
		</g:elseif>
		<g:elseif test="${requisition.isFulfilled() }">
			<g:render template="actionsRequested" model="[requisition:requisition]"/>
		</g:elseif>
		<g:elseif test="${requisition.status == org.pih.warehouse.requisition.RequisitionStatus.PICKED }">
			<g:render template="actionsRequested" model="[requisition:requisition]"/>
		</g:elseif>
		<g:else>
			<div class="actions" style="min-width: 300px;">
				<div class="action-menu-item center">
					<a href="#">No actions available for ${requisition?.status }</a>
				</div>
			</div>
		</g:else>
	</span>
</g:if>
