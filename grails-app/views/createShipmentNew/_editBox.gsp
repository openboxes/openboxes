<g:if test="${boxInstance}">
	<g:set var="formName" value="EditBox-${boxInstance?.id}" />
</g:if>
<g:else>
	<g:set var="formName" value="AddBox-${container?.id}" />
</g:else>

<script type="text/javascript">
	$(document).ready(function(){
		$("#btn${formName}").click(function() { $("#dlg${formName}").dialog('open'); });									
		$("#dlg${formName}").dialog({ autoOpen: ${!boxInstance && (addBox == containerInstance?.id) ? 'true' : 'false'}, modal: true, width: '600px' });				
	});
</script>	   
	<div id="dlg${formName}" title="Edit a Box" style="padding: 10px; display: none;" >
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Container" form="${formName}"/>

	<g:form name="${formName}" action="createShipment">
		<table>
			<tbody>
				<g:render template="containerFields" model="['boxInstance':boxInstance]"/>
				<g:if test="${containerInstance}">
					<g:hiddenField name="container.id" value="${containerInstance?.id }"/>
					<g:render template="itemFields" model=""/>
				</g:if>
				<g:if test="${boxInstance}">
					<g:hiddenField name="box.id" value="${boxInstance?.id }"/>
				</g:if>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveBox" value="Save Box"></g:submitButton>
							<g:if test="${boxInstance}">
								<g:submitButton name="deleteBox" value="Remove Box"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlg${formName}').dialog('close');">Cancel</button>
						</div>
						<g:if test="${boxInstance}">
							<div class="buttons">
								<g:submitButton name="addItemToBox" value="Add an Item to this Box"></g:submitButton>
							</div>
						</g:if>
						<g:if test="${containerInstance}">
							<div class="buttons">
								<g:submitButton name="addAnotherBox" value="Save Box and Add Another Box"></g:submitButton>
							</div>
						</g:if>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

