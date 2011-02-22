<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditItem").dialog({ autoOpen: true, modal: true, width: '600px' });				
	});
</script>

	<div id="dlgEditItem" title="Edit an Item" style="padding: 10px; display: none;" >
		
		<g:form name="editItem" action="createShipment">

		<table>
			<tbody>
				<g:if test="${addItemToContainerId}">
					<g:hiddenField name="container.id" value="${addItemToContainerId}"/>
					<g:render template="itemFields" model=""/>
				</g:if>
				<g:if test="${itemToEdit}">
					<g:hiddenField name="item.id" value="${itemToEdit.id }"/>
					<g:render template="itemFields" model="['item':itemToEdit]"/>
				</g:if>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveItem" value="Save Item"></g:submitButton>
							<g:if test="${itemToEdit}">
								<g:submitButton name="deleteItem" value="Remove Item"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditItem').dialog('close');">Cancel</button>
						</div>
						<g:if test="${addItemToContainerId}">
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
		     

