<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditItem").dialog({ autoOpen: true, modal: true, width: '600px' });				
	});
</script>

	<div id="dlgEditItem" title="Edit an Item" style="padding: 10px; display: none;" >
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItem"/>
			<g:if test="${addItemToContainerId}">
				<g:render template="itemSearch" model="['containerId':addItemToContainerId]"/>
			</g:if>
			<g:if test="${itemToEdit}">
				<g:render template="itemFields" model="['item':itemToEdit]"/>
			</g:if>
	</div>		
		     

