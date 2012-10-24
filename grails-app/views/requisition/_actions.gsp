<g:if test="${requestInstance?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<g:if test="${requestInstance?.isRequested() }">
			<g:render template="actionsRequested" model="[requestInstance:requestInstance]"/>
		</g:if>
		<g:elseif test="${requestInstance?.isNew() }">
			<g:render template="actionsNotYetRequested" model="[requestInstance:requestInstance]"/>
		</g:elseif>
		<g:elseif test="${requestInstance.isPending() }">
			<g:render template="actionsRequested" model="[requestInstance:requestInstance]"/>
		</g:elseif>
		<g:elseif test="${requestInstance.isFulfilled() }">
			<g:render template="actionsRequested" model="[requestInstance:requestInstance]"/>
		</g:elseif>
		<g:elseif test="${requestInstance.status == org.pih.warehouse.requisition.RequisitionStatus.PICKED }">
			<g:render template="actionsRequested" model="[requestInstance:requestInstance]"/>
		</g:elseif>
		<g:else>
			<div class="actions" style="min-width: 300px;">
				<div class="action-menu-item center">
					<a href="#">No actions available for ${requestInstance?.status }</a>
				</div>
			</div>
		</g:else>
	</span>
</g:if>