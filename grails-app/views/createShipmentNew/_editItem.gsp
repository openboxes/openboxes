<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditItem-${itemInstance?.id}").click(function() { $("#dlgEditItem-${itemInstance?.id}").dialog('open'); });									
		$("#dlgEditItem-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	
		$("#btnAddItem-${containerInstance?.id}").click(function() { $("#dlgAddItem-${containerInstance?.id}").dialog('open'); });									
		$("#dlgAddItem-${containerInstance?.id}").dialog({ autoOpen: ${addItem == containerInstance?.id ? 'true' : 'false'}, modal: true, width: '600px' });				
	});
</script>
<g:if test="${itemInstance}">	   
	<div id="dlgEditItem-${itemInstance?.id}" title="Edit an item" style="padding: 10px; display: none;" >
</g:if>
<g:else>
	<div id="dlgAddItem-${containerInstance?.id}" title="Add an item" style="padding: 10px; display: none;" >
</g:else>
	<g:form action="createShipment">
		<table>
			<tbody>
				<g:if test="${containerInstance}">
					<g:hiddenField name="container.id" value="${containerInstance?.id }"/>
					<g:render template="itemFields" model="['containerInstance':containerInstance]"/>
				</g:if>
				<g:if test="${itemInstance}">
					<g:hiddenField name="item.id" value="${itemInstance?.id }"/>
					<g:render template="itemFields" model="['itemInstance':itemInstance]"/>
				</g:if>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveItem" value="Save Item"></g:submitButton>
							<g:submitButton name="deleteItem" value="Delete Item"></g:submitButton>
							<g:submitButton name="cancelItem" value="Cancel"></g:submitButton>
						</div>
						<g:if test="${containerInstance}">
							<div class="buttons">
								<g:submitButton name="addAnotherItem" value="Save Item and Add Another Item"></g:submitButton>
							</div>
						</g:if>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

