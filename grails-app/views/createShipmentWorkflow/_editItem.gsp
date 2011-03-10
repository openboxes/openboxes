<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditItem").dialog({ autoOpen: true, modal: true, width: '600px', });				
	});
</script>


<div id="dlgEditItem" title="Edit an Item" style="padding: 10px; display: none;" >
	

	<g:if test="${addItemToContainerId}">
		<%-- 
		<g:hiddenField name="container.id" value="${addItemToContainerId}"/>
		--%>						
		<g:render template="itemSearch" model="['containerId':addItemToContainerId]"/>
	</g:if>
	<g:if test="${itemToEdit}">
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItem"/>
		<g:form name="editItem" action="createShipment">
			<table>
				<tbody>
					<g:hiddenField name="item.id" value="${itemToEdit.id }"/>
					<g:render template="itemFields" model="['item':itemToEdit]"/>
				</tbody>
			</table>
		</g:form>														
	</g:if>
</div>		
		     

