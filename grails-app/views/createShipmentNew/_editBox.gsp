<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditBox-${boxInstance?.id}").click(function() { $("#dlgEditBox-${boxInstance?.id}").dialog('open'); });									
		$("#dlgEditBox-${boxInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	
		$("#btnAddBox-${containerInstance?.id}").click(function() { $("#dlgAddBox-${containerInstance?.id}").dialog('open'); });									
		$("#dlgAddBox-${containerInstance?.id}").dialog({ autoOpen: ${addBox == containerInstance?.id ? 'true' : 'false'}, modal: true, width: '600px' });				
	});
</script>
<g:if test="${boxInstance}">	   
	<div id="dlgEditBox-${boxInstance?.id}" title="Edit a Box" style="padding: 10px; display: none;" >
</g:if>
<g:else>
	<div id="dlgAddBox-${containerInstance?.id}" title="Add a Box" style="padding: 10px; display: none;" >
</g:else>
	<g:form action="createShipment">
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
							<g:submitButton name="cancelDialog" value="Cancel"></g:submitButton>
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
		     

