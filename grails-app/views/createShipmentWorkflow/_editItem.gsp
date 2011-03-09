<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditItem").dialog({ autoOpen: true, modal: true, width: '600px', });				
	});
</script>


<%--
	<div id="dlgEditItem" title="Edit an Item" style="padding: 10px; display: none; height: 600px;" >
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItem"/>
		
		
		<g:form name="editItem" action="createShipment">	
			<g:hiddenField id="container-id" name="container.id" value="${containerId}" size="20" />
	
			<table>
				<tbody>
		
					<g:if test="${addItemToContainerId}">
						<g:render template="itemSearch" model="['containerId':addItemToContainerId]"/>
					</g:if>
					<g:if test="${itemToEdit}">
						<g:render template="itemFields" model="['item':itemToEdit]"/>
					</g:if>
		
		
				</tbody>
			</table>
		</g:form>
	</div>		
--%>

<div id="dlgEditItem" title="Edit an Item" style="padding: 10px; display: none;" >
	
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItem"/>
	<g:if test="${addItemToContainerId}">
		<%-- 
		<g:hiddenField name="container.id" value="${addItemToContainerId}"/>
		--%>						
		<g:render template="itemSearch" model="['containerId':addItemToContainerId]"/>
	</g:if>
	<g:if test="${itemToEdit}">
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
		     

