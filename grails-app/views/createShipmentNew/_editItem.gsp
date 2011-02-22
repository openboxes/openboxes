<g:if test="${itemInstance}">
	<g:set var="formName" value="EditItem-${itemInstance?.id}" />
</g:if>
<g:if test="${containerInstance}">
	<g:set var="formName" value="AddItem-${containerInstance?.id}" />
</g:if>

<script type="text/javascript">
	$(document).ready(function(){
		$("#btn${formName}").click(function() { $("#dlg${formName}").dialog('open'); });									
		$("#dlg${formName}").dialog({ autoOpen: ${containerInstance && (addItem == containerInstance?.id) ? 'true' : 'false'}, modal: true, width: '600px' });				
	});
</script>

	<div id="dlg${formName}" title="Edit an Item" style="padding: 10px; display: none;" >
		
		<g:form name="${formName}" action="createShipment">

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
							<g:if test="${itemInstance}">
								<g:submitButton name="deleteItem" value="Remove Item"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlg${formName}').dialog('close');">Cancel</button>
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
		     

