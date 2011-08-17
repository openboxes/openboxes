<div class="action-menu" style="padding: 10px;">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<a href="javascript:void(0);" class="actionButton" id="addToShipmentBtn">
				<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" alt="Add to shipment"/>
				&nbsp;<warehouse:message code="inventory.addToShipments.label"/>
			</a>
		</div>
		<div class="action-menu-item">														
			<a href="javascript:void(0);" class="actionButton" id="addToTransactionBtn">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add to transaction" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="inventory.addToTransaction.label"/>
			</a>
		</div>	
	</div>
</span>
